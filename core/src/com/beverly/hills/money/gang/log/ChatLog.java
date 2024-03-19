package com.beverly.hills.money.gang.log;


import java.util.ArrayDeque;
import java.util.Queue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatLog {


  private final Runnable onNewMessage;
  static final int MAX_MSG_TO_PRINT = 5;
  static final int MAX_MSG_DURATION_MLS = 5_000;
  private final Queue<String> chatMessageQueue = new ArrayDeque<>();
  private long expireTime;

  public void addMessage(String playerName, String message) {
    chatMessageQueue.add((playerName + ":  " + message).toUpperCase());
    expireTime = System.currentTimeMillis() + MAX_MSG_DURATION_MLS;
    if (chatMessageQueue.size() > MAX_MSG_TO_PRINT) {
      chatMessageQueue.remove();
    }
    onNewMessage.run();
  }

  public boolean hasChatMessage() {
    return System.currentTimeMillis() <= expireTime && !chatMessageQueue.isEmpty();
  }

  public String getChatMessages() {
    StringBuilder chatLogBuilder = new StringBuilder();
    chatMessageQueue.forEach(message -> chatLogBuilder.append(message).append("\n"));
    return chatLogBuilder.toString();
  }

}
