package com.beverly.hills.money.gang.screens.loading;

import com.beverly.hills.money.gang.configs.EnvConfigs;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.network.GlobalGameConnection;
import com.beverly.hills.money.gang.screens.data.CompleteJoinGameData;
import com.beverly.hills.money.gang.screens.menu.ErrorScreen;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ReconnectableScreen extends AbstractLoadingScreen {

  private static final Logger LOG = LoggerFactory.getLogger(ReconnectableScreen.class);

  private Runnable reconnectionRunnable;

  protected final int connectionTrial;

  private final long screenCreateTimeMls = System.currentTimeMillis();

  private static final int TIME_DELAY_BEFORE_RECONNECT = 5000;

  private final String loadingMessage;

  public ReconnectableScreen(DaiKombatGame game, int connectionTrial) {
    super(game);
    this.connectionTrial = connectionTrial;
    if (connectionTrial > 0) {
      this.loadingMessage = "RECONNECTING " + (connectionTrial);
    } else {
      this.loadingMessage = Constants.CONNECTING;
    }
    LOG.info("Start connecting");
  }

  protected boolean isTimeToReconnect() {
    return System.currentTimeMillis() > screenCreateTimeMls + TIME_DELAY_BEFORE_RECONNECT;
  }

  protected void reconnect(final String errorMessage,
      final GlobalGameConnection gameConnection, final CompleteJoinGameData completeJoinGameData) {
    LOG.info("Reconnect. Error is '{}'", errorMessage);
    reconnectionRunnable = () -> {
      if (connectionTrial >= EnvConfigs.MAX_RECONNECTIONS) {
        LOG.info("Go to error screen");
        removeAllEntities();
        getGame().setScreen(
            new ErrorScreen(getGame(), StringUtils.defaultIfBlank(errorMessage, "Can't connect")));
        return;
      } else if (!isTimeToReconnect()) {
        return;
      }
      LOG.info("Start reconnecting");
      removeAllEntities();
      Optional.ofNullable(gameConnection).ifPresent(GlobalGameConnection::disconnect);
      LOG.info("Retry connection");
      getGame().setScreen(
          new ConnectServerScreen(getGame(), completeJoinGameData, connectionTrial + 1));
    };
  }

  protected abstract void onLoadingRenderInternal(final float delta);

  @Override
  protected final void onLoadingRender(final float delta) {
    if (reconnectionRunnable != null) {
      reconnectionRunnable.run();
      return;
    }
    onLoadingRenderInternal(delta);
  }

  @Override
  protected String getBaseLoadingMessage() {
    return loadingMessage;
  }
}
