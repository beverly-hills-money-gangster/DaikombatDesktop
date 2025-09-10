package com.beverly.hills.money.gang.screens;

import static com.beverly.hills.money.gang.configs.Constants.MAX_CHAT_MSG_LEN;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.configs.KeyMappings;
import com.beverly.hills.money.gang.input.TextInputProcessor;
import com.beverly.hills.money.gang.log.ChatLog;
import com.beverly.hills.money.gang.network.GlobalGameConnection;
import com.beverly.hills.money.gang.proto.PushChatEventCommand;
import com.beverly.hills.money.gang.screens.data.GameBootstrapData;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

public class ChatBox {

  @Getter
  @Setter
  private boolean chatMode;

  private final TextInputProcessor chatTextInputProcessor;

  @Getter
  private final ChatLog chatLog;

  private final GameBootstrapData gameBootstrapData;

  private final Texture hudBlackTexture;

  private final GlobalGameConnection gameConnection;

  private final GameScreen gameScreen;

  public ChatBox(GameBootstrapData gameBootstrapData, GlobalGameConnection gameConnection,
      GameScreen gameScreen) {
    this.gameScreen = gameScreen;
    this.gameBootstrapData = gameBootstrapData;
    this.gameConnection = gameConnection;
    chatTextInputProcessor = new TextInputProcessor(MAX_CHAT_MSG_LEN,
        () -> gameScreen.getGame().getAssMan()
            .getUserSettingSound(SoundRegistry.TYPING_SOUND_SEQ.getNext())
            .play(Constants.DEFAULT_SFX_TYPING_VOLUME));

    chatLog = new ChatLog(
        () -> gameScreen.getGame().getAssMan().getUserSettingSound(SoundRegistry.PING)
            .play(Constants.DEFAULT_SFX_VOLUME));
    hudBlackTexture = gameScreen.createTexture(Color.BLACK);
  }


  public boolean handleChatInput() {

    if (Gdx.input.isKeyJustPressed(KeyMappings.CHAT.getKey())) {
      chatMode = !chatMode;
    }
    if (chatMode) {
      handleChatInputInternal();
      return true;
    }
    return false;
  }

  private void printBlackShadowText(final int x, final int y, final String text,
      final BitmapFont font, final float alphaChannel) {
    gameScreen.printShadowText(x, y, text, font, hudBlackTexture, alphaChannel);
  }

  private void handleChatInputInternal() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && StringUtils.isNotBlank(
        chatTextInputProcessor.getText())) {
      chatMode = false;
      gameConnection.write(PushChatEventCommand.newBuilder()
          .setMessage(chatTextInputProcessor.getText())
          .setPlayerId(gameBootstrapData.getPlayerId())
          .setGameId(gameBootstrapData.getCompleteJoinGameData().getGameRoomId())
          .build());
      chatLog.addMessage(
          gameBootstrapData.getCompleteJoinGameData().getJoinGameData().getPlayerName(),
          chatTextInputProcessor.getText());
      chatTextInputProcessor.clear();
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      chatMode = false;
      chatTextInputProcessor.clear();
    } else {
      chatTextInputProcessor.handleInput();
    }
  }


  public void renderChat() {
    if (chatMode) {
      printBlackShadowText(32, 128 - 16 + 64, ">" + chatTextInputProcessor.getText(),
          gameScreen.getUiFont(),
          0.5f);
    }
    if (chatLog.hasChatMessage()) {
      printBlackShadowText(32, 256, chatLog.getChatMessages(), gameScreen.getUiFont(), 0.15f);
    }
  }

  public void greetPlayers(final int players) {
    Optional.ofNullable(getGreeting(players)).ifPresent(chatLog::addChatLog);
  }

  static String getGreeting(int players) {
    if (players == 0) {
      return null;
    }
    String greeting;
    if (players == 1) {
      greeting = "JOINED CHAT.";
    } else {
      greeting = "YOU + " + (players - 1) + " MORE IN CHAT.";
    }
    return greeting + " PRESS " + KeyMappings.CHAT.getKeyName() + " TO TEXT";
  }

}
