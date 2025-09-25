package com.leorces.rest.constants;


public class SwaggerConstants {

    // HTTP status codes
    public static final String STATUS_200_OK = "200";
    public static final String STATUS_201_CREATED = "201";
    public static final String STATUS_204_NO_CONTENT = "204";
    public static final String STATUS_400_BAD_REQUEST = "400";
    public static final String STATUS_404_NOT_FOUND = "404";
    public static final String STATUS_500_INTERNAL_ERROR = "500";

    // Swagger response descriptions
    public static final String RESPONSE_200_OK = "Operation completed successfully";
    public static final String RESPONSE_201_CREATED = "Resource created successfully";
    public static final String RESPONSE_204_NO_CONTENT = "Operation completed successfully with no content";
    public static final String RESPONSE_400_BAD_REQUEST = "Invalid request parameters";
    public static final String RESPONSE_404_NOT_FOUND = "Resource not found";
    public static final String RESPONSE_500_INTERNAL_ERROR = "Internal server error";

    // Pagination parameter descriptions
    public static final String PAGE_DESCRIPTION = "Page number (0-based)";
    public static final String SIZE_DESCRIPTION = "Number of items per page";
    public static final String SORT_DESCRIPTION = "Field to sort by";
    public static final String ORDER_DESCRIPTION = "Sort order (asc or desc)";
    public static final String FILTER_DESCRIPTION = "Filter criteria";
    public static final String STATE_DESCRIPTION = "State filter";

    private SwaggerConstants() {

    }

}
