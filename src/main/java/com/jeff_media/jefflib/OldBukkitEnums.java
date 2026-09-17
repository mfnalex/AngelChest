package com.jeff_media.jefflib;

import org.bukkit.entity.EntityType;

public final class OldBukkitEnums {

    private static final EntityTypes ENTITY_TYPES = new EntityTypes();

    private OldBukkitEnums() {
    }

    public static EntityTypes entityTypes() {
        return ENTITY_TYPES;
    }

    public static final class EntityTypes {
        public final EntityType ENDER_CRYSTAL = EnumUtils.getIfPresent(EntityType.class, "ENDER_CRYSTAL", "END_CRYSTAL").orElse(null);
        public final EntityType PRIMED_TNT = EnumUtils.getIfPresent(EntityType.class, "PRIMED_TNT", "TNT").orElse(null);
    }
}
