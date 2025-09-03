package com.beverly.hills.money.gang.screens;

import static com.beverly.hills.money.gang.Constants.HUD_ALPHA_CHANNEL;
import static com.beverly.hills.money.gang.Constants.SHADOW_MARGIN;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.beverly.hills.money.gang.Configs;
import com.beverly.hills.money.gang.DaiKombatGame;
import com.beverly.hills.money.gang.assets.managers.registry.FontRegistry;
import com.beverly.hills.money.gang.entities.Entity;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.network.GlobalGameConnection;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;

public abstract class GameScreen implements Screen {

  @Getter
  private final DaiKombatGame game;
  @Getter
  private final Vector3 currentSpherePos = new Vector3();
  @Getter
  @Setter
  private Camera currentCam;
  @Getter
  private final Viewport viewport;
  @Getter
  @Setter
  private Player player;

  protected final BitmapFont guiFont64;
  protected final BitmapFont guiFont32;

  private final AtomicBoolean exiting = new AtomicBoolean(false);

  public GameScreen(final DaiKombatGame game, final Viewport viewport) {
    this.game = game;
    this.viewport = viewport;
    this.game.setGameIsPaused(false);
    game.getEntMan().setScreen(this);
    guiFont64 = getGame().getAssMan().getFont(FontRegistry.FONT_64);
    guiFont32 = getGame().getAssMan().getFont(FontRegistry.FONT_32);
  }

  public BitmapFont getUiFont() {
    if (Configs.DEV_MODE) {
      return guiFont32;
    } else {
      return guiFont64;
    }
  }

  public boolean isExiting() {
    return exiting.get();
  }


  public void renderHint(String hint) {
    renderHints(List.of(hint));
  }

  public void renderHints(final List<String> hints) {
    int indent = 32;
    for (int i = 0; i < hints.size(); i++) {
      final var hint = hints.get(i);
      final var currentIndent = i * indent;
      drawBlinking(guiFont32, bitmapFont -> {
        var glyphHint = new GlyphLayout(bitmapFont, hint);
        bitmapFont.draw(getGame().getBatch(), hint,
            getViewport().getWorldWidth() / 2f - glyphHint.width / 2f,
            getViewport().getWorldHeight() - 128 - currentIndent);
      });
    }
  }

  public void checkOverlaps(final RectanglePlus rect) {
    checkOverlapX(rect);
    checkOverlapY(rect);

    rect.setOverlapX(false);
    rect.setOverlapY(false);
  }

  /**
   * Check for overlap in angle X.
   */
  private void checkOverlapX(final RectanglePlus rect) {
    rect.setX(rect.getNewPosition().x);

    rect.setOverlapX(game.getRectMan().checkCollision(rect));

    if (rect.isOverlapX()) {
      rect.getNewPosition().x = rect.getOldPosition().x;
    }

    rect.setX(rect.getNewPosition().x);
  }


  protected void printShadowText(final int x, final int y, final String text,
      final BitmapFont font, final Texture texture, final float alphaChannel) {
    GlyphLayout glyph = new GlyphLayout(font, text);
    float blockWidth = glyph.width + SHADOW_MARGIN * 2;
    float blockHeight = glyph.height + SHADOW_MARGIN * 2;
    float blockX = x - SHADOW_MARGIN;
    float blockY = y - SHADOW_MARGIN;
    var oldColor = getGame().getBatch().getColor().cpy();
    getGame().getBatch().setColor(1, 1, 1, alphaChannel);
    getGame().getBatch()
        .draw(texture, blockX, blockY, blockWidth, blockHeight);
    getGame().getBatch().setColor(oldColor);
    getUiFont().setColor(1, 1, 1, HUD_ALPHA_CHANNEL);
    getUiFont().draw(getGame().getBatch(), text, x, y + glyph.height);
    getUiFont().setColor(Color.WHITE);
  }

  protected Texture createTexture(final Color color) {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fill();
    var texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }

  /**
   * Check for overlap in angle Y.
   */
  private void checkOverlapY(final RectanglePlus rect) {
    rect.setY(rect.getNewPosition().y);

    rect.setOverlapY(game.getRectMan().checkCollision(rect));

    if (rect.isOverlapY()) {
      rect.getNewPosition().y = rect.getOldPosition().y;
    }
    rect.setY(rect.getNewPosition().y);
  }

  @Override
  public void dispose() {

  }

  public boolean frustumCull(final Camera cam, final ModelInstanceBB modelInst) {
    modelInst.calculateBoundingBox(modelInst.getRenderBox());
    modelInst.getRenderBox().mul(modelInst.transform.cpy());

    modelInst.transform.getTranslation(currentSpherePos);
    currentSpherePos.add(modelInst.getCenter());

    return cam.frustum.sphereInFrustum(currentSpherePos, modelInst.getRadius());
  }

  public abstract void handleInput(final float delta);

  @Override
  public void hide() {

  }

  @Override
  public void pause() {

  }

  protected void renderGameTechStats(int playersOnline, GlobalGameConnection gameConnection) {
    StringBuilder gameTechStats = new StringBuilder();
    gameTechStats.append(playersOnline).append(" ONLINE ");
    gameTechStats.append("| PING ")
        .append(Objects.toString(gameConnection.getPrimaryNetworkStats().getPingMls(), "-"))
        .append(" MS | ");
    gameTechStats.append(Gdx.graphics.getFramesPerSecond()).append(" FPS");

    var gameTechStatsGlyph = new GlyphLayout(getUiFont(), gameTechStats);
    getUiFont().setColor(1, 1, 1, HUD_ALPHA_CHANNEL);
    getUiFont().draw(getGame().getBatch(), gameTechStats,
        getViewport().getWorldWidth() - 32 - gameTechStatsGlyph.width,
        getViewport().getWorldHeight() - 32 - gameTechStatsGlyph.height);
    getUiFont().setColor(Color.WHITE);
  }

  protected void removeAllEntities() {
    for (final Entity ent : game.getEntMan().entities) {
      ent.destroy();
    }

    game.getEntMan().entities.clear(); // Removes cell3Ds and doors.
    game.getRectMan().getRects().clear(); // remove rect walls too.
  }

  @Override
  public void render(final float delta) {
    if (exiting.get()) {
      return;
    }
    handleInput(delta);
    tick(delta);
  }

  @Override
  public void resize(final int width, final int height) {
    if (viewport != null) {
      viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
  }

  @Override
  public void resume() {

  }

  @Override
  public void show() {

  }

  public void tick(final float delta) {
    if (!game.isGameIsPaused()) {
      game.getEntMan().tickAllEntities(delta);
    }
  }

  public void drawBlinking(
      final BitmapFont bitmapFont,
      final Consumer<BitmapFont> drawLogic) {
    var oldColor = bitmapFont.getColor().cpy();
    bitmapFont.setColor(oldColor.r, oldColor.g, oldColor.b,
        Math.max(0.5f, (float) Math.sin(getGame().getTimeSinceLaunch() * 5)));
    drawLogic.accept(bitmapFont);
    bitmapFont.setColor(oldColor);
  }

  public final void exit(GameScreen screenToTransitionTo) {
    exiting.set(true);
    onExitScreen(screenToTransitionTo);
  }

  public void onExitScreen(GameScreen screenToTransitionTo) {
    // do nothing by default
  }

}
