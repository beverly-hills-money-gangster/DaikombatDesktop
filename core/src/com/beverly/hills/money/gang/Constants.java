package com.beverly.hills.money.gang;

import com.badlogic.gdx.graphics.Color;
import org.apache.commons.lang3.Range;


public interface Constants {

  float FLOAT_COMPARE_EPS = 0.000001f;
  Range<Float> FRONT_RANGE = Range.of(180f - 45, 180f + 45);
  Range<Float> LEFT_RANGE = Range.of(45f, 180f - 44.9f);
  Range<Float> RIGHT_RANGE = Range.of(180 + 45.01f, 320f);

  float PLAYER_RECT_SIZE = 0.25f + 0.300f;

  long SHOOTING_SOUND_FREQ_MLS = 550;

  float MOUSE_CAMERA_ROTATION_SPEED = 15f;
  float ARROWS_CAMERA_ROTATION = 2.5f;
  float BLOOD_OVERLAY_ALPHA_MAX = 1f;
  float BLOOD_OVERLAY_ALPHA_MIN = 0f;
  float BLOOD_OVERLAY_ALPHA_SPEED = 5f;

  int LOGO_INDENT = 256 - 16;
  int DEFAULT_SELECTION_INDENT = 32;
  int SPAWN_ANIMATION_MLS = 250;
  int MENU_OPTION_INDENT = 32;

  float DEFAULT_ENEMY_Y = 0.5057522f;

  Color FOG_COLOR = new Color(66 / 256f, 33 / 256f, 54 / 256f, 1f);
  float DEFAULT_PLAYER_CAM_Y = 0.40f;
  String YOU_DIED = "YOU DIED!";
  String CONNECTING = "CONNECTING";
  String SELECTED_OPTION_MARK = ">";

  float PPU = 1f / 16f;
  float HALF_UNIT = 0.5f;

  float DEFAULT_MUSIC_VOLUME = 0.5f;

  float DEFAULT_SFX_VOLUME = 0.5f;

  float DEFAULT_SFX_TYPING_VOLUME = 0.5f;

  float PLAYER_FX_VOLUME = 0.25f;

  float QUAKE_NARRATOR_FX_VOLUME = 0.4f;

  float MK_NARRATOR_FX_VOLUME = 1f;

  int FBO_WIDTH_ORIGINAL = 800; // 160
  int FBO_HEIGHT_ORIGINAL = 600; // 120

}
