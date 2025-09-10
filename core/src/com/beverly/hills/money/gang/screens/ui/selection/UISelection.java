package com.beverly.hills.money.gang.screens.ui.selection;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.screens.GameScreen;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class UISelection<T> {

  private int selectedOption;

  @Getter
  private final List<T> selections;

  @Setter
  private int menuItemSize = Constants.MENU_OPTION_INDENT;

  public UISelection(T[] selections) {
    this(List.of(selections));
  }

  public UISelection(List<T> selections) {
    this.selections = selections;
  }

  public T getSelectedOption() {
    return selections.get(selectedOption);
  }

  public void next() {
    selectedOption--;
    if (selectedOption == -1) {
      selectedOption = selections.size() - 1;
    }
  }

  public void prev() {
    selectedOption++;
    if (selectedOption == selections.size()) {
      selectedOption = 0;
    }
  }

  public void render(
      final BitmapFont guiFont64,
      final GameScreen gameScreen,
      final int verticalIndent) {
    int indent = verticalIndent;
    for (int i = 0; i < selections.size(); i++) {
      var mainMenuUISelection = this.getSelections().get(i);
      var optionGlyph = new GlyphLayout(guiFont64, mainMenuUISelection.toString());
      final float optionX = gameScreen.getViewport().getWorldWidth() / 2f - optionGlyph.width / 2f;
      final float optionY = indent - optionGlyph.height / 2f;
      guiFont64.draw(gameScreen.getGame().getBatch(), mainMenuUISelection.toString(), optionX,
          optionY);

      indent -= menuItemSize;
      if (getSelectedOption() == mainMenuUISelection) {
        guiFont64.draw(gameScreen.getGame().getBatch(), Constants.SELECTED_OPTION_MARK,
            optionX - Constants.DEFAULT_SELECTION_INDENT, optionY);
      }
    }
  }

}
