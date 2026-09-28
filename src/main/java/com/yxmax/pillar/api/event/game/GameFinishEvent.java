package com.yxmax.pillar.api.event.game;

import com.yxmax.pillar.api.PerfectLuckyPillarAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameFinishEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public Player getWinner(){
        return PerfectLuckyPillarAPI.getGamePlayerManager().getWinner();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
