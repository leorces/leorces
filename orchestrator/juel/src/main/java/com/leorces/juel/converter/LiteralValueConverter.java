package com.leorces.juel.converter;


import org.springframework.stereotype.Component;


/**
 * Handles conversion of string literals to specific types.
 */
@Component
public class LiteralValueConverter {

    public <T> T convert(String value, Class<T> resultType) {
        if (resultType == String.class) {
            return resultType.cast(value);
        } else if (resultType == Integer.class) {
            return convertToInteger(value);
        } else if (resultType == Boolean.class) {
            return convertToBoolean(value);
        } else if (resultType == Double.class) {
            return convertToDouble(value);
        } else {
            return resultType.cast(value);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convertToInteger(String value) {
        if (value == null) return null;

        try {
            return (T) Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convertToBoolean(String value) {
        if (value == null) {
            return null;
        }

        var trimmedValue = value.trim();
        return "true".equalsIgnoreCase(trimmedValue) || "false".equalsIgnoreCase(trimmedValue)
                ? (T) Boolean.valueOf(trimmedValue)
                : null;
    }

    @SuppressWarnings("unchecked")
    private <T> T convertToDouble(String value) {
        if (value == null) {
            throw new RuntimeException("Cannot convert null to Double");
        }

        try {
            return (T) Double.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
