package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.input.TextInputProcessor;
import com.beverly.hills.money.gang.screens.data.PlayerServerInfoContextData;
import org.apache.commons.lang3.StringUtils;

public class EnterServerPasswordScreen extends AbstractMainMenuScreen {

  private static final int MAX_PASS_LEN = 16;
  private static final String ENTER_PASSWORD_MSG = "ENTER SERVER PIN CODE";

  private final BitmapFont guiFont64;
  private final UserSettingSound boomSound2;
  private final TextInputProcessor passwordTextInputProcessor;

  private final PlayerServerInfoContextData.PlayerServerInfoContextDataBuilder playerServerInfoContextDataBuilder;

  public EnterServerPasswordScreen(final DaiKombatGame game,
      final PlayerServerInfoContextData.PlayerServerInfoContextDataBuilder playerServerInfoContextDataBuilder) {
    super(game);
    this.playerServerInfoContextDataBuilder = playerServerInfoContextDataBuilder;
    guiFont64 = getGame().getAssMan().getFont(FontRegistry.FONT_64);
    boomSound2 = getGame().getAssMan().getUserSettingSound(SoundRegistry.BOOM_2);
    passwordTextInputProcessor = new TextInputProcessor(MAX_PASS_LEN,
        () -> getGame().getAssMan().
            getUserSettingSound(SoundRegistry.TYPING_SOUND_SEQ.getNextSound())
            .play(Constants.DEFAULT_SFX_TYPING_VOLUME));
  }

  @Override
  public void handleInput(final float delta) {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && StringUtils.isNotEmpty(
        passwordTextInputProcessor.getText())) {
      removeAllEntities();
      boomSound2.play(Constants.DEFAULT_SFX_VOLUME);
      playerServerInfoContextDataBuilder.serverPassword(passwordTextInputProcessor.getText());
      getGame().setScreen(
          new JoinGameScreen(getGame(), playerServerInfoContextDataBuilder.build()));
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      removeAllEntities();
      getGame().setScreen(new MainMenuScreen(getGame()));
    } else {
      passwordTextInputProcessor.handleInput();
    }
  }


  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();
    GlyphLayout glyphLayoutEnterYourName = new GlyphLayout(guiFont64, ENTER_PASSWORD_MSG);
    guiFont64.draw(getGame().getBatch(), ENTER_PASSWORD_MSG,
        getViewport().getWorldWidth() / 2f - glyphLayoutEnterYourName.width / 2f,
        getViewport().getWorldHeight() / 2f - glyphLayoutEnterYourName.height / 2f
            - Constants.LOGO_INDENT);

    String playerPasswordInput = ">" + "*".repeat(passwordTextInputProcessor.getText().length());
    GlyphLayout glyphLayoutPlayerInput = new GlyphLayout(guiFont64, playerPasswordInput);
    guiFont64.draw(getGame().getBatch(), playerPasswordInput,
        getViewport().getWorldWidth() / 2f - glyphLayoutPlayerInput.width / 2f,
        getViewport().getWorldHeight() / 2f - glyphLayoutPlayerInput.height / 2f
            - Constants.LOGO_INDENT
            - Constants.MENU_OPTION_INDENT);
    getGame().getBatch().end();
  }
}
