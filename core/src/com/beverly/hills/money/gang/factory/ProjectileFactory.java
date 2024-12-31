package com.beverly.hills.money.gang.factory;

import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.entities.projectile.Projectile;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;

public interface ProjectileFactory {


  Projectile create(Player player, WeaponState weaponState);

}
