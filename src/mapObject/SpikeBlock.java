package mapObject;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import gameState.GameStateManager;
import tileManager.TileManager;

public class SpikeBlock extends MapObject {
  private final int ANIM_LEFT = 0;
  private final int ANIM_STANDBY = 1;
  private final int ANIM_RIGHT = 2;
  private final int ANIM_DEATH = 3;
  private final int ANIM_TOTAL = 4;

  public SpikeBlock(GameStateManager sm) {
    super(sm);

    faceRight = true;

    hp = 50;
    score = 25;
    
    fallSlow = 0.5;
    fallSpeed = 0.2;
    fallSpeedMax = 10;

    spawnPoint.setLocation(new Point2D.Double(50.0, 50.0));
    coords.setLocation(spawnPoint);

    hitBox.setSize(12, 12);
    timeAfterDeath = 100;

    // TILEDATA
    {
      animation = new Animation[ANIM_TOTAL];
      animation[ANIM_LEFT] = new Animation((15 * 32 + 21), 1);
      animation[ANIM_STANDBY] = new Animation((15 * 32 + 21), 1);
      animation[ANIM_RIGHT] = new Animation((15 * 32 + 21), 1);
      animation[ANIM_DEATH] = new Animation((15 * 32 + 21), 1);
      animation[ANIM_LEFT].setDelayLimit(20);
      animation[ANIM_STANDBY].setDelayLimit(50);
      animation[ANIM_RIGHT].setDelayLimit(20);
      animation[ANIM_DEATH].setDelayLimit(3);
      animation[ANIM_DEATH].playOnce();
    }
  }

  @Override
  public void update() {
    if (alive) {
      tempCoords.setLocation(coords);
      move();
      checkTileMapCollision(false, true, false, false);
      limitToScreen();
      setPos(tempCoords);
    } else {
      stopXMovement(0.025);
      checkTileMapCollision(false, true, false, false);
      setPos(tempCoords);
      currentAnim = ANIM_DEATH;
      --timeAfterDeath;
      if (animation[ANIM_DEATH].hasPlayedOnce() && timeAfterDeath < 0) {
        removeMe();
      }
    }

  }

  private void move() {
    currentAnim = ANIM_STANDBY;
    animation[ANIM_LEFT].reset();
    animation[ANIM_RIGHT].reset();
  }

  // Check if something is in the way of the objects movement, and take action
  // accordingly.
  // Will also update the coordinates
  @Override
  protected void checkTileMapCollision(boolean up, boolean down, boolean left,
      boolean right) {

    // Just shorten up the variables a bit, save back at end
    double x = coords.getX();
    double y = coords.getY();
    double dx = delta.getX();
    double dy = delta.getY();
    double tx = x;
    double ty = y;

    int mapy = getMapY(y);

    double dex = x + dx;
    double dey = y + dy;

    if (down) {
      checkHitBoxCorners(new Point2D.Double(x, dey));
      if (down) {
        if (dy > 0.0) {
          if (hitBoxSW || hitBoxSE) {
            dy = 0.0;
            ty = (mapy + 1) * TileManager.TILE_SIZE - TileManager.HALF_TILE;
            falling = false;
          } else {
            ty += dy;
          }
        }
      }
    }
    checkHitBoxCorners(new Point2D.Double(x, dey + 1));
    if (!hitBoxSW && !hitBoxSE) {
      falling = true;
    }

    tempCoords.setLocation(tx, ty);
    destCoords.setLocation(dex, dey);
    delta.setLocation(dx, dy);
  }

  @Override
  public void render(Graphics2D g2d) {
    double x = coords.getX();
    double y = coords.getY();

    // Draw the mushroom

    g2d.drawImage(TileManager.getTileImage(animation[currentAnim].getFrame()),
        (int) (x - TileManager.HALF_TILE), (int) (y - TileManager.HALF_TILE), null);
    drawHit(g2d);
    drawHitBox(g2d);
  }
}
