package com.beverly.hills.money.gang.screens.ui.taunt;

import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.PseudoRandomSequence;
import com.beverly.hills.money.gang.proto.Taunt;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GameTaunt {

  DO_NOT_MAKE_ME_LAUGH(SoundRegistry.TAUNT_DO_NOT_MAKE_ME_LAUGH,
      SoundRegistry.ENEMY_TAUNT_DO_NOT_MAKE_ME_LAUGH,
      "DON'T MAKE ME LAUGH", Taunt.DO_NOT_MAKE_ME_LAUGH),
  I_WIN(SoundRegistry.TAUNT_I_WIN, SoundRegistry.ENEMY_TAUNT_I_WIN,
      "I WIN!", Taunt.I_WIN),
  OFFICIAL_SUCK(SoundRegistry.TAUNT_OFFICIAL_SUCK, SoundRegistry.ENEMY_TAUNT_OFFICIAL_SUCK,
      "IT'S OFFICIAL. YOU SUCK!", Taunt.OFFICIAL_SUCK),
  PATHETIC(SoundRegistry.TAUNT_PATHETIC, SoundRegistry.ENEMY_TAUNT_PATHETIC,
      "THAT WAS PATHETIC", Taunt.THAT_WAS_PATHETIC),
  STILL_TRYING(SoundRegistry.TAUNT_STILL_TRYING, SoundRegistry.ENEMY_TAUNT_STILL_TRYING,
      "STILL TRYING TO WIN?", Taunt.STILL_TRYING),
  YOU_NEVER_WIN(SoundRegistry.TAUNT_YOU_NEVER_WIN, SoundRegistry.ENEMY_TAUNT_YOU_NEVER_WIN,
      "YOU WILL NEVER WIN", Taunt.U_NEVER_WIN),
  YOU_SUCK(SoundRegistry.TAUNT_YOU_SUCK, SoundRegistry.ENEMY_TAUNT_YOU_SUCK,
      "YOU SUCK", Taunt.U_SUCK),
  IS_THAT_YOUR_BEST(SoundRegistry.TAUNT_IS_THAT_YOUR_BEST,
      SoundRegistry.ENEMY_TAUNT_IS_THAT_YOUR_BEST,
      "IS THAT YOUR BEST?", Taunt.IS_THAT_YOUR_BEST),
  PREPARE_TO_DIE(SoundRegistry.TAUNT_PREPARE_TO_DIE, SoundRegistry.ENEMY_TAUNT_PREPARE_TO_DIE,
      "PREPARE TO DIE", Taunt.PREPARE_TO_DIE),
  YOU_ARE_NOTHING(SoundRegistry.TAUNT_YOU_ARE_NOTHING, SoundRegistry.ENEMY_TAUNT_YOU_ARE_NOTHING,
      "YOU ARE NOTHING", Taunt.U_R_NOTHING),
  YOU_ARE_WEAK_PATHETIC_FOOL(SoundRegistry.TAUNT_YOU_WEAK_PATHETIC_FOOL,
      SoundRegistry.ENEMY_TAUNT_YOU_WEAK_PATHETIC_FOOL,
      "YOU ARE WEAK PATHETIC FOOL", Taunt.U_R_WEAK_PATHETIC_FOOL);

  @Getter
  private final SoundRegistry playerSound;
  @Getter
  private final SoundRegistry enemySound;
  @Getter
  private final String chatMessage;

  @Getter
  private final Taunt tauntType;


  public static final PseudoRandomSequence<GameTaunt> TAUNTS_SEQ = new PseudoRandomSequence<>(
      GameTaunt.values());

  public static GameTaunt map(Taunt taunt) {
    return switch (taunt) {
      case I_WIN -> GameTaunt.I_WIN;
      case U_SUCK -> GameTaunt.YOU_SUCK;
      case U_NEVER_WIN -> GameTaunt.YOU_NEVER_WIN;
      case STILL_TRYING -> GameTaunt.STILL_TRYING;
      case OFFICIAL_SUCK -> GameTaunt.OFFICIAL_SUCK;
      case THAT_WAS_PATHETIC -> GameTaunt.PATHETIC;
      case DO_NOT_MAKE_ME_LAUGH -> GameTaunt.DO_NOT_MAKE_ME_LAUGH;
      case U_R_NOTHING -> GameTaunt.YOU_ARE_NOTHING;
      case PREPARE_TO_DIE -> GameTaunt.PREPARE_TO_DIE;
      case IS_THAT_YOUR_BEST -> GameTaunt.IS_THAT_YOUR_BEST;
      case U_R_WEAK_PATHETIC_FOOL -> GameTaunt.YOU_ARE_WEAK_PATHETIC_FOOL;
      case UNRECOGNIZED -> throw new IllegalArgumentException("Not supported taunt " + taunt);
    };
  }
}
