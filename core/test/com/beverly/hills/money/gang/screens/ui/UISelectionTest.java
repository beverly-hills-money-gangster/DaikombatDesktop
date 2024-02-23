package com.beverly.hills.money.gang.screens.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UISelectionTest {

    private UISelection<TestUISelection> uiSelection;

    @BeforeEach
    public void setUp() {
        uiSelection = new UISelection<>(TestUISelection.values());
    }

    @Test
    public void testGetSelectedOptionDefault() {
        assertEquals(TestUISelection.FIRST, uiSelection.getSelectedOption(),
                "By default, the first enum must be picked");
    }

    @Test
    public void testUp() {
        uiSelection.up();
        assertEquals(TestUISelection.THIRD, uiSelection.getSelectedOption(),
                "We should get the last selection if we go up from the first selection");
    }

    @Test
    public void testDown() {
        uiSelection.down();
        assertEquals(TestUISelection.SECOND, uiSelection.getSelectedOption(),
                "2nd selection should go after the 1st if player goes down the selection");
    }

    @Test
    public void testUpFullCircle() {
        for (int i = 0; i < TestUISelection.values().length; i++) {
            uiSelection.up();
        }
        assertEquals(TestUISelection.FIRST, uiSelection.getSelectedOption(),
                "We must get back to the first selection if we made the full circle by pressing up");
    }

    @Test
    public void testDownFullCircle() {
        for (int i = 0; i < TestUISelection.values().length; i++) {
            uiSelection.down();
        }
        assertEquals(TestUISelection.FIRST, uiSelection.getSelectedOption(),
                "We must get back to the first selection if we made the full circle by pressing down");
    }


    private enum TestUISelection {
        FIRST, SECOND, THIRD
    }

}
