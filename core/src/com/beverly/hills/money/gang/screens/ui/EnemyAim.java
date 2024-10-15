package com.beverly.hills.money.gang.screens.ui;


import lombok.Builder;
import lombok.Getter;

@Builder

public class EnemyAim {

  @Getter
  private final String name;
  @Getter
  private final String playerClass;
  private final int hp;

  public String getHp() {
    return hp + " HP";
  }

}
