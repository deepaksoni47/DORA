package com.dora.backend.service;

import com.dora.backend.entity.Document;
import java.io.StringReader;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private static final int MAX_RESULTS = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 700;

    private final RestTemplate restTemplate;

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
            String url = UriComponentsBuilder.fromHttpUrl(ARXIV_SEARCH_URL)
                    .queryParam("search_query", "all:" + trimmedQuery)
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
            logger.info("Arxiv query='{}' fetched {} papers", trimmedQuery, papers.size());
            logger.info("Arxiv size: {}", papers.size());
            return papers;
        } catch (Exception ex) {
            logger.error("Failed to fetch arXiv papers for query='{}'", trimmedQuery, ex);
            logger.info("Arxiv size: 0");
            return Collections.emptyList();
        }
    }

    private List<Document> parseArxivResponse(String xmlResponse) throws Exception {
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
            Node node = entries.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element entry = (Element) node;
            String title = cleanText(getFirstElementText(entry, "title"));
            String summary = cleanText(getFirstElementText(entry, "summary"));
            String id = cleanText(getFirstElementText(entry, "id"));
            Integer year = extractPublishedYear(getFirstElementText(entry, "published"));

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
            paper.setScore(50.0);
            papers.add(paper);
        }

        return papers;
    }

    private String getFirstElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", tagName);
        if (nodes.getLength() == 0) {
            return "";
        }
        return nodes.item(0).getTextContent();
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