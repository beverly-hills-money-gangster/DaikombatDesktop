package com.beverly.hills.money.gang.entities.ui;

import com.beverly.hills.money.gang.stats.NetworkStatsReader;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;

@RequiredArgsConstructor
public class UINetworkStats {

  private final NetworkStatsReader primaryNetworkStats;
  private final Iterable<NetworkStatsReader> secondaryNetworkStats;

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

  private String networkStatsToString(NetworkStatsReader networkStatsReader) {
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
    sb.append("PRIMARY ").append(networkStatsToString(primaryNetworkStats)).append("\n");
    int totalPing = Optional.ofNullable(primaryNetworkStats.getPingMls()).orElse(0);
    int totalConnections = 1;
    for (NetworkStatsReader networkStatsReader : secondaryNetworkStats) {
      sb.append("SECONDARY ").append(networkStatsToString(networkStatsReader))
          .append("\n");
      totalReceivedMsg += networkStatsReader.getReceivedMessages();
      totalSentMsg += networkStatsReader.getSentMessages();
      totalInboundBytes += networkStatsReader.getInboundPayloadBytes();
      totalOutboundBytes += networkStatsReader.getOutboundPayloadBytes();
      totalPing += Optional.ofNullable(networkStatsReader.getPingMls()).orElse(0);
      totalConnections++;
    }
    sb.append("TOTAL ").append(
            networkStatsToString(totalReceivedMsg, totalSentMsg, totalInboundBytes, totalOutboundBytes,
                totalPing / totalConnections))
        .append("\n");
    return sb.toString();
  }

}
