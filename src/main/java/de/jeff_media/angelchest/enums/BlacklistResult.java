package de.jeff_media.angelchest.enums;

import org.jetbrains.annotations.Nullable;

public enum BlacklistResult {
    MATCH_IGNORE, MATCH_DELETE, NO_MATCH_LORE_EXACT, NO_MATCH_LORE_CONTAINS, NO_MATCH_NAME_EXACT, NO_MATCH_NAME_CONTAINS, NO_MATCH_MATERIAL, NO_MATCH_ENCHANTMENTS, NO_MATCH_SLOT, NO_MATCH_PDC_KEYS, NO_MATCH_TO_STRING_REGEX,
    NO_MATCH_CUSTOM_MODEL_DATA_MIN,
    NO_MATCH_CUSTOM_MODEL_DATA_MAX;

    private @Nullable String name;

    public @Nullable String getName() {
        return name;
    }

    public void setName(@Nullable final String name) {
        this.name = name;
    }
}
