package com.dora.backend.util;

import org.springframework.stereotype.Component;

@Component
public class QueryNormalizer {

    public String normalize(String query) {
        return QueryProcessor.normalizeQuery(query);
    }
}
