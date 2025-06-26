package com.beverly.hills.money.gang.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.screens.menu.AbstractMainMenuScreen;
import com.beverly.hills.money.gang.screens.menu.MainMenuScreen;
import com.beverly.hills.money.gang.screens.ui.selection.SureExitUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.UISelection;

public class SureExitScreen extends AbstractMainMenuScreen {

  private final BitmapFont guiFont64;
  private final UserSettingSound boomSound1;
  private final UserSettingSound dingSound1;

  private static final String SURE_EXIT_MSG = "SURE WANT TO QUIT?";
  private final UISelection<SureExitUISelection> selection = new UISelection<>(
      SureExitUISelection.values());


  public SureExitScreen(final DaiKombatGame game) {
    super(game);
    guiFont64 = game.getAssMan().getFont(FontRegistry.FONT_64);

    boomSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.BOOM_1);
    dingSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.DING_1);
    showLogo = false;
  }

  @Override
  public void handleInput(final float delta) {

    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      boomSound1.play(Constants.DEFAULT_SFX_VOLUME);
      switch (selection.getSelectedOption()) {
        case YES -> Gdx.app.exit();
        case NO -> {
          removeAllEntities();
          getGame().setScreen(new MainMenuScreen(getGame()));
        }
      }
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      Gdx.app.exit();
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
      selection.next();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      selection.prev();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    }
  }


  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();

    GlyphLayout glyphLayoutEnterYourClass = new GlyphLayout(guiFont64, SURE_EXIT_MSG);
    guiFont64.draw(getGame().getBatch(), SURE_EXIT_MSG,
        getViewport().getWorldWidth() / 2f - glyphLayoutEnterYourClass.width / 2f,
        getViewport().getWorldHeight() / 2f - glyphLayoutEnterYourClass.height / 2f
            - Constants.LOGO_INDENT + 64);

    selection.render(guiFont64, this, Constants.LOGO_INDENT);
    getGame().getBatch().end();
  }

}
