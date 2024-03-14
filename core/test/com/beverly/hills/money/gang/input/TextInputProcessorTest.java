package com.beverly.hills.money.gang.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextInputProcessorTest {


    @Test
    public void testAppendEmpty() {
        TextInputProcessor textInputProcessor = new TextInputProcessor(16, () -> {
        });
        textInputProcessor.append("");
        assertEquals("", textInputProcessor.getText());
    }


    @Test
    public void testAppendNull() {
        TextInputProcessor textInputProcessor = new TextInputProcessor(16, () -> {
        });
        textInputProcessor.append(null);
        assertEquals("", textInputProcessor.getText());
    }


    @Test
    public void testAppend() {
        TextInputProcessor textInputProcessor = new TextInputProcessor(16, () -> {
        });
        textInputProcessor.append("a");
        textInputProcessor.append("b");
        textInputProcessor.append("c");
        assertEquals("abc", textInputProcessor.getText());
    }


    @Test
    public void testAppendOverflow() {
        TextInputProcessor textInputProcessor = new TextInputProcessor(4, () -> {
        });
        textInputProcessor.append("123");
        textInputProcessor.append("45");
        assertEquals("1234", textInputProcessor.getText());
    }
}
