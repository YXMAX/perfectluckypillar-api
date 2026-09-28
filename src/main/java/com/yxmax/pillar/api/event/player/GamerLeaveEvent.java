package com.yxmax.pillar.api.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GamerLeaveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Player player;

    public GamerLeaveEvent(Player player){
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
