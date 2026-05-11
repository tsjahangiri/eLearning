package com.elearning.management.elearning_service.dto.response;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;

public class CacheablePage<T> extends PageImpl<T> implements Serializable {

    public CacheablePage(final List<T> content,
                         final Pageable pageable,
                         final long total) {
        super(content, pageable, total);
    }
}

