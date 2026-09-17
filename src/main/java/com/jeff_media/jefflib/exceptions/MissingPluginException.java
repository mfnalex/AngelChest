package com.jeff_media.jefflib.exceptions;

public class MissingPluginException extends RuntimeException {
    public MissingPluginException(final String message) {
        super(message);
    }

    public MissingPluginException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
