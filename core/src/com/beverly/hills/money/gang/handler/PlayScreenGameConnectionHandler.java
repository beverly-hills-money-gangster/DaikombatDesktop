package com.beverly.hills.money.gang.handler;

import static com.beverly.hills.money.gang.Constants.DEFAULT_ENEMY_Y;
import static com.beverly.hills.money.gang.proto.WeaponType.PUNCH;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
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
import com.beverly.hills.money.gang.proto.PlayerClass;
import com.beverly.hills.money.gang.proto.PlayerSkinColor;
import com.beverly.hills.money.gang.proto.ServerResponse;
import com.beverly.hills.money.gang.proto.ServerResponse.GameEvent.GameEventType;
import com.beverly.hills.money.gang.proto.ServerResponse.GamePowerUp;
import com.beverly.hills.money.gang.proto.ServerResponse.GamePowerUpType;
import com.beverly.hills.money.gang.proto.Vector;
import com.beverly.hills.money.gang.registry.EnemiesRegistry;
import com.beverly.hills.money.gang.registry.EnemyPlayerProjectileBoomFactoriesRegistry;
import com.beverly.hills.money.gang.registry.EnemyPlayerProjectileShootingFactoriesRegistry;
import com.beverly.hills.money.gang.screens.PlayScreen;
import com.beverly.hills.money.gang.screens.ui.selection.PlayerClassUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import com.beverly.hills.money.gang.screens.ui.taunt.GameTaunt;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponMapper;
import com.beverly.hills.money.gang.utils.Converter;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO add manual tests for that
@RequiredArgsConstructor
public class PlayScreenGameConnectionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PlayScreenGameConnectionHandler.class);

  private final PlayScreen playScreen;

  private final EnemiesRegistry enemiesRegistry;

  private final EnemyPlayerProjectileBoomFactoriesRegistry enemyPlayerProjectileBoomFactoriesRegistry
      = new EnemyPlayerProjectileBoomFactoriesRegistry();
  private final EnemyPlayerProjectileShootingFactoriesRegistry enemyPlayerProjectileShootingFactoriesRegistry
      = new EnemyPlayerProjectileShootingFactoriesRegistry();

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
    if (chatEvent.hasTaunt()) {
      enemiesRegistry.getEnemy(chatEvent.getPlayerId()).ifPresent(
          enemyPlayer -> playScreen.getGame().getAssMan()
              .getUserSettingSound(GameTaunt.map(chatEvent.getTaunt()).getEnemySound())
              .play(enemyPlayer.getSFXVolume(), enemyPlayer.getSFXPan()));
    }
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
                    .getUserSettingSound(SoundRegistry.SPAWN_SOUND_SEQ.getNext())
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
        getSkinColor(gameEvent.getPlayer().getSkinColor()), createEnemyListeners(),
        playScreen.getPlayerConnectionContextData().getSpeed(),
        gameEvent.getPlayer().getHealth(),
        createPlayerClass(gameEvent.getPlayer().getPlayerClass()));
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
          .getUserSettingSound(SoundRegistry.SPAWN_SOUND_SEQ.getNext())
          .play(enemyPlayer.getSFXVolume(), enemyPlayer.getSFXPan());
    }
  }

  private PlayerClassUISelection createPlayerClass(PlayerClass playerClass) {
    return switch (playerClass) {
      case WARRIOR -> PlayerClassUISelection.WARRIOR;
      case DEMON_TANK -> PlayerClassUISelection.DEMON_TANK;
      case ANGRY_SKELETON -> PlayerClassUISelection.ANGRY_SKELETON;
      default -> throw new IllegalArgumentException("Not supported class " + playerClass.name());
    };
  }

  private void activateEnemyPowerUpOnSpawn(EnemyPlayer enemyPlayer, PowerUpType powerUpType,
      int lastsForMls) {
    enemyPlayer.getEnemyEffects().activatePowerUp(powerUpType, lastsForMls);
    playScreen.removePowerUp(powerUpType);
  }

  private SkinUISelection getSkinColor(
      PlayerSkinColor playerSkinColor) {
    return switch (playerSkinColor) {
      case BLUE -> SkinUISelection.BLUE;
      case PURPLE -> SkinUISelection.PURPLE;
      case PINK -> SkinUISelection.PINK;
      case GREEN -> SkinUISelection.GREEN;
      case ORANGE -> SkinUISelection.ORANGE;
      case YELLOW -> SkinUISelection.YELLOW;
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
          if (gameEvent.getPlayer().hasHealth()) {
            enemyPlayer.setHp(gameEvent.getPlayer().getHealth());
          }
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
    EnemyPlayerActionType enemyPlayerActionType =
        gameEvent.hasWeaponType() ? switch (gameEvent.getWeaponType()) {
          // if we missed the punch then we just move to the position
          case PUNCH -> EnemyPlayerActionType.MOVE;
          case SHOTGUN, RAILGUN, MINIGUN, ROCKET_LAUNCHER -> EnemyPlayerActionType.ATTACK;
          default -> throw new IllegalArgumentException(
              "Not supported weapon type " + gameEvent.getWeaponType());
        } : EnemyPlayerActionType.MOVE;

    if (gameEvent.hasWeaponType()) {
      Optional.ofNullable(WeaponMapper.getWeapon(gameEvent.getWeaponType()).getProjectileRef())
          .ifPresent(
              projectile -> {
                var position = Converter.convertToVector2(gameEvent.getPlayer().getPosition());
                var direction = Converter.convertToVector2(gameEvent.getPlayer().getDirection());
                playScreen.getGame().getEntMan()
                    .addEntity(enemyPlayerProjectileShootingFactoriesRegistry.get(projectile)
                        .create(position, direction, playScreen.getPlayer()));
              });
    }

    enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
        .ifPresent(enemyPlayer -> enemyPlayer.queueAction(EnemyPlayerAction.builder()
            .eventSequenceId(gameEvent.getSequence())
            .enemyPlayerActionType(enemyPlayerActionType)
            .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
            .onComplete(() -> {
              if (gameEvent.hasWeaponType()) {
                if (gameEvent.getWeaponType() == PUNCH) {
                  new TimeLimitedSound(
                      playScreen.getGame().getAssMan()
                          .getUserSettingSound(SoundRegistry.ENEMY_PUNCH_THROWN))
                      .play(enemyPlayer.getSFXVolume(), enemyPlayer.getSFXPan());
                } else {
                  enemyPlayer.attack(WeaponMapper.getWeapon(gameEvent.getWeaponType()), false);
                }
              } else if (gameEvent.hasProjectile()) {
                var boomPosition = Converter.convertToVector2(
                    gameEvent.getProjectile().getPosition());
                playScreen.getGame().getEntMan().addEntity(
                    enemyPlayerProjectileBoomFactoriesRegistry.get(WeaponMapper.getWeaponProjectile(
                            gameEvent.getProjectile().getProjectileType()))
                        .create(boomPosition, playScreen.getPlayer()));
              }
            })
            .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition())).build()));
  }

  private void handleGetHit(ServerResponse.GameEvent gameEvent) {
    EnemyPlayerActionType enemyPlayerActionType = EnemyPlayerActionType.ATTACK;

    // if I hit somebody
    if (gameEvent.getPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {
      // if I hit myself
      if (gameEvent.getAffectedPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
          .getPlayerId()) {
        playScreen.getPlayer().setHP(gameEvent.getAffectedPlayer().getHealth());
      } else {
        enemiesRegistry.getEnemy(gameEvent.getAffectedPlayer().getPlayerId())
            .ifPresent(enemyPlayer -> enemyPlayer.setHp(gameEvent.getAffectedPlayer().getHealth()));
      }
    } else if (gameEvent.getAffectedPlayer().getPlayerId()
        == playScreen.getPlayerConnectionContextData().getPlayerId()) {
      // if I get hit
      enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
          .ifPresent(enemyPlayer -> {
            enemyPlayer.setHp(gameEvent.getPlayer().getHealth());
            enemyPlayer.queueAction(EnemyPlayerAction.builder()
                .eventSequenceId(gameEvent.getSequence())
                .enemyPlayerActionType(enemyPlayerActionType)
                .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
                .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
                .onComplete(() -> {
                  if (gameEvent.hasWeaponType()) {
                    enemyPlayer.attack(WeaponMapper.getWeapon(gameEvent.getWeaponType()), true);
                  } else if (gameEvent.hasProjectile()) {
                    playScreen.getGame().getEntMan().addEntity(
                        enemyPlayerProjectileBoomFactoriesRegistry.get(
                                WeaponMapper.getWeaponProjectile(
                                    gameEvent.getProjectile().getProjectileType()))
                            .create(getPlayerBoomPosition(), playScreen.getPlayer()));
                  }
                  playScreen.getPlayer().getHit(gameEvent.getAffectedPlayer().getHealth());
                  new TimeLimitedSound(
                      playScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry
                          .VOICE_GET_HIT_SOUND_SEQ.getNext())).play(SoundVolumeType.LOW_QUIET,
                      0.f, 1000);
                })
                .build());
          });
    } else {
      // enemies hitting each other
      enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
          .ifPresent(enemyPlayer -> {
            enemyPlayer.setHp(gameEvent.getPlayer().getHealth());
            enemyPlayer.queueAction(EnemyPlayerAction.builder()
                .eventSequenceId(gameEvent.getSequence())
                .enemyPlayerActionType(enemyPlayerActionType)
                .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
                .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
                .onComplete(
                    () -> {
                      if (gameEvent.hasWeaponType()) {
                        enemyPlayer.attack(WeaponMapper.getWeapon(gameEvent.getWeaponType()),
                            false);
                      } else {
                        playScreen.getGame().getEntMan().addEntity(
                            enemyPlayerProjectileBoomFactoriesRegistry.get(
                                    WeaponMapper.getWeaponProjectile(
                                        gameEvent.getProjectile().getProjectileType()))
                                .create(
                                    Converter.convertToVector2(
                                        gameEvent.getProjectile().getPosition()),
                                    playScreen.getPlayer()));
                      }
                      enemiesRegistry.getEnemy(gameEvent.getAffectedPlayer().getPlayerId())
                          .ifPresent(EnemyPlayer::getHit);
                    })
                .build());
          });
      enemiesRegistry.getEnemy(gameEvent.getAffectedPlayer().getPlayerId())
          .ifPresent(enemyPlayer -> enemyPlayer.setHp(gameEvent.getAffectedPlayer().getHealth()));

    }
  }


  private void handleDeath(ServerResponse.GameEvent gameEvent) {
    EnemyPlayerActionType enemyPlayerActionType = EnemyPlayerActionType.ATTACK;
    playScreen.getUiLeaderBoard().registerKill(gameEvent.getPlayer().getPlayerId(),
        gameEvent.getAffectedPlayer().getPlayerId());

    if (gameEvent.getAffectedPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {
      // if I get killed
      if (gameEvent.getPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
          .getPlayerId()) {
        // if I get killed by myself
        diePlayer("yourself");
      } else {
        String killedBy = enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
            .map(EnemyPlayer::getName).orElse("killer");
        enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
            .ifPresent(enemyPlayer -> enemyPlayer.queueAction(EnemyPlayerAction.builder()
                .eventSequenceId(gameEvent.getSequence())
                .enemyPlayerActionType(enemyPlayerActionType)
                .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
                .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
                .onComplete(() -> {
                  if (gameEvent.hasWeaponType()) {
                    enemyPlayer.attack(WeaponMapper.getWeapon(gameEvent.getWeaponType()), true);
                  } else if (gameEvent.hasProjectile()) {
                    playScreen.getGame().getEntMan().addEntity(
                        enemyPlayerProjectileBoomFactoriesRegistry.get(
                                WeaponMapper.getWeaponProjectile(
                                    gameEvent.getProjectile().getProjectileType()))
                            .create(getPlayerBoomPosition(), playScreen.getPlayer()));
                  }
                  diePlayer(killedBy);
                })
                .build()));
      }
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
      var weapon = gameEvent.hasWeaponType() ? WeaponMapper.getWeapon(gameEvent.getWeaponType())
          : Weapon.getWeaponForProjectile(
              WeaponMapper.getWeaponProjectile(gameEvent.getProjectile().getProjectileType()));
      AchievementFactory.create(weapon, killStats)
          .ifPresent(achievement -> playScreen.getNarratorSoundQueue()
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
                if (gameEvent.hasWeaponType()) {
                  enemyPlayer.attack(WeaponMapper.getWeapon(gameEvent.getWeaponType()), false);
                } else if (gameEvent.hasProjectile()) {
                  playScreen.getGame().getEntMan().addEntity(
                      enemyPlayerProjectileBoomFactoriesRegistry.get(
                              WeaponMapper.getWeaponProjectile(
                                  gameEvent.getProjectile().getProjectileType()))
                          .create(
                              Converter.convertToVector2(gameEvent.getProjectile().getPosition()),
                              playScreen.getPlayer()));
                }
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
            .getUserSettingSound(SoundRegistry.ENEMY_DEATH_SOUND_SEQ.getNext())
            .play(enemy.getSFXVolume(), enemy.getSFXPan()))
        .onGetShot(enemy -> new TimeLimitedSound(playScreen.getGame().getAssMan()
            .getUserSettingSound(SoundRegistry.ENEMY_GET_HIT_SOUND_SEQ.getNext()))
            .play(enemy.getSFXVolume(), enemy.getSFXPan(), 1500))
        .onAttack(enemyWeapon -> {
              var enemy = enemyWeapon.getEnemy();
              var sound = switch (enemyWeapon.getWeapon()) {
                case SHOTGUN -> SoundRegistry.ENEMY_SHOTGUN;
                case GAUNTLET -> SoundRegistry.ENEMY_PUNCH_HIT;
                case RAILGUN -> SoundRegistry.ENEMY_RAILGUN;
                case MINIGUN -> SoundRegistry.ENEMY_MINIGUN;
                case ROCKET_LAUNCHER -> SoundRegistry.ENEMY_ROCKET_LAUNCHER;
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

  private void diePlayer(final String killedBy) {
    playScreen.getPlayer().die(killedBy);
    playScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry
        .VOICE_GET_HIT_SOUND_SEQ.getNext()).play(Constants.PLAYER_FX_VOLUME);
    playScreen.getGame().getAssMan()
        .getUserSettingSound(SoundRegistry.LOOSING_SOUND_SEQ.getNext())
        .play(Constants.MK_NARRATOR_FX_VOLUME);
    playScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry.BELL)
        .play(Constants.DEFAULT_SFX_VOLUME);
  }

  private Vector2 getPlayerBoomPosition() {
    var currentPosition = playScreen.getPlayer().getCurrent2DPosition();
    var currentDirection = playScreen.getPlayer().getCurrent2DDirection();
    return new Vector2(
        currentPosition.x + Constants.PLAYER_RECT_SIZE / 2 + currentDirection.x * 0.25f,
        currentPosition.y + Constants.PLAYER_RECT_SIZE / 2 + currentDirection.y * 0.25f);
  }

  private static Vector2 createVector(Vector serverVector) {
    return new Vector2(serverVector.getX(), serverVector.getY());
  }
}