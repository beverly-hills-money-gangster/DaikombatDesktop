package com.beverly.hills.money.gang.entities.ui;

import com.beverly.hills.money.gang.stats.GameNetworkStatsReader;
import com.beverly.hills.money.gang.stats.VoiceChatNetworkStatsReader;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;

@RequiredArgsConstructor
public class UINetworkStats {

  private final GameNetworkStatsReader primaryNetworkStats;
  private final Iterable<GameNetworkStatsReader> secondaryNetworkStats;
  private final VoiceChatNetworkStatsReader voiceChatNetworkStats;

  private String networkStatsToString(int receivedMsg, int sentMsg, long inboundBytes,
      long outboundBytes, Integer pingMls) {
    return String.format(Locale.ENGLISH,
            "RCV %s MSG | SENT %s MSG | IN %s | OUT %s | PING %s MS",
            receivedMsg,
            sentMsg,
            FileUtils.byteCountToDisplaySize(inboundBytes),
            FileUtils.byteCountToDisplaySize(outboundBytes),
            Objects.toString(pingMls, "-"))
        .toUpperCase();
  }

  private String networkStatsToString(VoiceChatNetworkStatsReader voiceChatNetworkStats) {
    return String.format(Locale.ENGLISH,
            "RCV %s MSG | SENT %s MSG | IN %s | OUT %s",
            voiceChatNetworkStats.getReceivedMessages(),
            voiceChatNetworkStats.getSentMessages(),
            FileUtils.byteCountToDisplaySize(voiceChatNetworkStats.getInboundPayloadBytes()),
            FileUtils.byteCountToDisplaySize(voiceChatNetworkStats.getOutboundPayloadBytes()))
        .toUpperCase();
  }

  private String networkStatsToString(GameNetworkStatsReader networkStatsReader) {
    return networkStatsToString(networkStatsReader.getReceivedMessages(),
        networkStatsReader.getSentMessages(), networkStatsReader.getInboundPayloadBytes(),
        networkStatsReader.getOutboundPayloadBytes(), networkStatsReader.getPingMls());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    int totalReceivedMsg = primaryNetworkStats.getReceivedMessages();
    int totalSentMsg = primaryNetworkStats.getSentMessages();
    long totalInboundBytes = primaryNetworkStats.getInboundPayloadBytes();
    long totalOutboundBytes = primaryNetworkStats.getOutboundPayloadBytes();
    sb.append("TCP PRIMARY ").append(networkStatsToString(primaryNetworkStats)).append("\n");
    int totalPing = Optional.ofNullable(primaryNetworkStats.getPingMls()).orElse(0);
    int totalConnections = 1;
    for (GameNetworkStatsReader networkStatsReader : secondaryNetworkStats) {
      sb.append("TCP SECONDARY ").append(networkStatsToString(networkStatsReader))
          .append("\n");
      totalReceivedMsg += networkStatsReader.getReceivedMessages();
      totalSentMsg += networkStatsReader.getSentMessages();
      totalInboundBytes += networkStatsReader.getInboundPayloadBytes();
      totalOutboundBytes += networkStatsReader.getOutboundPayloadBytes();
      totalPing += Optional.ofNullable(networkStatsReader.getPingMls()).orElse(0);
      totalConnections++;
    }
    sb.append("TCP TOTAL ").append(
            networkStatsToString(totalReceivedMsg, totalSentMsg, totalInboundBytes, totalOutboundBytes,
                totalPing / totalConnections))
        .append("\n");
    sb.append("UDP VOICE CHAT ").append(networkStatsToString(voiceChatNetworkStats)).append("\n");
    return sb.toString();
  }

}
