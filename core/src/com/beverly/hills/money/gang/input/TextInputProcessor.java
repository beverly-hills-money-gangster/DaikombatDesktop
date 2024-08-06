package com.beverly.hills.money.gang.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import java.util.Locale;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;

public class TextInputProcessor {

  private final int maxLength;

  private final Runnable onKeyStroke;

  private final StringBuilder textBuilder = new StringBuilder();

  public TextInputProcessor(int maxLength, Runnable onKeyStroke) {
    if (maxLength <= 0) {
      throw new IllegalArgumentException();
    }
    this.maxLength = maxLength;
    this.onKeyStroke = onKeyStroke;
  }

  public void append(String text) {
    if (text == null) {
      return;
    }
    int charsLeft = maxLength - textBuilder.length();
    if (charsLeft != 0) {
      textBuilder.append(text.toLowerCase(Locale.ENGLISH), 0, Math.min(text.length(), charsLeft));
    }
  }

  public void clear() {
    textBuilder.setLength(0);
  }

  public String getText() {
    return textBuilder.toString();
  }

  public void handleInput() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) || Gdx.input.isKeyJustPressed(
        Input.Keys.DEL)) {
      if (StringUtils.isNotEmpty(textBuilder)) {
        onKeyStroke.run();
        textBuilder.setLength(textBuilder.length() - 1);
      }
    } else {
      IntConsumer keyStrokeConsumer = value -> {
        if (Gdx.input.isKeyJustPressed(value) && textBuilder.length() <= maxLength) {
          onKeyStroke.run();
          if (value == Input.Keys.SPACE) {
            textBuilder.append(" ");
          } else if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            switch (value) {
              case Input.Keys.NUM_1 -> textBuilder.append("!");
              case Input.Keys.MINUS -> textBuilder.append("_");
              case Input.Keys.SLASH -> textBuilder.append("?");
              case Input.Keys.SEMICOLON -> textBuilder.append(":");
              default -> textBuilder.append(Input.Keys.toString(value));
            }
          } else {
            textBuilder.append(Input.Keys.toString(value));
          }
        }
      };
      IntStream alphanumericKeyStream = IntStream.concat(
          IntStream.rangeClosed(Input.Keys.A, Input.Keys.Z),
          IntStream.rangeClosed(Input.Keys.NUM_0, Input.Keys.NUM_9));
      IntStream.concat(
              alphanumericKeyStream,
              IntStream.of(Input.Keys.SPACE,
                  Input.Keys.MINUS, Input.Keys.SLASH, Input.Keys.SEMICOLON, Input.Keys.COLON,
                  Input.Keys.PERIOD))
          .forEach(keyStrokeConsumer);
    }
  }
}
