package mapObject;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Random;

import game.Game;
import gameState.GameStateManager;
import tileManager.TileManager;

public class Bullet extends MapObject {
  private Random r = new Random();
  
  public Bullet(GameStateManager sm, Point2D.Double spawnPosition,
      boolean faceRight) {
    super(sm);

    hitBoxTile.setSize(TileManager.HALF_TILE, TileManager.HALF_TILE);
    hitBox.setSize(6, 6);

    moveSpeed = 5.0;
    spawnPoint.setLocation(spawnPosition);
    coords.setLocation(spawnPoint);

    // Add bad aim
    double aim = ( r.nextDouble() - 0.65 ) * 0.2;

    if (faceRight) {
      delta.setLocation(moveSpeed, aim);
    } else {
      delta.setLocation(-moveSpeed, aim);
    }

    // Initiate bullet animations (if any)
    {
      int[] bullet = { (9 * 32 + 7) };
      animation = new Animation[1];
      animation[0] = new Animation(bullet);
    }

  }

  @Override
  public void update() {
    if (alive) {
      doMovement();
      limitToScreen();
      setPos(tempCoords);
    } else {
      removeMe();
    }
  }

  private void doMovement() {
    double tx = coords.getX();
    double ty = coords.getY();
    double dx = delta.getX();
    double dy = delta.getY();

    tx += dx;
    ty += dy;

    tempCoords.setLocation(tx, ty);
  }

  @Override
  protected void limitToScreen() {
    double tx = tempCoords.getX();
    double ty = tempCoords.getY();

    if (tx > Game.WIN_W - TileManager.TILE_SIZE) {
      tx = Game.WIN_W - TileManager.TILE_SIZE;
      alive = false;
      removeMe();
    } else if (tx < 0.0) {
      tx = 0.0;
      alive = false;
      removeMe();
    }

    tempCoords.setLocation(tx, ty);
  }

  @Override
  public void render(Graphics2D g2d) {
    double x = coords.getX();
    double y = coords.getY();

    g2d.drawImage(
        TileManager.getQuarterTileImage(animation[currentAnim].getFrame(), 0),
        (int) (x - TileManager.HALF_TILE / 2), (int) (y - TileManager.HALF_TILE / 2),
        null);

    drawHitBox(g2d);
  }

}
