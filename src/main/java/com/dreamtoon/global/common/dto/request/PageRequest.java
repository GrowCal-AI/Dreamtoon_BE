package com.dreamtoon.global.common.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@NoArgsConstructor
public class PageRequest {
    
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    
    private int page = DEFAULT_PAGE;
    private int size = DEFAULT_SIZE;
    private String sort;
    
    public org.springframework.data.domain.PageRequest toPageable() {
        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
        
        if (sort != null && !sort.isBlank()) {
            String[] sortParams = sort.split(",");
            String property = sortParams[0];
            Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            
            return org.springframework.data.domain.PageRequest.of(page, size, Sort.by(direction, property));
        }
        
        return org.springframework.data.domain.PageRequest.of(page, size);
    }
}
