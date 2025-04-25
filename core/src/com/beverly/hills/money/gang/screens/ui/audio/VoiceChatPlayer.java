package com.beverly.hills.money.gang.screens.ui.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.entity.VoiceChatPayload;
import com.beverly.hills.money.gang.network.GlobalGameConnection;
import com.beverly.hills.money.gang.network.GlobalGameConnection.VoiceChatConfigs;
import com.beverly.hills.money.gang.registry.EnemiesRegistry;
import com.beverly.hills.money.gang.screens.ui.selection.UserSettingsUISelection;
import com.beverly.hills.money.gang.strategy.EnemyPlayerActionQueueStrategy;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceChatPlayer {

  private static final Logger LOG = LoggerFactory.getLogger(EnemyPlayerActionQueueStrategy.class);

  private final AtomicBoolean recordAudio = new AtomicBoolean();

  private AudioRecorder audioRecorder;

  private AudioDevice audioPlayer;

  private Thread audioRecorderThread;

  private Thread audioPlayerThread;

  private final AtomicReference<Long> recordedVoiceLastTime = new AtomicReference<>();

  private final AtomicBoolean exceptionThrown = new AtomicBoolean();

  private final GlobalGameConnection gameConnection;
  private final int playerId;
  private final EnemiesRegistry enemiesRegistry;

  private final VoiceChatConfigs voiceChatConfigs;

  private final AtomicBoolean stop = new AtomicBoolean();

  public VoiceChatPlayer(GlobalGameConnection gameConnection, int playerId,
      EnemiesRegistry enemiesRegistry, boolean record) {
    this.gameConnection = gameConnection;
    this.playerId = playerId;
    this.enemiesRegistry = enemiesRegistry;
    recordAudio.set(record);
    this.voiceChatConfigs = gameConnection.getVoiceChatConfigs();
  }

  public void init() {
    try {

      this.audioRecorder = Gdx.audio.newAudioRecorder(voiceChatConfigs.getSampleRate(), true);
      this.audioPlayer = Gdx.audio.newAudioDevice(voiceChatConfigs.getSampleRate(), true);

    } catch (Exception e) {
      exceptionThrown.set(true);
      LOG.error("Can't record", e);
      return;
    }
    // max volume unless muted
    audioPlayer.setVolume(UserSettingsUISelection.SOUND.getState().getNormalized() == 0 ? 0 : 1);

    audioRecorderThread = new Thread(() -> {
      short[] shortPCM = new short[voiceChatConfigs.getSampleSize()];
      try {
        while (!stop.get()) {
          synchronized (recordAudio) {
            if (!recordAudio.get()) {
              recordAudio.wait();
              continue;
            }
          }
          audioRecorder.read(shortPCM, 0, shortPCM.length);
          recordedVoiceLastTime.set(System.currentTimeMillis());
          gameConnection.write(
              VoiceChatPayload.builder().playerId(playerId)
                  .gameId(Configs.GAME_ID)
                  .pcm(shortPCM)
                  .build());
        }
      } catch (InterruptedException ignored) {
        LOG.info("Audio recorder interrupted");
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        LOG.error("Error while recording audio", e);
        exceptionThrown.set(true);
      }
    }
    );
    audioPlayerThread = new Thread(() -> {
      int mlsToBlock = 100;
      var pcmSilence = new short[1];
      while (!stop.get()) {
        try {
          List<VoiceChatPayload> shortPCMs = gameConnection.pollPCMBlocking(mlsToBlock);
          if (shortPCMs.isEmpty()) {
            // play silence if no pcm
            audioPlayer.writeSamples(pcmSilence, 0, pcmSilence.length);
          } else {
            var mixedPCM = mixPCMs(shortPCMs);
            shortPCMs.forEach(payload -> enemiesRegistry.getEnemy(payload.getPlayerId())
                .ifPresent(enemyPlayer -> enemyPlayer.talking(getAvgAmpl(payload.getPcm()))));
            audioPlayer.writeSamples(mixedPCM, 0, mixedPCM.length);
          }
        } catch (InterruptedException ignored) {
          LOG.info("Audio player interrupted");
          Thread.currentThread().interrupt();
        } catch (Exception e) {
          LOG.error("Failed to play audio", e);
          exceptionThrown.set(true);
        }
      }
    });
    audioPlayerThread.setDaemon(true);
    audioRecorderThread.setDaemon(true);
    audioRecorderThread.setName("Audio recorder");
    audioPlayerThread.setName("Audio player");
    audioRecorderThread.start();
    audioPlayerThread.start();
  }

  public void recordAudio(boolean record) {
    synchronized (recordAudio) {
      recordAudio.set(record);
      recordAudio.notify();
    }
  }

  private float getAvgAmpl(short[] pcm) {
    long total = 0;
    for (short value : pcm) {
      total += value;
    }
    return (float) Math.abs(total / pcm.length);
  }

  private short[] mixPCMs(List<VoiceChatPayload> pcms) {
    short[] mixed = new short[pcms.get(0).getPcm().length];
    for (int i = 0; i < mixed.length; i++) {
      long sum = 0;
      for (var pcm : pcms) {
        sum += pcm.getPcm()[i];
      }
      mixed[i] = (short) (sum / pcms.size());
    }
    return mixed;
  }

  public boolean failedToRecord() {
    var lastTimeRecorded = recordedVoiceLastTime.get();
    return exceptionThrown.get() || lastTimeRecorded != null && isRecording() &&
        (System.currentTimeMillis() - lastTimeRecorded > 1_000);
  }


  public boolean isRecording() {
    return recordAudio.get();
  }

  public void stop() {
    LOG.info("Stop voice chat");
    stop.set(true);
    Optional.ofNullable(audioPlayerThread).ifPresent(Thread::interrupt);
    Optional.ofNullable(audioRecorderThread).ifPresent(Thread::interrupt);
    Optional.ofNullable(audioRecorder).ifPresent(AudioRecorder::dispose);
    Optional.ofNullable(audioPlayer).ifPresent(AudioDevice::dispose);
  }
}
