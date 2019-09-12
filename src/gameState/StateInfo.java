package gameState;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import background.Background;

public class StateInfo extends GameState {

  public StateInfo(GameStateManager sm) {
    super(sm);
    // Initiate resources
    initResources();
  }

  private void initResources() {
    initTileMap();
    initLayers();
  }

  private void initTileMap() {
    // Load up the state tile map
    load("/assets/map/info/backgroundnight.xml");
  }

  private void initLayers() {
    // Backgrounds, rendered in order of appearance
    layers.get(GameStateManager.LAYER_BACKGROUND)
        .add(new Background(sm, "/assets/map/info/backgroundnight.xml"));
    layers.get(GameStateManager.LAYER_BACKGROUND)
        .get(layers.get(GameStateManager.LAYER_BACKGROUND).size() - 1)
        .setVec(new Point2D.Double(0.0, 0.05));
  }

  public void update() {
    updateBackground();
    updatePlaygroundBack();
    updatePlaygroundFront();
    updateForeground();
  }

  public void render(Graphics2D g2d) {
    renderBackgrounds(g2d);
    infoScreen(g2d);
    renderHud(g2d);
  }

  private void infoScreen(Graphics2D g2d) {
    renderControls(g2d);
  }

  private void renderControls(Graphics2D g2d) {
    renderTextOverlay(g2d, "Controls", new Point(3, 4), 10);
    String[] t = { "Move left.......arrow left", "Move right......arrow right",
        "Jump............x, spacebar", "Fire............z, c",
        "Slowfall........up, while falling", "Pick up item....up",
        "Open door.......up", "", "Best played with controller.",
        "any joytokey converter should work." };
    renderTextOverlaySmall(g2d, t, new Point(2, 6));
    renderSmallFont(g2d, 5, 14, "press fire for main menu", false, false);
  }

  public void keyPressed(int key) {
    if (key == KeyEvent.VK_Z || key == KeyEvent.VK_C) {
      sm.setState(GameStateManager.LEVEL_MENU);
    }
  }

  public void keyReleased(int key) {
  }
}
