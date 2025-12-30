package com.beverly.hills.money.gang.entities.ui;

import com.beverly.hills.money.gang.stats.TCPGameNetworkStatsReader;
import com.beverly.hills.money.gang.stats.UDPGameNetworkStatsReader;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;

@RequiredArgsConstructor
public class UINetworkStats {

  private final TCPGameNetworkStatsReader tcpGameNetworkStatsReader;
  private final UDPGameNetworkStatsReader udpGameNetworkStatsReader;

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

  private String networkStatsToString(UDPGameNetworkStatsReader udpGameNetworkStats) {
    return String.format(Locale.ENGLISH,
            "RCV %s MSG | SENT %s MSG | IN %s | OUT %s",
            udpGameNetworkStats.getReceivedMessages(),
            udpGameNetworkStats.getSentMessages(),
            FileUtils.byteCountToDisplaySize(udpGameNetworkStats.getInboundPayloadBytes()),
            FileUtils.byteCountToDisplaySize(udpGameNetworkStats.getOutboundPayloadBytes()))
        .toUpperCase();
  }

  private String networkStatsToString(TCPGameNetworkStatsReader networkStatsReader) {
    return networkStatsToString(networkStatsReader.getReceivedMessages(),
        networkStatsReader.getSentMessages(), networkStatsReader.getInboundPayloadBytes(),
        networkStatsReader.getOutboundPayloadBytes(), networkStatsReader.getPingMls());
  }

  @Override
  public String toString() {
    return "TCP " + networkStatsToString(tcpGameNetworkStatsReader) + "\n"
        + "UDP " + networkStatsToString(udpGameNetworkStatsReader) + "\n";
  }

}
