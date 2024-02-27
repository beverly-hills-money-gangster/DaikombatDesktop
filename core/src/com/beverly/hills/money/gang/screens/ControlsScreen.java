package com.beverly.hills.money.gang.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;

public class ControlsScreen extends AbstractMainMenuScreen {

    private static final String[] CONTROLS_MAPPING = {
            "MOVE - WASD",
            "SHOOT - LMC/RIGHT ALT",
            "PUNCH - RMC/RIGHT CTRL",
            "CHAT - TILDA",
            "LEADERBOARD - TAB"};
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
                    getViewport().getWorldHeight() / 2f - glyphLayoutControlsMapping.height / 2f - Constants.LOGO_INDENT - indent);
            indent += Constants.MENU_OPTION_INDENT;
        }
        getGame().getBatch().end();
    }


}
