package mapObject;

import java.awt.Graphics2D;
import game.Game;
import gameState.GameStateManager;
import tileManager.TileManager;

public class SparklyBoss extends MapObject {
  private final int ANIM_TOTAL = 2;
  private final int ANIM_STANDBY = 0;
  private final int ANIM_DEATH = 1;

  private double sinCounter;

  public SparklyBoss(GameStateManager sm) {
    super(sm);

    faceRight = true;

    hp = 40;
    score = 1000000;

    moveAcceleration = 0.2;
    moveSpeed = 0.1;
    moveSpeedMax = 2;
    moveStopAcceleration = 0.2;

    hitBox.setSize((TileManager.TILE_SIZE * 2) - 4,
        (TileManager.TILE_SIZE * 2) - 4);
    timeAfterDeath = 200;

    // TILEDATA
    {
      animation = new Animation[ANIM_TOTAL];
      int[] tAnimAlive = { (18 * 32 + 1), (18 * 32 + 4), (18 * 32 + 7),
          (18 * 32 + 10), (18 * 32 + 13), (18 * 32 + 16), (18 * 32 + 19),
          (18 * 32 + 22), (18 * 32 + 25), (18 * 32 + 28) };
      int[] tAnimDeath = { (21 * 32 + 1), (21 * 32 + 4), (21 * 32 + 7),
          (21 * 32 + 10), (21 * 32 + 13), (21 * 32 + 16), (21 * 32 + 19),
          (21 * 32 + 22), (21 * 32 + 15), (21 * 32 + 28) };
      animation[ANIM_STANDBY] = new Animation(tAnimAlive);
      animation[ANIM_DEATH] = new Animation(tAnimDeath);
      animation[ANIM_STANDBY].setDelayLimit(50);
      animation[ANIM_DEATH].setDelayLimit(10);
    }
  }

  @Override
  public void update() {
    if (alive) {
      tempCoords.setLocation(coords);
      move();
      limitToScreen();
      setPos(tempCoords);
    } else {
      setPos(tempCoords);
      currentAnim = ANIM_DEATH;
      --timeAfterDeath;
      if (timeAfterDeath < 0) {
        removeMe();
      }
    }

  }

  // Limit the movement to the screen, and change direction if needed
  @Override
  protected void limitToScreen() {
    double tx = tempCoords.getX();
    double ty = tempCoords.getY();
    double dx = delta.getX();
    double dy = delta.getY();

    if (tx > Game.WIN_W - (hitBox.width / 2)) {
      keyLeft(true);
      keyRight(false);
      dx = -dx;
    } else if (tx < 0.0 + (hitBox.width / 2)) {
      keyLeft(false);
      keyRight(true);
      dx = -dx;
    }

    if (ty > Game.WIN_H - (hitBox.height / 2)) {
      keyUp(true);
      keyDown(false);
      dy = -dy;
    } else if (ty < 0.0 + (hitBox.height / 2)) {
      keyUp(false);
      keyDown(true);
      dy = -dy;
    }

    delta.setLocation(dx, dy);
    tempCoords.setLocation(tx, ty);
  }

  private void move() {
    // Just shorten up the variables a bit, save back at end
    double dx = delta.getX();
    double dy = delta.getY();
    double tx = coords.getX();
    double ty = coords.getY();

    double sx = (Math.sin(Math.toRadians(sinCounter)) * 1.5);
    double sy = (Math.cos(Math.toRadians(sinCounter/3)) * 1.5);
    
    if (kRight) {
      dx += moveAcceleration;
      if (dx > moveSpeedMax) {
        dx = moveSpeedMax + sx;
      }
    } else if (kLeft) {
      dx -= moveAcceleration;
      if (dx < -moveSpeedMax) {
        dx = -moveSpeedMax + sy;
      }
    } else {
      if (dx > 0.0) {
        dx -= moveStopAcceleration;
        if (dx < 0.0) {
          dx = 0.0;
        }
      } else if (dx < 0.0) {
        dx += moveStopAcceleration;
        if (dx > 0.0) {
          dx = 0.0;
        }
      }
    }

    if (kDown) {
      dy += moveAcceleration;
      if (dy > moveSpeedMax) {
        dy = moveSpeedMax + (Math.sin(Math.toRadians(sinCounter)) * 4);
      }
    } else if (kUp) {
      dy -= moveAcceleration;
      if (dy < -moveSpeedMax) {
        dy = -moveSpeedMax;
      }
    } else {
      if (dy > 0.0) {
        dy -= moveStopAcceleration;
        if (dy < 0.0) {
          dy = 0.0;
        }
      } else if (dy < 0.0) {
        dy += moveStopAcceleration;
        if (dy > 0.0) {
          dy = 0.0;
        }
      }
    }
    ++sinCounter;
    currentAnim = ANIM_STANDBY;

    tx += dx;
    ty += dy;

    tempCoords.setLocation(tx, ty);
    delta.setLocation(dx, dy);

  }

  @Override
  public void render(Graphics2D g2d) {
    double x = coords.getX();
    double y = coords.getY();

    // Draw the animation
    for (int tx = 0; tx < 3; ++tx) {
      for (int ty = 0; ty < 3; ++ty) {
        g2d.drawImage(
            TileManager.getTileImage(
                animation[currentAnim].getFrame() + (tx - 1) + ((ty - 1) * 32)),
            (int) (x - (hitBox.width / 2) + ((tx - 1) * (hitBox.width / 2)))
                + TileManager.HALF_TILE,
            (int) (y - (hitBox.height / 2) + ((ty - 1) * (hitBox.height / 2)))
                + TileManager.HALF_TILE,
            null);
      }
    }

    drawHit(g2d);
    drawHitBox(g2d);
  }

  @Override
  public void keyRight(boolean dir) {
    kRight = dir;
    faceRight = true;
  }

  @Override
  public void keyLeft(boolean dir) {
    kLeft = dir;
    faceRight = false;
  }
}
