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
import com.beverly.hills.money.gang.screens.data.ConnectGameData;
import com.beverly.hills.money.gang.validator.HostPortValidator;
import com.beverly.hills.money.gang.validator.Validator;
import java.util.Locale;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class EnterServerAddressScreen extends AbstractMainMenuScreen {

  private static final int MAX_SERVER_NAME_LEN = 32;

  private final Validator<String> hostPortValidator = new HostPortValidator();

  private static final String INSTRUCTION = "HOST:PORT COMBINATION MUST BE PROVIDED. USE CTRL+V TO COPY-PASTE";
  private static final String ENTER_SERVER_NAME_MSG = "ENTER HOST AND PORT";

  private String errorMessage;

  private final BitmapFont guiFont64;

  private final BitmapFont guiFont32;
  private final UserSettingSound boomSound2;
  private final TextInputProcessor nameTextInputProcessor;

  public EnterServerAddressScreen(final DaiKombatGame game) {
    super(game);
    guiFont64 = getGame().getAssMan().getFont(FontRegistry.FONT_64);
    guiFont32 = getGame().getAssMan().getFont(FontRegistry.FONT_32);
    boomSound2 = getGame().getAssMan().getUserSettingSound(SoundRegistry.BOOM_2);
    nameTextInputProcessor = new TextInputProcessor(MAX_SERVER_NAME_LEN,
        () -> getGame().getAssMan().
            getUserSettingSound(SoundRegistry.TYPING_SOUND_SEQ.getNext())
            .play(Constants.DEFAULT_SFX_TYPING_VOLUME));
  }

  @Override
  public void handleInput(final float delta) {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && StringUtils.isNotBlank(
        nameTextInputProcessor.getText())) {
      var validationResult = hostPortValidator.validate(nameTextInputProcessor.getText());
      if (!validationResult.isValid()) {
        errorMessage = validationResult.getMessage();
        return;
      }
      String[] hostPort = nameTextInputProcessor.getText().toLowerCase(Locale.ENGLISH).split(":");
      removeAllEntities();
      boomSound2.play(Constants.DEFAULT_SFX_VOLUME);
      getGame().setScreen(new EnterYourNameScreen(getGame(),
          ConnectGameData.builder().serverHost(hostPort[0])
              .serverPort(NumberUtils.toInt(hostPort[1]))));
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      removeAllEntities();
      getGame().setScreen(new MainMenuScreen(getGame()));
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.V)
        && (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(
        Input.Keys.CONTROL_RIGHT))) {
      nameTextInputProcessor.append(Gdx.app.getClipboard().getContents());
    } else {
      nameTextInputProcessor.handleInput();
    }
  }

  private void renderError(String errorMessage) {
    GlyphLayout glyphLayoutTitle = new GlyphLayout(guiFont32, errorMessage);
    guiFont32.draw(getGame().getBatch(), errorMessage,
        getViewport().getWorldWidth() / 2f - glyphLayoutTitle.width / 2f,
        getViewport().getWorldHeight() / 2f - glyphLayoutTitle.height / 2f
            - Constants.LOGO_INDENT - Constants.MENU_OPTION_INDENT * 5);
  }


  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();

    Optional.ofNullable(errorMessage).ifPresent(this::renderError);

    GlyphLayout glyphLayoutEnterYourName = new GlyphLayout(guiFont64, ENTER_SERVER_NAME_MSG);
    guiFont64.draw(getGame().getBatch(), ENTER_SERVER_NAME_MSG,
        getViewport().getWorldWidth() / 2f - glyphLayoutEnterYourName.width / 2f,
        getViewport().getWorldHeight() / 2f - glyphLayoutEnterYourName.height / 2f
            - Constants.LOGO_INDENT);

    GlyphLayout glyphLayoutInstruction = new GlyphLayout(guiFont32, INSTRUCTION);
    guiFont32.draw(getGame().getBatch(), INSTRUCTION,
        getViewport().getWorldWidth() / 2f - glyphLayoutInstruction.width / 2f,
        getViewport().getWorldHeight() / 2f - glyphLayoutInstruction.height / 2f
            - Constants.LOGO_INDENT - Constants.MENU_OPTION_INDENT * 2);

    String playerNameInput = ">" + nameTextInputProcessor.getText();
    GlyphLayout glyphLayoutPlayerInput = new GlyphLayout(guiFont64, playerNameInput);
    guiFont64.draw(getGame().getBatch(), playerNameInput,
        getViewport().getWorldWidth() / 2f - glyphLayoutPlayerInput.width / 2f,
        getViewport().getWorldHeight() / 2f - glyphLayoutPlayerInput.height / 2f
            - Constants.LOGO_INDENT
            - Constants.MENU_OPTION_INDENT * 3);
    getGame().getBatch().end();
  }
}
