package com.beverly.hills.money.gang.screens.ui.selection;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.screens.GameScreen;
import java.util.List;
import lombok.Getter;

public class UISelection<T extends Enum> {

  private int selectedOption;

  @Getter
  private final List<T> selections;

  public UISelection(T[] selections) {
    this.selections = List.of(selections);
  }

  public T getSelectedOption() {
    return selections.get(selectedOption);
  }

  public void up() {
    selectedOption--;
    if (selectedOption == -1) {
      selectedOption = selections.size() - 1;
    }
  }

  public void down() {
    selectedOption++;
    if (selectedOption == selections.size()) {
      selectedOption = 0;
    }
  }

  public void render(final BitmapFont guiFont64,
      final GameScreen gameScreen,
      final int verticalIndent) {
    int indent = verticalIndent;
    for (int i = 0; i < this.getSelections().size(); i++) {
      var mainMenuUISelection = this.getSelections().get(i);
      var optionGlyph = new GlyphLayout(guiFont64, mainMenuUISelection.toString());
      final float optionX = gameScreen.getViewport().getWorldWidth() / 2f - optionGlyph.width / 2f;
      final float optionY =
          gameScreen.getViewport().getWorldHeight() / 2f - optionGlyph.height / 2f - indent;
      guiFont64.draw(gameScreen.getGame().getBatch(), mainMenuUISelection.toString(), optionX,
          optionY);
      indent += Constants.MENU_OPTION_INDENT;
      if (getSelectedOption() == mainMenuUISelection) {
        guiFont64.draw(gameScreen.getGame().getBatch(), Constants.SELECTED_OPTION_MARK,
            optionX - Constants.DEFAULT_SELECTION_INDENT, optionY);
      }
    }
  }

}
