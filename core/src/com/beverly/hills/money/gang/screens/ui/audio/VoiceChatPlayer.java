package com.beverly.hills.money.gang.screens.ui.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.beverly.hills.money.gang.entity.VoiceChatPayload;
import com.beverly.hills.money.gang.network.GlobalGameConnection;
import com.beverly.hills.money.gang.network.GlobalGameConnection.VoiceChatConfigs;
import com.beverly.hills.money.gang.registry.EnemiesRegistry;
import com.beverly.hills.money.gang.screens.ui.selection.UserSettingsUISelection;
import com.beverly.hills.money.gang.strategy.EnemyPlayerActionQueueStrategy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceChatPlayer {

  private static final Logger LOG = LoggerFactory.getLogger(EnemyPlayerActionQueueStrategy.class);

  private AudioRecorder audioRecorder;

  private AudioDevice audioPlayer;

  private Thread audioRecorderThread;

  private Thread audioPlayerThread;

  private final String id = UUID.randomUUID().toString();

  private final AtomicReference<Long> recordedVoiceLastTime = new AtomicReference<>();

  private final AtomicBoolean exceptionThrown = new AtomicBoolean();

  private final GlobalGameConnection gameConnection;
  private final int playerId;
  private final EnemiesRegistry enemiesRegistry;

  private final VoiceChatConfigs voiceChatConfigs;

  private final AtomicBoolean stop = new AtomicBoolean();

  private final AtomicLong recordAudioUntil = new AtomicLong();

  private final AtomicReference<Float> normalizedAvgAmplitude = new AtomicReference<>(0f);

  private final int gameId;

  public VoiceChatPlayer(GlobalGameConnection gameConnection, int playerId,
      EnemiesRegistry enemiesRegistry, int gameId) {
    this.gameConnection = gameConnection;
    this.playerId = playerId;
    this.enemiesRegistry = enemiesRegistry;
    this.voiceChatConfigs = gameConnection.getVoiceChatConfigs();
    this.gameId = gameId;
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
    audioPlayer.setVolume(UserSettingsUISelection.SOUND.getState().getNormalized());

    audioRecorderThread = new Thread(() -> {
      short[] shortPCM = new short[voiceChatConfigs.getSampleSize()];
      try {
        while (!stop.get()) {
          synchronized (recordAudioUntil) {
            if (!isRecording()) {
              recordAudioUntil.wait();
              continue;
            }
          }
          audioRecorder.read(shortPCM, 0, shortPCM.length);
          normalizedAvgAmplitude.set(Math.min(1, getNormalizedAvgAmpl(shortPCM) * 16f));
          recordedVoiceLastTime.set(System.currentTimeMillis());
          gameConnection.write(
              VoiceChatPayload.builder().playerId(playerId)
                  .gameId(gameId)
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
            amplify(mixedPCM, 3.85f);
            shortPCMs.forEach(payload -> enemiesRegistry.getEnemy(payload.getPlayerId())
                .ifPresent(enemyPlayer -> enemyPlayer.talking(getAvgAmpl(payload.getPcm()))));
            audioPlayer.writeSamples(mixedPCM, 0, mixedPCM.length);
          }
        } catch (InterruptedException ignored) {
          LOG.info("Audio player interrupted");
          Thread.currentThread().interrupt();
          stop.set(true);
        } catch (Exception e) {
          LOG.error("Failed to play audio", e);
          exceptionThrown.set(true);
          stop.set(true);
        }
      }
    });
    audioPlayerThread.setDaemon(true);
    audioRecorderThread.setDaemon(true);
    audioRecorderThread.setName("Audio recorder");
    audioPlayerThread.setName("Audio player");
    audioRecorderThread.start();
    audioPlayerThread.start();
    LOG.info("Voice chat player {} has been initialized", id);
  }

  public void recordAudio(boolean record) {
    if (record) {
      synchronized (recordAudioUntil) {
        recordAudioUntil.set(System.currentTimeMillis() + 1_000);
        recordAudioUntil.notify();
      }
    }
  }

  private void amplify(short[] pcm, float ampl) {
    for (int i = 0; i < pcm.length; i++) {
      pcm[i] = (short) Math.max(Math.min(pcm[i] * ampl, Short.MAX_VALUE), Short.MIN_VALUE);
    }
  }

  private float getAvgAmpl(short[] pcm) {
    long total = 0;
    for (short value : pcm) {
      total += Math.abs(value);
    }
    return (float) Math.abs(total / pcm.length);
  }

  private float getNormalizedAvgAmpl(short[] pcm) {
    return Math.min(1, getAvgAmpl(pcm) / Short.MAX_VALUE);
  }

  public float getLastNormalizedAvgAmpl() {
    return normalizedAvgAmplitude.get();
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
    return System.currentTimeMillis() < recordAudioUntil.get();
  }

  public void stop() {
    LOG.info("Stop voice chat {}", id);
    stop.set(true);
    Optional.ofNullable(audioRecorder).ifPresent(AudioRecorder::dispose);
    Optional.ofNullable(audioPlayer).ifPresent(AudioDevice::dispose);
    Optional.ofNullable(audioPlayerThread).ifPresent(Thread::interrupt);
    Optional.ofNullable(audioRecorderThread).ifPresent(Thread::interrupt);
  }

}
