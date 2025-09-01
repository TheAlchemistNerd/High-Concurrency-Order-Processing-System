package com.ecommerce.orderprocessing.dto.response;

import java.util.List;

/**
 * Record for paginated responses.
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {
    // Custom constructor to calculate derived boolean fields
    public PagedResponse(List<T> content, int page, int size, long totalElements, int totalPages) {
        this(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page == 0,
                totalPages > 0 && page == totalPages - 1,
                totalPages > 0 && page < totalPages - 1,
                page > 0
        );
    }
}
