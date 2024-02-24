package com.beverly.hills.money.gang.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import org.apache.commons.lang3.StringUtils;

import java.util.function.IntConsumer;
import java.util.stream.IntStream;

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

    public void clear() {
        textBuilder.setLength(0);
    }

    public String getText() {
        return textBuilder.toString();
    }

    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) || Gdx.input.isKeyJustPressed(Input.Keys.DEL)) {
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
                            case Input.Keys.SLASH -> textBuilder.append("?");
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
                                    Input.Keys.MINUS, Input.Keys.SLASH))
                    .forEach(keyStrokeConsumer);
        }
    }
}
