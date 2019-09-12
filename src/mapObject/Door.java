package mapObject;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import gameState.GameStateManager;
import tileManager.TileManager;

public class Door extends MapObject{

  public Door(GameStateManager sm) {
    super(sm);

    spawnPoint.setLocation(new Point2D.Double(50.0, 50.0));
    coords.setLocation(spawnPoint);

    hitBox.setSize(16, 16);

    animation = new Animation[1];
    animation[0] = new Animation(((11 * 32) + 0), 1);
  }

  @Override
  public void update(){
  }
  
  @Override
  public void render(Graphics2D g2d) {
    double x = coords.getX();
    double y = coords.getY();

    // Draw the mushroom

    g2d.drawImage(TileManager.getTileImage(animation[currentAnim].getFrame()),
        (int) (x - TileManager.HALF_TILE), (int) (y - TileManager.HALF_TILE), null);

    drawHitBox(g2d);
  }

  
}
