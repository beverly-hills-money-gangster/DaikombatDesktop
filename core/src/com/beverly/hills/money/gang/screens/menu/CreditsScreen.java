package com.beverly.hills.money.gang.screens.menu;

import static com.beverly.hills.money.gang.configs.Constants.DEFAULT_SELECTION_INDENT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;

public class CreditsScreen extends AbstractMainMenuScreen {

  private static final String[] CREDITS = {
      "GAME IS FORK OF 'ULTRA NIGHTMARE' BY GITHUB.COM/PILZHERE",
      "CODING - GITHUB.COM/BEVERLY-HILLS-MONEY-GANGSTER",
      "TEXTURES - MINECRAFT, DOOM 2, DUKE NUKEM, HEXEN",
      "MUSIC AND SFX - MORTAL KOMBAT 3, QUAKE 3"};
  private final BitmapFont guiFont64;

  public CreditsScreen(final DaiKombatGame game) {
    super(game);
    showLogo = false;
    guiFont64 = game.getAssMan().getFont(FontRegistry.FONT_64);
  }

  @Override
  public void handleInput(final float delta) {
    if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
      removeAllEntities();
      getGame().setScreen(new MainMenuScreen(getGame()));
    }
  }


  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();

    int indent = 0;
    for (String credit : CREDITS) {
      GlyphLayout glyphLayoutControlsMapping = new GlyphLayout(guiFont64, credit);
      guiFont64.draw(getGame().getBatch(), credit,
          getViewport().getWorldWidth() / 2f - glyphLayoutControlsMapping.width / 2f,
          getViewport().getWorldHeight() / 2f - glyphLayoutControlsMapping.height / 2f - indent);
      indent += DEFAULT_SELECTION_INDENT + 16;
    }
    getGame().getBatch().end();
  }


}
