package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.MapRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.entities.item.QuadDamagePowerUp;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.entities.ui.UILeaderBoard;
import com.beverly.hills.money.gang.handler.PlayScreenGameConnectionHandler;
import com.beverly.hills.money.gang.input.TextInputProcessor;
import com.beverly.hills.money.gang.log.ChatLog;
import com.beverly.hills.money.gang.log.MyPlayerKillLog;
import com.beverly.hills.money.gang.network.GameConnection;
import com.beverly.hills.money.gang.proto.PushChatEventCommand;
import com.beverly.hills.money.gang.proto.PushGameEventCommand;
import com.beverly.hills.money.gang.proto.PushGameEventCommand.GameEventType;
import com.beverly.hills.money.gang.screens.data.PlayerConnectionContextData;
import com.beverly.hills.money.gang.screens.ui.selection.ActivePlayUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.DeadPlayUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.UISelection;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class PlayScreen extends GameScreen {

  private static final Logger LOG = LoggerFactory.getLogger(PlayScreen.class);
  private static final int DEAD_SCREEN_INPUT_DELAY_MLS = 1_000;

  private GameScreen screenToTransition;
  private static final int MAX_CHAT_MSG_LEN = 32;
  private static final float BLOOD_OVERLAY_ALPHA_SWITCH = 0.5f;
  private final TextureRegion texRegBloodOverlay, texRegBlackOverlay;
  private final Environment env;
  private String enemyAimName;
  private final TextInputProcessor chatTextInputProcessor;
  private final BitmapFont guiFont64;
  private final BitmapFont guiFont32;
  private final GlyphLayout glyphLayoutHeadsupDead;
  private final GlyphLayout glyphLayoutAim;
  private final UserSettingSound fightSound;
  private final UserSettingSound musicBackground;
  private final UserSettingSound dingSound1;
  private final UserSettingSound boomSound1;
  private final UserSettingSound boomSound2;
  private final UserSettingSound youLead;
  private final UserSettingSound lostLead;
  private final UserSettingSound oneFragLeftSound;
  private final UserSettingSound twoFragsLeftSound;
  private final UserSettingSound threeFragsLeftSound;

  @Getter
  @Setter
  private boolean gameOver;

  private final UISelection<ActivePlayUISelection> activePlayUISelectionUISelection
      = new UISelection<>(ActivePlayUISelection.values());

  private final UISelection<DeadPlayUISelection> deadPlayUISelectionUISelection
      = new UISelection<>(DeadPlayUISelection.values());

  @Setter
  private int playersOnline;

  @Setter
  private String errorMessage;

  @Getter
  private final UILeaderBoard uiLeaderBoard;
  private boolean showLeaderBoard;

  private long nextTimeToFlushPlayerActions;
  private boolean chatMode;
  private boolean showGuiMenu;

  private final PlayScreenGameConnectionHandler playScreenGameConnectionHandler;

  private final ChatLog chatLog;
  private final MyPlayerKillLog myPlayerKillLog;
  private final GameConnection gameConnection;
  private final PlayerConnectionContextData playerConnectionContextData;

  private QuadDamagePowerUp quadDamagePowerUp;

  public PlayScreen(final DaiKombatGame game,
      final GameConnection gameConnection,
      final PlayerConnectionContextData playerConnectionContextData) {
    super(game, new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
    this.gameConnection = gameConnection;
    dingSound1 = getGame().getAssMan().getUserSettingSound(SoundRegistry.DING_1);
    this.playerConnectionContextData = playerConnectionContextData;
    this.playersOnline = playerConnectionContextData.getPlayersOnline();
    myPlayerKillLog = new MyPlayerKillLog();
    env = new Environment();
    env.set(new ColorAttribute(ColorAttribute.AmbientLight, 1, 1, 1, 1f));
    env.set(new ColorAttribute(ColorAttribute.Fog, Constants.FOG_COLOR));

    chatTextInputProcessor = new TextInputProcessor(MAX_CHAT_MSG_LEN,
        () -> getGame().getAssMan()
            .getUserSettingSound(SoundRegistry.TYPING_SOUND_SEQ.getNextSound())
            .play(Constants.DEFAULT_SFX_TYPING_VOLUME));
    texRegBloodOverlay = getGame().getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 0, 0, 2, 2);
    texRegBlackOverlay = getGame().getAssMan().getTextureRegion(TexturesRegistry.ATLAS, 3, 0, 2, 2);

    guiFont64 = getGame().getAssMan().getFont(FontRegistry.FONT_64);
    guiFont32 = getGame().getAssMan().getFont(FontRegistry.FONT_32);

    glyphLayoutHeadsupDead = new GlyphLayout(guiFont64, Constants.YOU_DIED);
    glyphLayoutAim = new GlyphLayout(guiFont64, "+");

    musicBackground = getGame().getAssMan()
        .getUserSettingSound(SoundRegistry.BATTLE_BG_SEQ.getNextSound());
    fightSound = getGame().getAssMan().getUserSettingSound(SoundRegistry.FIGHT);
    boomSound1 = getGame().getAssMan().getUserSettingSound(SoundRegistry.BOOM_1);
    boomSound2 = getGame().getAssMan().getUserSettingSound(SoundRegistry.BOOM_2);
    youLead = getGame().getAssMan().getUserSettingSound(SoundRegistry.YOU_LEAD);
    lostLead = getGame().getAssMan().getUserSettingSound(SoundRegistry.LOST_LEAD);
    fightSound.play(Constants.QUAKE_NARRATOR_FX_VOLUME);
    oneFragLeftSound = getGame().getAssMan().getUserSettingSound(SoundRegistry.ONE_FRAG_LEFT);
    twoFragsLeftSound = getGame().getAssMan().getUserSettingSound(SoundRegistry.TWO_FRAGS_LEFT);
    threeFragsLeftSound= getGame().getAssMan().getUserSettingSound(SoundRegistry.THREE_FRAGS_LEFT);

    getGame().getMapBuilder().buildMap(getGame().getAssMan().getMap(MapRegistry.ONLINE_MAP));

    chatLog = new ChatLog(() -> getGame().getAssMan().getUserSettingSound(SoundRegistry.PING)
        .play(Constants.DEFAULT_SFX_VOLUME));

    setPlayer(new Player(this,
        playerWeapon -> {
          PushGameEventCommand.GameEventType eventType;
          switch (playerWeapon.getWeapon()) {
            case GAUNTLET -> eventType = PushGameEventCommand.GameEventType.PUNCH;
            case SHOTGUN -> eventType = PushGameEventCommand.GameEventType.SHOOT;
            default -> throw new IllegalArgumentException(
                "Not supported weapon " + playerWeapon.getWeapon());
          }
          var direction = getPlayer().getCurrent2DDirection();
          var position = getPlayer().getCurrent2DPosition();
          boolean hitEnemy = getPlayer().getEnemyRectInRangeFromCam(enemy -> {

            enemy.getHit();
            playerWeapon.getPlayer().playWeaponHitSound(playerWeapon.getWeapon());
            gameConnection.write(PushGameEventCommand.newBuilder()
                .setGameId(Configs.GAME_ID)
                .setPlayerId(playerConnectionContextData.getPlayerId())
                .setDirection(
                    PushGameEventCommand.Vector.newBuilder().setX(direction.x).setY(direction.y)
                        .build())
                .setPosition(
                    PushGameEventCommand.Vector.newBuilder().setX(position.x).setY(position.y)
                        .build())
                .setAffectedPlayerId(enemy.getEnemyPlayerId())
                .setEventType(eventType)
                .build());
          }, playerWeapon.getPlayer().getWeaponDistance(playerWeapon.getWeapon()));
          if (!hitEnemy) {
            // if we haven't hit anybody
            gameConnection.write(PushGameEventCommand.newBuilder()
                .setGameId(Configs.GAME_ID)
                .setPlayerId(playerConnectionContextData.getPlayerId())
                .setDirection(
                    PushGameEventCommand.Vector.newBuilder().setX(direction.x).setY(direction.y)
                        .build())
                .setPosition(
                    PushGameEventCommand.Vector.newBuilder().setX(position.x).setY(position.y)
                        .build())
                .setEventType(eventType)
                .build());
          }
        },
        enemy -> enemyAimName = enemy.getName(),
        player -> {
          if (System.currentTimeMillis() < nextTimeToFlushPlayerActions
              || gameConnection.isDisconnected()) {
            return;
          }
          sendCurrentPlayerPosition();
          nextTimeToFlushPlayerActions =
              System.currentTimeMillis() + playerConnectionContextData.getMovesUpdateFreqMls();

        },
        playerConnectionContextData.getSpawn(),
        playerConnectionContextData.getDirection()));
    getGame().getEntMan().addEntity(getPlayer());

    setCurrentCam(getPlayer().getPlayerCam());
    getViewport().setCamera(getCurrentCam());
    Gdx.input.setCursorCatched(true);
    musicBackground.loop(Constants.DEFAULT_MUSIC_VOLUME);
    uiLeaderBoard = new UILeaderBoard(
        playerConnectionContextData.getPlayerId(),
        playerConnectionContextData.getLeaderBoardItemList().stream()
            .map(leaderBoardItem -> UILeaderBoard.LeaderBoardPlayer.builder()
                .name(leaderBoardItem.getPlayerName())
                .id(leaderBoardItem.getPlayerId())
                .deaths(leaderBoardItem.getDeaths())
                .kills(leaderBoardItem.getKills())
                .build())
            .collect(Collectors.toList()),
        playerConnectionContextData.getFragsToWin(),
        () -> {
          if (!gameOver) {
            youLead.play(Constants.QUAKE_NARRATOR_FX_VOLUME);
          }
        },
        () -> {
          if (!gameOver) {
            lostLead.play(Constants.QUAKE_NARRATOR_FX_VOLUME);
          }
        }, fragsLeft -> {
          switch (fragsLeft) {
            case 3 -> threeFragsLeftSound.play(Constants.QUAKE_NARRATOR_FX_VOLUME);
            case 2 -> twoFragsLeftSound.play(Constants.QUAKE_NARRATOR_FX_VOLUME);
            case 1 -> oneFragLeftSound.play(Constants.QUAKE_NARRATOR_FX_VOLUME);
          }
        }

    );
    playScreenGameConnectionHandler = new PlayScreenGameConnectionHandler(this);
    if (Configs.DEV_MODE && Configs.MIMIC_CONSTANT_NETWORK_ACTIVITY) {
      mimicNetworkActivity();
    }
  }

  public void spawnQuadDamage(Vector2 position) {
    if (quadDamagePowerUp != null) {
      return;
    }
    quadDamagePowerUp = new QuadDamagePowerUp(new Vector3(position.x, 0.0f, position.y), this,
        getPlayer(),
        () -> {
          var currentPosition = getPlayer().getCurrent2DPosition();
          var currentDirection = getPlayer().getCurrent2DDirection();
          gameConnection.write(PushGameEventCommand.newBuilder()
              .setPlayerId(playerConnectionContextData.getPlayerId())
              .setEventType(GameEventType.QUAD_DAMAGE_POWER_UP)
              .setGameId(Configs.GAME_ID)
              .setPosition(PushGameEventCommand.Vector.newBuilder()
                  .setX(currentPosition.x).setY(currentPosition.y).build())
              .setDirection(PushGameEventCommand.Vector.newBuilder()
                  .setX(currentDirection.x).setY(currentDirection.y).build())
              .build());
        });
    getGame().getEntMan().addEntity(quadDamagePowerUp);
  }

  public void removeQuadDamageOrb() {
    if (quadDamagePowerUp == null) {
      return;
    }
    quadDamagePowerUp.destroy();
    quadDamagePowerUp = null;
  }

  private void mimicNetworkActivity() {
    new Thread(() -> {
      while (!isExiting() && !getPlayer().isDead()) {
        try {
          Thread.sleep(5);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        sendCurrentPlayerPosition();
      }
    }).start();

  }

  @Override
  public void handleInput(final float delta) {
    showLeaderBoard = false;
    if (Gdx.input.isKeyJustPressed(Input.Keys.valueOf("`"))) {
      chatMode = !chatMode;
    }
    if (chatMode) {
      handleChatInput();
    } else if (getPlayer().isDead()) {
      handleDeadGuiInput();
    } else {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        if (chatMode) {
          chatMode = false;
        } else {
          Gdx.input.setCursorCatched(true);
          showGuiMenu = !showGuiMenu;
        }
      } else {
        if (showGuiMenu) {
          handleAliveGuiInput();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.TAB)) {
          showLeaderBoard = true;
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
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
      activePlayUISelectionUISelection.up();
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
      activePlayUISelectionUISelection.down();
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(
        Input.Keys.SPACE)) {
      switch (activePlayUISelectionUISelection.getSelectedOption()) {
        case CONTINUE -> {
          Gdx.input.setCursorCatched(true);
          boomSound1.play(Constants.DEFAULT_SFX_VOLUME);
          showGuiMenu = false;
        }
        case QUIT -> screenToTransition = new MainMenuScreen(getGame());
      }
    }
  }

  private void handleDeadGuiInput() {
    if (System.currentTimeMillis() < getPlayer().getDeathTimeMls() + DEAD_SCREEN_INPUT_DELAY_MLS) {
      // we put a small delay. otherwise, players respawn too quickly
      return;
    }
    showLeaderBoard = false;
    if (Gdx.input.isKeyPressed(Input.Keys.TAB)) {
      showLeaderBoard = true;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
      deadPlayUISelectionUISelection.up();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      deadPlayUISelectionUISelection.down();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
        || Gdx.input.isKeyJustPressed(Input.Keys.ALT_RIGHT)
        || Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_RIGHT)
        || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
        || Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
      switch (deadPlayUISelectionUISelection.getSelectedOption()) {
        case RESPAWN ->
            screenToTransition = new RespawnScreen(getGame(), playerConnectionContextData,
                gameConnection);
        case QUIT -> screenToTransition = new MainMenuScreen(getGame());
      }
    }
  }


  private void handleChatInput() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && StringUtils.isNotBlank(
        chatTextInputProcessor.getText())) {
      chatMode = false;
      gameConnection.write(PushChatEventCommand.newBuilder()
          .setMessage(chatTextInputProcessor.getText())
          .setPlayerId(playerConnectionContextData.getPlayerId())
          .setGameId(Configs.GAME_ID)
          .build());
      chatLog.addMessage(
          playerConnectionContextData.getJoinGameData().getPlayerName(),
          chatTextInputProcessor.getText());
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
        .setPlayerId(playerConnectionContextData.getPlayerId())
        .setEventType(PushGameEventCommand.GameEventType.MOVE)
        .setGameId(Configs.GAME_ID)
        .setPosition(PushGameEventCommand.Vector.newBuilder()
            .setX(currentPosition.x).setY(currentPosition.y).build())
        .setDirection(PushGameEventCommand.Vector.newBuilder()
            .setX(currentDirection.x).setY(currentDirection.y).build())
        .build());
  }

  @Override
  public void render(final float delta) {
    super.render(delta);

    getCurrentCam().update();

    getGame().getFbo().begin();
    Gdx.gl.glClearColor(Constants.FOG_COLOR.r, Constants.FOG_COLOR.g, Constants.FOG_COLOR.b,
        Constants.FOG_COLOR.a);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

    getGame().getMdlBatch().begin(getCurrentCam());
    getGame().getEntMan().render3DAllEntities(getGame().getMdlBatch(), env, delta);
    getGame().getMdlBatch().end();
    getGame().getFbo().end();

    getGame().getBatch().begin();

    renderBloodOverlay01();

    if (getPlayer().isDead()) {
      getGame().getBatch().setColor(1, 0, 0, 1f);
    }

    getGame().getBatch()
        .draw(getGame().getFbo().getColorBufferTexture(), 0, 0, getViewport().getWorldWidth(),
            getViewport().getWorldHeight());

    getGame().getBatch().end();
    getGame().getBatch().begin();
    if (getPlayer().isQuadDamageEffectActive() && !getPlayer().isDead()) {
      getGame().getBatch().setColor(Color.SKY.cpy()
          .lerp(Color.WHITE, (float) Math.sin(getGame().getTimeSinceLaunch() * 20)));
    }
    var activeWeapon = getPlayer().getActiveWeaponRenderingData();
    float gunWidth = getViewport().getWorldWidth() * activeWeapon.getScreenRatioX();
    float gunHeight = getViewport().getWorldHeight() * activeWeapon.getScreenRatioY();
    getGame().getBatch().draw(activeWeapon.getTextureRegion(),
        getViewport().getWorldWidth() * 0.5f + activeWeapon.getPositioning().x,
        (int) getPlayer().getWeaponY() + activeWeapon.getPositioning().y,
        gunWidth, gunHeight);

    getGame().getBatch().end();
    getGame().getBatch().begin();
    renderBloodOverlay02();
    renderGameTechStats();
    if (!getPlayer().isDead()) {
      if (myPlayerKillLog.hasKillerMessage()) {
        String killerMessage = myPlayerKillLog.getKillerMessage();
        GlyphLayout glyphLayoutKillerMessage = new GlyphLayout(guiFont32, killerMessage);
        guiFont32.draw(getGame().getBatch(), killerMessage,
            getViewport().getWorldWidth() / 2f - glyphLayoutKillerMessage.width / 2f,
            getViewport().getWorldHeight() / 2f - glyphLayoutKillerMessage.height / 2f + 128);
      }
      String killStats = uiLeaderBoard.getMyStatsMessage(
          playerConnectionContextData.getFragsToWin());
      guiFont64.draw(getGame().getBatch(), killStats,
          getViewport().getWorldWidth() - 32 - new GlyphLayout(guiFont64, killStats).width,
          128 - 32);

    }
    if (chatMode) {
      guiFont64.draw(getGame().getBatch(), ">" + chatTextInputProcessor.getText(), 32,
          128 - 32 + 64);
    }
    if (chatLog.hasChatMessage()) {
      String chatMessages = chatLog.getChatMessages();
      GlyphLayout glyphLayoutChatLog = new GlyphLayout(guiFont64, chatMessages);
      guiFont64.draw(getGame().getBatch(), chatMessages, 32,
          128 - 32 + 64 + glyphLayoutChatLog.height);
    }

    if (enemyAimName != null) {
      GlyphLayout enemyNameGlyph = new GlyphLayout(guiFont32, enemyAimName);
      guiFont32.setColor(1, 1, 1, 0.8f);
      guiFont32.draw(getGame().getBatch(), enemyAimName,
          getViewport().getWorldWidth() / 2f - enemyNameGlyph.width / 2f,
          getViewport().getWorldHeight() / 2f - (enemyNameGlyph.height / 2f) + 32);
      enemyAimName = null;
      guiFont32.setColor(1, 1, 1, 1);
    }

    getGame().getBatch().setColor(1, 1, 1, 1); // Never cover HUD in blood.
    if (!getPlayer().isDead()) {
      guiFont64.draw(getGame().getBatch(), getPlayer().getCurrentHP() + " HP", 32, 128 - 32);
      guiFont64.draw(getGame().getBatch(), "+",
          getViewport().getWorldWidth() / 2f - glyphLayoutAim.width / 2f,
          getViewport().getWorldHeight() / 2f - glyphLayoutAim.height / 2f);
    }

    if (showLeaderBoard) {
      String leaderBoard = getUiLeaderBoard().toString();
      var glyphLayoutRecSentMessages = new GlyphLayout(guiFont64, leaderBoard);
      guiFont64.draw(getGame().getBatch(),
          leaderBoard, getViewport().getWorldWidth() / 2f - glyphLayoutRecSentMessages.width / 2f,
          getViewport().getWorldHeight() - Constants.MENU_OPTION_INDENT * 2);
    } else {
      // gui menu
      if (showGuiMenu) {
        if (getPlayer().isDead()) {
          renderDeadGui();
        } else {
          renderAliveGui();
        }
      }
    }
    if (getPlayer().isDead()) {
      showGuiMenu = true;
    }
    if (gameOver) {
      screenToTransition = new GameOverScreen(getGame(), uiLeaderBoard,
          playerConnectionContextData.getJoinGameData());
    } else if (gameConnection.isDisconnected()) {
      while (gameConnection.getErrors().size() != 0) {
        gameConnection.getErrors().poll()
            .ifPresent(playScreenGameConnectionHandler::handleException);
      }
      screenToTransition = new ErrorScreen(getGame(),
          StringUtils.defaultIfBlank(errorMessage, "Connection lost"));
    } else {
      try {
        playScreenGameConnectionHandler.handle();
      } catch (Exception e) {
        LOG.error("Can't handle screen actions", e);
        screenToTransition = new ErrorScreen(getGame(),
            StringUtils.defaultIfEmpty(e.getMessage(), "Can't handle connection")
                + ". Check internet signal. Last ping " + gameConnection.getNetworkStats()
                .getPingMls() + " mls.");
      }
    }

    getGame().getBatch().end();
    if (screenToTransition != null) {
      if (!(screenToTransition instanceof RespawnScreen)) {
        gameConnection.disconnect();
      }
      musicBackground.stop();
      removeAllEntities();
      getGame().setScreen(screenToTransition);
      boomSound2.play(Constants.DEFAULT_SFX_VOLUME);
    }
  }

  private void renderGameTechStats() {
    StringBuilder gameTechStats = new StringBuilder();
    gameTechStats.append(playersOnline).append(" ONLINE ");
    gameTechStats.append("| PING ")
        .append(Optional.of(gameConnection.getNetworkStats().getPingMls())
            .filter(ping -> ping >= 0)
            .map(String::valueOf).orElse("-"))
        .append(" MLS | ");
    gameTechStats.append(Gdx.graphics.getFramesPerSecond()).append(" FPS");

    var gameTechStatsGlyph = new GlyphLayout(guiFont64, gameTechStats);
    guiFont64.draw(getGame().getBatch(), gameTechStats,
        getViewport().getWorldWidth() - 32 - gameTechStatsGlyph.width,
        getViewport().getWorldHeight() - 32 - gameTechStatsGlyph.height);

    if (Configs.DEV_MODE) {
      renderDevModeGameTechStats();
    }
  }

  private void renderDevModeGameTechStats() {
    String networkStats = String.format(Locale.ENGLISH,
            "NETWORK: RECV %s MSG | SENT %s MSG | INBOUND %s | OUTBOUND %s",
            gameConnection.getNetworkStats().getReceivedMessages(),
            gameConnection.getNetworkStats().getSentMessages(),
            FileUtils.byteCountToDisplaySize(gameConnection.getNetworkStats().getInboundPayloadBytes()),
            FileUtils.byteCountToDisplaySize(
                gameConnection.getNetworkStats().getOutboundPayloadBytes()))
        .toUpperCase();
    var glyphNetStatsMessages = new GlyphLayout(guiFont32, networkStats);
    guiFont32.draw(getGame().getBatch(),
        networkStats, getViewport().getWorldWidth() / 2f - glyphNetStatsMessages.width / 2f,
        getViewport().getWorldHeight() - 32);
  }


  private void renderDeadGui() {
    float halfViewportWidth = getViewport().getWorldWidth() / 2f;
    float halfViewportHeight = getViewport().getWorldHeight() / 2f;
    guiFont64.draw(getGame().getBatch(), Constants.YOU_DIED,
        halfViewportWidth - glyphLayoutHeadsupDead.width / 2f,
        halfViewportHeight - glyphLayoutHeadsupDead.height / 2f);
    String killedBy = "KILLED BY " + getPlayer().getKilledBy().toUpperCase();
    GlyphLayout glyphLayoutKilledBy = new GlyphLayout(guiFont64, killedBy);
    final float killedByX = halfViewportWidth - glyphLayoutKilledBy.width / 2f;
    final float killedByY = halfViewportHeight - glyphLayoutKilledBy.height / 2f - 64;
    guiFont64.draw(getGame().getBatch(), killedBy, killedByX, killedByY);
    deadPlayUISelectionUISelection.render(guiFont64, this, 128);
    String pressTabToSeeLeaderboard = "PRESS TAB TO SEE LEADERBOARD";
    GlyphLayout glyphLayoutLeaderBoardHint = new GlyphLayout(guiFont32, pressTabToSeeLeaderboard);
    guiFont32.draw(getGame().getBatch(), pressTabToSeeLeaderboard,
        halfViewportWidth - glyphLayoutLeaderBoardHint.width / 2f,
        halfViewportHeight - glyphLayoutLeaderBoardHint.height / 2f + 128);
  }

  private void renderAliveGui() {
    activePlayUISelectionUISelection.render(guiFont64, this, 64);
  }

  private void renderBloodOverlay01() {
    if (getPlayer().bloodOverlayAlpha >= BLOOD_OVERLAY_ALPHA_SWITCH) {
      getGame().getBatch().setColor(1, 0, 0, getPlayer().bloodOverlayAlpha);
      getGame().getBatch().draw(texRegBlackOverlay, 0, 0, getViewport().getWorldWidth(),
          getViewport().getWorldHeight());
    }
  }

  private void renderBloodOverlay02() {
    if (getPlayer().renderBloodOverlay
        && getPlayer().bloodOverlayAlpha < BLOOD_OVERLAY_ALPHA_SWITCH) {
      getGame().getBatch().setColor(1, 1, 1, getPlayer().bloodOverlayAlpha);
      getGame().getBatch().draw(texRegBloodOverlay, 0, 0, getViewport().getWorldWidth(),
          getViewport().getWorldHeight());
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
  public void dispose() {
    LOG.info("DISPOSE!");
    gameConnection.disconnect();
  }

}
