package com.beverly.hills.money.gang.network;

import java.io.Closeable;
import java.io.IOException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class NetworkConnections implements Closeable {

  @NonNull
  private final GlobalGameConnection loadBalancedGameConnection;
  @NonNull
  private final VoiceChatConnection voiceChatConnection;


  @Override
  public void close() throws IOException {
    loadBalancedGameConnection.disconnect();
    voiceChatConnection.close();
  }
}
