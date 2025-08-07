package com.beverly.hills.money.gang.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.screens.loading.JoinGameScreen;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorScreen extends AbstractMainMenuScreen {

  private static final Logger LOG = LoggerFactory.getLogger(JoinGameScreen.class);

  private final String errorMessage;

  public ErrorScreen(final DaiKombatGame game, final String errorMessage) {
    super(game);
    this.errorMessage = errorMessage.toUpperCase(Locale.ENGLISH);
    this.showLogo = false;
    LOG.error("Go to error screen with error '{}'", errorMessage);
  }

  @Override
  public void handleInput(final float delta) {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
        || Gdx.input.isKeyJustPressed(Keys.ENTER)) {
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
        getLogoYOffset() - glyphLayoutTitle.height / 2f);

    GlyphLayout glyphLayoutErrorMsg = new GlyphLayout(guiFont32, errorMessage);
    guiFont32.draw(getGame().getBatch(), errorMessage,
        getViewport().getWorldWidth() / 2f - glyphLayoutErrorMsg.width / 2f,
        getLogoYOffset() - glyphLayoutErrorMsg.height / 2f - 64);

    getGame().getBatch().end();
  }


}
