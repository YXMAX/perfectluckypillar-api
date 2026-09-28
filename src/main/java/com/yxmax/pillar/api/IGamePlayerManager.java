package com.yxmax.pillar.api;

import org.bukkit.entity.Player;

import java.util.Set;

public interface IGamePlayerManager {

    Set<Player> getSurvivedGamers();

    Player getWinner();

    Player getAttacker(Player player);
}
