package de.jeff_media.AngelChestPlus;

public class PendingConfirm {

    public final AngelChest chest;
    public final Action action;

    public enum Action {
        TP, Fetch
    }

    public PendingConfirm(AngelChest chest, Action action) {
        this.chest=chest;
        this.action=action;
    }

}
