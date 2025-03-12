package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.screens.ui.selection.MainMenuUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.UISelection;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.FileUtils;

public class MainMenuScreen extends AbstractMainMenuScreen {

  private final BitmapFont guiFont64;
  private final UserSettingSound boomSound1;
  private final UserSettingSound dingSound1;

  private final int maxAudioPacketByteSize = 1000;

  private int totalBytesSent = 0;

  private final int samplingRate = 8000;

  private final AtomicBoolean recordAudio = new AtomicBoolean();

  private final AtomicReference<Double> avgAmplitudeRef = new AtomicReference<>();

  private AudioRecorder audioRecorder;

  private AudioDevice audioPlayer;

  private Thread audioRecorderThread;

  private Thread audioPlayerThread;

  private final BlockingQueue<short[]> audioData = new ArrayBlockingQueue<>(10);

  private final UISelection<MainMenuUISelection> menuSelection
      = new UISelection<>(MainMenuUISelection.values());


  public MainMenuScreen(final DaiKombatGame game) {
    super(game);
    guiFont64 = game.getAssMan().getFont(FontRegistry.FONT_64);

    boomSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.BOOM_1);
    dingSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.DING_1);

  }

  @Override
  public void show() {
    audioRecorder = Gdx.audio.newAudioRecorder(samplingRate, true);
    audioPlayer = Gdx.audio.newAudioDevice(samplingRate, true);
    audioRecorderThread = new Thread(() -> {
      short[] shortPCM;
      try {
        while (!Thread.currentThread().isInterrupted()) {
          System.out.println("Record loop");
          if (!recordAudio.get()) {
            continue;
          }
          shortPCM = new short[maxAudioPacketByteSize / 2];
          audioRecorder.read(shortPCM, 0, shortPCM.length);

          var avgAmplitude = getAvgAmplitude(shortPCM);
          avgAmplitudeRef.set(avgAmplitude);
          audioData.put(shortPCM);
          totalBytesSent += maxAudioPacketByteSize;
          System.out.println(FileUtils.byteCountToDisplaySize(totalBytesSent));
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    );
    audioPlayerThread = new Thread(() -> {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          short[] shortPCM = audioData.poll(50, TimeUnit.MILLISECONDS);
          if (shortPCM == null) {
            shortPCM = new short[maxAudioPacketByteSize / 2];
          }
          audioPlayer.writeSamples(shortPCM, 0, shortPCM.length);

        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });
    audioPlayerThread.setDaemon(true);
    audioRecorderThread.setDaemon(true);
    audioRecorderThread.start();
    audioPlayerThread.start();
  }

  @Override
  public void handleInput(final float delta) {

    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      boomSound1.play(Constants.DEFAULT_SFX_VOLUME);
      switch (menuSelection.getSelectedOption()) {
        case PLAY -> {
          removeAllEntities();
          getGame().setScreen(new ChooseServerScreen(getGame()));
        }
        case CONTROLS -> {
          removeAllEntities();
          getGame().setScreen(new ControlsScreen(getGame()));
        }
        case SETTINGS -> {
          removeAllEntities();
          getGame().setScreen(new SettingsScreen(getGame()));
        }
        case CREDITS -> {
          removeAllEntities();
          getGame().setScreen(new CreditsScreen(getGame()));
        }
        default -> Gdx.app.exit();

      }
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      removeAllEntities();
      getGame().setScreen(new SureExitScreen(getGame()));
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
      menuSelection.next();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      menuSelection.prev();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    }
    recordAudio.set(Gdx.input.isKeyPressed(Keys.V));
  }


  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();
    menuSelection.render(guiFont64, this, Constants.LOGO_INDENT);

    Optional.ofNullable(avgAmplitudeRef.get()).ifPresent(aDouble -> {
      String text = "MIC IS ON";
      float normalizedAmplitude = (float) normalizeAmplitude(aDouble) / 2f;
      guiFont64.setColor(1, 1, 1, normalizedAmplitude);
      GlyphLayout glyphLayoutInstruction = new GlyphLayout(guiFont64, text);
      guiFont64.draw(getGame().getBatch(), text,
          getViewport().getWorldWidth() / 2f - glyphLayoutInstruction.width / 2f,
          getViewport().getWorldHeight() / 2f - glyphLayoutInstruction.height / 2f - 128);
      guiFont64.setColor(Color.WHITE);
    });

    getGame().getBatch().end();
  }

  private double getAvgAmplitude(short[] data) {
    long sum = 0;
    for (short sample : data) {
      sum += Math.abs(sample); // Get absolute amplitude
    }
    return sum / data.length;
  }

  private double normalizeAmplitude(double amplitude) {
    return amplitude / Short.MAX_VALUE;
  }


  @Override
  public void dispose() {
    super.dispose();
    stopAudioRecording();
  }

  @Override
  public void hide() {
    super.hide();
    stopAudioRecording();
  }


  private void stopAudioRecording() {
    Optional.ofNullable(audioRecorderThread).ifPresent(Thread::interrupt);
    Optional.ofNullable(audioPlayerThread).ifPresent(Thread::interrupt);
    audioRecorder.dispose();
    audioPlayer.dispose();
  }


}
