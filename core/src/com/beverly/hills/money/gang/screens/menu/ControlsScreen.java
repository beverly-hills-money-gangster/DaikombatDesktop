package com.beverly.hills.money.gang.screens.menu;

import static com.beverly.hills.money.gang.configs.Constants.DEFAULT_SELECTION_INDENT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.configs.KeyMappings;
import java.util.List;

public class ControlsScreen extends AbstractMainMenuScreen {

  private static final List<String> CONTROLS_MAPPING = List.of(
      "MOVE - WASD",
      "SHOOT - LEFT MOUSE CLICK/RIGHT ALT",
      "PUNCH - RIGHT MOUSE CLICK/RIGHT CTRL",
      "CHAT - " + KeyMappings.CHAT.getKeyName(),
      "TAUNT - " + KeyMappings.TAUNT.getKeyName(),
      "LEADERBOARD - " + KeyMappings.LEADERBOARD.getKeyName(),
      "PUSH-TO-TALK - " + KeyMappings.TALK.getKeyName(),
      "NEXT/PREV WEAPON - MOUSE SCROLL, " + KeyMappings.NEXT_WEAPONS.getKeyName() + "/"
          + KeyMappings.PREV_WEAPONS.getKeyName(),
      "SWITCH WEAPON - NUMBERS");
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
          getLogoYOffset() - glyphLayoutControlsMapping.height / 2f - indent);
      indent += DEFAULT_SELECTION_INDENT;
    }
    getGame().getBatch().end();
  }


}
