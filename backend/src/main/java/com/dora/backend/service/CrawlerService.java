package com.dora.backend.service;

import com.dora.backend.entity.CrawledPage;
import com.dora.backend.entity.Document;
import com.dora.backend.repository.CrawledPageRepository;
import com.dora.backend.repository.DocumentRepository;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CrawlerService {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);
    private static final int DEFAULT_MAX_DEPTH = 1;
    private static final int DEFAULT_MAX_PAGES = 50;
    private static final int DEFAULT_ACADEMIC_MAX_PAGES = 10;
    private static final List<String> ACADEMIC_SEED_URLS = List.of(
            "https://openstax.org/",
            "https://www.khanacademy.org/",
            "https://www.edx.org/",
            "https://www.open.edu/openlearn/",
            "https://www.saylor.org/",
            "https://oli.cmu.edu/",
            "https://www.nature.com/",
            "https://www.science.org/",
            "https://www.sciencedirect.com/",
            "https://link.springer.com/",
            "https://doaj.org/",
            "https://www.jstor.org/",
            "https://www.cambridge.org/core/",
            "https://academic.oup.com/journals",
            "https://www.tandfonline.com/",
            "https://plato.stanford.edu/",
            "https://iep.utm.edu/",
            "https://www.gutenberg.org/",
            "https://archive.org/details/texts",
            "https://online.stat.psu.edu/",
            "https://ocw.mit.edu/courses/mathematics/",
            "https://openstax.org/subjects/math",
            "https://mathworld.wolfram.com/",
            "https://www.ams.org/home/page",
            "https://www.siam.org/",
            "https://www.physicsclassroom.com/",
            "https://ocw.mit.edu/courses/physics/",
            "https://openstax.org/subjects/science",
            "https://chem.libretexts.org/",
            "https://bio.libretexts.org/",
            "https://medlineplus.gov/",
            "https://www.ncbi.nlm.nih.gov/books/",
            "https://www.ncbi.nlm.nih.gov/pmc/",
            "https://www.nih.gov/",
            "https://www.who.int/",
            "https://www.cdc.gov/",
            "https://www.genome.gov/",
            "https://www.hhmi.org/biointeractive",
            "https://www.economicshelp.org/",
            "https://www.imf.org/en/Publications",
            "https://www.worldbank.org/en/research",
            "https://www.oecd.org/economy/",
            "https://www.nber.org/",
            "https://scholar.harvard.edu/",
            "https://open.lib.umn.edu/",
            "https://www.britannica.com/",
            "https://ocw.mit.edu/",
            "https://openlearninglibrary.mit.edu/",
            "https://cs50.harvard.edu/",
            "https://cs50.harvard.edu/ai/",
            "https://cs50.harvard.edu/x/",
            "https://pll.harvard.edu/",
            "https://openstax.org/",
            "https://www.khanacademy.org/",
            "https://www.edx.org/",
            "https://www.open.edu/openlearn/",
            "https://online.stanford.edu/",
            "https://online-learning.harvard.edu/",
            "https://www.saylor.org/",
            "https://oli.cmu.edu/",
            "https://www.nptel.ac.in/",
            "https://www.geeksforgeeks.org/",
            "https://www.tutorialspoint.com/",
            "https://www.javatpoint.com/",
            "https://developer.mozilla.org/",
            "https://realpython.com/",
            "https://pytorch.org/tutorials/",
            "https://pytorch.org/tutorials/beginner/basics/intro.html",
            "https://www.tensorflow.org/learn",
            "https://developers.google.com/machine-learning/crash-course",
            "https://scikit-learn.org/stable/user_guide.html",
            "https://d2l.ai/",
            "https://course.fast.ai/",
            "https://cs231n.stanford.edu/",
            "https://web.stanford.edu/class/cs224n/",
            "https://www.deeplearningbook.org/",
            "https://www.geeksforgeeks.org/data-structures/",
            "https://www.geeksforgeeks.org/operating-systems/",
            "https://www.tutorialspoint.com/data_structures_algorithms/index.htm",
            "https://www.tutorialspoint.com/operating_system/index.htm",
            "https://machinelearningmastery.com/",
            "https://www.baeldung.com/",
            "https://arxiv.org/",
            "https://www.jmlr.org/",
            "https://distill.pub/",
            "https://www.siam.org/publications/",
            "https://www.nature.com/subjects/computer-science",
            "https://www.sciencedirect.com/browse/journals-and-books/computer-science",
            "https://dl.acm.org/",
            "https://ieeexplore.ieee.org/");

    private final CrawledPageRepository crawledPageRepository;
    private final DocumentRepository documentRepository;

    public CrawlerService(CrawledPageRepository crawledPageRepository, DocumentRepository documentRepository) {
        this.crawledPageRepository = crawledPageRepository;
        this.documentRepository = documentRepository;
    }

    public void crawlPage(String url, Integer maxDepth, Integer maxPages) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL is required");
        }

        String normalizedUrl = url.trim();
        int effectiveMaxDepth = normalizeLimit(maxDepth, DEFAULT_MAX_DEPTH);
        int effectiveMaxPages = normalizeLimit(maxPages, DEFAULT_MAX_PAGES);
        String baseDomain = extractDomain(normalizedUrl);
        Set<String> visitedUrls = new HashSet<>();
        AtomicInteger pagesCrawled = new AtomicInteger(0);

        crawl(normalizedUrl, 0, effectiveMaxDepth, effectiveMaxPages, baseDomain, visitedUrls, pagesCrawled);
    }

    public int crawlAcademicSources(Integer maxDepth, Integer maxPagesPerSource) {
        int effectiveMaxDepth = normalizeLimit(maxDepth, DEFAULT_MAX_DEPTH);
        int effectiveMaxPagesPerSource = normalizeLimit(maxPagesPerSource, DEFAULT_ACADEMIC_MAX_PAGES);
        int successfulSeeds = 0;

        for (String seedUrl : ACADEMIC_SEED_URLS) {
            try {
                crawlPage(seedUrl, effectiveMaxDepth, effectiveMaxPagesPerSource);
                successfulSeeds++;
            } catch (RuntimeException ex) {
                logger.warn("Academic seed crawl failed for URL: {}", seedUrl, ex);
            }
        }

        return successfulSeeds;
    }

    public List<String> getAcademicSeedUrls() {
        return ACADEMIC_SEED_URLS;
    }

    private void crawl(
            String url,
            int depth,
            int maxDepth,
            int maxPages,
            String baseDomain,
            Set<String> visitedUrls,
            AtomicInteger pagesCrawled) {
        if (depth > maxDepth) {
            return;
        }

        if (pagesCrawled.get() >= maxPages) {
            return;
        }

        if (!isHttpUrl(url) || visitedUrls.contains(url)) {
            return;
        }

        if (baseDomain != null && !baseDomain.equalsIgnoreCase(extractDomain(url))) {
            return;
        }

        visitedUrls.add(url);
        logger.info("Crawling URL: {} | depth: {} | pages crawled: {}", url, depth, pagesCrawled.get());

        try {
            org.jsoup.nodes.Document parsedDocument = Jsoup.connect(url).get();
            String title = parsedDocument.title();
            String content = parsedDocument.body() != null ? parsedDocument.body().text() : "";

            CrawledPage crawledPage = new CrawledPage();
            crawledPage.setUrl(url);
            crawledPage.setTitle(title);
            crawledPage.setContent(content);
            crawledPage.setSource("crawler");
            crawledPage.setCrawledAt(LocalDateTime.now());
            CrawledPage savedPage = crawledPageRepository.save(crawledPage);
            pagesCrawled.incrementAndGet();

            if (documentRepository.findByUrl(savedPage.getUrl()).isEmpty()) {
                Document document = convertToDocument(savedPage);
                documentRepository.save(document);
            }

            if (depth == maxDepth || pagesCrawled.get() >= maxPages) {
                return;
            }

            Elements links = parsedDocument.select("a[href]");
            for (Element link : links) {
                if (pagesCrawled.get() >= maxPages) {
                    break;
                }

                String nextUrl = link.attr("abs:href");
                if (nextUrl == null || nextUrl.isBlank()) {
                    continue;
                }

                crawl(nextUrl, depth + 1, maxDepth, maxPages, baseDomain, visitedUrls, pagesCrawled);
            }
        } catch (IOException ex) {
            logger.error("Crawler failed for URL: {}", url, ex);
            throw new IllegalStateException("Failed to crawl page: " + url, ex);
        }
    }

    private int normalizeLimit(Integer value, int defaultValue) {
        if (value == null || value < 0) {
            return defaultValue;
        }
        return value;
    }



    private boolean isHttpUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    private Document convertToDocument(CrawledPage page) {
        Document document = new Document();
        document.setTitle(page.getTitle());
        document.setUrl(page.getUrl());
        document.setDescription(buildDescription(page.getContent()));
        document.setSource("crawler");
        
        String domain = extractDomain(page.getUrl());
        String type = "article";
        if (domain != null) {
            String lowerDomain = domain.toLowerCase();
            if (lowerDomain.contains("ocw.mit.edu") || lowerDomain.contains("edx.org") || lowerDomain.contains("khanacademy.org") || lowerDomain.contains("saylor.org")) {
                type = "course";
            } else if (lowerDomain.contains("gutenberg.org") || lowerDomain.contains("archive.org")) {
                type = "book";
            } else if (lowerDomain.contains("baeldung.com") || lowerDomain.contains("geeksforgeeks.org") || lowerDomain.contains("realpython.com")) {
                type = "blog";
            } else if (lowerDomain.contains("plato.stanford.edu") || lowerDomain.contains("britannica.com")) {
                type = "reference";
            }
        }
        
        document.setType(type);
        document.setYear(Year.now().getValue());
        document.setScore(75.0);
        return document;
    }

    private String buildDescription(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        int maxLength = 300;
        return content.length() <= maxLength ? content : content.substring(0, maxLength);
    }
}
