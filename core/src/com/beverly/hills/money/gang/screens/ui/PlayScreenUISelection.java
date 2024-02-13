package com.beverly.hills.money.gang.screens.ui;

public enum PlayScreenUISelection {
    QUIT, RESPAWN, CONTINUE;

    public static final PlayScreenUISelection[] DEAD_SELECTION = {RESPAWN, QUIT};
    public static final PlayScreenUISelection[] ALIVE_SELECTION = {CONTINUE, QUIT};

    private static PlayScreenUISelection getSelection(PlayScreenUISelection[] selections, int counter) {
        return selections[Math.abs(counter) % selections.length];
    }

    public static PlayScreenUISelection getAliveSelection(int counter) {
        return getSelection(ALIVE_SELECTION, counter);
    }

    public static PlayScreenUISelection getDeadSelection(int counter) {
        return getSelection(DEAD_SELECTION, counter);
    }
}
