package com.yxmax.pillar.api.event.player;

import com.yxmax.pillar.api.PerfectLuckyPillarAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GamerDamageEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;

    private Player target;

    private Entity damager;

    public GamerDamageEvent(Player player,Entity damager) {
        this.target = player;
        this.damager = damager;
    }

    public Player getPlayer() {
        return target;
    }

    public Entity getDamager() {
        return damager;
    }

    public Player getPlayerDamager(){
        if(damager != null && (damager instanceof Player)){
            return (Player) damager;
        }
        return PerfectLuckyPillarAPI.getGamePlayerManager().getAttacker(this.target);
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
