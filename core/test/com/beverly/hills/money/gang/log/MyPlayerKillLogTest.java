package com.beverly.hills.money.gang.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MyPlayerKillLogTest {

    private MyPlayerKillLog killLog;

    @BeforeEach
    public void setUp() {
        killLog = new MyPlayerKillLog();
    }

    @Test
    public void testHasKillerMessageDefault() {
        assertFalse(killLog.hasKillerMessage(), "By default, should be no message");
    }

    @Test
    public void testHasKillerMessage() {
        killLog.myPlayerKill("XXX", 20);
        assertTrue(killLog.hasKillerMessage());
    }

    @Test
    public void testHasKillerMessageAfterDelay() throws InterruptedException {
        killLog.myPlayerKill("XXX", 20);
        // wait a little
        Thread.sleep(MyPlayerKillLog.MAX_MSG_DURATION_MLS + 50);
        assertFalse(killLog.hasKillerMessage(), "After big delay, there should be no message");
    }

    @Test
    public void testGetKillerMessageDefault() {
        assertEquals("", killLog.getKillerMessage(), "By default, message is empty");
    }

    @Test
    public void testGetKillerMessage() {
        killLog.myPlayerKill("XXX", 20);
        assertEquals("YOU KILLED XXX. VAMPIRE HP BUFF +20 HP", killLog.getKillerMessage());
    }

    @Test
    public void testGetKillerMessageNoBuff() {
        killLog.myPlayerKill("XXX", 0);
        assertEquals("YOU KILLED XXX", killLog.getKillerMessage());
    }
}
