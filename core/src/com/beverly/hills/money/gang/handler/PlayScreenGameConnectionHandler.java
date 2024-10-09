package com.beverly.hills.money.gang.handler;

import static com.beverly.hills.money.gang.Constants.DEFAULT_ENEMY_Y;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.SoundVolumeType;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound;
import com.beverly.hills.money.gang.entities.achievement.AchievementFactory;
import com.beverly.hills.money.gang.entities.achievement.KillStats;
import com.beverly.hills.money.gang.entities.enemies.Enemy;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayer;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayerAction;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayerActionType;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.entities.ui.UILeaderBoard;
import com.beverly.hills.money.gang.proto.ServerResponse;
import com.beverly.hills.money.gang.proto.ServerResponse.GameEvent.GameEventType;
import com.beverly.hills.money.gang.proto.ServerResponse.GameEvent.WeaponType;
import com.beverly.hills.money.gang.proto.ServerResponse.GamePowerUp;
import com.beverly.hills.money.gang.proto.ServerResponse.GamePowerUpType;
import com.beverly.hills.money.gang.registry.EnemiesRegistry;
import com.beverly.hills.money.gang.screens.PlayScreen;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponMapper;
import com.beverly.hills.money.gang.utils.Converter;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class PlayScreenGameConnectionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PlayScreenGameConnectionHandler.class);

  private final PlayScreen playScreen;

  private final EnemiesRegistry enemiesRegistry = new EnemiesRegistry();

  private final KillStats killStats = new KillStats();

  public void handle() {
    if (playScreen.isExiting()) {
      LOG.info("Stop handling");
      return;
    }

    playScreen.getUiLeaderBoard().setPing(
        playScreen.getPlayerConnectionContextData().getPlayerId(),
        Optional.ofNullable(playScreen.getGameConnection()
            .getPrimaryNetworkStats().getPingMls()).orElse(0));

    playScreen.getGameConnection().pollResponses().forEach(serverResponse -> {
      if (serverResponse.hasChatEvents()) {
        handleChat(serverResponse.getChatEvents());
      } else if (serverResponse.hasGameEvents()) {
        handleGameEvent(serverResponse.getGameEvents());
      } else if (serverResponse.hasGameOver()) {
        handleGameOver(serverResponse.getGameOver());
      } else if (serverResponse.hasErrorEvent()) {
        handleErrorEvent(serverResponse.getErrorEvent());
      } else if (serverResponse.hasPowerUpSpawn()) {
        handlePowerUpSpawn(serverResponse.getPowerUpSpawn());
      } else if (serverResponse.hasTeleportSpawn()) {
        handleTeleportSpawn(serverResponse.getTeleportSpawn());
      }
    });
    playScreen.getGameConnection().pollErrors().forEach(this::handleException);
  }

  private void handleChat(ServerResponse.ChatEvent chatEvent) {
    playScreen.getChatLog().addMessage(chatEvent.getName(), chatEvent.getMessage());
  }

  private void handleGameEvent(ServerResponse.GameEvents gameEvents) {
    if (gameEvents.hasPlayersOnline()) {
      playScreen.setPlayersOnline(gameEvents.getPlayersOnline());
    }
    gameEvents.getEventsList().forEach(gameEvent -> {
      switch (gameEvent.getEventType()) {
        case SPAWN, JOIN, RESPAWN -> handleSpawn(gameEvent);
        case EXIT -> handleExit(gameEvent);
        case KILL -> handleDeath(gameEvent);
        case MOVE -> handleMove(gameEvent);
        case ATTACK -> handleAttackMiss(gameEvent);
        case GET_ATTACKED -> handleGetHit(gameEvent);
        case TELEPORT -> handleTeleport(gameEvent);
      }
    });
  }

  private void handleTeleport(ServerResponse.GameEvent gameEvent) {
    // if I'm getting teleported
    if (gameEvent.getPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {
      playScreen.getPlayer().teleport(
          createVector(gameEvent.getPlayer().getPosition()),
          createVector(gameEvent.getPlayer().getDirection()));
      return;
    }

    enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
        .ifPresent(enemyPlayer -> {
          enemyPlayer.queueAction(EnemyPlayerAction
              .builder()
              .eventSequenceId(gameEvent.getSequence())
              .direction(enemyPlayer.getLastDirection())
              .route(enemyPlayer.getRect().getOldPosition())
              .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
              .onComplete(() -> {
                playScreen.getGame().getAssMan()
                    .getUserSettingSound(SoundRegistry.ENEMY_PLAYER_GOING_THROUGH_TELEPORT)
                    .play(enemyPlayer.getSFXVolume(), enemyPlayer.getSFXPan());
                enemyPlayer.teleport(new Vector3(gameEvent.getPlayer().getPosition().getX(),
                        DEFAULT_ENEMY_Y, gameEvent.getPlayer().getPosition().getY()),
                    createVector(gameEvent.getPlayer().getDirection()));
                playScreen.getGame().getAssMan()
                    .getUserSettingSound(SoundRegistry.SPAWN_SOUND_SEQ.getNextSound())
                    .play(enemyPlayer.getSFXVolume(), enemyPlayer.getSFXPan());
              })
              .build());
        });
  }

  private void handleGameOver(ServerResponse.GameOver gameOver) {
    playScreen.setGameOver(true);
    playScreen.getUiLeaderBoard().set(gameOver.getLeaderBoard().getItemsList().stream()
        .map(leaderBoardItem -> UILeaderBoard.LeaderBoardPlayer.builder()
            .name(leaderBoardItem.getPlayerName())
            .id(leaderBoardItem.getPlayerId())
            .ping(leaderBoardItem.getPingMls())
            .deaths(leaderBoardItem.getDeaths())
            .kills(leaderBoardItem.getKills())
            .build())
        .collect(Collectors.toList()));
  }

  private void handleSpawn(ServerResponse.GameEvent gameEvent) {
    if (gameEvent.getPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {
      LOG.warn("This is my own spawn");
      return;
    } else if (enemiesRegistry.exists(gameEvent.getPlayer().getPlayerId())) {
      LOG.info("Player already spawned {}", gameEvent);
      return;
    }
    LOG.info("Spawn player {}", gameEvent.getPlayer());
    EnemyPlayer enemyPlayer = new EnemyPlayer(playScreen.getPlayer(),
        gameEvent.getPlayer().getPlayerId(),
        new Vector3(gameEvent.getPlayer().getPosition().getX(),
            DEFAULT_ENEMY_Y, gameEvent.getPlayer().getPosition().getY()),
        createVector(gameEvent.getPlayer().getDirection()),
        playScreen, gameEvent.getPlayer().getPlayerName(),
        getEnemyTexture(gameEvent.getPlayer().getSkinColor()), createEnemyListeners(),
        playScreen.getPlayerConnectionContextData().getSpeed());
    gameEvent.getPlayer().getActivePowerUpsList().forEach(
        gamePowerUp -> activateEnemyPowerUpOnSpawn(
            enemyPlayer, getPowerUpType(gamePowerUp.getType()), gamePowerUp.getLastsForMls()));

    playScreen.getGame().getEntMan().addEntity(enemyPlayer);
    enemiesRegistry.addEnemy(gameEvent.getPlayer().getPlayerId(), enemyPlayer);

    playScreen.getUiLeaderBoard().addNewPlayer(UILeaderBoard.LeaderBoardPlayer.builder()
        .name(enemyPlayer.getName())
        .ping(gameEvent.getPlayer().getPingMls())
        .id(enemyPlayer.getEnemyPlayerId())
        .deaths(
            gameEvent.getPlayer().hasGameMatchStats() ? gameEvent.getPlayer().getGameMatchStats()
                .getDeaths() : 0)
        .kills(gameEvent.getPlayer().hasGameMatchStats() ? gameEvent.getPlayer().getGameMatchStats()
            .getKills() : 0)
        .build());

    if (gameEvent.getEventType() == GameEventType.JOIN) {
      playScreen.getChatLog()
          .addMessage("game log", gameEvent.getPlayer().getPlayerName() + " has joined the game");
    }
    if (gameEvent.getEventType() == GameEventType.JOIN
        || gameEvent.getEventType() == GameEventType.RESPAWN) {
      playScreen.getGame().getAssMan()
          .getUserSettingSound(SoundRegistry.SPAWN_SOUND_SEQ.getNextSound())
          .play(enemyPlayer.getSFXVolume(), enemyPlayer.getSFXPan());
    }
  }

  private void activateEnemyPowerUpOnSpawn(EnemyPlayer enemyPlayer, PowerUpType powerUpType,
      int lastsForMls) {
    enemyPlayer.getEnemyEffects().activatePowerUp(powerUpType, lastsForMls);
    playScreen.removePowerUp(powerUpType);
  }

  private TexturesRegistry getEnemyTexture(ServerResponse.PlayerSkinColor playerSkinColor) {
    return switch (playerSkinColor) {
      case BLUE -> TexturesRegistry.ENEMY_PLAYER_SPRITES_BLUE;
      case PURPLE -> TexturesRegistry.ENEMY_PLAYER_SPRITES_PURPLE;
      case PINK -> TexturesRegistry.ENEMY_PLAYER_SPRITES_PINK;
      case GREEN -> TexturesRegistry.ENEMY_PLAYER_SPRITES_GREEN;
      case ORANGE -> TexturesRegistry.ENEMY_PLAYER_SPRITES_ORANGE;
      case YELLOW -> TexturesRegistry.ENEMY_PLAYER_SPRITES_YELLOW;
      default -> throw new IllegalStateException("Not supported skin color " + playerSkinColor);
    };
  }

  private void handleExit(ServerResponse.GameEvent gameEvent) {
    if (gameEvent.getPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {
      return;
    }
    enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
        .map(EnemyPlayer::getName)
        .ifPresent(playerName -> {
          playScreen.getChatLog().addMessage("game log", playerName + " has left the game");
          enemiesRegistry.removeEnemy(gameEvent.getPlayer().getPlayerId())
              .ifPresent(Enemy::destroy);
        });
    playScreen.getUiLeaderBoard().removePlayer(gameEvent.getPlayer().getPlayerId());
  }

  private void handleMove(ServerResponse.GameEvent gameEvent) {
    if (gameEvent.getPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {
      if (gameEvent.getPlayer().hasHealth()) {
        playScreen.getPlayer().setHP(gameEvent.getPlayer().getHealth());
      }
      gameEvent.getPlayer().getActivePowerUpsList().forEach(this::activatePlayerPowerUp);
      return;
    }
    enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
        .ifPresent(enemyPlayer -> {
          gameEvent.getPlayer().getActivePowerUpsList().forEach(gamePowerUp
              -> activateEnemyPowerUp(enemyPlayer, gamePowerUp));
          playScreen.getUiLeaderBoard()
              .setPing(gameEvent.getPlayer().getPlayerId(), gameEvent.getPlayer().getPingMls());
          enemyPlayer.queueAction(EnemyPlayerAction.builder()
              .eventSequenceId(gameEvent.getSequence())
              .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
              .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
              .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition())).build());
        });
  }

  private void activatePlayerPowerUp(GamePowerUp powerUp) {

    PowerUpType powerUpType = getPowerUpType(powerUp.getType());
    if (playScreen.getPlayer().getPlayerEffects().isPowerUpActive(powerUpType)) {
      return;
    }
    playScreen.getGame().getAssMan()
        .getUserSettingSound(powerUpType.getPlayerPickSound())
        .play(Constants.DEFAULT_SFX_VOLUME);
    playScreen.getPlayer().getPlayerEffects()
        .activatePowerUp(powerUpType, powerUp.getLastsForMls());
    playScreen.removePowerUp(powerUpType);
  }

  private void activateEnemyPowerUp(EnemyPlayer enemyPlayer, GamePowerUp powerUp) {
    PowerUpType powerUpType = getPowerUpType(powerUp.getType());
    if (enemyPlayer.getEnemyEffects().isPowerUpActive(powerUpType)) {
      return;
    }
    playScreen.getGame().getAssMan()
        .getUserSettingSound(powerUpType.getEnemyPickSound())
        .play(enemyPlayer.getSFXVolume(), enemyPlayer.getSFXPan());
    enemyPlayer.getEnemyEffects()
        .activatePowerUp(powerUpType, powerUp.getLastsForMls());
    playScreen.removePowerUp(powerUpType);
  }

  private PowerUpType getPowerUpType(GamePowerUpType powerUpType) {
    return switch (powerUpType) {
      case INVISIBILITY -> PowerUpType.INVISIBILITY;
      case QUAD_DAMAGE -> PowerUpType.QUAD_DAMAGE;
      case DEFENCE -> PowerUpType.DEFENCE;
      case HEALTH -> PowerUpType.HEALTH;
      default -> throw new IllegalArgumentException("Not supported power-up type " + powerUpType);
    };
  }


  private void handleAttackMiss(ServerResponse.GameEvent gameEvent) {
    if (gameEvent.getPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {
      return;
    }
    EnemyPlayerActionType enemyPlayerActionType = switch (gameEvent.getWeaponType()) {
      // if we missed the punch then we just move to the position
      case PUNCH -> EnemyPlayerActionType.MOVE;
      case SHOTGUN, RAILGUN, MINIGUN -> EnemyPlayerActionType.ATTACK;
      default -> throw new IllegalArgumentException(
          "Not supported event type " + gameEvent.getEventType());
    };
    enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
        .ifPresent(enemyPlayer -> enemyPlayer.queueAction(EnemyPlayerAction.builder()
            .eventSequenceId(gameEvent.getSequence())
            .enemyPlayerActionType(enemyPlayerActionType)
            .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
            .onComplete(() -> {
              if (gameEvent.getWeaponType() == WeaponType.PUNCH) {
                new TimeLimitedSound(
                    playScreen.getGame().getAssMan()
                        .getUserSettingSound(SoundRegistry.ENEMY_PUNCH_THROWN))
                    .play(enemyPlayer.getSFXVolume(), enemyPlayer.getSFXPan());
              } else {
                enemyPlayer.attack(WeaponMapper.getWeapon(gameEvent.getWeaponType()), false);
              }
            })
            .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition())).build()));
  }

  private void handleGetHit(ServerResponse.GameEvent gameEvent) {
    EnemyPlayerActionType enemyPlayerActionType = EnemyPlayerActionType.ATTACK;

    // if I hit somebody, then do nothing. the animation is played one client immediately

    // if I get hit
    if (gameEvent.getAffectedPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {

      enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
          .ifPresent(enemyPlayer -> {
            enemyPlayer.queueAction(EnemyPlayerAction.builder()
                .eventSequenceId(gameEvent.getSequence())
                .enemyPlayerActionType(enemyPlayerActionType)
                .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
                .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
                .onComplete(() -> {
                  enemyPlayer.attack(WeaponMapper.getWeapon(gameEvent.getWeaponType()), true);
                  playScreen.getPlayer().getHit(gameEvent.getAffectedPlayer().getHealth());
                  new TimeLimitedSound(
                      playScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry
                          .VOICE_GET_HIT_SOUND_SEQ.getNextSound())).play(SoundVolumeType.LOW_QUIET,
                      0.f, 1000);
                })
                .build());
          });
    } else if (gameEvent.getPlayer().getPlayerId() != playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {

      // enemies hitting each other
      enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
          .ifPresent(enemyPlayer -> enemyPlayer.queueAction(EnemyPlayerAction.builder()
              .eventSequenceId(gameEvent.getSequence())
              .enemyPlayerActionType(enemyPlayerActionType)
              .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
              .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
              .onComplete(
                  () -> {
                    enemyPlayer.attack(WeaponMapper.getWeapon(gameEvent.getWeaponType()), false);
                    enemiesRegistry.getEnemy(gameEvent.getAffectedPlayer().getPlayerId())
                        .ifPresent(EnemyPlayer::getHit);
                  })
              .build()));

    }
  }


  private void handleDeath(ServerResponse.GameEvent gameEvent) {
    EnemyPlayerActionType enemyPlayerActionType = EnemyPlayerActionType.ATTACK;
    playScreen.getUiLeaderBoard().registerKill(gameEvent.getPlayer().getPlayerId(),
        gameEvent.getAffectedPlayer().getPlayerId());
    if (gameEvent.getAffectedPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {
      String killedBy = enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
          .map(EnemyPlayer::getName).orElse("killer");

      enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
          .ifPresent(enemyPlayer -> enemyPlayer.queueAction(EnemyPlayerAction.builder()
              .eventSequenceId(gameEvent.getSequence())
              .enemyPlayerActionType(enemyPlayerActionType)
              .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
              .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
              .onComplete(() -> {
                enemyPlayer.attack(WeaponMapper.getWeapon(gameEvent.getWeaponType()), true);
                playScreen.getPlayer().die(killedBy);
                playScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry
                    .VOICE_GET_HIT_SOUND_SEQ.getNextSound()).play(Constants.PLAYER_FX_VOLUME);
                playScreen.getGame().getAssMan()
                    .getUserSettingSound(SoundRegistry.LOOSING_SOUND_SEQ.getNextSound())
                    .play(Constants.MK_NARRATOR_FX_VOLUME);
                playScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry.BELL)
                    .play(Constants.DEFAULT_SFX_VOLUME);
              })
              .build()));

      LOG.info("I'm dead");
    } else if (gameEvent.getPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {
      var victimPlayerOpt = enemiesRegistry.removeEnemy(
          gameEvent.getAffectedPlayer().getPlayerId());
      victimPlayerOpt.ifPresent(Enemy::die);
      int oldHealth = playScreen.getPlayer().getCurrentHP();
      int newHealth = gameEvent.getPlayer().getHealth();
      playScreen.getPlayer().setHP(newHealth);
      int buff = newHealth - oldHealth;
      playScreen.getMyPlayerKillLog()
          .myPlayerKill(victimPlayerOpt.map(EnemyPlayer::getName).orElse("victim"), buff);
      killStats.registerKill();
      AchievementFactory.create(WeaponMapper.getWeapon(gameEvent.getWeaponType()), killStats)
          .ifPresent(
              achievement -> playScreen.getNarratorSoundQueue()
                  .addSound(playScreen.getGame().getAssMan()
                      .getUserSettingSound(achievement.getSound())));

    } else {
      enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
          .ifPresent(enemyPlayer -> enemyPlayer.queueAction(EnemyPlayerAction.builder()
              .eventSequenceId(gameEvent.getSequence())
              .enemyPlayerActionType(enemyPlayerActionType)
              .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
              .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
              .onComplete(() -> {
                enemyPlayer.attack(WeaponMapper.getWeapon(gameEvent.getWeaponType()), false);
                enemiesRegistry.removeEnemy(
                    gameEvent.getAffectedPlayer().getPlayerId()).ifPresent(
                    Enemy::die);
              })
              .build()));
    }
  }

  public void handleErrorEvent(ServerResponse.ErrorEvent errorEvent) {
    playScreen.setErrorMessage(errorEvent.getMessage());
  }

  public void handlePowerUpSpawn(ServerResponse.PowerUpSpawnEvent powerUpSpawnEvent) {
    powerUpSpawnEvent.getItemsList().forEach(powerUpSpawnEventItem
        -> playScreen.spawnPowerUp(getPowerUpType(powerUpSpawnEventItem.getType()),
        createVector(powerUpSpawnEventItem.getPosition())));
  }

  public void handleTeleportSpawn(ServerResponse.TeleportSpawnEvent teleportSpawnEvent) {
    teleportSpawnEvent.getItemsList().forEach(teleportSpawnEventItem
        -> playScreen.spawnTeleport(teleportSpawnEventItem.getId(),
        createVector(teleportSpawnEventItem.getPosition())));
  }

  public void handleException(Throwable error) {
    LOG.error("Got error", error);
    playScreen.setErrorMessage(ExceptionUtils.getMessage(error));
  }

  private Enemy.EnemyListeners createEnemyListeners() {
    return Enemy.EnemyListeners
        .builder()
        .onDeath(enemy -> playScreen.getGame().getAssMan()
            .getUserSettingSound(SoundRegistry.ENEMY_DEATH_SOUND_SEQ.getNextSound())
            .play(enemy.getSFXVolume(), enemy.getSFXPan()))
        .onGetShot(enemy -> new TimeLimitedSound(playScreen.getGame().getAssMan()
            .getUserSettingSound(SoundRegistry.ENEMY_GET_HIT_SOUND_SEQ.getNextSound()))
            .play(enemy.getSFXVolume(), enemy.getSFXPan(), 1500))
        .onAttack(enemyWeapon -> {
              var enemy = enemyWeapon.getEnemy();
              var sound = switch (enemyWeapon.getWeapon()) {
                case SHOTGUN -> SoundRegistry.ENEMY_SHOTGUN;
                case GAUNTLET -> SoundRegistry.ENEMY_PUNCH_HIT;
                case RAILGUN -> SoundRegistry.ENEMY_RAILGUN;
                case MINIGUN -> SoundRegistry.ENEMY_MINIGUN;
              };
              new TimeLimitedSound(
                  playScreen.getGame().getAssMan().getUserSettingSound(sound))
                  .play(enemyWeapon.isAttackingPlayer() ? SoundVolumeType.HIGH_LOUD
                          : enemy.getSFXVolume(), enemy.getSFXPan(),
                      enemy.getEnemyEffects().isPowerUpActive(PowerUpType.QUAD_DAMAGE)
                          ? playScreen.getGame()
                          .getAssMan()
                          .getUserSettingSound(SoundRegistry.ENEMY_QUAD_DAMAGE_ATTACK) : null);
            }
        ).build();
  }

  private static Vector2 createVector(ServerResponse.Vector serverVector) {
    return new Vector2(serverVector.getX(), serverVector.getY());
  }
}