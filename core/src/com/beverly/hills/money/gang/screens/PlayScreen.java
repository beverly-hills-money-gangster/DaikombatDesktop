package com.beverly.hills.money.gang.screens;

import static com.beverly.hills.money.gang.screens.ui.taunt.GameTaunt.TAUNTS_SEQ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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
import com.beverly.hills.money.gang.assets.managers.sound.SoundQueue;
import com.beverly.hills.money.gang.assets.managers.sound.SoundVolumeType;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound.SoundConf;
import com.beverly.hills.money.gang.entities.item.PowerUp;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.entities.player.PlayerFactory;
import com.beverly.hills.money.gang.entities.teleport.Teleport;
import com.beverly.hills.money.gang.entities.ui.UILeaderBoard;
import com.beverly.hills.money.gang.entities.ui.UINetworkStats;
import com.beverly.hills.money.gang.handler.PlayScreenGameConnectionHandler;
import com.beverly.hills.money.gang.input.TextInputProcessor;
import com.beverly.hills.money.gang.log.ChatLog;
import com.beverly.hills.money.gang.log.MyPlayerKillLog;
import com.beverly.hills.money.gang.network.LoadBalancedGameConnection;
import com.beverly.hills.money.gang.proto.PushChatEventCommand;
import com.beverly.hills.money.gang.proto.PushGameEventCommand;
import com.beverly.hills.money.gang.proto.PushGameEventCommand.GameEventType;
import com.beverly.hills.money.gang.proto.Vector;
import com.beverly.hills.money.gang.registry.EnemiesRegistry;
import com.beverly.hills.money.gang.screens.data.PlayerConnectionContextData;
import com.beverly.hills.money.gang.screens.ui.EnemyAim;
import com.beverly.hills.money.gang.screens.ui.selection.ActivePlayUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.DeadPlayUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.UISelection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class PlayScreen extends GameScreen {

  private static final Logger LOG = LoggerFactory.getLogger(PlayScreen.class);
  private static final int DEAD_SCREEN_INPUT_DELAY_MLS = 1_000;
  private static final int TAUNT_DELAY_MLS = 1_250;
  private static final int SHADOW_MARGIN = 16;
  private GameScreen screenToTransition;
  private boolean showNetworkStats;
  private final SoundQueue narratorSoundQueue = new SoundQueue(1_500,
      Constants.QUAKE_NARRATOR_FX_VOLUME);
  private final EnemiesRegistry enemiesRegistry = new EnemiesRegistry();
  private static final int MAX_CHAT_MSG_LEN = 32;
  private static final float BLOOD_OVERLAY_ALPHA_SWITCH = 0.5f;
  private final TextureRegion texRegBloodOverlay, texRegBlackOverlay;
  private final Environment env;
  private EnemyAim enemyAim;
  private final TextInputProcessor chatTextInputProcessor;
  private final BitmapFont guiFont64;
  private long lastTauntTime = 0;
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
  private final UserSettingSound playerGoingThroughTeleport;
  private final UserSettingSound oneFragLeftSound;
  private final UserSettingSound twoFragsLeftSound;
  private final UserSettingSound threeFragsLeftSound;
  private final AtomicInteger actionSequence = new AtomicInteger(0);

  private final Texture hudBlackTexture;

  private final Texture hudRedTexture;

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
  private final LoadBalancedGameConnection gameConnection;
  private final PlayerConnectionContextData playerConnectionContextData;

  private final UINetworkStats uiNetworkStats;

  private final Map<PowerUpType, PowerUp> powerUps = new HashMap<>();

  public PlayScreen(final DaiKombatGame game,
      final LoadBalancedGameConnection gameConnection,
      final PlayerConnectionContextData playerConnectionContextData) {
    super(game, new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
    this.gameConnection = gameConnection;
    this.uiNetworkStats = new UINetworkStats(gameConnection.getPrimaryNetworkStats(),
        gameConnection.getSecondaryNetworkStats());
    dingSound1 = getGame().getAssMan().getUserSettingSound(SoundRegistry.DING_1);
    playerGoingThroughTeleport = game.getAssMan()
        .getUserSettingSound(SoundRegistry.PLAYER_GOING_THROUGH_TELEPORT);
    this.playerConnectionContextData = playerConnectionContextData;
    this.playersOnline = playerConnectionContextData.getPlayersOnline();
    myPlayerKillLog = new MyPlayerKillLog();
    env = new Environment();
    env.set(new ColorAttribute(ColorAttribute.AmbientLight, 1, 1, 1, 1f));
    env.set(new ColorAttribute(ColorAttribute.Fog, Constants.FOG_COLOR));

    chatTextInputProcessor = new TextInputProcessor(MAX_CHAT_MSG_LEN,
        () -> getGame().getAssMan()
            .getUserSettingSound(SoundRegistry.TYPING_SOUND_SEQ.getNext())
            .play(Constants.DEFAULT_SFX_TYPING_VOLUME));
    texRegBloodOverlay = getGame().getAssMan().getTextureRegion(
        TexturesRegistry.ATLAS, 0, 0, 2, 2);
    texRegBlackOverlay = getGame().getAssMan().getTextureRegion(
        TexturesRegistry.ATLAS, 3, 0, 2, 2);

    guiFont64 = getGame().getAssMan().getFont(FontRegistry.FONT_64);
    guiFont32 = getGame().getAssMan().getFont(FontRegistry.FONT_32);

    glyphLayoutHeadsupDead = new GlyphLayout(getUiFont(), Constants.YOU_DIED);
    glyphLayoutAim = new GlyphLayout(getUiFont(), "+");

    musicBackground = getGame().getAssMan()
        .getUserSettingSound(SoundRegistry.BATTLE_BG_SEQ.getNext());
    fightSound = getGame().getAssMan().getUserSettingSound(SoundRegistry.FIGHT);
    boomSound1 = getGame().getAssMan().getUserSettingSound(SoundRegistry.BOOM_1);
    boomSound2 = getGame().getAssMan().getUserSettingSound(SoundRegistry.BOOM_2);
    youLead = getGame().getAssMan().getUserSettingSound(SoundRegistry.YOU_LEAD);
    lostLead = getGame().getAssMan().getUserSettingSound(SoundRegistry.LOST_LEAD);
    fightSound.play(Constants.QUAKE_NARRATOR_FX_VOLUME);
    oneFragLeftSound = getGame().getAssMan().getUserSettingSound(SoundRegistry.ONE_FRAG_LEFT);
    twoFragsLeftSound = getGame().getAssMan().getUserSettingSound(SoundRegistry.TWO_FRAGS_LEFT);
    threeFragsLeftSound = getGame().getAssMan().getUserSettingSound(SoundRegistry.THREE_FRAGS_LEFT);

    getGame().getMapBuilder().buildMap(getGame().getAssMan().getMap(MapRegistry.ONLINE_MAP));

    chatLog = new ChatLog(() -> getGame().getAssMan().getUserSettingSound(SoundRegistry.PING)
        .play(Constants.DEFAULT_SFX_VOLUME));
    setPlayer(PlayerFactory.create(this, gameConnection, playerConnectionContextData));
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
                .ping(leaderBoardItem.getPingMls())
                .kills(leaderBoardItem.getKills())
                .build())
            .collect(Collectors.toList()),
        playerConnectionContextData.getFragsToWin(),
        () -> {
          if (!gameOver) {
            narratorSoundQueue.addSound(youLead);
          }
        },
        () -> {
          if (!gameOver) {
            narratorSoundQueue.addSound(lostLead);
          }
        }, fragsLeft -> {
      switch (fragsLeft) {
        case 3 -> narratorSoundQueue.addSound(threeFragsLeftSound);
        case 2 -> narratorSoundQueue.addSound(twoFragsLeftSound);
        case 1 -> narratorSoundQueue.addSound(oneFragLeftSound);
      }
    }

    );
    playScreenGameConnectionHandler = new PlayScreenGameConnectionHandler(this, enemiesRegistry);
    hudBlackTexture = createTexture(Color.BLACK);
    hudRedTexture = createTexture(new Color(1, 0, 0.15f, 1f));
  }

  private Texture createTexture(final Color color) {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fill();
    var texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }

  public BitmapFont getUiFont() {
    if (Configs.DEV_MODE) {
      return guiFont32;
    } else {
      return guiFont64;
    }
  }

  public void spawnTeleport(int teleportId, Vector2 position) {
    Teleport teleport = new Teleport(new Vector3(position.x, 0.0f, position.y), this, getPlayer(),
        teleportId, t -> {
      playerGoingThroughTeleport.play(Constants.DEFAULT_SFX_VOLUME);
      LOG.info("Collide with teleport");
      var currentPosition = getPlayer().getCurrent2DPosition();
      var currentDirection = getPlayer().getCurrent2DDirection();
      gameConnection.write(PushGameEventCommand.newBuilder()
          .setSequence(actionSequence.incrementAndGet())
          .setPingMls(
              Optional.ofNullable(gameConnection.getPrimaryNetworkStats().getPingMls())
                  .orElse(0))
          .setPlayerId(playerConnectionContextData.getPlayerId())
          .setEventType(GameEventType.TELEPORT)
          .setPosition(Vector.newBuilder()
              .setX(currentPosition.x).setY(currentPosition.y).build())
          .setDirection(Vector.newBuilder()
              .setX(currentDirection.x).setY(currentDirection.y).build())
          .setGameId(Configs.GAME_ID)
          .setTeleportId(teleportId)
          .build());
      getPlayer().setColliedTeleport(t);
    });
    getGame().getEntMan().addEntity(teleport);
  }

  public void spawnPowerUp(PowerUpType powerUpType, Vector2 position) {

    if (powerUps.containsKey(powerUpType)) {
      return;
    }
    LOG.info("Spawn power-up {}", powerUpType);
    var power = new PowerUp(new Vector3(position.x, 0.0f, position.y), this,
        getPlayer(),
        powerUpType.getTexture(),
        () -> {
          LOG.info("Collide with power-up");
          var currentPosition = getPlayer().getCurrent2DPosition();
          var currentDirection = getPlayer().getCurrent2DDirection();
          gameConnection.write(PushGameEventCommand.newBuilder()
              .setSequence(actionSequence.incrementAndGet())
              .setPingMls(Optional.ofNullable(gameConnection.getPrimaryNetworkStats().getPingMls())
                  .orElse(0))
              .setPlayerId(playerConnectionContextData.getPlayerId())
              .setEventType(powerUpType.getPickType())
              .setGameId(Configs.GAME_ID)
              .setPosition(Vector.newBuilder()
                  .setX(currentPosition.x).setY(currentPosition.y).build())
              .setDirection(Vector.newBuilder()
                  .setX(currentDirection.x).setY(currentDirection.y).build())
              .build());
          removePowerUp(powerUpType);
        });
    powerUps.put(powerUpType, power);
    getGame().getEntMan().addEntity(power);
  }

  public void removePowerUp(PowerUpType powerUpType) {
    LOG.info("Remove power up {}", powerUpType);
    Optional.ofNullable(powerUps.remove(powerUpType)).ifPresent(PowerUp::destroy);
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
        if (Gdx.input.isKeyJustPressed(Keys.P)) {
          LOG.info("Player position {}, direction {}",
              getPlayer().getCurrent2DPosition(), getPlayer().getCurrent2DDirection());
        }
        if (Gdx.input.isKeyJustPressed(Keys.N)) {
          showNetworkStats = !showNetworkStats;
        }
        if (Gdx.input.isKeyJustPressed(Keys.X)
            && System.currentTimeMillis() > lastTauntTime + TAUNT_DELAY_MLS) {
          handleTaunt();
        }
        getPlayer().handleInput(delta);
      }
    }
  }

  private void handleTaunt() {
    var taunt = TAUNTS_SEQ.getNext();
    getGame().getAssMan().getUserSettingSound(taunt.getPlayerSound())
        .play(SoundConf.builder()
            .volume(SoundVolumeType.LOW_LOUD.getVolume())
            .pitch(getPlayer().getPlayerClass().getVoicePitch()).build());
    String message = taunt.getChatMessage();
    chatLog.addMessage(
        playerConnectionContextData.getConnectGameData().getPlayerName(),
        message);
    lastTauntTime = System.currentTimeMillis();
    gameConnection.write(PushChatEventCommand.newBuilder()
        .setMessage(message)
        .setTaunt(taunt.getTauntType())
        .setPlayerId(playerConnectionContextData.getPlayerId())
        .setGameId(Configs.GAME_ID)
        .build());
  }

  private void handleAliveGuiInput() {
    if (Configs.DEV_MODE) {
      Gdx.input.setCursorCatched(false);
    }
    if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
      activePlayUISelectionUISelection.next();
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
      activePlayUISelectionUISelection.prev();
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
      deadPlayUISelectionUISelection.next();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      deadPlayUISelectionUISelection.prev();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
        || Gdx.input.isKeyJustPressed(Input.Keys.ALT_RIGHT)
        || Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_RIGHT)
        || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
        || Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
      screenToTransition = switch (deadPlayUISelectionUISelection.getSelectedOption()) {
        case RESPAWN -> new RespawnScreen(getGame(), playerConnectionContextData,
            gameConnection);
        case QUIT -> new MainMenuScreen(getGame());
      };
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
          playerConnectionContextData.getConnectGameData().getPlayerName(),
          chatTextInputProcessor.getText());
      chatTextInputProcessor.clear();
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      chatMode = false;
      chatTextInputProcessor.clear();
    } else {
      chatTextInputProcessor.handleInput();
    }
  }

  public void sendCurrentPlayerPosition() {
    var currentPosition = getPlayer().getCurrent2DPosition();
    var currentDirection = getPlayer().getCurrent2DDirection();
    gameConnection.write(PushGameEventCommand.newBuilder()
        .setPingMls(
            Optional.ofNullable(gameConnection.getPrimaryNetworkStats().getPingMls()).orElse(0))
        .setSequence(actionSequence.incrementAndGet())
        .setPlayerId(playerConnectionContextData.getPlayerId())
        .setEventType(PushGameEventCommand.GameEventType.MOVE)
        .setGameId(Configs.GAME_ID)
        .setPosition(Vector.newBuilder()
            .setX(currentPosition.x).setY(currentPosition.y).build())
        .setDirection(Vector.newBuilder()
            .setX(currentDirection.x).setY(currentDirection.y).build())
        .build());
    setTimeToSendMoves();
  }

  private void powerUpEffect(Color color, PowerUpType powerUpType) {
    getGame().getBatch().setColor(new Color(color.r, color.g, color.b,
        getPlayer().getAlphaChannel()).lerp(Color.WHITE, (float) Math.sin(
        getGame().getTimeSinceLaunch() * getPlayer().getPlayerEffects()
            .getPowerUpEffectIntensity(powerUpType).getLevel())));
  }

  @Override
  public void render(final float delta) {
    super.render(delta);
    if (!gameOver) {
      narratorSoundQueue.play();
    }
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
    if (!getPlayer().isDead()) {
      if (getPlayer().getPlayerEffects().isPowerUpActive(PowerUpType.QUAD_DAMAGE)) {
        powerUpEffect(Color.SKY, PowerUpType.QUAD_DAMAGE);
      } else if (getPlayer().getPlayerEffects().isPowerUpActive(PowerUpType.DEFENCE)) {
        powerUpEffect(Color.LIME, PowerUpType.DEFENCE);
      } else if (getPlayer().getPlayerEffects().isPowerUpActive(PowerUpType.INVISIBILITY)) {
        powerUpEffect(Color.WHITE, PowerUpType.INVISIBILITY);
      }
    }

    if (!getPlayer().isDead()) {
      var activeWeapon = getPlayer().getActiveWeaponRenderingData();
      float gunWidth = getViewport().getWorldWidth() * activeWeapon.getScreenRatioX();
      float gunHeight = getViewport().getWorldHeight() * activeWeapon.getScreenRatioY();
      getGame().getBatch().draw(activeWeapon.getTextureRegion(),
          getViewport().getWorldWidth() * 0.5f + activeWeapon.getPositioning().x - (
              activeWeapon.isCenter() ? gunWidth / 2 : 0),
          (int) getPlayer().getWeaponY() + activeWeapon.getPositioning().y,
          gunWidth, gunHeight);
    }
    getGame().getBatch().end();
    getGame().getBatch().begin();
    renderBloodOverlay02();
    renderGameTechStats();
    if (!getPlayer().isDead()) {
      if (myPlayerKillLog.hasKillerMessage()) {
        String killerMessage = myPlayerKillLog.getKillerMessage();
        GlyphLayout glyphLayoutKillerMessage = new GlyphLayout(getUiFont(), killerMessage);
        getUiFont().draw(getGame().getBatch(), killerMessage,
            getViewport().getWorldWidth() / 2f - glyphLayoutKillerMessage.width / 2f,
            getViewport().getWorldHeight() / 2f - glyphLayoutKillerMessage.height / 2f + 128);
      }
      String killStats = uiLeaderBoard.getMyStatsMessage(
          playerConnectionContextData.getFragsToWin());
      getUiFont().draw(getGame().getBatch(), killStats,
          getViewport().getWorldWidth() - 32 - new GlyphLayout(getUiFont(), killStats).width,
          128);

    }
    if (chatMode) {
      printShadowText(32, 128 - 16 + 64, ">" + chatTextInputProcessor.getText(), getUiFont(),
          0.5f);
    }
    if (chatLog.hasChatMessage()) {

      printShadowText(32, 256, chatLog.getChatMessages(), getUiFont(), 0.15f);

    }

    if (enemyAim != null) {

      guiFont32.setColor(1, 1, 1, 0.8f);
      getUiFont().setColor(1, 1, 1, 0.8f);

      GlyphLayout glyphName = new GlyphLayout(getUiFont(), enemyAim.getName());
      getUiFont().draw(getGame().getBatch(), enemyAim.getName(),
          getViewport().getWorldWidth() / 2f - glyphName.width / 2f,
          getViewport().getWorldHeight() / 2f - (glyphName.height / 2f) + 64);
      GlyphLayout glyphHP = new GlyphLayout(guiFont32, enemyAim.getHp());
      guiFont32.draw(getGame().getBatch(), enemyAim.getHp(),
          getViewport().getWorldWidth() / 2f - glyphHP.width / 2f,
          getViewport().getWorldHeight() / 2f - (glyphHP.height / 2f) + 64 - 32 - 8);

      enemyAim = null;
      guiFont32.setColor(1, 1, 1, 1);
      getUiFont().setColor(1, 1, 1, 1);
    }

    getGame().getBatch().setColor(1, 1, 1, 1); // Never cover HUD in blood.
    if (!getPlayer().isDead()) {

      printShadowText(32, 128 - 32,
          getPlayer().getCurrentHP() + " HP | " + playerConnectionContextData.getConnectGameData()
              .getPlayerName() + " | " + playerConnectionContextData.getConnectGameData()
              .getGamePlayerClass().toString(), getUiFont(), hudRedTexture,
          getHealthBlinkingAlphaChannel(getPlayer().getCurrentHP()));

      getUiFont().draw(getGame().getBatch(), "+",
          getViewport().getWorldWidth() / 2f - glyphLayoutAim.width / 2f,
          getViewport().getWorldHeight() / 2f - glyphLayoutAim.height / 2f);
    }

    if (showLeaderBoard) {
      String leaderBoard = getUiLeaderBoard().toString();
      var glyphLayoutRecSentMessages = new GlyphLayout(getUiFont(), leaderBoard);
      getUiFont().draw(getGame().getBatch(),
          leaderBoard, getViewport().getWorldWidth() / 2f - glyphLayoutRecSentMessages.width / 2f,
          getViewport().getWorldHeight() - 128);
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
      GamePlayerClass winnerClass;
      SkinUISelection winnerColor;
      if (uiLeaderBoard.getFirstPlacePlayerId() == playerConnectionContextData.getPlayerId()) {
        winnerClass = playerConnectionContextData.getConnectGameData().getGamePlayerClass();
        winnerColor = playerConnectionContextData.getConnectGameData().getSkinUISelection();
      } else {
        var enemyWinner = enemiesRegistry
            .getEnemy(uiLeaderBoard.getFirstPlacePlayerId()).orElseThrow(
                () -> new IllegalStateException("Can't find enemy by id"));
        winnerClass = enemyWinner.getEnemyClass();
        winnerColor = enemyWinner.getSkinUISelection();
      }
      screenToTransition = new GameOverScreen(getGame(), uiLeaderBoard,
          playerConnectionContextData.getConnectGameData(), winnerColor, winnerClass);
    } else if (gameConnection.isAnyDisconnected()) {
      gameConnection.pollErrors().forEach(playScreenGameConnectionHandler::handleException);
      playerConnectionContextData.getConnectGameData()
          .setPlayerIdToRecover(playerConnectionContextData.getPlayerId());
      screenToTransition = new ConnectServerScreen(getGame(),
          playerConnectionContextData.getConnectGameData());
    } else {
      try {
        playScreenGameConnectionHandler.handle();
      } catch (Exception e) {
        LOG.error("Can't handle screen actions", e);
        screenToTransition = new ErrorScreen(getGame(),
            StringUtils.defaultIfEmpty(e.getMessage(), "Can't handle connection")
                + ". Check internet signal. Last ping " + gameConnection.getPrimaryNetworkStats()
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

  private float getHealthBlinkingAlphaChannel(int health) {
    if (health > 50) {
      return 0.0f;
    }
    float healthRatio = 100f / health;
    float speed = Math.min(25, healthRatio * healthRatio);
    return 0.5f
        + (float) Math.sin(getGame().getTimeSinceLaunch() * speed) / 2;
  }

  private void renderGameTechStats() {
    StringBuilder gameTechStats = new StringBuilder();
    gameTechStats.append(playersOnline).append(" ONLINE ");
    gameTechStats.append("| PING ")
        .append(Objects.toString(gameConnection.getPrimaryNetworkStats().getPingMls(), "-"))
        .append(" MS | ");
    gameTechStats.append(Gdx.graphics.getFramesPerSecond()).append(" FPS");

    var gameTechStatsGlyph = new GlyphLayout(getUiFont(), gameTechStats);
    getUiFont().draw(getGame().getBatch(), gameTechStats,
        getViewport().getWorldWidth() - 32 - gameTechStatsGlyph.width,
        getViewport().getWorldHeight() - 32 - gameTechStatsGlyph.height);

    if (showNetworkStats) {
      renderDevModeGameTechStats();
    }
  }

  public boolean isTimeToSendMoves() {
    return System.currentTimeMillis() >= nextTimeToFlushPlayerActions;
  }

  private void setTimeToSendMoves() {
    nextTimeToFlushPlayerActions =
        System.currentTimeMillis() + playerConnectionContextData.getMovesUpdateFreqMls();
  }

  private void renderDevModeGameTechStats() {
    String networkStats = uiNetworkStats.toString();
    GlyphLayout glyphNetworkStats = new GlyphLayout(guiFont32, networkStats);
    guiFont32.draw(getGame().getBatch(),
        networkStats, Constants.DEFAULT_SELECTION_INDENT,
        getViewport().getWorldHeight() - getViewport().getWorldHeight() / 3
            + glyphNetworkStats.height / 2);
  }


  private void renderDeadGui() {
    float halfViewportWidth = getViewport().getWorldWidth() / 2f;
    float halfViewportHeight = getViewport().getWorldHeight() / 2f;
    getUiFont().draw(getGame().getBatch(), Constants.YOU_DIED,
        halfViewportWidth - glyphLayoutHeadsupDead.width / 2f,
        halfViewportHeight - glyphLayoutHeadsupDead.height / 2f);
    String killedBy = "KILLED BY " + getPlayer().getKilledBy().toUpperCase();
    GlyphLayout glyphLayoutKilledBy = new GlyphLayout(getUiFont(), killedBy);
    final float killedByX = halfViewportWidth - glyphLayoutKilledBy.width / 2f;
    final float killedByY = halfViewportHeight - glyphLayoutKilledBy.height / 2f - 64;
    getUiFont().draw(getGame().getBatch(), killedBy, killedByX, killedByY);
    deadPlayUISelectionUISelection.render(getUiFont(), this, 128);
    String pressTabToSeeLeaderboard = "PRESS TAB TO SEE LEADERBOARD";
    GlyphLayout glyphLayoutLeaderBoardHint = new GlyphLayout(guiFont32, pressTabToSeeLeaderboard);
    guiFont32.draw(getGame().getBatch(), pressTabToSeeLeaderboard,
        halfViewportWidth - glyphLayoutLeaderBoardHint.width / 2f,
        halfViewportHeight - glyphLayoutLeaderBoardHint.height / 2f + 128);
  }

  private void renderAliveGui() {
    activePlayUISelectionUISelection.render(getUiFont(), this, 64);
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

  public void setEnemyAim(EnemyAim enemyAim) {
    this.enemyAim = enemyAim;
  }

  private void printShadowText(final int x, final int y, final String text,
      final BitmapFont font, final Texture texture, final float alphaChannel) {
    GlyphLayout glyphLayoutChatLog = new GlyphLayout(font, text);
    float blockWidth = glyphLayoutChatLog.width + SHADOW_MARGIN * 2;
    float blockHeight = glyphLayoutChatLog.height + SHADOW_MARGIN * 2;
    float blockX = x - SHADOW_MARGIN;
    float blockY = y - SHADOW_MARGIN;
    var oldColor = getGame().getBatch().getColor().cpy();
    getGame().getBatch().setColor(1, 1, 1, alphaChannel);
    getGame().getBatch()
        .draw(texture, blockX, blockY, blockWidth, blockHeight);
    getGame().getBatch().setColor(oldColor);
    getUiFont().draw(getGame().getBatch(), text, x, y + glyphLayoutChatLog.height);
  }

  private void printShadowText(final int x, final int y, final String text,
      final BitmapFont font, final float alphaChannel) {
    printShadowText(x, y, text, font, hudBlackTexture, alphaChannel);
  }
}
