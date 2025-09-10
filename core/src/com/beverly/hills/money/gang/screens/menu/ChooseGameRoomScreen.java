package com.beverly.hills.money.gang.screens.menu;

import static com.beverly.hills.money.gang.configs.Constants.PRESS_TO_REFRESH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.UserSettingSound;
import com.beverly.hills.money.gang.configs.KeyMappings;
import com.beverly.hills.money.gang.screens.data.CompleteJoinGameData;
import com.beverly.hills.money.gang.screens.data.ConnectServerData;
import com.beverly.hills.money.gang.screens.data.JoinGameData;
import com.beverly.hills.money.gang.screens.loading.ConnectServerScreen;
import com.beverly.hills.money.gang.screens.loading.DownloadMapAssetsScreen;
import com.beverly.hills.money.gang.screens.ui.selection.GameRoom;
import com.beverly.hills.money.gang.screens.ui.selection.UISelection;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

public class ChooseGameRoomScreen extends AbstractMainMenuScreen {

  private final UserSettingSound boomSound1;
  private final UserSettingSound dingSound1;

  private final JoinGameData joinGameData;

  private final ConnectServerData connectServerData;

  private static final String SELECT_GAME_ROOM = "SELECT GAME ROOM";

  private final UISelection<GameRoom> gameRoomSelection;


  public ChooseGameRoomScreen(final DaiKombatGame game,
      final JoinGameData joinGameData,
      final ConnectServerData connectServerData,
      final List<GameRoom> gameRooms) {
    super(game);
    boomSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.BOOM_1);
    dingSound1 = game.getAssMan().getUserSettingSound(SoundRegistry.DING_1);
    gameRoomSelection = new UISelection<>(gameRooms);
    gameRoomSelection.setMenuItemSize(32);
    this.joinGameData = joinGameData;
    this.connectServerData = connectServerData;
  }

  @Override
  public void handleInput(final float delta) {

    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      boomSound1.play(Constants.DEFAULT_SFX_VOLUME);
      onSelected();
    } else if (Gdx.input.isKeyJustPressed(KeyMappings.REFRESH.getKey())) {
      removeAllEntities();
      getGame().setScreen(new GetGameRoomsScreen(getGame(), joinGameData, connectServerData));
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      removeAllEntities();
      getGame().setScreen(new MainMenuScreen(getGame()));
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
      gameRoomSelection.next();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      gameRoomSelection.prev();
      dingSound1.play(Constants.DEFAULT_SFX_VOLUME);
    }
  }

  @Override
  public void render(final float delta) {
    super.render(delta);
    getGame().getBatch().begin();
    if (gameRoomSelection.getSelections().size() == 1) {
      onSelected();
    } else {
      renderHint(PRESS_TO_REFRESH);
      var glyphSelection = new GlyphLayout(guiFont64, SELECT_GAME_ROOM);
      guiFont64.draw(getGame().getBatch(), SELECT_GAME_ROOM,
          getViewport().getWorldWidth() / 2f - glyphSelection.width / 2f,
          getLogoYOffset() - glyphSelection.height / 2f);

      String description = StringUtils.defaultIfBlank(
              gameRoomSelection.getSelectedOption().getDescription(), "")
          .toUpperCase(Locale.ENGLISH);
      var glyphRoomDescription = new GlyphLayout(guiFont32, description);
      guiFont32.draw(getGame().getBatch(), description,
          getViewport().getWorldWidth() / 2f - glyphRoomDescription.width / 2f,
          getLogoYOffset() - glyphRoomDescription.height / 2f - 32 - 16);

      gameRoomSelection.render(guiFont64, this, getLogoYOffset() - 64 - 16);
    }
    getGame().getBatch().end();
  }

  private void onSelected() {
    removeAllEntities();
    var selectedGameRoom = gameRoomSelection.getSelectedOption();
    var completeJoinGameData = CompleteJoinGameData.builder()
        .connectServerData(connectServerData).joinGameData(joinGameData)
        .gameRoomId(selectedGameRoom.getRoomId())
        .mapName(selectedGameRoom.getMapName())
        .mapHash(selectedGameRoom.getMapHash())
        .build();
    removeAllEntities();
    if (getGame().getAssMan()
        .mapExists(selectedGameRoom.getMapName(), selectedGameRoom.getMapHash())) {
      getGame().setScreen(new ConnectServerScreen(getGame(), completeJoinGameData));
    } else {
      getGame().setScreen(
          new DownloadMapAssetsScreen(getGame(), completeJoinGameData));
    }
  }

}
