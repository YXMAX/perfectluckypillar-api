package com.yxmax.pillar.api;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;

public interface AreaEvent {

    String getEventName(Player player);

    String getEventId();

    void eventStart(World world, Set<Player> players);

    void eventEnd(World world, Set<Player> players);
}
