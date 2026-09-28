package com.yxmax.pillar.api;

public class PerfectLuckyPillarAPI {

    private static IGamePlayerManager gamePlayerManager;

    private static IGameStatusManager gameStatusManager;

    private static IGameWorldManager gameWorldManager;

    private static IGameEventManager gameEventManager;

    private static IPlayerLocaleManager playerLocaleManager;

    public static void register(IGamePlayerManager playerManager,IGameStatusManager statusManager,IGameWorldManager worldManager,IGameEventManager eventManager,IPlayerLocaleManager localeManager){
        if (gamePlayerManager == null) {
            gamePlayerManager = playerManager;
        }
        if (gameStatusManager == null) {
            gameStatusManager = statusManager;
        }
        if(gameWorldManager == null){
            gameWorldManager = worldManager;
        }
        if(gameEventManager == null){
            gameEventManager = eventManager;
        }
        if(playerLocaleManager == null){
            playerLocaleManager = localeManager;
        }
    }

    public static void unregister(){
        gamePlayerManager = null;
        gameStatusManager = null;
        gameWorldManager = null;
        gameEventManager = null;
        playerLocaleManager = null;
    }

    public static IGamePlayerManager getGamePlayerManager(){
        return gamePlayerManager;
    }

    public static IGameStatusManager getGameStatusManager(){
        return gameStatusManager;
    }

    public static IGameWorldManager getGameWorldManager(){
        return gameWorldManager;
    }

    public static IGameEventManager getGameEventManager(){
        return gameEventManager;
    }

    public static IPlayerLocaleManager getPlayerLocaleManager(){
        return playerLocaleManager;
    }
}
