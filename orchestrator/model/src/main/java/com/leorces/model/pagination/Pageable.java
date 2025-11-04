package com.leorces.model.pagination;

import lombok.Builder;

import java.util.Locale;

@Builder(toBuilder = true)
public record Pageable(
        long offset,
        int limit,
        String filter,
        String state,
        String sortByField,
        Direction order
) {

    public Pageable() {
        this(0L, 0, "", "", null, null);
    }

    public Pageable(long offset, int limit) {
        this(offset, limit, "", "", null, null);
    }

    public Pageable(long offset, int limit, String filter) {
        this(offset, limit, filter, "", null, null);
    }

    public Pageable(long offset, int limit, String filter, String state) {
        this(offset, limit, filter, state, null, null);
    }

    public enum Direction {
        ASC, DESC;


        public static Direction fromString(String value) {
            try {
                return valueOf(value.toUpperCase(Locale.US));
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Invalid value " + value + " for orders given! Has to be either 'desc' or 'asc' (case insensitive).",
                        e
                );
            }
        }

        public boolean isAscending() {
            return this == ASC;
        }


        public boolean isDescending() {
            return this == DESC;
        }
    }

}

