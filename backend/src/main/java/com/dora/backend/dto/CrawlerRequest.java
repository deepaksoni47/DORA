package com.dora.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrawlerRequest {

    private String url;
    private Integer maxDepth;
    private Integer maxPages;
}
