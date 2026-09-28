package com.yxmax.pillar.api.event.game;

import com.yxmax.pillar.api.PerfectLuckyPillarAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Set;

public class GameFightEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public Set<Player> getGamers(){
        return PerfectLuckyPillarAPI.getGamePlayerManager().getSurvivedGamers();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
