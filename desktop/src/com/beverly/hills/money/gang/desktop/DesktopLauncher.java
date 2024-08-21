package com.beverly.hills.money.gang.desktop;


import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;


public class DesktopLauncher {

  // TODO test on TV
  // TODO support alt+tab
  // TODO think about license and copyrights
  public static void main(final String[] arg) {

    final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("DAIKOMBAT");
    config.setBackBufferConfig(8, 8, 8, 8, 24, 8, 0);
    config.setForegroundFPS(120);
    config.setInitialBackgroundColor(Constants.FOG_COLOR);
    config.useVsync(true);

    if (Configs.DEV_MODE) {
      config.setWindowSizeLimits(1024, 768, 1024, 768);
      config.setResizable(false);
      config.setWindowedMode(1024, 768);
    } else {
      config.setFullscreenMode(Lwjgl3ApplicationConfiguration
          .getDisplayMode(Lwjgl3ApplicationConfiguration.getPrimaryMonitor()));
    }
    new Lwjgl3Application(new DaiKombatGame(), config);
  }
}
