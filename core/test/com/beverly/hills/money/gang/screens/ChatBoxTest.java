package com.beverly.hills.money.gang.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ChatBoxTest {

  @Test
  public void testGetGreetingNoPlayers() {
    assertNull(ChatBox.getGreeting(0));
  }

  @Test
  public void testGetGreetingOnePlayer() {
    assertEquals("JOINED CHAT. PRESS ` TO TEXT",
        ChatBox.getGreeting(1));
  }

  @Test
  public void testGetGreetingThreePlayers() {
    assertEquals("YOU + 2 MORE IN CHAT. PRESS ` TO TEXT",
        ChatBox.getGreeting(3));
  }

}
