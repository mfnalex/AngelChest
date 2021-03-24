package de.jeff_media.AngelChest.gui;

import de.jeff_media.AngelChest.*;
import de.jeff_media.AngelChest.config.Config;
import de.jeff_media.AngelChest.config.Permissions;
import de.jeff_media.AngelChest.data.AngelChest;
import de.jeff_media.AngelChest.enums.Features;
import de.jeff_media.AngelChest.enums.TeleportAction;
import de.jeff_media.AngelChest.utils.CommandUtils;
import de.jeff_media.AngelChest.utils.HeadCreator;
import de.jeff_media.AngelChest.utils.Utils;
import de.jeff_media.AngelChest.utils.XPUtils;
import de.jeff_media.daddy.Daddy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class GUIManager {

    private final Main main;

    public GUIManager() {
        this.main = Main.getInstance();

    }

    public void updatePreviewInvs(Player originalPlayer, AngelChest angelChest) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.equals(originalPlayer)) continue;
            if(player.getOpenInventory() == null) continue;
            if(player.getOpenInventory().getTopInventory() == null) continue;
            if(!(player.getOpenInventory().getTopInventory().getHolder() instanceof GUIHolder)) continue;
            GUIHolder guiHolder = (GUIHolder) player.getOpenInventory().getTopInventory().getHolder();
            if(guiHolder.getSpecialAngelChest() != null && guiHolder.getSpecialAngelChest().equals(angelChest)) {
                main.debug("This AngelChest "+angelChest.toString()+" is also in use by "+player.getName()+", updating...");
                if(!angelChest.isEmpty()) {
                    showPreviewGUI(player, angelChest, guiHolder.isReadOnlyPreview(), false);
                } else {
                    player.closeInventory();
                }
            }
        }
    }

    public void updateGUI(Player player, int brokenChestId) {
        if (player.getOpenInventory() == null) return;
        if (player.getOpenInventory().getTopInventory() == null) return;
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof GUIHolder)) return;

        GUIHolder holder = (GUIHolder) player.getOpenInventory().getTopInventory().getHolder();

        if (holder.getContext() == GUIContext.MAIN_MENU) {
            showMainGUI(player);
            return;
        }

        int selectedChest = holder.getChestIdStartingAt1();
        if (selectedChest < brokenChestId) {
            //noinspection UnnecessaryReturnStatement
            return;
        } else if (brokenChestId == selectedChest) {
            showMainGUI(player);
        } else {
            holder.setChestIdStartingAt1(holder.getChestIdStartingAt1() - 1);
            if(holder.getContext()==GUIContext.CHEST_MENU) {
                showChestGUI(player,holder,holder.getChestIdStartingAt1());
            } else if(holder.getContext()==GUIContext.CONFIRM_MENU) {
                showConfirmGUI(player,holder,holder.getAction());
            }
        }
    }

    private ItemStack getChestItem(AngelChest angelChest, int id) {
        ItemStack item = new ItemStack(main.getChestMaterial(angelChest));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getChestItemName(angelChest, id));
        meta.setLore(getChestItemLore(angelChest, id));
        item.setItemMeta(meta);
        return item;
    }

    private String getChestItemName(@SuppressWarnings("unused") AngelChest angelChest, int id) {
        return String.format("§6AngelChest #%d", id);
    }

    private List<String> getChestItemLore(AngelChest angelChest,@SuppressWarnings("unused") int id) {

        String[] lore = new String[]{
                String.format("§4%s",CommandUtils.getTimeLeft(angelChest)),
                String.format("§aX: §f%d", angelChest.block.getX()),
                String.format("§aY: §f%d", angelChest.block.getY()),
                String.format("§aZ: §f%d", angelChest.block.getZ()),
                String.format(/*"§aWorld: "+*/"§f%s", angelChest.block.getWorld().getName())
        };
        return Arrays.asList(lore);
    }

    private static int getInventorySize(int numberOfChests) {
        if (numberOfChests <= 9) return 9;
        if (numberOfChests <= 18) return 18;
        if (numberOfChests <= 27) return 27;
        if (numberOfChests <= 36) return 36;
        if (numberOfChests <= 45) return 45;
        return 54;
    }

    private boolean hasOpen(Player player, Inventory inventory) {
        if(player.getOpenInventory() == null) return false;
        if(player.getOpenInventory().getTopInventory() == null) return false;
        return player.getOpenInventory().getTopInventory().equals(inventory);
    }

    public void showLatestChestGUI(Player player) {
        GUIHolder holder = new GUIHolder(player, GUIContext.MAIN_MENU);
        int latestChest = holder.getNumberOfAngelChests();
        int inventorySize = getInventorySize(latestChest);
        Inventory inventory = Bukkit.createInventory(holder, inventorySize, main.messages.GUI_TITLE_MAIN);
        holder.setInventory(inventory);

        holder.setChestIdStartingAt1(latestChest);
        main.guiManager.showChestGUI(player, holder, latestChest);
    }

    public void showMainGUI(Player player) {
        GUIHolder holder = new GUIHolder(player, GUIContext.MAIN_MENU);
        int inventorySize = getInventorySize(holder.getNumberOfAngelChests());
        Inventory inventory = Bukkit.createInventory(holder, inventorySize, main.messages.GUI_TITLE_MAIN);
        holder.setInventory(inventory);

        if (Utils.getAllAngelChestsFromPlayer(player).size() == 1) {
            holder.setChestIdStartingAt1(1);
            main.guiManager.showChestGUI(player, holder, 1);
            return;
        }

        int id = 1;
        for (AngelChest chest : holder.getAngelChests()) {
            if (id > inventorySize) break;

            ItemStack item = getChestItem(chest, id);
            inventory.setItem(id - 1, item);

            id++;
        }

        player.openInventory(inventory);

        Bukkit.getScheduler().scheduleSyncDelayedTask(main,() -> {
            if(hasOpen(player,inventory)) {
                showMainGUI(player);
            }
        }, 20L);
    }

    public void showPreviewGUI(Player player, AngelChest angelChest, boolean isPreview, boolean firstOpened) {
        GUIHolder holder = new GUIHolder(player,GUIContext.PREVIEW_MENU);
        Inventory inventory = Bukkit.createInventory(holder,54,main.messages.GUI_TITLE_MAIN);
        holder.setInventory(inventory);
        holder.setChestIdStartingAt1(Utils.getAllAngelChestsFromPlayer(angelChest.owner).indexOf(angelChest));
        holder.setAngelChest(angelChest);
        //holder.setChestIdStartingAt1(Utils.getAllAngelChestsFromPlayer(angelChest.owner,main).indexOf(angelChest)+1);
        /*inventory = */GUIUtils.loadChestIntoPreviewInventory(holder.getAngelChest(),inventory);

        if(isPreview) {
            inventory.setItem(GUI.SLOT_PREVIEW_BACK,getBackButton());
            holder.setReadOnlyPreview(true);
        }
        if(angelChest.experience > 0 || angelChest.levels > 0) {
            inventory.setItem(GUI.SLOT_PREVIEW_XP,getButton(Material.EXPERIENCE_BOTTLE,"§6"+ XPUtils.xpToString(angelChest.experience),null));
        }

        if(Daddy.allows(Features.GENERIC)) { // Don't add feature here
            if (!isPreview && firstOpened && !player.getUniqueId().equals(angelChest.owner) && main.getConfig().getBoolean(Config.SHOW_MESSAGE_WHEN_OTHER_PLAYER_OPENS_CHEST)) {
                if (Bukkit.getPlayer(angelChest.owner) != null) {
                    Bukkit.getPlayer(angelChest.owner).sendMessage(main.messages.MSG_OPENED.replaceAll("\\{player}", player.getName()));
                }
            }
        }

        player.openInventory(inventory);
    }

    public void showChestGUI(Player player, GUIHolder holder, int id) {
        AngelChest angelChest = holder.getAngelChest();
        GUIHolder newHolder = new GUIHolder(player, GUIContext.CHEST_MENU, id);
        Inventory inventory = Bukkit.createInventory(newHolder, 9, getTitle(holder.getAngelChest(),holder.getChestIdStartingAt1()));
        newHolder.setInventory(inventory);

        inventory.setItem(GUI.SLOT_CHEST_BACK, getBackButton());
        inventory.setItem(GUI.SLOT_CHEST_INFO, getInfoButton(angelChest, id));
        if(player.hasPermission(Permissions.ALLOW_TELEPORT)) inventory.setItem(GUI.SLOT_CHEST_TP, getTPButton());
        if(player.hasPermission(Permissions.ALLOW_FETCH)) inventory.setItem(GUI.SLOT_CHEST_FETCH, getFetchButton());
        if(player.hasPermission(Permissions.ALLOW_PROTECT) && angelChest.isProtected) inventory.setItem(GUI.SLOT_CHEST_UNLOCK, getUnlockButton());
        if(player.hasPermission(Permissions.ALLOW_PREVIEW)) inventory.setItem(GUI.SLOT_CHEST_PREVIEW, getPreviewButton());
        player.openInventory(inventory);

        Bukkit.getScheduler().scheduleSyncDelayedTask(main,() -> {
            if(hasOpen(player,inventory)) {
                showChestGUI(player, holder, holder.getChestIdStartingAt1());
            }
        }, 20L);
    }

    public void showConfirmGUI(Player player, GUIHolder holder, TeleportAction action) {
        GUIHolder newHolder = new GUIHolder(player, GUIContext.CONFIRM_MENU, holder.getChestIdStartingAt1());
        newHolder.setAction(action);
        Inventory inventory = Bukkit.createInventory(newHolder, 9, getTitle(holder.getAngelChest(),holder.getChestIdStartingAt1()));
        newHolder.setInventory(inventory);

        inventory.setItem(GUI.SLOT_CONFIRM_INFO, getConfirmInfoButton(action.getPrice(player)));
        inventory.setItem(GUI.SLOT_CONFIRM_ACCEPT, getConfirmAcceptButton());
        inventory.setItem(GUI.SLOT_CONFIRM_DECLINE, getConfirmDeclineButton());
        player.openInventory(inventory);

        Bukkit.getScheduler().scheduleSyncDelayedTask(main,() -> {
            try {
                if (hasOpen(player, inventory)) {
                    showConfirmGUI(player, holder, holder.getAction());
                }
            } catch (NullPointerException ignored) {
                //main.debug("Null in repeating task in showConfirmGUI");
                // TODO: No idea why it happens, but everything still works normally lol so fuck it.
            }
        }, 20L);
    }

    private ItemStack getPreviewButton() {
        return getButton(Material.BOOK,main.messages.GUI_PREVIEW,null);
    }

    private ItemStack getBackButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_BACK), main.messages.GUI_BACK, null);
    }

    private ItemStack getInfoButton(AngelChest angelChest, int id) {
        return getButton(Material.PAPER, main.messages.GUI_INFO, getChestItemLore(angelChest, id));
    }

    private ItemStack getTPButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_TELEPORT), main.messages.GUI_TELEPORT, null);
    }

    private ItemStack getFetchButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_FETCH), main.messages.GUI_FETCH, null);
    }

    private ItemStack getUnlockButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_UNLOCK), main.messages.GUI_UNLOCK, null);
    }

    private ItemStack getConfirmAcceptButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_CONFIRM_ACCEPT), main.messages.GUI_ACCEPT, null);
    }

    private ItemStack getConfirmDeclineButton() {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_CONFIRM_DECLINE), main.messages.GUI_DECLINE, null);
    }

    private ItemStack getConfirmInfoButton(double price) {
        return getButton(main.getConfig().getString(Config.GUI_BUTTON_CONFIRM_INFO), main.messages.GUI_INFO,
                getLore(main.messages.GUI_INFO_LORE
                        .replaceAll("\\{price}", String.valueOf(price))
                        .replaceAll("\\{currency}", CommandUtils.getCurrency(price, main))));
    }

    @SuppressWarnings("SameParameterValue")
    private ItemStack getButton(Material material, String name, @Nullable List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getButton(String head, String name, @Nullable List<String> lore) {
        ItemStack item = HeadCreator.getHead(head);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private List<String> getLore(String text) {
        return Arrays.asList(text.split("\n"));
    }

    private String getTitle(AngelChest chest, int id) {
        return main.messages.GUI_TITLE_CHEST.replaceAll("\\{id}", String.valueOf(id))
                        .replaceAll("\\{time}",CommandUtils.getTimeLeft(chest));

    }


}
