package com.beverly.hills.money.gang.assets.managers.registry;

import com.beverly.hills.money.gang.assets.managers.sound.PseudoRandomSequence;
import lombok.Getter;

public enum SoundRegistry {

  ENEMY_PUNCH_THROWN("sfx/enemy_punch_thrown.mp3"),
  WEAPON_CHANGE("sfx/weapon_change.mp3"),
  ENEMY_RAILGUN("sfx/enemy_railgun.mp3"),
  PLAYER_GOING_THROUGH_TELEPORT("sfx/player_going_through_teleport.mp3"),
  PLAYER_ROCKET_LAUNCHER("sfx/player_rocket_launcher.mp3"),
  ENEMY_PLAYER_GOING_THROUGH_TELEPORT("sfx/enemy_going_through_teleport.mp3"),
  PLAYER_PLASMAGUN_FIRE("sfx/player_plasmagun_fire.wav"),
// TODO make it mono
  PLASMA_BOOM("sfx/plasma_boom.mp3"),
  ONE_FRAG_LEFT("sfx/one_frag_left.mp3"),
  TWO_FRAGS_LEFT("sfx/two_frags_left.mp3"),
  THREE_FRAGS_LEFT("sfx/three_frags_left.mp3"),
  // TODO try sounds from C:\Users\35795\Downloads\PC Computer - Quake III Arena Team Arena - Weapons\weapons\plasma
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
  HOLY_SHIT("sfx/winning/holy_shit.mp3"),
  ACCURACY("sfx/winning/accuracy.mp3"),
  TWO_FRAGS_TO_SEC("sfx/winning/two_frags_to_sec.mp3"),
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
  TAUNT_DO_NOT_MAKE_ME_LAUGH("voice/taunt/player/do_not_make_me_laugh.mp3"),
  TAUNT_I_WIN("voice/taunt/player/i_win.mp3"),
  TAUNT_OFFICIAL_SUCK("voice/taunt/player/official_suck.mp3"),
  TAUNT_PATHETIC("voice/taunt/player/pathetic.mp3"),
  TAUNT_STILL_TRYING("voice/taunt/player/still_trying.mp3"),
  TAUNT_YOU_NEVER_WIN("voice/taunt/player/you_never_win.mp3"),
  TAUNT_YOU_SUCK("voice/taunt/player/you_suck.mp3"),
  TAUNT_IS_THAT_YOUR_BEST("voice/taunt/player/is_that_your_best.mp3"),
  TAUNT_PREPARE_TO_DIE("voice/taunt/player/prepare_to_die.mp3"),
  TAUNT_YOU_ARE_NOTHING("voice/taunt/player/you_are_nothing.mp3"),
  TAUNT_YOU_WEAK_PATHETIC_FOOL("voice/taunt/player/you_are_weak_pathetic_fool.mp3"),

  ENEMY_TAUNT_DO_NOT_MAKE_ME_LAUGH("voice/taunt/enemy/do_not_make_me_laugh.mp3"),
  ENEMY_TAUNT_I_WIN("voice/taunt/enemy/i_win.mp3"),
  ENEMY_TAUNT_OFFICIAL_SUCK("voice/taunt/enemy/official_suck.mp3"),
  ENEMY_TAUNT_PATHETIC("voice/taunt/enemy/pathetic.mp3"),
  ENEMY_TAUNT_STILL_TRYING("voice/taunt/enemy/still_trying.mp3"),
  ENEMY_TAUNT_YOU_NEVER_WIN("voice/taunt/enemy/you_never_win.mp3"),
  ENEMY_TAUNT_YOU_SUCK("voice/taunt/enemy/you_suck.mp3"),
  ENEMY_TAUNT_IS_THAT_YOUR_BEST("voice/taunt/enemy/is_that_your_best.mp3"),
  ENEMY_TAUNT_PREPARE_TO_DIE("voice/taunt/enemy/prepare_to_die.mp3"),
  ENEMY_TAUNT_YOU_ARE_NOTHING("voice/taunt/enemy/you_are_nothing.mp3"),
  ENEMY_TAUNT_YOU_WEAK_PATHETIC_FOOL("voice/taunt/enemy/you_are_weak_pathetic_fool.mp3"),

  ENEMY_SHOTGUN("sfx/enemy_shotgun.mp3"),
  ENEMY_ROCKET_LAUNCHER("sfx/enemy_player_rocket_launcher.mp3"),
  ENEMY_PLASMAGUN_FIRE("sfx/enemy_player_plasmagun_fire.mp3"),
  QUAD_DAMAGE_ATTACK("sfx/quad_damage_attack.mp3"),
  GAUNTLET_HUMILIATION("sfx/gauntlet_humiliation.mp3"),
  // TODO sounds like ass
  QUAD_DAMAGE_PICK("sfx/quad_damage_pick.mp3"),
  DEFENCE_PICK("sfx/defence_pick.mp3"),
  REGENERATION_PICK("sfx/regeneration.mp3"),
  ENEMY_REGENERATION_PICK("sfx/enemy_regeneration.mp3"),
  INVISIBILITY_PICK("sfx/invisibility_pick.mp3"),
  ENEMY_QUAD_DAMAGE_ATTACK("sfx/enemy_quad_damage_attack.mp3"),
  ENEMY_QUAD_DAMAGE_PICK("sfx/enemy_quad_damage_pick.mp3"),
  ENEMY_INVISIBILITY_PICK("sfx/enemy_invisibility_pick.mp3"),
  ENEMY_DEFENCE_PICK("sfx/enemy_defence_pick.mp3"),
  PLAYER_SHOTGUN("sfx/player_shotgun.mp3"),
  ROCKET_BOOM("sfx/rocket_boom.mp3"),
  ENEMY_MINIGUN("sfx/enemy_minigun.mp3"),
  PLAYER_MINIGUN("sfx/player_minigun.mp3"),
  HIT_SOUND("sfx/shoot_hit_sound.mp3"),
  SPAWN1("sfx/spawn/spawn1.mp3"),
  SPAWN2("sfx/spawn/spawn2.mp3"),
  SPAWN3("sfx/spawn/spawn3.mp3"),

  DOOR_OPEN("sfx/door_open.mp3"),
  DOOR_CLOSE("sfx/door_close.mp3");

  @Getter
  private final String fileName;

  SoundRegistry(String fileName) {
    this.fileName = "sound/" + fileName;
  }


  public static final PseudoRandomSequence<SoundRegistry> TYPING_SOUND_SEQ
      = new PseudoRandomSequence<>(DING_1, DING_2);
  public static final PseudoRandomSequence<SoundRegistry> LOOSING_SOUND_SEQ
      = new PseudoRandomSequence<>(LAUGH, LAUGH_2, LAUGH_3);

  public static final PseudoRandomSequence<SoundRegistry> SPAWN_SOUND_SEQ
      = new PseudoRandomSequence<>(SPAWN1, SPAWN2, SPAWN3);

  public static final PseudoRandomSequence<SoundRegistry> VOICE_GET_HIT_SOUND_SEQ
      = new PseudoRandomSequence<>(VOICE_GET_HIT, VOICE_GET_HIT_2);

  public static final PseudoRandomSequence<SoundRegistry> ENEMY_GET_HIT_SOUND_SEQ
      = new PseudoRandomSequence<>(VOICE_ENEMY_GET_HIT, VOICE_ENEMY_GET_HIT_2,
      VOICE_ENEMY_GET_HIT_3, VOICE_ENEMY_GET_HIT_4);

  public static final PseudoRandomSequence<SoundRegistry> BATTLE_BG_SEQ
      = new PseudoRandomSequence<>(BATTLE, BATTLE2);

  public static final PseudoRandomSequence<SoundRegistry> ENEMY_DEATH_SOUND_SEQ
      = new PseudoRandomSequence<>(VOICE_ENEMY_DEATH, VOICE_ENEMY_DEATH_2, VOICE_ENEMY_DEATH_3);

}
