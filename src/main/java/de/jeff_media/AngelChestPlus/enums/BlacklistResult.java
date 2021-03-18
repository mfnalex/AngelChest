package de.jeff_media.AngelChestPlus.enums;

import org.jetbrains.annotations.Nullable;

public enum BlacklistResult {
    MATCH, NO_MATCH_LORE_EXACT, NO_MATCH_LORE_CONTAINS, NO_MATCH_NAME_EXACT, NO_MATCH_NAME_CONTAINS, NO_MATCH_MATERIAL;

    private @Nullable String name;

    BlacklistResult(String name) {
        this.name=name;
    }

    BlacklistResult() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }
}
