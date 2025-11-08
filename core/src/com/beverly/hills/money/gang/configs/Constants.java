package com.beverly.hills.money.gang.configs;

import com.badlogic.gdx.graphics.Color;
import org.apache.commons.lang3.Range;


public interface Constants {

  int MAX_CHAT_MSG_LEN = 32;

  float HUD_ALPHA_CHANNEL = 0.7f;

  float BLOOD_OVERLAY_ALPHA_SWITCH = 0.5f;

  int DEAD_SCREEN_INPUT_DELAY_MLS = 1_000;

  int TAUNT_DELAY_MLS = 1_250;

  int SHADOW_MARGIN = 16;

  String PRESS_TO_SEE_LEADERBOARD =
      "PRESS " + KeyMappings.LEADERBOARD.getKeyName()
          + " TO SEE LEADERBOARD";

  String PRESS_TO_TALK =
      "PUSH " + KeyMappings.TALK.getKeyName() + " TO TALK";

  String PRESS_TO_CHAT =
      "PRESS " + KeyMappings.CHAT.getKeyName() + " TO CHAT";

  String PRESS_TO_REFRESH =
      "PRESS " + KeyMappings.REFRESH.getKeyName() + " TO REFRESH";

  int LONG_TIME_NO_MOVE_MLS = 200;

  float SFX_VOLUME_COEFFICIENT = 1 / 3.5f;

  int MAX_ENEMY_IDLE_TIME_MLS = LONG_TIME_NO_MOVE_MLS * 4;

  int PROJECTILE_SPEED = 30;

  int PUNCH_ANIMATION_MLS = 155;

  float FLOAT_COMPARE_EPS = 0.000001f;
  Range<Float> FRONT_RANGE = Range.of(180f - 45, 180f + 45);
  Range<Float> LEFT_RANGE = Range.of(45f, 180f - 44.9f);
  Range<Float> RIGHT_RANGE = Range.of(180 + 45.01f, 320f);

  float PLAYER_RECT_SIZE = 0.45f;

  int TIME_LIMITED_SOUND_FREQ_MLS = 125;

  float MOUSE_CAMERA_ROTATION_SPEED = 15f;
  float ARROWS_CAMERA_ROTATION = 2.5f;
  float BLOOD_OVERLAY_ALPHA_MAX = 1f;
  float BLOOD_OVERLAY_ALPHA_MIN = 0f;
  float BLOOD_OVERLAY_ALPHA_SPEED = 5f;

  int DEFAULT_SELECTION_INDENT = 32;
  int SPAWN_ANIMATION_MLS = 200;
  int MENU_OPTION_INDENT = 32;

  float DEFAULT_ENEMY_Y = 0.5057522f;

  Color FOG_COLOR = new Color(66 / 256f, 33 / 256f, 54 / 256f, 1f);
  float DEFAULT_PLAYER_CAM_Y = 0.40f;
  String YOU_DIED = "YOU DIED!";
  String CONNECTING = "CONNECTING";

  String DOWNLOAD_MAP = "DOWNLOADING MAP";

  String SERVER_CONNECT = "CONNECTING TO SERVER";

  String GAME_JOIN = "JOINING GAME";
  String SELECTED_OPTION_MARK = ">";

  float PPU = 1f / 16f;
  float HALF_UNIT = 0.5f;

  float DEFAULT_MUSIC_VOLUME = 0.25f * SFX_VOLUME_COEFFICIENT;

  float DEFAULT_SFX_VOLUME = 0.5f * SFX_VOLUME_COEFFICIENT;

  float DEFAULT_SHOOTING_VOLUME = 0.35f * SFX_VOLUME_COEFFICIENT;

  float DEFAULT_SFX_TYPING_VOLUME = 0.5f * SFX_VOLUME_COEFFICIENT;

  float PLAYER_FX_VOLUME = 0.25f * SFX_VOLUME_COEFFICIENT;

  float QUAKE_NARRATOR_FX_VOLUME = 0.4f * SFX_VOLUME_COEFFICIENT;

  float MK_NARRATOR_FX_VOLUME = 1f * SFX_VOLUME_COEFFICIENT;

  int FBO_WIDTH_ORIGINAL = 800; // 160
  int FBO_HEIGHT_ORIGINAL = 600; // 120

}
