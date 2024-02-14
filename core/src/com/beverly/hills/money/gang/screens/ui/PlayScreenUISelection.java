package com.beverly.hills.money.gang.screens.ui;

import org.apache.commons.lang3.ArrayUtils;

public enum PlayScreenUISelection {
    QUIT, RESPAWN, CONTINUE;

    private static final PlayScreenUISelection[] DEAD_SELECTION = {RESPAWN, QUIT};
    private static final PlayScreenUISelection[] ALIVE_SELECTION = {CONTINUE, QUIT};

    private static PlayScreenUISelection getSelection(PlayScreenUISelection[] selections, int counter) {
        return selections[Math.abs(counter) % selections.length];
    }

    public static boolean isDeadSelection(PlayScreenUISelection screenUISelection) {
        return ArrayUtils.contains(PlayScreenUISelection.DEAD_SELECTION, screenUISelection);
    }

    public static boolean isAliveSelection(PlayScreenUISelection screenUISelection) {
        return ArrayUtils.contains(PlayScreenUISelection.ALIVE_SELECTION, screenUISelection);
    }

    public static PlayScreenUISelection getAliveSelection(int counter) {
        return getSelection(ALIVE_SELECTION, counter);
    }

    public static PlayScreenUISelection getDeadSelection(int counter) {
        return getSelection(DEAD_SELECTION, counter);
    }
}
