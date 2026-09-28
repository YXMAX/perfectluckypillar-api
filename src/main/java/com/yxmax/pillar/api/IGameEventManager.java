package com.yxmax.pillar.api;

public interface IGameEventManager {

    AreaEvent getCurrentEvent();

    AreaEvent getNextEvent();

    int getRunEventAmount();

    void registerEvent(AreaEvent event);

    int getDamageMultiply();

    void setDamageMultiply(int multiply);
}
