package com.jeff_media.jefflib;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

public final class EnumUtils {

    private EnumUtils() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E> Optional<E> getIfPresent(final Class<E> type, final String... candidates) {
        for (String candidate : candidates) {
            if (candidate == null) continue;
            try {
                if (type.isEnum()) {
                    return Optional.ofNullable((E) Enum.valueOf((Class) type, candidate));
                }
                final Field field = type.getField(candidate);
                if (Modifier.isStatic(field.getModifiers()) && type.isAssignableFrom(field.getType())) {
                    return Optional.ofNullable(type.cast(field.get(null)));
                }
            } catch (IllegalArgumentException | ReflectiveOperationException ignored) {
            }
        }
        return Optional.empty();
    }
}
