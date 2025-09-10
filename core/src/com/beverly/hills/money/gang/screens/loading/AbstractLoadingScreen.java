package com.beverly.hills.money.gang.screens.loading;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.screens.menu.AbstractMainMenuScreen;
import com.beverly.hills.money.gang.screens.menu.ErrorScreen;
import com.beverly.hills.money.gang.screens.menu.MainMenuScreen;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractLoadingScreen extends AbstractMainMenuScreen {

  private final long LOAD_UNTIL_MLS = System.currentTimeMillis() + 15_000;
  private static final long LOADING_ANIMATION_MLS = 250;
  private static final int MAX_LOADING_DOTS = 3;
  private final AtomicBoolean stopLoading = new AtomicBoolean();
  private final BitmapFont guiFont64;
  private long loadingAnimationSwitchMls = 0;
  private String loadingDots = "";

  public AbstractLoadingScreen(final DaiKombatGame game) {
    super(game);
    guiFont64 = getGame().getAssMan().getFont(FontRegistry.FONT_64);
  }


  @Override
  public void handleInput(final float delta) {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      removeAllEntities();
      onEscape();
      getGame().setScreen(new MainMenuScreen(getGame()));
    }
  }

  protected abstract void onEscape();


  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();
    if (!stopLoading.get()) {
      String loadingMsg = getLoadingMessage();
      GlyphLayout glyphLayoutLoading = new GlyphLayout(guiFont64, loadingMsg);
      guiFont64.draw(getGame().getBatch(), loadingMsg,
          getViewport().getWorldWidth() / 2f - glyphLayoutLoading.width / 2f,
          getLogoYOffset() - glyphLayoutLoading.height / 2f);
    }
    if (System.currentTimeMillis() > LOAD_UNTIL_MLS) {
      stopLoading.set(true);
      onTimeout();
      removeAllEntities();
      getGame().setScreen(new ErrorScreen(getGame(), "Loading timeout"));
    } else {
      onLoadingRender(delta);
    }
    getGame().getBatch().end();
  }

  protected abstract void onTimeout();

  protected abstract void onLoadingRender(final float delta);

  private String getLoadingMessage() {
    if (System.currentTimeMillis() >= loadingAnimationSwitchMls) {
      loadingAnimationSwitchMls = System.currentTimeMillis() + LOADING_ANIMATION_MLS;
      if (loadingDots.length() == MAX_LOADING_DOTS) {
        loadingDots = "";
      } else {
        loadingDots += ".";
      }
    }
    return getBaseLoadingMessage() + loadingDots;
  }

  protected String getBaseLoadingMessage() {
    return Constants.CONNECTING;
  }

}
