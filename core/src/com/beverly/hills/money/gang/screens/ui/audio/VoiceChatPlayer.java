package com.beverly.hills.money.gang.screens.ui.audio;

import static com.beverly.hills.money.gang.configs.Constants.HUD_ALPHA_CHANNEL;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.configs.KeyMappings;
import com.beverly.hills.money.gang.entity.VoiceChatPayload;
import com.beverly.hills.money.gang.network.GlobalGameConnection;
import com.beverly.hills.money.gang.network.GlobalGameConnection.VoiceChatConfigs;
import com.beverly.hills.money.gang.screens.GameScreen;
import com.beverly.hills.money.gang.screens.ui.selection.UserSettingsUISelection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceChatPlayer {

  private static final Logger LOG = LoggerFactory.getLogger(VoiceChatPlayer.class);

  private AudioRecorder audioRecorder;

  private AudioDevice audioPlayer;

  private Thread audioRecorderThread;

  private Thread audioPlayerThread;

  private final AtomicReference<Long> recordedVoiceLastTime = new AtomicReference<>();

  private final AtomicBoolean exceptionThrown = new AtomicBoolean();

  private final GlobalGameConnection gameConnection;
  private final int playerId;

  private final VoiceChatConfigs voiceChatConfigs;

  private final AtomicBoolean stop = new AtomicBoolean();

  private final AtomicLong recordAudioUntil = new AtomicLong();

  private final AtomicReference<Float> normalizedAvgAmplitude = new AtomicReference<>(0f);

  private final int gameId;
  private final GameScreen gameScreen;
  private final TextureRegion micTexture;

  private final Consumer<VoiceChatPayload> onPlayerTalking;

  public VoiceChatPlayer(
      GlobalGameConnection gameConnection,
      int playerId,
      int gameId,
      final GameScreen gameScreen,
      final Consumer<VoiceChatPayload> onPlayerTalking) {
    this.gameConnection = gameConnection;
    this.playerId = playerId;
    this.voiceChatConfigs = gameConnection.getVoiceChatConfigs();
    this.gameId = gameId;
    this.gameScreen = gameScreen;
    this.micTexture = gameScreen.getGame().getAssMan().getTextureRegion(TexturesRegistry.MIC);
    this.onPlayerTalking = onPlayerTalking;
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
          recordedVoiceLastTime.set(System.currentTimeMillis());
          audioRecorder.read(shortPCM, 0, shortPCM.length);
          normalizedAvgAmplitude.set(Math.min(1, getNormalizedAvgAmpl(shortPCM) * 16f));
          gameConnection.write(
              VoiceChatPayload.builder().playerId(playerId)
                  .gameId(gameId)
                  .pcm(shortPCM)
                  .build());
        }
      } catch (InterruptedException ignored) {
        LOG.info("Audio recorder interrupted");
        Thread.currentThread().interrupt();
        stop.set(true);
      } catch (Exception e) {
        LOG.error("Error while recording audio", e);
        exceptionThrown.set(true);
        stop.set(true);
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
            shortPCMs.forEach(onPlayerTalking);
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
    LOG.info("Voice chat player has been initialized");
  }

  private void recordAudio(boolean record) {
    if (record) {
      synchronized (recordAudioUntil) {
        recordAudioUntil.set(System.currentTimeMillis() + 1_000);
        recordAudioUntil.notify();
      }
    }
  }

  public void handleInput() {
    recordAudio(isVoiceChatMode());
  }

  public boolean isVoiceChatMode() {
    return Gdx.input.isKeyPressed(KeyMappings.TALK.getKey());
  }

  public void renderGui() {
    var game = gameScreen.getGame();
    var viewPort = gameScreen.getViewport();
    if (!failedToRecord()) {
      String recordingText = "VOICE RECORDING";
      var glyphLayoutRecording = new GlyphLayout(gameScreen.getUiFont(), recordingText);
      int micSize = 64;
      float ampl = getLastNormalizedAvgAmpl();
      // TODO refactor

      game.getBatch()
          .setColor(new Color(1, 1 - ampl, 1 - ampl, Math.max(0.1f, ampl)));
      game.getBatch().draw(micTexture,
          viewPort.getWorldWidth() / 2f - micSize / 2f,
          viewPort.getWorldHeight() / 2f - viewPort.getWorldHeight() / 4f,
          micSize,
          micSize);
      game.getBatch().setColor(Color.WHITE);
      gameScreen.getUiFont().setColor(1, 1, 1, HUD_ALPHA_CHANNEL);
      gameScreen.getUiFont().draw(game.getBatch(), recordingText,
          viewPort.getWorldWidth() / 2f - glyphLayoutRecording.width / 2,
          viewPort.getWorldHeight() / 2f - glyphLayoutRecording.height / 2f
              - viewPort.getWorldHeight() / 4f);
      gameScreen.getUiFont().setColor(Color.WHITE);
    } else {
      String recordingText = "FAILED TO RECORD VOICE";
      var glyphLayoutRecording = new GlyphLayout(gameScreen.getUiFont(), recordingText);
      gameScreen.getUiFont().setColor(Color.RED);
      gameScreen.getUiFont().draw(game.getBatch(), recordingText,
          viewPort.getWorldWidth() / 2f - glyphLayoutRecording.width / 2,
          viewPort.getWorldHeight() / 2f - glyphLayoutRecording.height / 2f
              - viewPort.getWorldHeight() / 4f);
      gameScreen.getUiFont().setColor(Color.WHITE);
    }
  }

  private void amplify(short[] pcm, float ampl) {
    for (int i = 0; i < pcm.length; i++) {
      pcm[i] = (short) Math.max(Math.min(pcm[i] * ampl, Short.MAX_VALUE), Short.MIN_VALUE);
    }
  }

  public static float getAvgAmpl(short[] pcm) {
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

  private boolean failedToRecord() {
    var lastTimeRecorded = recordedVoiceLastTime.get();
    return exceptionThrown.get() || lastTimeRecorded != null && isRecording() &&
        (System.currentTimeMillis() - lastTimeRecorded > 1_000);
  }


  private boolean isRecording() {
    return System.currentTimeMillis() < recordAudioUntil.get();
  }

  public void stop() {
    LOG.info("Stop voice chat");
    stop.set(true);
    Optional.ofNullable(audioPlayerThread).ifPresent(this::killWaitThread);
    Optional.ofNullable(audioRecorderThread).ifPresent(this::killWaitThread);
    // libgdx classes are generally not thread-safe
    // I don't want shoot myself in the leg again, so I'm actually waiting for
    // the threads to finish before disposing in the main thread
    Optional.ofNullable(audioRecorder).ifPresent(AudioRecorder::dispose);
    Optional.ofNullable(audioPlayer).ifPresent(AudioDevice::dispose);
  }

  private void killWaitThread(final Thread thread) {
    thread.interrupt();
    try {
      thread.join(500);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}
