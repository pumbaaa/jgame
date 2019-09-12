package mapObject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import audioPlayer.AudioPlayer;
import game.Game;
import gameState.GameStateManager;
import gameState.PlayerSave;
import tileManager.TileManager;

public class Player extends MapObject {
  private ArrayList<MapObject> ammo;
  private final int ANIM_LEFT = 0;
  private final int ANIM_STANDBY_RIGHT = 1;
  private final int ANIM_STANDBY_LEFT = 2;
  private final int ANIM_RIGHT = 3;
  private final int ANIM_DEATH = 4;
  private final int ANIM_TOTAL = 5;
  private final int WEAPON_LEFT = (7 * 32 + 20);
  private final int WEAPON_LEFT_QTILE = 3;
  private final int WEAPON_RIGHT = (7 * 32 + 20);
  private final int WEAPON_RIGHT_QTILE = 2;

  private long ammoTimer;
  private int ammoDelay;
  private int ammoFired;
  private boolean recoil;
  private double recoilDistance;
  private boolean spawnBullet;

  public Player(GameStateManager sm) {
    super(sm);

    ammo = new ArrayList<MapObject>();
    ammoDelay = 150000000; // Nano seconds. divide with 1.000.000.000 to get s
    ammoTimer = System.nanoTime() - ammoDelay;
    recoil = false;
    recoilDistance = 2.0;
    spawnBullet = false;

    faceRight = true;

    moveAcceleration = 2.0;
    moveSpeed = 0.1;
    moveSpeedMax = 2;
    moveStopAcceleration = 2.0;

    // Make the character jump just about 2.5 tiles
    jumpStopSpeed = 0.4;
    jumpInitialSpeed = -4.0;
    jumpSpeed = 2.0;
    maxJumpHeight = TileManager.TILE_SIZE * 2;

    fallSpeed = 0.3;
    fallSpeedMax = 5.0;
    fallSlow = 0.9;

    timeAfterDeath = 50;

    spawnPoint.setLocation(1 * TileManager.TILE_SIZE + TileManager.HALF_TILE,
        14 * TileManager.TILE_SIZE + TileManager.HALF_TILE);
    coords.setLocation(spawnPoint);

    hitBox.setSize(12, 12);

    // TILEDATA
    {
      // Put the animation into the correct array, and set the delay between
      // frames
      animation = new Animation[ANIM_TOTAL];
      animation[ANIM_LEFT] = new Animation((8 * 32 + 12), 4);
      animation[ANIM_LEFT].setDelayLimit(10);
      animation[ANIM_STANDBY_LEFT] = new Animation((10 * 32 + 12), 4);
      animation[ANIM_STANDBY_LEFT].setDelayLimit(50);
      animation[ANIM_STANDBY_RIGHT] = new Animation((9 * 32 + 12), 4);
      animation[ANIM_STANDBY_RIGHT].setDelayLimit(50);
      animation[ANIM_RIGHT] = new Animation((7 * 32 + 12), 4);
      animation[ANIM_RIGHT].setDelayLimit(10);
      animation[ANIM_DEATH] = new Animation((11 * 32 + 12), 9);
      animation[ANIM_DEATH].setDelayLimit(5);
      animation[ANIM_DEATH].playOnce();
    }
    AudioPlayer.play(AudioPlayer.SFX_SPAWN);
  }

  public boolean didShoot() {
    boolean sb = spawnBullet;
    spawnBullet = false;
    return sb;
  }

  @Override
  public void update() {
    if (alive) {
      tempCoords.setLocation(coords);
      shooting();
      move();
      jump();
      // Call the super class collision check
      checkTileMapCollision(true, true, true, true);
      // Call the specialised collision check for recoil
      checkTileMapCollisionRecoil();
      limitToScreen();
      setPos(tempCoords);
      // animation[currentAnim].nextFrame();
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

  // Spawn new bullets if kFire is triggered and the timer is ready
  private void shooting() {
    if (kFire && System.nanoTime() - ammoDelay >= ammoTimer) {
      // Keeping the shots tied to the player. If player dies, so does the shots
      spawnBullet = true;
      ammoTimer = System.nanoTime();
      ammoFired = 2;

      // The recoil
      recoil = true;

      // Play a sound when shooting
      AudioPlayer.play(AudioPlayer.SFX_SHOOT);
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
    // update the animation so it corresponds with
    // movement/non-movement/direction
    if (kRight) {
      currentAnim = ANIM_RIGHT;
      animation[ANIM_LEFT].reset();
      animation[ANIM_STANDBY_RIGHT].reset();
      animation[ANIM_STANDBY_LEFT].reset();
    } else if (kLeft) {
      currentAnim = ANIM_LEFT;
      animation[ANIM_RIGHT].reset();
      animation[ANIM_STANDBY_RIGHT].reset();
      animation[ANIM_STANDBY_LEFT].reset();
    } else if (!kRight && !kLeft && faceRight) {
      currentAnim = ANIM_STANDBY_RIGHT;
      animation[ANIM_LEFT].reset();
      animation[ANIM_RIGHT].reset();
      animation[ANIM_STANDBY_LEFT].reset();
    } else {
      currentAnim = ANIM_STANDBY_LEFT;
      animation[ANIM_LEFT].reset();
      animation[ANIM_RIGHT].reset();
      animation[ANIM_STANDBY_RIGHT].reset();
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
      // Play a sound when jumping
      AudioPlayer.play(AudioPlayer.SFX_SSH_SHORT);
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
        if (kUp && PlayerSave.has(Game.ACTION_SLOWFALL)) {
          dy = fallSlow;
        } else {
          dy += fallSpeed;
        }
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
  // up, down, left, and right is marking which direction that should be checked
  protected void checkTileMapCollisionRecoil() {

    double dx = 0.0;

    // In which direction is the recoil
    if (recoil) {
      if (faceRight) {
        dx = -recoilDistance;
      } else {
        dx = recoilDistance;
      }
    }

    // Just shorten up the variables a bit, save back at end
    double x = tempCoords.getX();
    double tx = x;

    int mapx = getMapX(x);

    // The destination coordinates to check against
    double dex = x + dx;

    checkHitBoxCorners(new Point2D.Double(dex, coords.getY()));
    // Are we moving left but not facing left? (to prevent recoil speed boost)
    if (dx < 0.0 && faceRight && !kLeft) {
      // Did any of the corners to the left hit a solid tile?
      if (hitBoxNW || hitBoxSW) {
        // We hit something, so we stop the movement and move to the edge of the
        // solid tile
        tx = mapx * TileManager.TILE_SIZE + TileManager.HALF_TILE;
      } else {
        // We did not hit anything, so we can move in that direction
        tx += dx;
      }
    }
    // Are we moving right but not facing right? (to prevent recoil speed boost)
    if (dx > 0.0 && !faceRight && !kRight) {
      // Did any of the corners to the right hit a solid tile?
      if (hitBoxNE || hitBoxSE) {
        // We hit something, so we stop the movement and move to the edge of
        // the solid tile
        tx = (mapx + 1) * TileManager.TILE_SIZE - TileManager.HALF_TILE;
      } else {
        // We did not hit anything, so we can move in that direction
        tx += dx;
      }
    }

    if (recoil) {
      recoil = false;
    }

    tempCoords.setLocation(tx, tempCoords.getY());
    destCoords.setLocation(dex, destCoords.getY());
  }

  @Override
  public void render(Graphics2D g2d) {
    double x = coords.getX();
    double y = coords.getY();

    // Draw the player
    g2d.drawImage(TileManager.getTileImage(animation[currentAnim].getFrame()),
        (int) x - TileManager.HALF_TILE, (int) y - TileManager.HALF_TILE, null);

    // At the moment all bullets are tied to the player, move to the current
    // state or something like that
    for (MapObject bullet : ammo) {
      bullet.render(g2d);
    }

    // The gun
    if (faceRight) {
      g2d.drawImage(
          TileManager.getQuarterTileImage(WEAPON_RIGHT, WEAPON_RIGHT_QTILE),
          (int) x, (int) y, null);
    } else {
      g2d.drawImage(
          TileManager.getQuarterTileImage(WEAPON_LEFT, WEAPON_LEFT_QTILE),
          (int) x - TileManager.HALF_TILE, (int) y, null);
    }

    // If player has fired, display the flash
    g2d.setColor(Color.YELLOW);
    if (ammoFired > 0) {
      int offset = 0;
      if (faceRight) {
        offset = 10;
      } else {
        offset = -10;
      }
      g2d.drawImage(TileManager.getTileImage(9 * 32 + 8),
          (int) coords.getX() + offset - TileManager.HALF_TILE,
          (int) coords.getY() - 5, null);
      --ammoFired;
    }
    drawHitBox(g2d);
  }

  @Override
  public void keyRight(boolean dir) {
    kRight = dir;
    if (!kFire) {
      faceRight = true;
    }
  }

  @Override
  public void keyLeft(boolean dir) {
    kLeft = dir;
    if (!kFire) {
      faceRight = false;
    }
  }
}
