package com.beverly.hills.money.gang.screens;

import static com.beverly.hills.money.gang.Constants.DEFAULT_SELECTION_INDENT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.screens.ui.weapon.Weapon;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ControlsScreen extends AbstractMainMenuScreen {

  private static final String[] CONTROLS_MAPPING = {
      "MOVE - WASD",
      "SHOOT - LEFT MOUSE CLICK/RIGHT ALT",
      "PUNCH - RIGHT MOUSE CLICK",
      "CHAT - TILDA",
      "LEADERBOARD - TAB",
      "Q, E, MOUSE SCROLL - NEXT/PREV WEAPON",
      Arrays.stream(Weapon.values())
          .map(weapon -> Keys.toString(weapon.getSelectKeyCode()))
          .collect(Collectors.joining(", "))
          + " - SELECT WEAPON"};
  private final BitmapFont guiFont64;

  public ControlsScreen(final DaiKombatGame game) {
    super(game);
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

    int indent = 0;
    for (String controlMapping : CONTROLS_MAPPING) {
      GlyphLayout glyphLayoutControlsMapping = new GlyphLayout(guiFont64, controlMapping);
      guiFont64.draw(getGame().getBatch(), controlMapping,
          getViewport().getWorldWidth() / 2f - glyphLayoutControlsMapping.width / 2f,
          getViewport().getWorldHeight() / 2f - glyphLayoutControlsMapping.height / 2f
              - Constants.LOGO_INDENT - indent);
      indent += DEFAULT_SELECTION_INDENT;
    }
    getGame().getBatch().end();
  }


}
