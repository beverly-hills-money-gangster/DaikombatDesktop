package com.beverly.hills.money.gang.assets.managers.registry;

import com.beverly.hills.money.gang.assets.managers.sound.PseudoRandomSoundSequence;
import lombok.Getter;

public enum SoundRegistry {
  ENEMY_PUNCH_THROWN("sfx/enemy_punch_thrown.mp3"),
  WEAPON_CHANGE("sfx/weapon_change.mp3"),
  ENEMY_RAILGUN("sfx/enemy_railgun.mp3"),
  PLAYER_GOING_THROUGH_TELEPORT("sfx/player_going_through_teleport.mp3"),
  ENEMY_PLAYER_GOING_THROUGH_TELEPORT("sfx/enemy_going_through_teleport.mp3"),
  ONE_FRAG_LEFT("sfx/one_frag_left.mp3"),
  TWO_FRAGS_LEFT("sfx/two_frags_left.mp3"),
  THREE_FRAGS_LEFT("sfx/three_frags_left.mp3"),
  YOU_LEAD("sfx/you_lead.mp3"),
  LOST_LEAD("sfx/lost_lead.mp3"),
  BELL("sfx/bell.mp3"),
  BATTLE("music/battle.wav"),
  BATTLE2("music/battle2.wav"),
  MAIN_MENU("music/main_menu.mp3"),
  WIN_MUSIC("music/win_music.mp3"),
  LAUGH("sfx/losing/laugh.mp3"),
  LAUGH_2("sfx/losing/laugh_2.mp3"),
  LAUGH_3("sfx/losing/laugh_3.mp3"),
  PUNCH_HIT("sfx/punch_hit.mp3"),
  ENEMY_PUNCH_HIT("sfx/enemy_punch_hit.mp3"),
  PUNCH_THROWN("sfx/punch_thrown.mp3"),
  PLAYER_RAILGUN("sfx/player_railgun.mp3"),
  FIGHT("sfx/fight.mp3"),
  DING_1("sfx/ui/ding_1.mp3"),
  DING_2("sfx/ui/ding_2.mp3"),
  PING("sfx/ui/ping.mp3"),
  BOOM_1("sfx/ui/boom_1.mp3"),
  BOOM_2("sfx/ui/boom_2.mp3"),
  EXCELLENT("sfx/winning/excellent.wav"),
  HOLY_SHIT("sfx/winning/holyshit.wav"),
  HUMILIATION("sfx/winning/humiliation.wav"),
  IMPRESSIVE("sfx/winning/impressive.wav"),
  YOU_WIN("sfx/winning/you_win.wav"),
  PERFECT("sfx/winning/perfect.wav"),
  VOICE_GET_HIT("voice/pain/voice_get_shot.mp3"),
  VOICE_GET_HIT_2("voice/pain/voice_get_shot_2.mp3"),

  VOICE_ENEMY_GET_HIT("voice/enemy/pain/oof.mp3"),
  VOICE_ENEMY_GET_HIT_2("voice/enemy/pain/oof_2.mp3"),
  VOICE_ENEMY_GET_HIT_3("voice/enemy/pain/oof_3.mp3"),
  VOICE_ENEMY_GET_HIT_4("voice/enemy/pain/oof_4.mp3"),

  VOICE_ENEMY_DEATH("voice/enemy/death/death.mp3"),
  VOICE_ENEMY_DEATH_2("voice/enemy/death/death_2.mp3"),
  VOICE_ENEMY_DEATH_3("voice/enemy/death/death_3.mp3"),
  ENEMY_SHOTGUN("sfx/shotgun_mono.mp3"),
  QUAD_DAMAGE_ATTACK("sfx/quad_damage_attack.mp3"),
  QUAD_DAMAGE_PICK("sfx/quad_damage_pick.mp3"),
  DEFENCE_PICK("sfx/defence_pick.mp3"),
  INVISIBILITY_PICK("sfx/invisibility_pick.mp3"),
  ENEMY_QUAD_DAMAGE_ATTACK("sfx/enemy_quad_damage_attack.mp3"),
  ENEMY_QUAD_DAMAGE_PICK("sfx/enemy_quad_damage_pick.mp3"),
  ENEMY_INVISIBILITY_PICK("sfx/enemy_invisibility_pick.mp3"),
  ENEMY_DEFENCE_PICK("sfx/enemy_defence_pick.mp3"),
  PLAYER_SHOTGUN("sfx/player_shotgun.mp3"),
  HIT_SOUND("sfx/shoot_hit_sound.mp3"),
  SPAWN1("sfx/spawn/spawn1.mp3"),
  SPAWN2("sfx/spawn/spawn2.mp3"),
  SPAWN3("sfx/spawn/spawn3.mp3");

  @Getter
  private final String fileName;

  SoundRegistry(String fileName) {
    this.fileName = "sound/" + fileName;
  }


  public static final PseudoRandomSoundSequence TYPING_SOUND_SEQ
      = new PseudoRandomSoundSequence(DING_1, DING_2);
  public static final PseudoRandomSoundSequence LOOSING_SOUND_SEQ
      = new PseudoRandomSoundSequence(LAUGH, LAUGH_2, LAUGH_3);

  public static final PseudoRandomSoundSequence SPAWN_SOUND_SEQ
      = new PseudoRandomSoundSequence(SPAWN1, SPAWN2, SPAWN3);
  public static final PseudoRandomSoundSequence WINNING_SOUND_SEQ
      = new PseudoRandomSoundSequence(EXCELLENT, HUMILIATION, HOLY_SHIT, IMPRESSIVE, PERFECT);
  public static final PseudoRandomSoundSequence GET_HIT_SOUND_SEQ
      = new PseudoRandomSoundSequence(VOICE_GET_HIT, VOICE_GET_HIT_2);

  public static final PseudoRandomSoundSequence ENEMY_GET_HIT_SOUND_SEQ
      = new PseudoRandomSoundSequence(VOICE_ENEMY_GET_HIT, VOICE_ENEMY_GET_HIT_2,
      VOICE_ENEMY_GET_HIT_3, VOICE_ENEMY_GET_HIT_4);

  public static final PseudoRandomSoundSequence BATTLE_BG_SEQ
      = new PseudoRandomSoundSequence(BATTLE, BATTLE2);

  public static final PseudoRandomSoundSequence ENEMY_DEATH_SOUND_SEQ
      = new PseudoRandomSoundSequence(VOICE_ENEMY_DEATH, VOICE_ENEMY_DEATH_2, VOICE_ENEMY_DEATH_3);

}
