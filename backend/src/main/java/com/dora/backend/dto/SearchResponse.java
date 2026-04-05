package com.dora.backend.dto;

import com.dora.backend.entity.Document;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<Document> results;
    private long totalResults;
    private int page;
    private int size;
}
