package de.jeff_media.angelchest.data;

import de.jeff_media.angelchest.enums.CommandAction;

/**
 * Represents a pending confirm when a player initiates an action that has a price and needs to be confirmed
 */
public final class PendingConfirm {

    public final int chestId;
    public final CommandAction action;

    public PendingConfirm(int chestId, CommandAction action) {
        this.chestId=chestId;
        this.action=action;
    }

    @Override
    public String toString() {
        return "PendingConfirm{chestId="+chestId+",action="+action+"}";
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof PendingConfirm) {
            PendingConfirm otherConfirm = (PendingConfirm) other;
            return this.chestId == otherConfirm.chestId && this.action == otherConfirm.action;
        }
        return false;
    }

}
