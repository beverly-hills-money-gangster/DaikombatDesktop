package com.beverly.hills.money.gang.handler;

import static com.beverly.hills.money.gang.Constants.DEFAULT_ENEMY_Y;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.AttackingSound;
import com.beverly.hills.money.gang.entities.enemies.Enemy;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayer;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayerAction;
import com.beverly.hills.money.gang.entities.enemies.EnemyPlayerActionType;
import com.beverly.hills.money.gang.entities.ui.UILeaderBoard;
import com.beverly.hills.money.gang.proto.ServerResponse;
import com.beverly.hills.money.gang.registry.EnemiesRegistry;
import com.beverly.hills.money.gang.screens.PlayScreen;
import com.beverly.hills.money.gang.utils.Converter;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class PlayScreenGameConnectionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PlayScreenGameConnectionHandler.class);

  private static final int EVENTS_TO_POLL = 10;

  private final PlayScreen playScreen;

  private boolean initialRequestHandled;

  private final EnemiesRegistry enemiesRegistry = new EnemiesRegistry();

  public void handle() {
    if (playScreen.isExiting()) {
      LOG.info("Stop handling");
      return;
    }
    playScreen.getGameConnection().getResponse().poll(EVENTS_TO_POLL).forEach(serverResponse -> {
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
      }
    });

    playScreen.getGameConnection().getErrors().poll()
        .ifPresent(this::handleException);
    if (!initialRequestHandled) {
      initialRequestHandled = true;
    }
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
        case SPAWN -> handleSpawn(gameEvent);
        case EXIT -> handleExit(gameEvent);
        case KILL_PUNCHING, KILL_SHOOTING -> handleDeath(gameEvent);
        case MOVE -> handleMove(gameEvent);
        case SHOOT, PUNCH -> handleAttackMiss(gameEvent);
        case GET_SHOT, GET_PUNCHED -> handleGetHit(gameEvent);
      }
    });
  }

  private void handleGameOver(ServerResponse.GameOver gameOver) {
    playScreen.setGameOver(true);
    playScreen.getUiLeaderBoard().set(gameOver.getLeaderBoard().getItemsList().stream()
        .map(leaderBoardItem -> UILeaderBoard.LeaderBoardPlayer.builder()
            .name(leaderBoardItem.getPlayerName())
            .id(leaderBoardItem.getPlayerId())
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
      // this might happen when players spawn at the same time
      LOG.info("Player already spawned {}", gameEvent);
      return;
    }
    EnemyPlayer enemyPlayer = new EnemyPlayer(playScreen.getPlayer(),
        gameEvent.getPlayer().getPlayerId(),
        new Vector3(gameEvent.getPlayer().getPosition().getX(),
            DEFAULT_ENEMY_Y, gameEvent.getPlayer().getPosition().getY()),
        new Vector2(gameEvent.getPlayer().getDirection().getX(),
            gameEvent.getPlayer().getDirection().getY()),
        playScreen, gameEvent.getPlayer().getPlayerName(),
        getEnemyTexture(gameEvent.getPlayer().getSkinColor()), createEnemyListeners());
    enemyPlayer.getEnemyEffects().invisible(15_000);
    enemyPlayer.getEnemyEffects().quadDamage(20_000);
    gameEvent.getPlayer().getActivePowerUpsList().forEach(gamePowerUp -> {
      switch (gamePowerUp.getType()) {
        case QUAD_DAMAGE -> {
          enemyPlayer.getEnemyEffects().quadDamage(gamePowerUp.getLastsForMls());
          playScreen.removeQuadDamageOrb();
        }
        default -> throw new IllegalArgumentException(
            "Not supported power-up type " + gamePowerUp.getType());
      }
    });

    playScreen.getGame().getEntMan().addEntity(enemyPlayer);
    enemiesRegistry.addEnemy(gameEvent.getPlayer().getPlayerId(), enemyPlayer);

    playScreen.getUiLeaderBoard().addNewPlayer(UILeaderBoard.LeaderBoardPlayer.builder()
        .name(enemyPlayer.getName())
        .id(enemyPlayer.getEnemyPlayerId())
        .kills(0)
        .build());
    if (initialRequestHandled) {
      playScreen.getGame().getAssMan()
          .getUserSettingSound(SoundRegistry.SPAWN_SOUND_SEQ.getNextSound())
          .play(enemyPlayer.getSFXVolume());
    }
  }

  private TexturesRegistry getEnemyTexture(ServerResponse.PlayerSkinColor playerSkinColor) {
    switch (playerSkinColor) {
      case BLUE -> {
        return TexturesRegistry.ENEMY_PLAYER_SPRITES_BLUE;
      }
      case PURPLE -> {
        return TexturesRegistry.ENEMY_PLAYER_SPRITES_PURPLE;
      }
      case PINK -> {
        return TexturesRegistry.ENEMY_PLAYER_SPRITES_PINK;
      }
      case GREEN -> {
        return TexturesRegistry.ENEMY_PLAYER_SPRITES_GREEN;
      }
      case ORANGE -> {
        return TexturesRegistry.ENEMY_PLAYER_SPRITES_ORANGE;
      }
      case YELLOW -> {
        return TexturesRegistry.ENEMY_PLAYER_SPRITES_YELLOW;
      }
      default -> throw new IllegalStateException("Not supported skin color " + playerSkinColor);
    }
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
      gameEvent.getPlayer().getActivePowerUpsList().forEach(gamePowerUp -> {
        switch (gamePowerUp.getType()) {
          case QUAD_DAMAGE -> {
            playScreen.getGame().getAssMan()
                .getUserSettingSound(SoundRegistry.QUAD_DAMAGE_PICK)
                .play(Constants.DEFAULT_SFX_VOLUME);
            LOG.info("Picked up quad-damage. Lasts for {}", gamePowerUp.getLastsForMls());
            playScreen.getPlayer().getPlayerEffects().quadDamage(gamePowerUp.getLastsForMls());
            playScreen.removeQuadDamageOrb();
          }
          default -> throw new IllegalArgumentException(
              "Not supported power-up type " + gamePowerUp.getType());
        }
      });
      return;
    }
    enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
        .ifPresent(enemyPlayer -> {
          gameEvent.getPlayer().getActivePowerUpsList().forEach(gamePowerUp -> {
            switch (gamePowerUp.getType()) {
              case QUAD_DAMAGE -> {
                playScreen.getGame().getAssMan()
                    .getUserSettingSound(SoundRegistry.ENEMY_QUAD_DAMAGE_PICK)
                    .play(enemyPlayer.getSFXVolume(), enemyPlayer.getSFXPan());
                enemyPlayer.getEnemyEffects().quadDamage(gamePowerUp.getLastsForMls());
                playScreen.removeQuadDamageOrb();
              }
              default -> throw new IllegalArgumentException(
                  "Not supported power-up type " + gamePowerUp.getType());
            }
          });

          enemyPlayer.queueAction(EnemyPlayerAction.builder()
              .enemyPlayerActionType(EnemyPlayerActionType.MOVE)
              .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
              .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition())).build());
        });
  }

  private void handleAttackMiss(ServerResponse.GameEvent gameEvent) {
    if (gameEvent.getPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {
      return;
    }
    EnemyPlayerActionType enemyPlayerActionType;
    switch (gameEvent.getEventType()) {
      // if we missed the punch then we just move to the position
      case PUNCH -> enemyPlayerActionType = EnemyPlayerActionType.MOVE;
      case SHOOT -> enemyPlayerActionType = EnemyPlayerActionType.SHOOT;
      default -> throw new IllegalArgumentException(
          "Not supported event type " + gameEvent.getEventType());
    }
    enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
        .ifPresent(enemyPlayer -> enemyPlayer.queueAction(EnemyPlayerAction.builder()
            .enemyPlayerActionType(enemyPlayerActionType)
            .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
            .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition())).build()));
  }

  private void handleGetHit(ServerResponse.GameEvent gameEvent) {
    EnemyPlayerActionType enemyPlayerActionType;
    switch (gameEvent.getEventType()) {
      case GET_SHOT -> enemyPlayerActionType = EnemyPlayerActionType.SHOOT;
      case GET_PUNCHED -> enemyPlayerActionType = EnemyPlayerActionType.PUNCH;
      default -> throw new IllegalArgumentException(
          "Not supported event type " + gameEvent.getEventType());
    }

    // if I hit somebody, then do nothing. the animation is played one client immediately

    // if I get hit
    if (gameEvent.getAffectedPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {

      enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
          .ifPresent(enemyPlayer -> {
            enemyPlayer.queueAction(EnemyPlayerAction.builder()
                .enemyPlayerActionType(enemyPlayerActionType)
                .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
                .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
                .onComplete(() -> {
                  playScreen.getPlayer().getHit(gameEvent.getAffectedPlayer().getHealth());
                  playScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry
                      .GET_HIT_SOUND_SEQ.getNextSound()).play(Constants.PLAYER_FX_VOLUME);
                })
                .build());
          });
    } else if (gameEvent.getPlayer().getPlayerId() != playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {

      // enemies hitting each other
      enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
          .ifPresent(enemyPlayer -> enemyPlayer.queueAction(EnemyPlayerAction.builder()
              .enemyPlayerActionType(enemyPlayerActionType)
              .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
              .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
              .onComplete(
                  () -> enemiesRegistry.getEnemy(gameEvent.getAffectedPlayer().getPlayerId())
                      .ifPresent(EnemyPlayer::getHit))
              .build()));

    }
  }


  private void handleDeath(ServerResponse.GameEvent gameEvent) {
    EnemyPlayerActionType enemyPlayerActionType;
    switch (gameEvent.getEventType()) {
      case KILL_PUNCHING -> enemyPlayerActionType = EnemyPlayerActionType.PUNCH;
      case KILL_SHOOTING -> enemyPlayerActionType = EnemyPlayerActionType.SHOOT;
      default -> throw new IllegalArgumentException(
          "Not supported event type " + gameEvent.getEventType());
    }

    playScreen.getUiLeaderBoard().registerKill(gameEvent.getPlayer().getPlayerId(),
        gameEvent.getAffectedPlayer().getPlayerId());
    if (gameEvent.getAffectedPlayer().getPlayerId() == playScreen.getPlayerConnectionContextData()
        .getPlayerId()) {
      String killedBy = enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
          .map(EnemyPlayer::getName).orElse("killer");

      enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
          .ifPresent(enemyPlayer -> enemyPlayer.queueAction(EnemyPlayerAction.builder()
              .enemyPlayerActionType(enemyPlayerActionType)
              .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
              .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
              .onComplete(() -> {
                playScreen.getPlayer().die(killedBy);
                playScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry
                    .GET_HIT_SOUND_SEQ.getNextSound()).play(Constants.PLAYER_FX_VOLUME);
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
      victimPlayerOpt.ifPresent(EnemyPlayer::die);
      int oldHealth = playScreen.getPlayer().getCurrentHP();
      int newHealth = gameEvent.getPlayer().getHealth();
      playScreen.getPlayer().buffHp(newHealth);
      int buff = newHealth - oldHealth;
      playScreen.getMyPlayerKillLog()
          .myPlayerKill(victimPlayerOpt.map(EnemyPlayer::getName).orElse("victim"), buff);
      int kills = playScreen.getUiLeaderBoard().getMyKills();
      if (kills > 1 && kills % 5 == 0) {
        playScreen.getNarratorSoundQueue().addSound(
            playScreen.getGame().getAssMan()
                .getUserSettingSound(SoundRegistry.WINNING_SOUND_SEQ.getNextSound()));
      }
    } else {
      enemiesRegistry.getEnemy(gameEvent.getPlayer().getPlayerId())
          .ifPresent(enemyPlayer -> enemyPlayer.queueAction(EnemyPlayerAction.builder()
              .enemyPlayerActionType(enemyPlayerActionType)
              .direction(Converter.convertToVector2(gameEvent.getPlayer().getDirection()))
              .route(Converter.convertToVector2(gameEvent.getPlayer().getPosition()))
              .onComplete(() -> enemiesRegistry.removeEnemy(
                  gameEvent.getAffectedPlayer().getPlayerId()).ifPresent(EnemyPlayer::die))
              .build()));
    }
  }

  public void handleErrorEvent(ServerResponse.ErrorEvent errorEvent) {
    playScreen.setErrorMessage(errorEvent.getMessage());
  }

  public void handlePowerUpSpawn(ServerResponse.PowerUpSpawnEvent powerUpSpawnEvent) {
    switch (powerUpSpawnEvent.getType()) {
      case QUAD_DAMAGE -> playScreen.spawnQuadDamage(
          new Vector2(powerUpSpawnEvent.getPosition().getX(),
              powerUpSpawnEvent.getPosition().getY()));
      default -> throw new IllegalStateException(
          "Not supported power-up type " + powerUpSpawnEvent.getType());
    }
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
        .onGetShot(enemy -> playScreen.getGame().getAssMan()
            .getUserSettingSound(SoundRegistry.ENEMY_GET_HIT_SOUND_SEQ.getNextSound())
            .play(enemy.getSFXVolume(), enemy.getSFXPan()))
        .onShooting(enemy -> {
              new AttackingSound(
                  playScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry.ENEMY_SHOTGUN))
                  .play(enemy.getSFXVolume(), enemy.getSFXPan(),
                      enemy.getEnemyEffects().isQuadDamageEffectActive() ? playScreen.getGame()
                          .getAssMan()
                          .getUserSettingSound(SoundRegistry.ENEMY_QUAD_DAMAGE_ATTACK) : null);
            }
        ).onPunching(enemy -> new AttackingSound(
            playScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry.ENEMY_PUNCH_HIT))
            .play(enemy.getSFXVolume(), enemy.getSFXPan()))
        .build();
  }

}
