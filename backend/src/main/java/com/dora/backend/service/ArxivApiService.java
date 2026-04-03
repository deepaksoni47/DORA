package com.dora.backend.service;

import com.dora.backend.entity.Document;
import java.io.StringReader;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Service
public class ArxivApiService {

    private static final Logger logger = LoggerFactory.getLogger(ArxivApiService.class);
    private static final String ARXIV_SEARCH_URL = "https://export.arxiv.org/api/query";
    private static final int MAX_RESULTS = 20;
    private static final int MAX_DESCRIPTION_LENGTH = 700;
    private static final long MIN_REQUEST_INTERVAL_MS = 3_000L;

    private final RestTemplate restTemplate;
    private final AtomicLong lastRequestAt = new AtomicLong(0L);

    public ArxivApiService() {
        this.restTemplate = new RestTemplate();
    }

    public List<Document> searchPapers(String query) {
        if (query == null || query.trim().isEmpty()) {
            logger.info("Arxiv size: 0");
            return Collections.emptyList();
        }

        String trimmedQuery = query.trim();
        try {
            String formattedQuery = buildSearchQuery(trimmedQuery);
            if (formattedQuery.isBlank()) {
                logger.info("Arxiv size: 0");
                return Collections.emptyList();
            }

            throttleRequests();

            String url = UriComponentsBuilder.fromHttpUrl(ARXIV_SEARCH_URL)
                    .queryParam("search_query", formattedQuery)
                    .queryParam("start", 0)
                    .queryParam("max_results", MAX_RESULTS)
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isBlank()) {
                logger.info("Arxiv query='{}' fetched 0 papers", trimmedQuery);
                logger.info("Arxiv size: 0");
                return Collections.emptyList();
            }

            List<Document> papers = parseArxivResponse(response);
            logger.info("ArxivApiService: Found {} results for query='{}'", papers.size(), trimmedQuery);
            logger.info("Arxiv size: {}", papers.size());
            return papers;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while respecting arXiv rate limit for query='{}'", trimmedQuery);
            return Collections.emptyList();
        } catch (Exception ex) {
            logger.error("Failed to fetch arXiv papers for query='{}'", trimmedQuery, ex);
            logger.info("Arxiv size: 0");
            return Collections.emptyList();
        }
    }

    String buildSearchQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }

        List<String> tokens = java.util.Arrays.stream(query.trim().toLowerCase(Locale.ROOT).split("\\s+"))
                .filter(token -> !token.isBlank())
                .limit(4)
                .collect(Collectors.toList());

        if (tokens.isEmpty()) {
            return "";
        }

        if (tokens.size() == 1) {
            return "all:" + tokens.get(0);
        }

        return tokens.stream()
                .map(token -> "all:" + token)
                .collect(Collectors.joining(" AND "));
    }

    private void throttleRequests() throws InterruptedException {
        long now = System.currentTimeMillis();
        long previous = lastRequestAt.get();
        long elapsed = now - previous;

        if (previous > 0 && elapsed < MIN_REQUEST_INTERVAL_MS) {
            Thread.sleep(MIN_REQUEST_INTERVAL_MS - elapsed);
        }

        lastRequestAt.set(System.currentTimeMillis());
    }

    private List<Document> parseArxivResponse(String xmlResponse) throws Exception {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document xmlDocument = builder.parse(new InputSource(new StringReader(xmlResponse)));
            NodeList entries = xmlDocument.getElementsByTagNameNS("*", "entry");

            List<Document> papers = new ArrayList<>();
            for (int i = 0; i < entries.getLength(); i++) {
                try {
                    Node node = entries.item(i);
                    if (node.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    Element entry = (Element) node;
                    String title = safeGetText(entry, "title");
                    String summary = safeGetText(entry, "summary");
                    String id = safeGetText(entry, "id");
                    Integer year = extractPublishedYear(safeGetText(entry, "published"));

                    if (isBlank(title) || isBlank(id)) {
                        continue;
                    }

                    Document paper = new Document();
                    paper.setTitle(title);
                    paper.setDescription(trimDescription(summary));
                    paper.setUrl(id);
                    paper.setSource("arxiv");
                    paper.setType("paper");
                    paper.setYear(year);
                    paper.setScore(95.0);
                    papers.add(paper);
                } catch (Exception e) {
                    logger.debug("Failed to parse arXiv entry at index {}", i, e);
                }
            }

            return papers;
        } catch (Exception ex) {
            logger.error("ArxivApiService: Failed to parse XML response", ex);
            return Collections.emptyList();
        }
    }

    private String safeGetText(Element parent, String tag) {
        try {
            NodeList nodes = parent.getElementsByTagNameNS("*", tag);
            if (nodes.getLength() > 0 && nodes.item(0) != null) {
                return cleanText(nodes.item(0).getTextContent());
            }
        } catch (Exception ignored) {
            return "";
        }
        return "";
    }

    private Integer extractPublishedYear(String published) {
        if (isBlank(published)) {
            return Year.now().getValue();
        }

        try {
            return OffsetDateTime.parse(published.trim()).getYear();
        } catch (Exception ignored) {
            return Year.now().getValue();
        }
    }

    private String trimDescription(String description) {
        if (description == null) {
            return "";
        }
        if (description.length() <= MAX_DESCRIPTION_LENGTH) {
            return description;
        }
        return description.substring(0, MAX_DESCRIPTION_LENGTH).trim() + "...";
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('\n', ' ')
                .replace('\r', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
