package com.beverly.hills.money.gang.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ChatLogTest {

    private ChatLog chatLog;

    private Runnable onMessageRunnable;

    @BeforeEach
    public void setUp() {
        onMessageRunnable = mock(Runnable.class);
        chatLog = new ChatLog(onMessageRunnable);
    }

    @Test
    public void testDefaults() {
        assertEquals("", chatLog.getChatMessages(), "By default, should be no messages");
        assertFalse(chatLog.hasChatMessage(), "By default, should be no messages");
    }

    @Test
    public void testAddMessage() {
        chatLog.addMessage("PLAYER NAME", "MESSAGE");
        assertEquals("PLAYER NAME:  MESSAGE\n", chatLog.getChatMessages());
        assertTrue(chatLog.hasChatMessage());
        verify(onMessageRunnable).run();
    }

    @Test
    public void testAddMessageAfterDelay() throws InterruptedException {
        chatLog.addMessage("PLAYER NAME", "MESSAGE");
        // wait a little
        Thread.sleep(ChatLog.MAX_MSG_DURATION_MLS + 50);
        assertFalse(chatLog.hasChatMessage());
    }

    @Test
    public void testAddMessageFiveTimes() {
        chatLog.addMessage("PLAYER NAME", "MESSAGE1");
        chatLog.addMessage("PLAYER NAME", "MESSAGE2");
        chatLog.addMessage("PLAYER NAME", "MESSAGE3");
        chatLog.addMessage("PLAYER NAME", "MESSAGE4");
        chatLog.addMessage("PLAYER NAME", "MESSAGE5");
        assertEquals(
                "PLAYER NAME:  MESSAGE1\n" +
                        "PLAYER NAME:  MESSAGE2\n" +
                        "PLAYER NAME:  MESSAGE3\n" +
                        "PLAYER NAME:  MESSAGE4\n" +
                        "PLAYER NAME:  MESSAGE5\n", chatLog.getChatMessages());
        assertTrue(chatLog.hasChatMessage());
        verify(onMessageRunnable, times(5)).run();
    }

    @Test
    public void testAddMessageSixTimes() {
        chatLog.addMessage("PLAYER NAME", "MESSAGE1");
        chatLog.addMessage("PLAYER NAME", "MESSAGE2");
        chatLog.addMessage("PLAYER NAME", "MESSAGE3");
        chatLog.addMessage("PLAYER NAME", "MESSAGE4");
        chatLog.addMessage("PLAYER NAME", "MESSAGE5");
        chatLog.addMessage("PLAYER NAME", "MESSAGE6");
        assertEquals(
                "PLAYER NAME:  MESSAGE2\n" +
                        "PLAYER NAME:  MESSAGE3\n" +
                        "PLAYER NAME:  MESSAGE4\n" +
                        "PLAYER NAME:  MESSAGE5\n" +
                        "PLAYER NAME:  MESSAGE6\n", chatLog.getChatMessages(),
                "Only last 5 messages must be returned");
        assertTrue(chatLog.hasChatMessage());
        verify(onMessageRunnable, times(6)).run();
    }
}
