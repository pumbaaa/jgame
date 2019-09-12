package mapObject;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import gameState.GameStateManager;
import tileManager.TileManager;

public class Mushroom extends MapObject {
  private final int ANIM_LEFT = 0;
  private final int ANIM_STANDBY = 1;
  private final int ANIM_RIGHT = 2;
  private final int ANIM_DEATH = 3;
  private final int ANIM_TOTAL = 4;

  public Mushroom(GameStateManager sm) {
    super(sm);

    faceRight = true;

    hp = 4;
    score = 10000;

    moveAcceleration = 0.2;
    moveSpeed = 0.1;
    moveSpeedMax = 2;
    moveStopAcceleration = 0.2;

    jumpStopSpeed = 0.2;
    jumpInitialSpeed = -3.0;
    jumpSpeed = 2.0;
    fallSlow = 0.5;
    fallSpeed = 0.2;
    fallSpeedMax = 10;
    maxJumpHeight = TileManager.TILE_SIZE * 3;

    spawnPoint.setLocation(new Point2D.Double(50.0, 50.0));
    coords.setLocation(spawnPoint);

    hitBox.setSize(12, 12);
    timeAfterDeath = 100;

    // TILEDATA
    {
      animation = new Animation[ANIM_TOTAL];
      int[] tAnimDeath = { (10 * 32 + 21), (10 * 32 + 22), (10 * 32 + 23),
          (10 * 32 + 24), (10 * 32 + 25), (10 * 32 + 26), (10 * 32 + 27),
          (10 * 32 + 28), (10 * 32 + 29), (10 * 32 + 30), (10 * 32 + 31),
          (11 * 32 + 26), (11 * 32 + 27), (11 * 32 + 28), (11 * 32 + 29),
          (11 * 32 + 30), (11 * 32 + 31) };
      animation[ANIM_LEFT] = new Animation((8 * 32 + 21), 6);
      animation[ANIM_STANDBY] = new Animation((9 * 32 + 21), 6);
      animation[ANIM_RIGHT] = new Animation((7 * 32 + 21), 6);
      animation[ANIM_DEATH] = new Animation(tAnimDeath);
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
      jump();
      checkTileMapCollision(true, true, true, true);
      limitToScreen();
      setPos(tempCoords);
    } else {
      stopXMovement(0.025);
      checkTileMapCollision(true, true, true, true);
      setPos(tempCoords);
      currentAnim = ANIM_DEATH;
      --timeAfterDeath;
      if (animation[ANIM_DEATH].hasPlayedOnce() && timeAfterDeath < 0) {
        removeMe();
      }
    }

  }

  private void move() {
    double dx = delta.getX();
    double dy = delta.getY();

    if (kRight) {
      dx += moveAcceleration;
      if (dx > moveSpeedMax) {
        dx = moveSpeedMax;
      }
    } else if (kLeft) {
      dx -= moveAcceleration;
      if (dx < -moveSpeedMax) {
        dx = -moveSpeedMax;
      }
    } else {
      if (dx > 0) {
        dx -= moveStopAcceleration;
        if (dx < 0) {
          dx = 0;
        }
      } else if (dx < 0) {
        dx += moveStopAcceleration;
        if (dx > 0) {
          dx = 0;
        }
      }
    }

    if (dx > 0.0) {
      currentAnim = ANIM_RIGHT;
      animation[ANIM_LEFT].reset();
      animation[ANIM_STANDBY].reset();
    } else if (dx < 0.0) {
      currentAnim = ANIM_LEFT;
      animation[ANIM_RIGHT].reset();
      animation[ANIM_STANDBY].reset();
    } else {
      currentAnim = ANIM_STANDBY;
      animation[ANIM_LEFT].reset();
      animation[ANIM_RIGHT].reset();
    }
    delta.setLocation(dx, dy);
  }

  private void jump() {
    double dx = delta.getX();
    double dy = delta.getY();

    // If not falling and triggered jump action, set the initial jump speed
    if (!falling && kJump && !jumping) {
      dy = jumpInitialSpeed;
      initialJumpPosY = coords.getY();
      jumping = true;
    }

    // If jumping but not falling
    if (jumping && !falling) {
      // If still holding the jump button
      if (kJump) {
        dy += -jumpSpeed;
      }

      // if not holding the jump button, but the jump has reached it's max
      if (!kJump || ((coords.getY() - initialJumpPosY) < maxJumpHeight)) {
        jumping = false;
        falling = true;
      }
    }

    if (falling) {
      // If falling but still on the way up after jump, then stop the jump
      if (dy < 0.0) {
        dy += jumpStopSpeed;
      }

      // If object falls downwards then add fall speed
      if (dy >= 0.0) {
        dy += fallSpeed;
      }

      // If object if falling faster than fallSpeedMax, then limit the fall
      // speed
      if (dy > fallSpeedMax) {
        dy = fallSpeedMax;
      }
    }
    delta.setLocation(dx, dy);
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

    int mapx = getMapX(x);
    int mapy = getMapY(y);

    double dex = x + dx;
    double dey = y + dy;

    if (left || right) {
      checkHitBoxCorners(new Point2D.Double(dex, y));
      if (left) {
        if (dx < 0.0) {
          if (hitBoxNW || hitBoxSW) {
            dx = 0.0;
            kRight = true;
            kLeft = false;
            faceRight = true;
            tx = mapx * TileManager.TILE_SIZE + TileManager.HALF_TILE;
          } else {
            tx += dx;
          }
        }
      }
      if (right) {
        if (dx > 0.0) {
          if (hitBoxNE || hitBoxSE) {
            dx = 0.0;
            kLeft = true;
            kRight = false;
            faceRight = false;
            tx = (mapx + 1) * TileManager.TILE_SIZE - TileManager.HALF_TILE;
          } else {
            tx += dx;
          }
        }
      }
    }
    if (up || down) {
      checkHitBoxCorners(new Point2D.Double(x, dey));
      if (up) {
        if (dy < 0.0) {
          if (hitBoxNW || hitBoxNE) {
            dy = 0.0;
            ty = mapy * TileManager.TILE_SIZE + TileManager.HALF_TILE;
          } else {
            ty += dy;
          }
        }
      }
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
        (int) (x - TileManager.HALF_TILE), (int) (y - TileManager.HALF_TILE),
        null);
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

  @Override
  public void keyJump(boolean dir) {
    kJump = dir;
  }

}
