package com.beverly.hills.money.gang.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;

public interface KeyUtil {


  static boolean downJustPressed() {
    return Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Keys.S);
  }

  static boolean upJustPressed() {
    return Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.W);
  }
}
