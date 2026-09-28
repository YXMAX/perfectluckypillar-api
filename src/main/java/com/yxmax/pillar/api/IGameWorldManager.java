package com.yxmax.pillar.api;

import org.bukkit.Location;
import org.bukkit.World;

public interface IGameWorldManager {

    World getWorld();

    World getWaitWorld();

    Location getCenterLocation();
}
