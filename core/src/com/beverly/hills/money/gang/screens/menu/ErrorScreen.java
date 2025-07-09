package com.beverly.hills.money.gang.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.screens.loading.JoinGameScreen;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorScreen extends AbstractMainMenuScreen {

  private static final Logger LOG = LoggerFactory.getLogger(JoinGameScreen.class);

  private final String errorMessage;
  private final BitmapFont guiFont32;

  private final BitmapFont guiFont64;

  public ErrorScreen(final DaiKombatGame game, final String errorMessage) {
    super(game);
    this.errorMessage = errorMessage.toUpperCase(Locale.ENGLISH);
    LOG.error("Go to error screen with error '{}'", errorMessage);
    guiFont32 = game.getAssMan().getFont(FontRegistry.FONT_32);
    guiFont64 = game.getAssMan().getFont(FontRegistry.FONT_64);
  }

  @Override
  public void handleInput(final float delta) {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      removeAllEntities();
      getGame().setScreen(new MainMenuScreen(getGame()));
    }
  }


  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();

    String errorTitle = "ERROR";
    GlyphLayout glyphLayoutTitle = new GlyphLayout(guiFont64, errorTitle);
    guiFont64.draw(getGame().getBatch(), errorTitle,
        getViewport().getWorldWidth() / 2f - glyphLayoutTitle.width / 2f,
        getViewport().getWorldHeight() / 2f - glyphLayoutTitle.height / 2f - Constants.LOGO_INDENT);

    GlyphLayout glyphLayoutErrorMsg = new GlyphLayout(guiFont32, errorMessage);
    guiFont32.draw(getGame().getBatch(), errorMessage,
        getViewport().getWorldWidth() / 2f - glyphLayoutErrorMsg.width / 2f,
        getViewport().getWorldHeight() / 2f - glyphLayoutErrorMsg.height / 2f
            - Constants.LOGO_INDENT - 64);

    getGame().getBatch().end();
  }


}
