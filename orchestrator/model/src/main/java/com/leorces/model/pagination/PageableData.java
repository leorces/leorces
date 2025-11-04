package com.leorces.model.pagination;

import java.util.List;

public record PageableData<T>(
        List<T> data,
        long total
) {

}

