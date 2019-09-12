package gameState;

import java.awt.Graphics2D;

public class StateLoading extends GameState {
  public StateLoading(GameStateManager sm) {
    super(sm);
    // Initiate resources
    initResources();
  }

  private void initResources() {
    // Load up the game state tile map
    load("/assets/map/loading/loading.xml");
  }

  public void reset() {
  }

  public void update() {
  }

  public void render(Graphics2D g2d) {
    renderPlayground(g2d);
  }

  public void keyPressed(int key) {
  }

  public void keyReleased(int key) {
  }
}
