package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.MapRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.handler.PlayScreenGameConnectionHandler;
import com.beverly.hills.money.gang.input.TextInputProcessor;
import com.beverly.hills.money.gang.log.ChatLog;
import com.beverly.hills.money.gang.log.PlayerKillLog;
import com.beverly.hills.money.gang.network.GameConnection;
import com.beverly.hills.money.gang.proto.PushChatEventCommand;
import com.beverly.hills.money.gang.proto.PushGameEventCommand;
import com.beverly.hills.money.gang.proto.ServerResponse;
import com.beverly.hills.money.gang.screens.data.PlayerLoadedData;
import com.beverly.hills.money.gang.screens.ui.PlayScreenUISelection;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

@Getter
public class PlayScreen extends GameScreen {

    private static final Logger LOG = LoggerFactory.getLogger(PlayScreen.class);

    private static final int MAX_CHAT_MSG_LEN = 32;
    private static final float BLOOD_OVERLAY_ALPHA_SWITCH = 0.5f;
    private final TextureRegion texRegBloodOverlay, texRegBlackOverlay;
    private final Environment env;
    private String enemyAimName;
    private final TextInputProcessor chatTextInputProcessor;
    private final BitmapFont guiFont64;
    private final BitmapFont guiFont32;
    private final GlyphLayout glyphLayoutOptionContinue;
    private final GlyphLayout glyphLayoutHeadsupDead;
    private final GlyphLayout glyphLayoutAim;
    private final GlyphLayout glyphLayoutOptionToMainMenu;
    private final Sound fightSound;
    private final Sound musicBackground;
    private final Sound dingSound1;
    private final Sound boomSound1;
    private final Sound boomSound2;

    private long nextTimeToFlushPlayerActions;
    private boolean chatMode;
    private boolean showGuiMenu;

    private final PlayScreenGameConnectionHandler playScreenGameConnectionHandler;

    @Setter
    private int playersOnline;

    private final ChatLog chatLog = new ChatLog();
    private final PlayerKillLog playerKillLog;

    private int menuSelectionCounter;
    private PlayScreenUISelection guiMenuSelection;

    private final GameConnection gameConnection;

    private final PlayerLoadedData playerLoadedData;

    public PlayScreen(final DaiKombatGame game,
                      final GameConnection gameConnection,
                      final PlayerLoadedData playerLoadedData) {
        super(game, new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        this.gameConnection = gameConnection;
        dingSound1 = getGame().getAssMan().getSound(SoundRegistry.DING_1);
        this.playerLoadedData = playerLoadedData;
        playerKillLog = new PlayerKillLog(playerLoadedData.getPlayerName());
        env = new Environment();
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, 1, 1, 1, 1f));
        env.set(new ColorAttribute(ColorAttribute.Fog, Constants.FOG_COLOR));

        chatTextInputProcessor = new TextInputProcessor(MAX_CHAT_MSG_LEN,
                () -> getGame().getAssMan().getSound(SoundRegistry.TYPING_SOUND_SEQ.getNextSound()).play(Constants.DEFAULT_SFX_TYPING_VOLUME));
        texRegBloodOverlay = getGame().getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 0, 0, 2, 2);
        texRegBlackOverlay = getGame().getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 3, 0, 2, 2);


        guiFont64 = getGame().getAssMan().getFont(FontRegistry.FONT_64);
        guiFont32 = getGame().getAssMan().getFont(FontRegistry.FONT_32);

        glyphLayoutOptionContinue = new GlyphLayout(guiFont64, Constants.OPTION_CONTINUE);
        glyphLayoutOptionToMainMenu = new GlyphLayout(guiFont64, Constants.OPTION_TO_MAIN_MENU);
        glyphLayoutHeadsupDead = new GlyphLayout(guiFont64, Constants.YOU_DIED);
        glyphLayoutAim = new GlyphLayout(guiFont64, "+");

        musicBackground = getGame().getAssMan().getSound(SoundRegistry.BATTLE_BG_SEQ.getNextSound());
        fightSound = getGame().getAssMan().getSound(SoundRegistry.FIGHT);
        boomSound1 = getGame().getAssMan().getSound(SoundRegistry.BOOM_1);
        boomSound2 = getGame().getAssMan().getSound(SoundRegistry.BOOM_2);
        fightSound.play(Constants.QUAKE_NARRATOR_FX_VOLUME);

        getGame().getMapBuilder().buildMap(getGame().getAssMan().getMap(MapRegistry.ONLINE_MAP));


        setPlayer(new Player(this,
                player -> {
                    var direction = getPlayer().getCurrent2DDirection();
                    var position = getPlayer().getCurrent2DPosition();
                    getPlayer().getEnemyRectInRangeFromCam(enemy -> {
                        enemy.getShot();
                        gameConnection.write(PushGameEventCommand.newBuilder()
                                .setGameId(Configs.GAME_ID)
                                .setPlayerId(playerLoadedData.getPlayerId())
                                .setDirection(PushGameEventCommand.Vector.newBuilder().setX(direction.x).setY(direction.y).build())
                                .setPosition(PushGameEventCommand.Vector.newBuilder().setX(position.x).setY(position.y).build())
                                .setAffectedPlayerId(enemy.getEnemyPlayerId())
                                .setEventType(PushGameEventCommand.GameEventType.SHOOT)
                                .build());
                    });

                    gameConnection.write(PushGameEventCommand.newBuilder()
                            .setGameId(Configs.GAME_ID)
                            .setPlayerId(playerLoadedData.getPlayerId())
                            .setDirection(PushGameEventCommand.Vector.newBuilder().setX(direction.x).setY(direction.y).build())
                            .setPosition(PushGameEventCommand.Vector.newBuilder().setX(position.x).setY(position.y).build())
                            .setEventType(PushGameEventCommand.GameEventType.SHOOT)
                            .build());
                },
                enemy -> enemyAimName = enemy.getName(),
                player -> {
                    if (System.currentTimeMillis() > nextTimeToFlushPlayerActions) {
                        if (gameConnection.isConnected()) {
                            sendCurrentPlayerPosition();
                        }
                    }

                },
                playerLoadedData.getSpawn(),
                playerLoadedData.getDirection()));
        getGame().getEntMan().addEntity(getPlayer());

        setCurrentCam(getPlayer().getPlayerCam());
        getViewport().setCamera(getCurrentCam());
        Gdx.input.setCursorCatched(true);
        musicBackground.loop(Constants.DEFAULT_MUSIC_VOLUME);

        playScreenGameConnectionHandler = new PlayScreenGameConnectionHandler(this);
    }

    @Override
    public void handleInput(final float delta) {
        if (getPlayer().isDead()) {
            showGuiMenu = true;
            chatMode = false;
            handleDeadGuiInput();
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.valueOf("`"))) {
                chatMode = !chatMode;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                if (chatMode) {
                    chatMode = false;
                } else {
                    Gdx.input.setCursorCatched(true);
                    showGuiMenu = !showGuiMenu;
                    menuSelectionCounter = 0;
                }
            } else if (chatMode) {
                handleChatInput();
            } else {
                if (showGuiMenu) {
                    handleAliveGuiInput();
                }
                getPlayer().handleInput(delta);
            }
        }
    }

    private void handleAliveGuiInput() {
        if (Configs.DEV_MODE) {
            Gdx.input.setCursorCatched(false);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            menuSelectionCounter++;
            dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
            guiMenuSelection = PlayScreenUISelection.getAliveSelection(menuSelectionCounter);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            menuSelectionCounter++;
            dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
            guiMenuSelection = PlayScreenUISelection.getAliveSelection(menuSelectionCounter);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            switch (guiMenuSelection) {
                case CONTINUE -> {
                    Gdx.input.setCursorCatched(true);
                    boomSound1.play(Constants.DEFAULT_SFX_VOLUME);
                    showGuiMenu = false;
                }
                case QUIT -> {
                    musicBackground.stop();
                    removeAllEntities();
                    getGame().setScreen(new MainMenuScreen(getGame()));
                    boomSound2.play(Constants.DEFAULT_SFX_VOLUME);
                }
            }
        }
    }

    private void handleDeadGuiInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            menuSelectionCounter++;
            dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
            guiMenuSelection = PlayScreenUISelection.getDeadSelection(menuSelectionCounter);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            menuSelectionCounter++;
            dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
            guiMenuSelection = PlayScreenUISelection.getDeadSelection(menuSelectionCounter);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            switch (guiMenuSelection) {
                case RESPAWN -> {
                    musicBackground.stop();
                    removeAllEntities();
                    getGame().setScreen(new LoadingScreen(getGame(),
                            playerLoadedData.getPlayerName(), playerLoadedData.getServerPassword()));
                    boomSound2.play(Constants.DEFAULT_SFX_VOLUME);
                }
                case QUIT -> {
                    musicBackground.stop();
                    removeAllEntities();
                    getGame().setScreen(new MainMenuScreen(getGame()));
                    boomSound2.play(Constants.DEFAULT_SFX_VOLUME);
                }
            }
        }
    }


    private void handleChatInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && StringUtils.isNotEmpty(chatTextInputProcessor.getText())) {
            chatMode = false;
            gameConnection.write(PushChatEventCommand.newBuilder()
                    .setMessage(chatTextInputProcessor.getText())
                    .setPlayerId(playerLoadedData.getPlayerId())
                    .setGameId(Configs.GAME_ID)
                    .build());
            chatLog.addMessage(playerLoadedData.getPlayerName(), chatTextInputProcessor.getText(),
                    () -> getGame().getAssMan().getSound(SoundRegistry.PING).play(Constants.DEFAULT_SFX_VOLUME));
            chatTextInputProcessor.clear();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            chatMode = false;
            chatTextInputProcessor.clear();
        } else {
            chatTextInputProcessor.handleInput();
        }
    }

    private void sendCurrentPlayerPosition() {
        var currentPosition = getPlayer().getCurrent2DPosition();
        var currentDirection = getPlayer().getCurrent2DDirection();
        gameConnection.write(PushGameEventCommand.newBuilder()
                .setPlayerId(playerLoadedData.getPlayerId())
                .setEventType(PushGameEventCommand.GameEventType.MOVE)
                .setGameId(Configs.GAME_ID)
                .setPosition(PushGameEventCommand.Vector.newBuilder()
                        .setX(currentPosition.x).setY(currentPosition.y).build())
                .setDirection(PushGameEventCommand.Vector.newBuilder()
                        .setX(currentDirection.x).setY(currentDirection.y).build())
                .build());
        nextTimeToFlushPlayerActions = System.currentTimeMillis() + Constants.FLUSH_ACTIONS_FREQ_MLS;
    }

    @Override
    public void render(final float delta) {
        super.render(delta);
        if (gameConnection.isDisconnected() && !isExiting() && !getPlayer().isDead() && gameConnection.getResponse().size() == 0) {
            musicBackground.stop();
            removeAllEntities();
            String errorMessage = gameConnection.getResponse().list().stream()
                    .filter(ServerResponse::hasErrorEvent)
                    .map(ServerResponse::getErrorEvent)
                    .map(ServerResponse.ErrorEvent::getMessage).findFirst()
                    .or((Supplier<Optional<String>>) () -> gameConnection.getErrors().poll().stream()
                            .findFirst().map(ExceptionUtils::getMessage)).orElse("Connection is lost");
            getGame().setScreen(new ErrorScreen(getGame(), errorMessage));
            boomSound2.play(Constants.DEFAULT_SFX_VOLUME);
            return;

        }
        playScreenGameConnectionHandler.handle();

        getCurrentCam().update();

        getGame().getFbo().begin();
        Gdx.gl.glClearColor(Constants.FOG_COLOR.r, Constants.FOG_COLOR.g, Constants.FOG_COLOR.b, Constants.FOG_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


        getGame().getMdlBatch().begin(getCurrentCam());
        getGame().getEntMan().render3DAllEntities(getGame().getMdlBatch(), env, delta);
        getGame().getMdlBatch().end();
        getGame().getFbo().end();

        getGame().getBatch().begin();

        renderBloodOverlay01();

        getGame().getBatch().draw(getGame().getFbo().getColorBufferTexture(), 0, 0, getViewport().getWorldWidth(),
                getViewport().getWorldHeight());

        float gunWidth = getViewport().getWorldWidth() * 0.35f;
        float gunHeight = getViewport().getWorldHeight() * 0.40f;
        getGame().getBatch().draw(getPlayer().getGuiCurrentGun(), getViewport().getWorldWidth() * 0.5f, (int) getPlayer().getGunY(),
                gunWidth, gunHeight);
        renderBloodOverlay02();

        if (Configs.DEV_MODE) {
            String recvSentMessages = String.format(Locale.ENGLISH, "NETWORK: RECV %s MSG | SENT %s MSG | INBOUND %s | OUTBOUND %s",
                            gameConnection.getNetworkStats().getReceivedMessages(), gameConnection.getNetworkStats().getSentMessages(),
                            FileUtils.byteCountToDisplaySize(gameConnection.getNetworkStats().getInboundPayloadBytes()),
                            FileUtils.byteCountToDisplaySize(gameConnection.getNetworkStats().getOutboundPayloadBytes()))
                    .toUpperCase();
            var glyphLayoutRecSentMessages = new GlyphLayout(guiFont32, recvSentMessages);
            guiFont32.draw(getGame().getBatch(),
                    recvSentMessages, getViewport().getWorldWidth() / 2f - glyphLayoutRecSentMessages.width / 2f,
                    getViewport().getWorldHeight() - Constants.MENU_OPTION_INDENT);
        }

        if (playersOnline > 0) {
            String playersOnlineText = playersOnline + " ONLINE";
            var onlineGlyph = new GlyphLayout(guiFont32, playersOnlineText);
            guiFont32.draw(getGame().getBatch(), playersOnlineText,
                    getViewport().getWorldWidth() - 32 - onlineGlyph.width,
                    getViewport().getWorldHeight() - 32 - onlineGlyph.height);
        }
        if (!getPlayer().isDead()) {
            if (playerKillLog.hasKillerMessage()) {
                String killerMessage = playerKillLog.getKillerMessage();
                guiFont64.draw(getGame().getBatch(), killerMessage, 32, 128 - 32);
            }
            String killsCount = playerKillLog.getKillCount();

            guiFont64.draw(getGame().getBatch(), killsCount, getViewport().getWorldWidth() - 32 - new GlyphLayout(guiFont64, killsCount).width, 128 - 32);

        }
        if (chatMode) {
            guiFont64.draw(getGame().getBatch(), ">" + chatTextInputProcessor.getText(), 32, 128 - 32 + 64);
        }
        if (chatLog.hasChatMessage()) {
            String chatMessages = chatLog.getChatMessages();
            GlyphLayout glyphLayoutChatLog = new GlyphLayout(guiFont64, chatMessages);
            guiFont64.draw(getGame().getBatch(), chatMessages, 32, 128 - 32 + 64 + glyphLayoutChatLog.height);
        }

        if (enemyAimName != null) {
            GlyphLayout enemyNameGlyph = new GlyphLayout(guiFont32, enemyAimName);
            guiFont32.setColor(1, 1, 1, 0.8f);
            guiFont32.draw(getGame().getBatch(), enemyAimName, getViewport().getWorldWidth() / 2f - enemyNameGlyph.width / 2f,
                    getViewport().getWorldHeight() / 2f - (enemyNameGlyph.height / 2f) + 32);
            enemyAimName = null;
            guiFont32.setColor(1, 1, 1, 1);
        }


        getGame().getBatch().setColor(1, 1, 1, 1); // Never cover HUD in blood.
        guiFont64.draw(getGame().getBatch(), "+" + getPlayer().getCurrentHP(), 32, 42);

        if (!getPlayer().isDead()) {
            guiFont64.draw(getGame().getBatch(), "+", getViewport().getWorldWidth() / 2f - glyphLayoutAim.width / 2f,
                    getViewport().getWorldHeight() / 2f - glyphLayoutAim.height / 2f);
        }

        // gui menu
        if (showGuiMenu) {
            if (getPlayer().isDead()) {
                getGame().setGameIsPaused(true);
                getGame().getBatch().setColor(1, 0, 0, 0.6f);
                getGame().getBatch().draw(texRegBlackOverlay, 0, 0, getViewport().getWorldWidth(), getViewport().getWorldHeight());
                renderDeadGui();
            } else {
                renderAliveGui();
            }
        }
        getGame().getBatch().end();

    }

    private void renderDeadGui() {
        float halfViewportWidth = getViewport().getWorldWidth() / 2f;
        float halfViewportHeight = getViewport().getWorldHeight() / 2f;
        guiFont64.draw(getGame().getBatch(), Constants.YOU_DIED, halfViewportWidth - glyphLayoutHeadsupDead.width / 2f,
                halfViewportHeight - glyphLayoutHeadsupDead.height / 2f);
        String killedBy = "KILLED BY " + getPlayer().getKilledBy().toUpperCase();
        GlyphLayout glyphLayoutKilledBy = new GlyphLayout(guiFont64, killedBy);
        final float killedByX = halfViewportWidth - glyphLayoutKilledBy.width / 2f;
        guiFont64.draw(getGame().getBatch(), killedBy, killedByX, halfViewportHeight - glyphLayoutKilledBy.height / 2f - 64);

        final float optionRespawnX = halfViewportWidth - glyphLayoutOptionContinue.width / 2f;
        final float optionRespawnY = halfViewportHeight - glyphLayoutOptionContinue.height / 2f - 128;
        guiFont64.draw(getGame().getBatch(), Constants.OPTION_RESPAWN, optionRespawnX, optionRespawnY);
        final float optionMainMenuX = halfViewportWidth - glyphLayoutOptionToMainMenu.width / 2f;
        final float optionMainMenuY = halfViewportHeight - glyphLayoutOptionToMainMenu.height / 2f - 128 - 64;
        guiFont64.draw(getGame().getBatch(), Constants.OPTION_TO_MAIN_MENU, optionMainMenuX, optionMainMenuY);

        if (!PlayScreenUISelection.isDeadSelection(guiMenuSelection)) {
            // if anything other than DEAD_SELECTION
            guiMenuSelection = PlayScreenUISelection.RESPAWN;
        }
        if (guiMenuSelection == PlayScreenUISelection.QUIT) {
            guiFont64.draw(getGame().getBatch(), Constants.SELECTED_OPTION_MARK, optionMainMenuX - 32, optionMainMenuY);
        } else if (guiMenuSelection == PlayScreenUISelection.RESPAWN) {
            guiFont64.draw(getGame().getBatch(), Constants.SELECTED_OPTION_MARK, optionRespawnX - 32, optionRespawnY);
        }

    }

    private void renderAliveGui() {
        float halfViewportHeight = getViewport().getWorldHeight() / 2f;
        float halfViewportWidth = getViewport().getWorldWidth() / 2f;
        final float optionContinueX = halfViewportWidth - glyphLayoutOptionContinue.width / 2f;
        final float optionContinueY = halfViewportHeight - glyphLayoutOptionContinue.height / 2f - 128;
        guiFont64.draw(getGame().getBatch(), Constants.OPTION_CONTINUE, optionContinueX, optionContinueY);
        final float optionMainMenuX = halfViewportWidth - glyphLayoutOptionToMainMenu.width / 2f;
        final float optionMainMenuY = halfViewportHeight - glyphLayoutOptionToMainMenu.height / 2f - 128 - 64;
        guiFont64.draw(getGame().getBatch(), Constants.OPTION_TO_MAIN_MENU, optionMainMenuX, optionMainMenuY);
        if (!PlayScreenUISelection.isAliveSelection(guiMenuSelection)) {
            guiMenuSelection = PlayScreenUISelection.CONTINUE;
        }
        if (guiMenuSelection == PlayScreenUISelection.QUIT) {
            guiFont64.draw(getGame().getBatch(), Constants.SELECTED_OPTION_MARK, optionMainMenuX - 32, optionMainMenuY);
        } else if (guiMenuSelection == PlayScreenUISelection.CONTINUE) {
            guiFont64.draw(getGame().getBatch(), Constants.SELECTED_OPTION_MARK, optionContinueX - 32, optionContinueY);
        }
    }

    private void renderBloodOverlay01() {
        if (getPlayer().bloodOverlayAlpha >= BLOOD_OVERLAY_ALPHA_SWITCH) {
            getGame().getBatch().setColor(1, 0, 0, getPlayer().bloodOverlayAlpha);
            getGame().getBatch().draw(texRegBlackOverlay, 0, 0, getViewport().getWorldWidth(), getViewport().getWorldHeight());
        }
    }

    private void renderBloodOverlay02() {
        if (getPlayer().renderBloodOverlay && getPlayer().bloodOverlayAlpha < BLOOD_OVERLAY_ALPHA_SWITCH) {
            getGame().getBatch().setColor(1, 1, 1, getPlayer().bloodOverlayAlpha);
            getGame().getBatch().draw(texRegBloodOverlay, 0, 0, getViewport().getWorldWidth(), getViewport().getWorldHeight());
        }
    }

    @Override
    public void resize(final int width, final int height) {
        super.resize(width, height);
    }

    @Override
    public void tick(final float delta) {
        super.tick(delta);
    }


    @Override
    public void onExitScreen() {
        gameConnection.disconnect();
    }

}
