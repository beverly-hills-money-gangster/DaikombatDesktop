package com.beverly.hills.money.gang.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.input.TextInputProcessor;
import com.beverly.hills.money.gang.screens.data.ConnectServerData;
import com.beverly.hills.money.gang.screens.data.JoinGameData;
import org.apache.commons.lang3.StringUtils;

public class EnterYourNameScreen extends AbstractMainMenuScreen {

  private static final int MAX_NAME_LEN = 16;
  private static final String ENTER_YOUR_NAME_MSG = "ENTER YOUR NAME";

  private final BitmapFont guiFont64;
  private final UserSettingSound boomSound2;
  private final TextInputProcessor nameTextInputProcessor;

  private final JoinGameData.JoinGameDataBuilder joinGameDataBuilder;

  private final ConnectServerData connectServerData;

  public EnterYourNameScreen(final DaiKombatGame game,
      final ConnectServerData connectServerData) {
    super(game);
    this.joinGameDataBuilder = JoinGameData.builder();
    this.connectServerData = connectServerData;
    guiFont64 = getGame().getAssMan().getFont(FontRegistry.FONT_64);
    boomSound2 = getGame().getAssMan().getUserSettingSound(SoundRegistry.BOOM_2);
    nameTextInputProcessor = new TextInputProcessor(MAX_NAME_LEN,
        () -> getGame().getAssMan().
            getUserSettingSound(SoundRegistry.TYPING_SOUND_SEQ.getNext())
            .play(Constants.DEFAULT_SFX_TYPING_VOLUME));
  }

  @Override
  public void handleInput(final float delta) {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && StringUtils.isNotBlank(
        nameTextInputProcessor.getText())) {
      removeAllEntities();
      boomSound2.play(Constants.DEFAULT_SFX_VOLUME);
      joinGameDataBuilder.playerName(nameTextInputProcessor.getText());
      getGame().setScreen(
          new ChoosePlayerClassScreen(getGame(), connectServerData, joinGameDataBuilder));
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      removeAllEntities();
      getGame().setScreen(new MainMenuScreen(getGame()));
    } else {
      nameTextInputProcessor.handleInput();
    }
  }


  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();
    GlyphLayout glyphLayoutEnterYourName = new GlyphLayout(guiFont64, ENTER_YOUR_NAME_MSG);
    guiFont64.draw(getGame().getBatch(), ENTER_YOUR_NAME_MSG,
        getViewport().getWorldWidth() / 2f - glyphLayoutEnterYourName.width / 2f,
        getLogoYOffset() - glyphLayoutEnterYourName.height / 2f);

    String playerNameInput = ">" + nameTextInputProcessor.getText();
    GlyphLayout glyphLayoutPlayerInput = new GlyphLayout(guiFont64, playerNameInput);
    guiFont64.draw(getGame().getBatch(), playerNameInput,
        getViewport().getWorldWidth() / 2f - glyphLayoutPlayerInput.width / 2f,
        getLogoYOffset() - glyphLayoutPlayerInput.height / 2f - Constants.MENU_OPTION_INDENT);
    getGame().getBatch().end();
  }
}
