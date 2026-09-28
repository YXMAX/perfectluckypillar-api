package com.yxmax.pillar.api.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GamerDeathEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;

    private Player target;

    private Player killer;

    public GamerDeathEvent(Player target, Player killer) {
        this.target = target;
        this.killer = killer;
    }

    public Player getPlayer() {
        return target;
    }

    public Player getKiller() {
        return killer;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
