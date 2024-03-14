package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.config.ClientConfig;
import com.beverly.hills.money.gang.entity.GameServerCreds;
import com.beverly.hills.money.gang.entity.HostPort;
import com.beverly.hills.money.gang.network.GameConnection;
import com.beverly.hills.money.gang.proto.JoinGameCommand;
import com.beverly.hills.money.gang.screens.data.PlayerContextData;
import com.beverly.hills.money.gang.screens.data.PlayerServerInfoContextData;
import com.beverly.hills.money.gang.utils.Converter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class JoinGameScreen extends AbstractMainMenuScreen {

    private static final Logger LOG = LoggerFactory.getLogger(JoinGameScreen.class);

    private boolean stopLoading;

    private final AtomicReference<String> errorMessageRef = new AtomicReference<>();

    private final AtomicReference<GameConnection> gameConnectionRef = new AtomicReference<>();
    private static final int MAX_LOADING_DOTS = 3;

    private final PlayerServerInfoContextData playerServerInfoContextData;

    private long loadingAnimationSwitchMls = 0;
    private static final long LOADING_ANIMATION_MLS = 250;
    private String loadingDots = "";

    private final BitmapFont guiFont64;

    public JoinGameScreen(final DaiKombatGame game, final PlayerServerInfoContextData playerServerInfoContextData) {
        super(game);
        this.playerServerInfoContextData = playerServerInfoContextData;
        Thread connectionInitThread = new Thread(() -> {
            try {
                gameConnectionRef.set(new GameConnection(GameServerCreds.builder()
                        .hostPort(HostPort.builder()
                                .host(playerServerInfoContextData.getServerHost())
                                .port(playerServerInfoContextData.getServerPort()).build())
                        .password(playerServerInfoContextData.getServerPassword())
                        .build()));
                gameConnectionRef.get().write(JoinGameCommand.newBuilder()
                        .setVersion(ClientConfig.VERSION)
                        .setGameId(Configs.GAME_ID)
                        .setPlayerName(playerServerInfoContextData.getPlayerName())
                        .build());
            } catch (Throwable e) {
                LOG.error("Can't create connection", e);
                errorMessageRef.set(ExceptionUtils.getMessage(e));
            }
        });
        connectionInitThread.setDaemon(true);
        connectionInitThread.start();

        guiFont64 = getGame().getAssMan().getFont(FontRegistry.FONT_64);
    }

    @Override
    public void handleInput(final float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            removeAllEntities();
            getGame().setScreen(new MainMenuScreen(getGame()));
            Optional.ofNullable(gameConnectionRef.get()).ifPresent(GameConnection::disconnect);
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
        Optional.ofNullable(errorMessageRef.get())
                .map(StringUtils::upperCase)
                .ifPresentOrElse(errorMessage -> {
                    stopLoading = true;
                    getGame().setScreen(new ErrorScreen(getGame(), errorMessage));
                }, () -> Optional.ofNullable(gameConnectionRef.get()).ifPresent(gameConnection
                        -> gameConnection.getResponse().poll().ifPresentOrElse(response -> {
                    if (response.hasErrorEvent()) {
                        errorMessageRef.set(response.getErrorEvent().getMessage());
                    } else if (response.hasGameEvents()) {
                        removeAllEntities();
                        stopBgMusic();
                        var mySpawnEvent = response.getGameEvents().getEvents(0);
                        LOG.info("My spawn {}", mySpawnEvent);
                        int playerId = mySpawnEvent.getPlayer().getPlayerId();
                        LOG.info("My player id {}", playerId);
                        getGame().setScreen(new PlayScreen(getGame(), gameConnection, PlayerContextData.builder()
                                .playerId(playerId)
                                .playersOnline(response.getGameEvents().getPlayersOnline())
                                .playerServerInfoContextData(playerServerInfoContextData)
                                .spawn(Converter.convertToVector2(mySpawnEvent.getPlayer().getPosition()))
                                .direction(Converter.convertToVector2(mySpawnEvent.getPlayer().getDirection()))
                                .leaderBoardItemList(mySpawnEvent.getLeaderBoard().getItemsList())
                                .build()));
                    } else {
                        errorMessageRef.set("Can't join to server");
                        gameConnection.disconnect();
                    }
                }, () -> gameConnection.getErrors().poll().ifPresent(throwable -> {
                    LOG.error("Error while loading", throwable);
                    gameConnection.disconnect();
                    errorMessageRef.set((ExceptionUtils.getMessage(throwable)));
                }))));

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
