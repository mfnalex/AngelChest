package com.jeff_media.jefflib.exceptions;

public class InvalidBlockDataException extends RuntimeException {
    public InvalidBlockDataException(final String message) {
        super(message);
    }

    public InvalidBlockDataException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
