package com.beverly.hills.money.gang.screens.ui.selection;

import com.beverly.hills.money.gang.proto.PlayerClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GamePlayerClass {
  WARRIOR("Well-rounded fighter. Reliable in any situation."),
  ANGRY_SKELETON("Fast, fragile, vicious. Hits hard, heals harder."),
  DEMON_TANK("Tough demon brute. Slow but hard to kill.");

  @Getter
  private final String description;

  @Override
  public String toString() {
    return this.name().replace("_", " ");
  }

  public float getVoicePitch() {
    return switch (this) {
      case WARRIOR -> 1.125f;
      case DEMON_TANK -> 0.965f;
      case ANGRY_SKELETON -> 1.385f;
    };
  }

  public static GamePlayerClass createPlayerClass(PlayerClass playerClass) {
    return switch (playerClass) {
      case WARRIOR -> GamePlayerClass.WARRIOR;
      case DEMON_TANK -> GamePlayerClass.DEMON_TANK;
      case ANGRY_SKELETON -> GamePlayerClass.ANGRY_SKELETON;
      default -> throw new IllegalArgumentException("Not supported class " + playerClass.name());
    };
  }

}
