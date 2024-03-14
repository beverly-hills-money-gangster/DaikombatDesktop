package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.network.GameConnection;
import com.beverly.hills.money.gang.proto.RespawnCommand;
import com.beverly.hills.money.gang.proto.ServerResponse;
import com.beverly.hills.money.gang.screens.data.PlayerContextData;
import com.beverly.hills.money.gang.utils.Converter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class RespawnScreen extends AbstractMainMenuScreen {

    private static final Logger LOG = LoggerFactory.getLogger(RespawnScreen.class);

    private final long LOAD_UNTIL_MLS = System.currentTimeMillis() + 5_000;

    private boolean stopLoading;

    private String errorMessage;

    private static final int MAX_LOADING_DOTS = 3;

    private final PlayerContextData oldPlayerContextData;

    private long loadingAnimationSwitchMls = 0;
    private static final long LOADING_ANIMATION_MLS = 250;
    private String loadingDots = "";

    private final BitmapFont guiFont64;

    private final GameConnection gameConnection;

    public RespawnScreen(final DaiKombatGame game,
                         final PlayerContextData oldPlayerContextData,
                         final GameConnection gameConnection) {
        super(game);
        LOG.info("My player id {}", oldPlayerContextData.getPlayerId());
        guiFont64 = getGame().getAssMan().getFont(FontRegistry.FONT_64);
        this.oldPlayerContextData = oldPlayerContextData;
        this.gameConnection = gameConnection;
    }

    @Override
    public void show() {
        if (gameConnection.isDisconnected()) {
            errorMessage = "Connection lost";
        } else {
            gameConnection.write(RespawnCommand.newBuilder()
                    .setGameId(Configs.GAME_ID)
                    .setPlayerId(oldPlayerContextData.getPlayerId()).build());
        }
    }

    @Override
    public void handleInput(final float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            removeAllEntities();
            gameConnection.disconnect();
            getGame().setScreen(new MainMenuScreen(getGame()));
        }
    }

    @Override
    public void render(final float delta) {
        super.render(delta);
        getGame().getBatch().begin();
        if (!stopLoading) {
            String loadingMsg = getLoadingMessage();
            GlyphLayout glyphLayoutLoading = new GlyphLayout(guiFont64, loadingMsg);
            guiFont64.draw(getGame().getBatch(), loadingMsg,
                    getViewport().getWorldWidth() / 2f - glyphLayoutLoading.width / 2f,
                    getViewport().getWorldHeight() / 2f - glyphLayoutLoading.height / 2f - Constants.LOGO_INDENT);
        }
        if (System.currentTimeMillis() > LOAD_UNTIL_MLS) {
            errorMessage = "Respawn timeout";
            gameConnection.disconnect();
        }
        Optional.ofNullable(errorMessage)
                .map(StringUtils::upperCase)
                .ifPresentOrElse(errorMessage -> {
                    stopLoading = true;
                    LOG.info("Server response {}", gameConnection.getResponse());
                    getGame().setScreen(new ErrorScreen(getGame(), errorMessage));
                }, () -> gameConnection.getResponse().poll().ifPresentOrElse(response -> {
                    if (response.hasErrorEvent()) {
                        errorMessage = response.getErrorEvent().getMessage();
                    } else if (response.hasGameEvents()) {
                        var gameEvent = response.getGameEvents().getEvents(0);
                        if (gameEvent.getEventType() != ServerResponse.GameEvent.GameEventType.SPAWN
                                || gameEvent.getPlayer().getPlayerId() != oldPlayerContextData.getPlayerId()) {
                            // not our event
                            return;
                        }
                        removeAllEntities();
                        stopBgMusic();
                        LOG.info("My spawn {}", gameEvent);
                        int playerId = gameEvent.getPlayer().getPlayerId();
                        getGame().setScreen(new PlayScreen(getGame(), gameConnection, PlayerContextData.builder()
                                .playerId(playerId)
                                .playersOnline(response.getGameEvents().getPlayersOnline())
                                .playerServerInfoContextData(oldPlayerContextData.getPlayerServerInfoContextData())
                                .spawn(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
                                .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
                                .leaderBoardItemList(gameEvent.getLeaderBoard().getItemsList())
                                .build()));
                    }
                }, () -> gameConnection.getErrors().poll().ifPresent(throwable -> {
                    LOG.error("Error while loading", throwable);
                    gameConnection.disconnect();
                    errorMessage = ExceptionUtils.getMessage(throwable);
                })));

        getGame().getBatch().end();
    }

    private String getLoadingMessage() {
        if (System.currentTimeMillis() >= loadingAnimationSwitchMls) {
            loadingAnimationSwitchMls = System.currentTimeMillis() + LOADING_ANIMATION_MLS;
            if (loadingDots.length() == MAX_LOADING_DOTS) {
                loadingDots = "";
            } else {
                loadingDots += ".";
            }
        }
        return Constants.CONNECTING + loadingDots;
    }
}
