package com.leorces.rest.constants;


public class PaginationConstants {

    // Pagination parameter names
    public static final String PAGE_PARAM = "page";
    public static final String SIZE_PARAM = "limit";
    public static final String SORT_PARAM = "sort";
    public static final String ORDER_PARAM = "order";
    public static final String FILTER_PARAM = "filter";
    public static final String STATE_PARAM = "state";

    // Pagination defaults
    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_SIZE = "10";
    public static final String DEFAULT_SORT_FIELD = "created_at";
    public static final String DEFAULT_ORDER = "desc";
    public static final String DEFAULT_FILTER = "";
    public static final String DEFAULT_STATE = "all";

    private PaginationConstants() {
    }

}
