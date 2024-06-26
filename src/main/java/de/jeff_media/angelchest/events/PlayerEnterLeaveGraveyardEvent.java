package de.jeff_media.angelchest.events;

import de.jeff_media.angelchest.data.Graveyard;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class PlayerEnterLeaveGraveyardEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter private final Player player;
    @Getter private final Graveyard graveyard;
    @Getter private final Action action;


    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public String toString() {
        return "PlayerEnterLeaveGraveyardEvent{" +
                "player=" + player +
                ", graveyard=" + graveyard +
                ", action=" + action +
                '}';
    }

    public enum Action {
        LEAVE, ENTER
    }
}
