package com.beverly.hills.money.gang.handler;

import static com.beverly.hills.money.gang.configs.Constants.DEFAULT_ENEMY_Y;
import static com.beverly.hills.money.gang.proto.WeaponType.PUNCH;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.SoundVolumeType;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound.TimeLimitSoundConf;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound.SoundConf;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.entities.achievement.AchievementFactory;
import com.beverly.hills.money.gang.entities.achievement.KillStats;
import com.beverly.hills.money.gang.entities.enemies.Enemy;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayer;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayerAction;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayerActionType;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.entities.ui.LeaderBoardPlayer;
import com.beverly.hills.money.gang.proto.ServerResponse;
import com.beverly.hills.money.gang.proto.ServerResponse.GameEvent.GameEventType;
import com.beverly.hills.money.gang.proto.ServerResponse.GamePowerUp;
import com.beverly.hills.money.gang.proto.ServerResponse.GamePowerUpType;
import com.beverly.hills.money.gang.proto.Vector;
import com.beverly.hills.money.gang.registry.EnemiesRegistry;
import com.beverly.hills.money.gang.registry.EnemyPlayerProjectileBoomFactoriesRegistry;
import com.beverly.hills.money.gang.registry.EnemyPlayerProjectileShootingFactoriesRegistry;
import com.beverly.hills.money.gang.screens.game.PlayScreen;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import com.beverly.hills.money.gang.screens.ui.taunt.GameTaunt;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponMapper;
import com.beverly.hills.money.gang.utils.Converter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        playScreen.getGameBootstrapData().getPlayerId(),
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
              .play(SoundConf.builder()
                  .volume(enemyPlayer.getSFXVolume().getVolume())
                  .pitch(enemyPlayer.getEnemyClass().getVoicePitch())
                  .pan(enemyPlayer.getSFXPan())
                  .build())
      );
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
    if (gameEvent.getPlayer().getPlayerId() == playScreen.getGameBootstrapData()
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
    playScreen.getUiLeaderBoard().set(LeaderBoardPlayer.createFromGameOver(gameOver));
  }

  private void handleSpawn(ServerResponse.GameEvent gameEvent) {
    if (gameEvent.getPlayer().getPlayerId() == playScreen.getGameBootstrapData()
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
        SkinUISelection.getSkinColor(gameEvent.getPlayer().getSkinColor()), createEnemyListeners(),
        // move a little faster. otherwise I see a little lag even with 0 ping
        gameEvent.getPlayer().getSpeed() * 1.1f,
        gameEvent.getPlayer().getHealth(),
        GamePlayerClass.createPlayerClass(gameEvent.getPlayer().getPlayerClass()),
        playScreen.getGameBootstrapData().getMaxVisibility());
    gameEvent.getPlayer().getActivePowerUpsList().forEach(
        gamePowerUp -> activateEnemyPowerUpOnSpawn(
            enemyPlayer, getPowerUpType(gamePowerUp.getType()), gamePowerUp.getLastsForMls()));

    playScreen.getGame().getEntMan().addEntity(enemyPlayer);
    playScreen.getGame().getEntMan().addEntity(enemyPlayer.getEnemyPlayerVoiceChatEffect());

    enemiesRegistry.addEnemy(gameEvent.getPlayer().getPlayerId(), enemyPlayer);

    playScreen.getUiLeaderBoard().addNewPlayer(LeaderBoardPlayer.builder()
        .name(enemyPlayer.getName())
        .ping(gameEvent.getPlayer().getPingMls())
        .id(enemyPlayer.getEnemyPlayerId())
        .playerClass(enemyPlayer.getEnemyClass())
        .skinUISelection(enemyPlayer.getSkinUISelection())
        .deaths(
            gameEvent.getPlayer().hasGameMatchStats() ? gameEvent.getPlayer().getGameMatchStats()
                .getDeaths() : 0)
        .kills(gameEvent.getPlayer().hasGameMatchStats() ? gameEvent.getPlayer().getGameMatchStats()
            .getKills() : 0)
        .build());

    if (gameEvent.getEventType() == GameEventType.JOIN) {
      playScreen.getChatLog()
          .addChatLog(gameEvent.getPlayer().getPlayerName() + " has joined the game");
    }
    if (gameEvent.getEventType() == GameEventType.JOIN
        || gameEvent.getEventType() == GameEventType.RESPAWN) {
      playScreen.getGame().getAssMan()
          .getUserSettingSound(SoundRegistry.SPAWN_SOUND_SEQ.getNext())
          .play(enemyPlayer.getSFXVolume(), enemyPlayer.getSFXPan());
    }
  }


  private void activateEnemyPowerUpOnSpawn(EnemyPlayer enemyPlayer, PowerUpType powerUpType,
      int lastsForMls) {
    enemyPlayer.getEnemyEffects().activatePowerUp(powerUpType, lastsForMls);
    playScreen.removePowerUp(powerUpType);
  }

  private void handleExit(ServerResponse.GameEvent gameEvent) {
    if (gameEvent.getPlayer().getPlayerId() == playScreen.getGameBootstrapData()
        .getPlayerId()) {
      return;
    }
    var playerName =
        gameEvent.getPlayer().hasPlayerName() ? gameEvent.getPlayer().getPlayerName() : null;
    Optional.ofNullable(playerName).ifPresent(
        name -> playScreen.getChatLog().addChatLog(name + " has left the game"));
    enemiesRegistry.removeEnemy(gameEvent.getPlayer().getPlayerId())
        .ifPresent(Enemy::destroy);
    playScreen.getUiLeaderBoard().removePlayer(gameEvent.getPlayer().getPlayerId());
  }

  private void handleMove(ServerResponse.GameEvent gameEvent) {
    if (gameEvent.getPlayer().getPlayerId() == playScreen.getGameBootstrapData()
        .getPlayerId()) {
      if (gameEvent.getPlayer().hasHealth()) {
        playScreen.getPlayer().setHP(gameEvent.getPlayer().getHealth());
      }
      gameEvent.getPlayer().getCurrentAmmoList()
          .forEach(playerCurrentWeaponAmmo -> playScreen.getPlayer()
              .setWeaponAmmo(WeaponMapper.getWeapon(playerCurrentWeaponAmmo.getWeapon()),
                  playerCurrentWeaponAmmo.getAmmo()));
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
      case BIG_AMMO -> PowerUpType.BIG_AMMO;
      case MEDIUM_AMMO -> PowerUpType.MEDIUM_AMMO;
      case BEAST -> PowerUpType.BEAST;
      case UNRECOGNIZED ->
          throw new IllegalArgumentException("Not supported power-up type " + powerUpType);
    };
  }


  private void handleAttackMiss(ServerResponse.GameEvent gameEvent) {
    if (gameEvent.getPlayer().getPlayerId() == playScreen.getGameBootstrapData()
        .getPlayerId()) {
      return;
    }
    EnemyPlayerActionType enemyPlayerActionType =
        gameEvent.hasWeaponType() ? switch (gameEvent.getWeaponType()) {
          // if we missed the punch then we just move to the position
          case PUNCH -> EnemyPlayerActionType.MOVE;
          case SHOTGUN, RAILGUN, MINIGUN, ROCKET_LAUNCHER, PLASMAGUN ->
              EnemyPlayerActionType.ATTACK;
          case UNRECOGNIZED -> throw new IllegalArgumentException(
              "Not supported weapon type " + gameEvent.getWeaponType());
        } : EnemyPlayerActionType.MOVE;

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
                      .play(TimeLimitSoundConf.builder()
                          .soundVolumeType(enemyPlayer.getSFXVolume())
                          .pan(enemyPlayer.getSFXPan()).build());
                } else {
                  enemyPlayer.attack(WeaponMapper.getWeapon(gameEvent.getWeaponType()), false);
                  Optional.ofNullable(
                          WeaponMapper.getWeapon(gameEvent.getWeaponType()).getProjectileRef())
                      .ifPresent(projectile -> {
                        playScreen.getGame().getEntMan()
                            .addEntity(
                                enemyPlayerProjectileShootingFactoriesRegistry.get(projectile)
                                    .create(Converter.convertToVector2(
                                            gameEvent.getPlayer().getPosition()),
                                        Converter.convertToVector2(
                                            gameEvent.getPlayer().getDirection()),
                                        playScreen.getPlayer()));
                      });
                }
              } else if (gameEvent.hasProjectile()) {
                var boomPosition = Converter.convertToVector2(
                    gameEvent.getProjectile().getBlowUpPosition());
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
    if (gameEvent.getPlayer().getPlayerId() == playScreen.getGameBootstrapData()
        .getPlayerId()) {
      // if I hit myself
      if (gameEvent.getAffectedPlayer().getPlayerId() == playScreen.getGameBootstrapData()
          .getPlayerId()) {
        playScreen.getPlayer().getHit(gameEvent.getAffectedPlayer().getHealth());
        playGetHitSound();
      } else {
        enemiesRegistry.getEnemy(gameEvent.getAffectedPlayer().getPlayerId())
            .ifPresent(enemyPlayer -> {
              if (!enemyPlayer.isVisible()) {
                playHitSound();
              }
              enemyPlayer.setHp(gameEvent.getAffectedPlayer().getHealth());
            });
      }
    } else if (gameEvent.getAffectedPlayer().getPlayerId()
        == playScreen.getGameBootstrapData().getPlayerId()) {
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
                  playGetHitSound();
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
                                        gameEvent.getProjectile().getBlowUpPosition()),
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

    if (gameEvent.getAffectedPlayer().getPlayerId() == playScreen.getGameBootstrapData()
        .getPlayerId()) {
      // if I get killed
      if (gameEvent.getPlayer().getPlayerId() == playScreen.getGameBootstrapData()
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
    } else if (gameEvent.getPlayer().getPlayerId() == playScreen.getGameBootstrapData()
        .getPlayerId()) {
      var victimPlayerOpt = enemiesRegistry.removeEnemy(
          gameEvent.getAffectedPlayer().getPlayerId());
      victimPlayerOpt.ifPresent(enemyPlayer -> {
        if (!enemyPlayer.isVisible()) {
          playHitSound();
        }
        enemyPlayer.die();
      });
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
          .ifPresent(killerPlayer -> killerPlayer.queueAction(EnemyPlayerAction.builder()
              .eventSequenceId(gameEvent.getSequence())
              .enemyPlayerActionType(enemyPlayerActionType)
              .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
              .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
              .onComplete(() -> {
                if (gameEvent.hasWeaponType()) {
                  killerPlayer.attack(WeaponMapper.getWeapon(gameEvent.getWeaponType()), false);
                } else if (gameEvent.hasProjectile()) {
                  playScreen.getGame().getEntMan().addEntity(
                      enemyPlayerProjectileBoomFactoriesRegistry.get(
                              WeaponMapper.getWeaponProjectile(
                                  gameEvent.getProjectile().getProjectileType()))
                          .create(Converter.convertToVector2(
                                  gameEvent.getProjectile().getBlowUpPosition()),
                              playScreen.getPlayer()));
                }
                enemiesRegistry.removeEnemy(gameEvent.getAffectedPlayer().getPlayerId())
                    .ifPresent(victimPlayer -> {
                      victimPlayer.die();
                      String killMessage;
                      if (victimPlayer.getEnemyPlayerId() == killerPlayer.getEnemyPlayerId()) {
                        killMessage = killerPlayer.getName() + " self-destructed";
                      } else {
                        killMessage = killerPlayer.getName() + " kills " + victimPlayer.getName();
                      }
                      playScreen.getChatLog().addChatLog(killMessage);
                    });
              })
              .build()));
    }
  }

  public void handleErrorEvent(ServerResponse.ErrorEvent errorEvent) {
    LOG.error("Error " + errorEvent.getMessage());
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
  }

  private Enemy.EnemyListeners createEnemyListeners() {
    return Enemy.EnemyListeners
        .builder()
        .onDeath(enemy -> playScreen.getGame().getAssMan()
            .getUserSettingSound(SoundRegistry.ENEMY_DEATH_SOUND_SEQ.getNext())
            .play(SoundConf.builder().volume(enemy.getSFXVolume().getVolume())
                .pitch(enemy.getEnemyClass().getVoicePitch())
                .pan(enemy.getSFXPan()).build()))
        .onGetShot(enemy -> new TimeLimitedSound(playScreen.getGame().getAssMan()
            .getUserSettingSound(SoundRegistry.ENEMY_GET_HIT_SOUND_SEQ.getNext()))
            .play(TimeLimitSoundConf.builder()
                .soundVolumeType(enemy.getSFXVolume())
                .pitch(enemy.getEnemyClass().getVoicePitch())
                .pan(enemy.getSFXPan())
                .frequencyMls(1500)
                .build()))
        .onAttack(enemyWeapon -> {
              var enemy = enemyWeapon.getEnemy();
              var sound = switch (enemyWeapon.getWeapon()) {
                case SHOTGUN -> SoundRegistry.ENEMY_SHOTGUN;
                case GAUNTLET -> SoundRegistry.ENEMY_PUNCH_HIT;
                case RAILGUN -> SoundRegistry.ENEMY_RAILGUN;
                case MINIGUN -> SoundRegistry.ENEMY_MINIGUN;
                case ROCKET_LAUNCHER -> SoundRegistry.ENEMY_ROCKET_LAUNCHER;
                case PLASMAGUN -> SoundRegistry.ENEMY_PLASMAGUN_FIRE;
              };
              new TimeLimitedSound(
                  playScreen.getGame().getAssMan().getUserSettingSound(sound))
                  .play(TimeLimitSoundConf.builder()
                      .soundVolumeType(enemyWeapon.isAttackingPlayer() ? SoundVolumeType.HIGH_LOUD
                          : enemy.getSFXVolume())
                      .pan(enemy.getSFXPan())
                      .extraSound(enemy.getEnemyEffects().isPowerUpActive(PowerUpType.QUAD_DAMAGE)
                          || enemy.getEnemyEffects().isPowerUpActive(PowerUpType.BEAST)
                          ? playScreen.getGame()
                          .getAssMan()
                          .getUserSettingSound(SoundRegistry.ENEMY_QUAD_DAMAGE_ATTACK) : null)
                      .build());
            }
        ).build();
  }

  private void diePlayer(final String killedBy) {
    playScreen.getPlayer().die(killedBy);
    playScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry
            .VOICE_GET_HIT_SOUND_SEQ.getNext())
        .play(SoundConf.builder().volume(Constants.PLAYER_FX_VOLUME)
            .pitch(playScreen.getPlayer().getPlayerClass().getVoicePitch())
            .build());
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


  private void playGetHitSound() {
    new TimeLimitedSound(
        playScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry
            .VOICE_GET_HIT_SOUND_SEQ.getNext())).play(
        TimeLimitSoundConf.builder()
            .soundVolumeType(SoundVolumeType.LOW_NORMAL)
            .pitch(playScreen.getPlayer().getPlayerClass().getVoicePitch())
            .frequencyMls(1_000)
            .build()
    );
    if (playScreen.getPlayer().getPlayerEffects().isPowerUpActive(PowerUpType.DEFENCE)
        || playScreen.getPlayer().getPlayerEffects().isPowerUpActive(PowerUpType.BEAST)) {
      new TimeLimitedSound(
          playScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry.DEFENCE_ON_HIT))
          .play(TimeLimitSoundConf.builder()
              .soundVolumeType(SoundVolumeType.HIGH_NORMAL)
              .frequencyMls(750)
              .build()
          );
    }
  }

  private void playHitSound() {
    new TimeLimitedSound(
        playScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry.HIT_SOUND))
        .play(TimeLimitSoundConf.builder()
            .soundVolumeType(SoundVolumeType.LOUD).frequencyMls(500)
            .build());
  }

  private static Vector2 createVector(Vector serverVector) {
    return new Vector2(serverVector.getX(), serverVector.getY());
  }
}