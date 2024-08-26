package com.beverly.hills.money.gang.screens;

import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.network.LoadBalancedGameConnection;
import com.beverly.hills.money.gang.screens.data.ConnectGameData;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO add manual test scenarios
public abstract class ReconnectableScreen extends AbstractLoadingScreen {

  private static final Logger LOG = LoggerFactory.getLogger(ReconnectableScreen.class);

  private final long screenCreateTimeMls = System.currentTimeMillis();

  private static final int TIME_DELAY_BEFORE_RECONNECT = 1500;

  public ReconnectableScreen(DaiKombatGame game) {
    super(game);
  }

  protected boolean isTimeToConnect(int connectionTrial) {
    if (connectionTrial == 0) {
      return true;
    }
    return System.currentTimeMillis() > screenCreateTimeMls + ((long) TIME_DELAY_BEFORE_RECONNECT
        * connectionTrial);
  }

  protected void reconnectOnError(
      final String errorMessage,
      final int connectionTrial,
      final LoadBalancedGameConnection gameConnection,
      final ConnectGameData connectGameData) {
    LOG.warn("Error while connecting '{}'", errorMessage);
    removeAllEntities();
    Optional.ofNullable(gameConnection).ifPresent(
        LoadBalancedGameConnection::disconnect);
    if (connectionTrial >= Configs.MAX_RECONNECTIONS) {
      getGame().setScreen(new ErrorScreen(getGame(), errorMessage));
    } else {
      LOG.warn("Retry connection");
      getGame().setScreen(new ConnectServerScreen(getGame(), connectGameData, connectionTrial + 1));
    }
  }
}
