package mapObject;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import game.Game;
import gameState.GameStateManager;
import tileManager.TileManager;

import java.awt.Rectangle;
import java.awt.geom.Point2D;

public abstract class MapObject {
  protected boolean showHitBox = false;

  protected GameStateManager sm;
  // Which image to use for the "normal" state.
  Animation[] animation;
  // array index of the active tile
  protected int currentAnim;

  // Background
  protected BufferedImage image;
  // Current Position (the total map x)
  protected Point2D.Double coords = new Point2D.Double(0.0, 0.0);
  // Position Movement Vector
  protected Point2D.Double delta = new Point2D.Double(0.0, 0.0);
  // Temporary positions to check boundaries
  protected Point2D.Double tempCoords = new Point2D.Double(0.0, 0.0);
  protected Point2D.Double destCoords = new Point2D.Double(0.0, 0.0);
  // hitBox for Objects versus Tiles
  protected Dimension hitBoxTile = new Dimension(TileManager.TILE_SIZE,
      TileManager.TILE_SIZE);

  // hitBox for Objects versus Objects
  protected Dimension hitBox = new Dimension(TileManager.TILE_SIZE,
      TileManager.TILE_SIZE);

  // Where should the player spawn when entering the play field
  protected Point2D.Double spawnPoint = new Point2D.Double(0.0, 0.0);

  protected boolean alive;
  protected boolean remove;
  protected int timeAfterDeath;

  protected int hp;
  protected int score;
  protected boolean isHit;

  protected int collisionAction;
  protected int collisionTeleport;

  // The speed of movement;
  protected double s;
  // Speed modifier for movement
  protected double moveAcceleration;
  protected double moveSpeed;
  protected double moveSpeedMax;
  protected double moveStopAcceleration;

  // Offset for setting the centre point
  protected Point2D.Double locationOffset;

  // Face right? If no, then facing left.
  protected boolean faceRight;

  // User modifiers, direction, jumping, shooting, falling etc
  protected boolean kUp, kDown, kLeft, kRight;
  protected boolean kJump, kFire;
  protected boolean jumping, falling, grounded;

  // Falling
  protected double fallSpeed;
  protected double fallSpeedMax;
  protected double fallSlow;

  // Jump modifiers
  protected double jumpSpeed;
  protected double jumpStopSpeed;
  protected double initialJumpPosY;
  protected double maxJumpHeight;
  protected double jumpInitialSpeed;

  protected boolean hitBoxNW;
  protected boolean hitBoxNE;
  protected boolean hitBoxSW;
  protected boolean hitBoxSE;

  public MapObject(GameStateManager sm) {
    this.sm = sm;
    alive = true;
    locationOffset = new Point2D.Double(0.0, 0.0);
  }

  public void setSpawnPos(Point2D.Double spawnPoint) {
    this.spawnPoint.setLocation(spawnPoint.getX(), spawnPoint.getY());
    coords.setLocation(this.spawnPoint);
  }

  public void setSpawnPoint(Point spawnPoint) {
    int xt = (int) ((spawnPoint.getX() * TileManager.TILE_SIZE)
        + TileManager.HALF_TILE);
    int yt = (int) ((spawnPoint.getY() * TileManager.TILE_SIZE)
        + TileManager.HALF_TILE);
    this.spawnPoint.setLocation(xt, yt);
    coords.setLocation(this.spawnPoint);
  }

  public void setSpeed(double moveSpeed) {
    this.moveSpeed = moveSpeed;
  }

  public void setPos(Point2D.Double coords) {
    this.coords.setLocation(coords.getX() % Game.WIN_W,
        coords.getY() % Game.WIN_H);
  }

  public void setVec(Point2D.Double delta) {
    this.delta.setLocation(delta.getX(), delta.getY());
  }

  public void update() {
    if (alive) {
      checkTileMapCollision(true, true, true, true);
      limitToScreen();
      setPos(tempCoords);
    }
  }

  public int getActionOnCollision() {
    return collisionAction;
  }

  public int getTeleport() {
    return collisionTeleport;
  }

  public void setActionOnCollision(int action) {
    collisionAction = action;
  }

  public void setTeleport(int teleport) {
    collisionTeleport = teleport;
  }

  // Check if the current objects hit box intersects with another map objects
  // hit box
  public boolean intersect(MapObject targetMapObject) {
    return getHitBox().intersects(targetMapObject.getHitBox());
  }

  // Check if the current objects hit box intersects with another map objects
  // hit box. Works with rectangles instead of an object
  public boolean intersect(Rectangle target) {
    return getHitBox().intersects(target);
  }

  // Return the targets hit box
  public Rectangle getHitBox() {
    Rectangle r = new Rectangle(
        new Point((int) coords.getX() - hitBox.width / 2,
            (int) coords.getY() - hitBox.height / 2),
        new Dimension(hitBox.width, hitBox.height));
    return r;
  }

  // Check if something is in the way of the objects movement, and take action
  // accordingly.
  // Will also update the coordinates
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

    // The destination coordinates to check against
    double dex = x + dx;
    double dey = y + dy;

    // Should we check left or right?
    if (left || right) {
      checkHitBoxCorners(new Point2D.Double(dex, y));
      // Should we check the left side?
      if (left) {
        // Are we moving left?
        if (dx < 0.0) {
          // Did any of the corners to the left hit a solid tile?
          if (hitBoxNW || hitBoxSW) {
            // We hit something, so we stop the movement and move to the edge of
            // the solid tile
            dx = 0.0;
            tx = mapx * TileManager.TILE_SIZE + TileManager.HALF_TILE;
          } else {
            // We did not hit anything, so we can move in that direction
            tx += dx;
          }
        }
      }
      // Should we check the right side?
      if (right) {
        // Are we moving right?
        if (dx > 0.0) {
          // Did any of the corners to the right hit a solid tile?
          if (hitBoxNE || hitBoxSE) {
            // We hit something, so we stop the movement and move to the edge of
            // the solid tile
            dx = 0.0;
            tx = (mapx + 1) * TileManager.TILE_SIZE - TileManager.HALF_TILE;
          } else {
            // We did not hit anything, so we can move in that direction
            tx += dx;
          }
        }
      }
    }
    // Should we check the upper or lower side?
    if (up || down) {
      checkHitBoxCorners(new Point2D.Double(x, dey));
      // Should we check the upper side?
      if (up) {
        // Are we moving upwards (jumping)?
        if (dy < 0.0) {
          // Did any of the corners upwards hit a solid tile?
          if (hitBoxNW || hitBoxNE) {
            // We hit something, so we stop the movement and move to the edge of
            // the solid tile
            dy = 0.0;
            ty = mapy * TileManager.TILE_SIZE + TileManager.HALF_TILE;
          } else {
            // We did not hit anything, so we can move in that direction
            ty += dy;
          }
        }
      }
      // Should we check the lower side?
      if (down) {
        // Are we moving downwards (falling)?
        if (dy > 0.0) {
          // Did any of the corners downwards hit a solid tile?
          if (hitBoxSW || hitBoxSE) {
            // We hit something, so we stop the movement and move to the edge of
            // the solid tile
            dy = 0.0;
            ty = (mapy + 1) * TileManager.TILE_SIZE - TileManager.HALF_TILE;
            // We hit a block below us, so should stop falling
            falling = false;
          } else {
            // We did not hit anything, so we can move in that direction
            ty += dy;
          }
        }
      }
    }
    // Do we have a solid tile below us?
    checkHitBoxCorners(new Point2D.Double(x, dey + 1));
    if (!hitBoxSW && !hitBoxSE) {
      // Nope, so we should start falling
      falling = true;
    }

    tempCoords.setLocation(tx, ty);
    destCoords.setLocation(dex, dey);
    delta.setLocation(dx, dy);
  }

  // Check if the corners of the hit box x, y, x + TILE_SIZE, y + TILE_SIZE
  // has a tile SOLID-tag attached to it. Set the corresponding boolean
  // This method SHOULD correct calculate the corners of the box
  protected void checkHitBoxCorners(Point2D.Double checkCoords) {
    int WTile = (int) (checkCoords.getX() - TileManager.HALF_TILE)
        / TileManager.TILE_SIZE;
    int ETile = (int) (checkCoords.getX() + TileManager.HALF_TILE - 1)
        / TileManager.TILE_SIZE;
    int NTile = (int) (checkCoords.getY() - TileManager.HALF_TILE)
        / TileManager.TILE_SIZE;
    int STile = (int) (checkCoords.getY() + TileManager.HALF_TILE - 1)
        / TileManager.TILE_SIZE;

    int NWTileType = sm.getTileType(new Point(WTile, NTile));
    int NETileType = sm.getTileType(new Point(ETile, NTile));
    int SWTileType = sm.getTileType(new Point(WTile, STile));
    int SETileType = sm.getTileType(new Point(ETile, STile));

    hitBoxNW = NWTileType == 1;
    hitBoxNE = NETileType == 1;
    hitBoxSW = SWTileType == 1;
    hitBoxSE = SETileType == 1;
  }

  // Return the tileMap position from global coordinates
  protected Point getMapCoords(Point2D.Double coords) {
    int mapx = (int) (coords.getX() / TileManager.TILE_SIZE);
    int mapy = (int) (coords.getY() / TileManager.TILE_SIZE);
    return new Point(mapx, mapy);
  }

  protected int getMapX(double x) {
    return (int) (x / TileManager.TILE_SIZE);
  }

  protected int getMapY(double y) {
    return (int) (y / TileManager.TILE_SIZE);
  }

  protected void limitToScreen() {
    double tx = tempCoords.getX();
    double ty = tempCoords.getY();

    if (tx > Game.WIN_W - TileManager.TILE_SIZE) {
      tx = Game.WIN_W - TileManager.TILE_SIZE;
    } else if (tx < 0.0) {
      tx = 0.0;
    }

    if (ty > Game.WIN_H - TileManager.TILE_SIZE) {
      ty = Game.WIN_H - TileManager.TILE_SIZE;
    } else if (ty < 0.0) {
      ty = 0.0;
    }

    tempCoords.setLocation(tx, ty);
  }

  protected void stopXMovement(double stopVel) {
    double dx = delta.getX();
    if (faceRight) {
      dx += -stopVel;
      if (dx < 0.0) {
        dx = 0.0;
      }
    } else {
      dx += stopVel;
      if (dx > 0.0) {
        dx = 0.0;
      }
    }
    delta.setLocation(dx, delta.getY());
  }

  public Point2D.Double getCoords() {
    return coords;
  }

  public boolean facingRight() {
    return faceRight;
  }

  public boolean isAlive() {
    return alive;
  }

  public boolean kill() {
    boolean isKilled = false;
    --hp;
    if (hp <= 0) {
      alive = false;
      isKilled = true;
    } else {
      isHit = true;
    }
    return isKilled;
  }

  public void removeMe() {
    remove = true;
  }

  public int getScore() {
    return score;
  }

  public boolean timeToRemove() {
    return remove;
  }

  public void keyRight(boolean dir) {
    kRight = dir;
  }

  public void keyLeft(boolean dir) {
    kLeft = dir;
  }

  public void keyUp(boolean dir) {
    kUp = dir;
  }

  public void keyDown(boolean dir) {
    kDown = dir;
  }

  public void keyFire(boolean dir) {
    kFire = dir;
  }

  public void keyJump(boolean dir) {
    kJump = dir;
  }

  public boolean getKeyRight() {
    return kRight;
  }

  public boolean geyKeyLeft() {
    return kLeft;
  }

  public boolean getKeyUp() {
    return kUp;
  }

  public boolean getKeyDown() {
    return kDown;
  }

  public boolean getKeyFire() {
    return kFire;
  }

  public boolean getKeyJump() {
    return kJump;
  }

  // Draw the hit box, for debugging purposes
  public void drawHitBox(Graphics2D g2d) {
    if (showHitBox) {
      g2d.setColor(Color.ORANGE);
      g2d.drawRect((int) (coords.getX() - hitBox.getWidth() / 2),
          (int) (coords.getY() - hitBox.getHeight() / 2),
          (int) hitBox.getWidth(), (int) hitBox.getHeight());
      g2d.setColor(Color.BLACK);
    }
  }

  public abstract void render(Graphics2D g2d);

  // Flash the thing that is hit, adjusts to the size of the hitbox
  public void drawHit(Graphics2D g2d) {
    if (isHit) {
      int sx = hitBox.width;
      int sy = hitBox.height;

      int x = (int) coords.getX() - (sx / 2);
      int y = (int) coords.getY() - (sy / 2);

      g2d.setColor(Color.WHITE);
      g2d.fillOval(x, y, sx, sy);
      isHit = false;
    }
  }

  public int getHp(){
    return hp;
  }
}
