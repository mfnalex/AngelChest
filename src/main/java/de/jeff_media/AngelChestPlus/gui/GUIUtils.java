package de.jeff_media.AngelChestPlus.gui;

import java.util.List;

public class GUIUtils {

    public static boolean isLootableInPreview(int slot) {
        return
                (slot >= GUI.PREVIEW_ARMOR_SIZE && slot < GUI.PREVIEW_ARMOR_SIZE + GUI.PREVIEW_ARMOR_SIZE)
                || (slot >= GUI.SLOT_PREVIEW_OFFHAND && slot < GUI.SLOT_PREVIEW_OFFHAND + GUI.PREVIEW_OFFHAND_SIZE)
                || (slot >= GUI.SLOT_PREVIEW_STORAGE_START && slot < GUI.SLOT_PREVIEW_STORAGE_START + GUI.PREVIEW_STORAGE_SIZE)
                || (slot >= GUI.SLOT_PREVIEW_HOTBAR_START && slot < GUI.SLOT_PREVIEW_HOTBAR_START + GUI.PREVIEW_HOTBAR_SIZE);

    }
}
