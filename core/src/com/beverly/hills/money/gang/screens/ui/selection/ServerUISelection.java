package com.beverly.hills.money.gang.screens.ui.selection;

import lombok.Getter;

public enum ServerUISelection {
    OFFICIAL("OFFICIAL SERVER"), CUSTOM("CUSTOM SERVER");

    @Getter
    private final String title;

    ServerUISelection(String title) {
        this.title = title;
    }


    @Override
    public String toString() {
        return this.title;
    }

}
