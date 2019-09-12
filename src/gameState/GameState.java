package gameState;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import audioPlayer.AudioPlayer;
import background.Background;
import game.Game;
import ioManager.IoManager;
import mapObject.Bullet;
import mapObject.MapObject;
import mapObject.Player;
import tileManager.TileManager;
import tileMap.RoomMap;

public abstract class GameState {
  protected Player player;

  // The enemies and projectiles (friend and foo)
  protected ArrayList<MapObject> enemy = new ArrayList<MapObject>();
  protected ArrayList<MapObject> ammo = new ArrayList<MapObject>();;

  // The intractable and other non intractable objects that are in the state
  protected ArrayList<MapObject> mapObjects = new ArrayList<MapObject>();

  // The tile map associated with the state
  protected RoomMap room;
  protected GameStateManager sm;

  protected HashMap<Integer, ArrayList<Background>> layers = new HashMap<Integer, ArrayList<Background>>();

  // Current Position (the total map x)
  protected Point2D.Double coords = new Point2D.Double(0.0, 0.0);

  // Position Movement Vector
  protected Point2D.Double delta = new Point2D.Double(0.0, 0.0);
  protected boolean autoMove;

  // The speed of movement;
  protected double s;

  public GameState(GameStateManager sm) {
    PlayerSave.reset();
    this.sm = sm;
    layers.put(GameStateManager.LAYER_BACKGROUND, new ArrayList<Background>());
    layers.put(GameStateManager.LAYER_PLAYGROUND_BEHIND_MAPOBJECTS,
        new ArrayList<Background>());
    layers.put(GameStateManager.LAYER_PLAYGROUND_INFRONTOF_MAPOBJECTS,
        new ArrayList<Background>());
    layers.put(GameStateManager.LAYER_FOREGROUND, new ArrayList<Background>());

    setPos(0.0, 0.0);
    setVec(new Point2D.Double(0.0, 0.0));
    disableAutoMove();
  }

  public void enableAutoMove() {
    autoMove = true;
  }

  public void disableAutoMove() {
    autoMove = false;
  }

  public void setPos(Point2D.Double coords) {
    this.coords.setLocation(coords.getX() % Game.WIN_W,
        coords.getY() % Game.WIN_H);
  }

  public void setPos(double x, double y) {

    this.coords.setLocation(x % Game.WIN_W, y % Game.WIN_H);
  }

  public void setVec(Point2D.Double delta) {
    this.delta.setLocation(delta.getX() % Game.WIN_W,
        delta.getY() % Game.WIN_H);
  }

  public abstract void update();

  // Pass on all calls to the current tile map
  public int getNumTilesWidth() {
    return room.getWidth();
  }

  public int getNumTilesHeight() {
    return room.getHeight();
  }

  public int getImageId(Point coords) {
    return room.getImageId(coords);
  }

  public int getType(Point coords) {
    return room.getType(coords);
  }

  public void modify(int imageId, int type, Point coords) {
    room.setTile(imageId, type, coords);
  }

  public void modifyImageId(int imageId, Point coords) {
    room.setImageId(coords, imageId);
  }

  public void modifyType(Point coords, int type) {
    room.setType(coords, type);
  }

  protected void load(String fileName) {
    IoManager io = new IoManager(fileName);
    io.load();
    room = io.getRoom();
  }

  public void updateBackground() {
    // Background
    for (Background b : layers.get(GameStateManager.LAYER_BACKGROUND)) {
      b.update();
    }
  }

  public void updatePlaygroundBack() {
    // Playground behind map objects
    for (Background b : layers
        .get(GameStateManager.LAYER_PLAYGROUND_BEHIND_MAPOBJECTS)) {
      b.update();
    }
  }

  public void updatePlaygroundFront() {
    // Playground in front of map objects
    for (Background b : layers
        .get(GameStateManager.LAYER_PLAYGROUND_INFRONTOF_MAPOBJECTS)) {
      b.update();
    }
  }

  public void updateForeground() {
    // Foreground
    for (Background b : layers.get(GameStateManager.LAYER_FOREGROUND)) {
      b.update();
    }
  }

  protected void mapObjectInteraction(MapObject mo) {
    // Check if the player can interact with a map object
    if (mo.intersect(player)) {
      // Check if the player activates the map object by holding up
      if (player.getKeyUp()) {
        switch (mo.getActionOnCollision()) {
        case Game.ACTION_TELEPORT:
          // If it's a teleport (a door), and the player has a key, then go
          actionTeleport(mo);
          break;
        case Game.ACTION_KEY:
          // if it's a key, then let the player pick it up
          actionKey(mo);
          break;
        case Game.ACTION_SLOWFALL:
          // if it's a key, then let the player pick it up
          actionSlowFall(mo);
          break;
        }
      } else {
        // Then it's something that will affect the player when standing on it
        switch (mo.getActionOnCollision()) {
        case Game.ACTION_DAMAGE:
          // if it's something that does damage, then let the player take damage
          actionDamage();
          break;
        }
      }
    }
  }

  protected void actionTeleport(MapObject mo) {
    if (PlayerSave.has(Game.ACTION_KEY)) {
      AudioPlayer.play(AudioPlayer.SFX_DOOR);
      sm.setState(mo.getTeleport());
    }
  }

  protected void actionKey(MapObject mo) {
    PlayerSave.add(Game.ACTION_KEY);
    mo.kill();
  }

  protected void actionSlowFall(MapObject mo) {
    PlayerSave.add(Game.ACTION_SLOWFALL);
    mo.kill();
  }

  protected void actionDamage() {
    player.kill();
  }

  protected void updateMapObjects() {

    // Map Objects
    for (MapObject mo : mapObjects) {
      mo.update();
      mapObjectInteraction(mo);
    }

    // Map Objects
    for (MapObject e : enemy) {
      e.update();
    }

    // Player
    player.update();
    // Update each bullet
    for (MapObject bullet : ammo) {
      bullet.update();
    }

    if (player.didShoot()) {
      int bulletDisplacement = -TileManager.HALF_TILE / 2;
      if (player.facingRight()) {
        bulletDisplacement = (TileManager.HALF_TILE / 2);
      }
      ammo.add(new Bullet(sm,
          new Point2D.Double(player.getCoords().getX() + bulletDisplacement,
              player.getCoords().getY() + TileManager.HALF_TILE / 2 / 2),
          player.facingRight()));
      	sm.shakeScreen();
    }

    // Resolve collisions
    for (MapObject e : enemy) {
      for (MapObject a : ammo) {
        if (a.isAlive() && e.isAlive()) {
          if (a.intersect(e)) {
            AudioPlayer.play(AudioPlayer.SFX_BOOM);
            a.kill();
            if (e.kill()) {
              AudioPlayer.play(AudioPlayer.SFX_ENEMYDEATH);
              PlayerSave.addPoint(PlayerSave.PLAYER_KILLCOUNT);
              PlayerSave.addPoint(PlayerSave.PLAYER_SCORE, e.getScore());
            }
          }
        }
      }

      if (player.intersect(e) && e.isAlive() && player.isAlive()) {
        player.kill();
        PlayerSave.addPoint(PlayerSave.PLAYER_DEATHCOUNT);
        AudioPlayer.play(AudioPlayer.SFX_DEATH);
      }
    }

    // Remove bullets, enemies and map objects that has remove tag set
    // Some enemies will not be removed, so the dead will stack up on screen
    for (int i = 0; i < ammo.size(); ++i) {
      if (ammo.get(i).timeToRemove()) {
        ammo.remove(i);
      }
    }

    for (int i = 0; i < mapObjects.size(); ++i) {
      if (mapObjects.get(i).timeToRemove()) {
        mapObjects.remove(i);
      }
    }
  }

  protected void renderBossHp(Graphics2D g2d, MapObject boss) {
    for (int y = 0; y < boss.getHp(); ++y) {
      g2d.setColor(Color.RED);
      g2d.drawLine(20, 20 + (y * 2), 30, 20 + (y * 2));
    }
  }

  protected void renderTextOverlaySmall(Graphics2D g2d, String[] t,
      Point coords) {
    int x = (int) coords.getX();
    int y = (int) coords.getY();

    boolean lineOffset = true;
    for (String row : t) {
      renderSmallFont(g2d, x, y, row, false, lineOffset);
      if (lineOffset) {
        lineOffset = false;
        ++y;
      } else {
        lineOffset = true;
      }
    }
  }

  protected void renderTextOverlay(Graphics2D g2d, String s, Point coords,
      int columns) {
    int x = (int) coords.getX();
    int y = (int) coords.getY();
    s = s.toLowerCase();
    int yOffset = 0;

    for (int i = 0; i < s.length(); ++i) {
      int c = (int) s.charAt(i);
      c = getAsciiPos(c);
      if (i % columns == 0 && i != 0) {
        ++yOffset;
      }
      // Row 6 + c(position in alphabet) in tile sheet
      int tile = 6 * 32 + c;
      int tx = (x + i % columns) * TileManager.TILE_SIZE;
      int ty = (y + yOffset) * TileManager.TILE_SIZE;
      g2d.drawImage(TileManager.getTileImage(tile), tx, ty, null);
    }
  }

  protected void renderBackgrounds(Graphics2D g2d) {
    for (Background b : layers.get(GameStateManager.LAYER_BACKGROUND)) {
      b.render(g2d);
    }

  }

  protected void renderPlaygroundsBack(Graphics2D g2d) {
    // Playground, behind the map objects
    for (Background b : layers
        .get(GameStateManager.LAYER_PLAYGROUND_BEHIND_MAPOBJECTS)) {
      b.render(g2d);
    }

  }

  protected void renderPlayground(Graphics2D g2d) {
    // Map position, where on the map to start reading tiles (in tiles)
    for (int tilePosX = 0; tilePosX < Game.WIN_COLS; ++tilePosX) {
      for (int tilePosY = 0; tilePosY < Game.WIN_ROWS; ++tilePosY) {
        // Create the coordinates for getting the tile image
        Point tileSheetPos = new Point(((tilePosX) % room.getWidth()),
            ((tilePosY) % room.getHeight()));
        // If the tile image is not id 0 (transparent) then draw the image
        int tx = (tilePosX * TileManager.TILE_SIZE)
            - (int) (coords.getX() % TileManager.TILE_SIZE);
        int ty = (tilePosY * TileManager.TILE_SIZE)
            - (int) (coords.getY() % TileManager.TILE_SIZE);
        if (room.getImageId(tileSheetPos) != 0) {
          int id = room.getImageId(new Point(tileSheetPos));
          int set = room.getSet(tileSheetPos);
          g2d.drawImage(TileManager.getTileImage(id, set), tx, ty, null);
        }

        /*
         * // Draw the tile type (for debugging) g2d.setFont(new Font("Arial",
         * Font.PLAIN, 8)); g2d.drawString("" + room.getType(new
         * Point(tileSheetPos)), tx + 4, ty + 12);
         */
      }
    }
  }

  protected void renderMapObjects(Graphics2D g2d) {
    // Map Objects
    for (MapObject mo : mapObjects) {
      mo.render(g2d);
    }

    // Map Objects
    for (MapObject e : enemy) {
      e.render(g2d);
    }

    // Player
    player.render(g2d);
    if (player.timeToRemove()) {
      renderTextOverlay(g2d,
          "Your data core  " + "has been        " + "uploaded to the "
              + "cloud.          " + "                " + "Press Fire to   "
              + "play again!     ",
          new Point(2, 2), 16);
    }

    // Map Objects
    for (MapObject a : ammo) {
      a.render(g2d);
    }
  }

  protected void renderPlaygroundsFront(Graphics2D g2d) {
    // Playground, in front of map objects
    for (Background b : layers
        .get(GameStateManager.LAYER_PLAYGROUND_INFRONTOF_MAPOBJECTS)) {
      b.render(g2d);
    }
  }

  protected void renderForegrounds(Graphics2D g2d) {
    // Foregrounds
    for (Background b : layers.get(GameStateManager.LAYER_FOREGROUND)) {
      b.render(g2d);
    }
  }

  protected void renderHud(Graphics2D g2d) {
    renderHudElement(g2d, 3, "deaths",
        PlayerSave.get(PlayerSave.PLAYER_DEATHCOUNT));
    renderHudElement(g2d, 12, "kills",
        PlayerSave.get(PlayerSave.PLAYER_KILLCOUNT));
    renderHudElement(g2d, 20, "score", PlayerSave.get(PlayerSave.PLAYER_SCORE));
    renderHudElement(g2d, 30, "key", (PlayerSave.has(Game.ACTION_KEY)) ? 1 : 0);
    if (PlayerSave.has(Game.ACTION_SLOWFALL)) {
      g2d.drawImage(TileManager.getTileImage((11 * 32) + 2),
          17 * TileManager.TILE_SIZE, 0, null);
    }
  }

  protected void renderSmallFont(Graphics2D g2d, int column, int row, String s,
      boolean offsetHalfTileX, boolean offsetHalfTileY) {
    s = s.toLowerCase();

    // Print out the object name
    for (int i = 0; i < s.length(); ++i) {
      int c = (int) s.charAt(i);
      c = getAsciiPos(c);

      int tile = getSmallFontPosition(c);
      int q = getSmallFontQuadrant(c, false);

      int xoffset = (offsetHalfTileX) ? TileManager.HALF_TILE : 0;
      int x = (i * TileManager.HALF_TILE) + (column * TileManager.TILE_SIZE)
          + xoffset;
      int yoffset = (offsetHalfTileY) ? TileManager.HALF_TILE : 0;
      int y = TileManager.HALF_TILE + (row * TileManager.TILE_SIZE) + yoffset;
      g2d.drawImage(TileManager.getQuarterTileImage(tile, q), x, y, null);
    }
  }

  protected void renderSmallFont(Graphics2D g2d, int column, int row,
      String s) {
    renderSmallFont(g2d, column, row, s, false, false);
  }

  private void renderHudElement(Graphics2D g2d, int column, String s,
      Integer v) {
    s = s.toLowerCase();

    // Print out the type of value ("score", "keys" etc)
    for (int i = 0; i < s.length(); ++i) {
      int c = (int) s.charAt(i);
      c = getAsciiPos(c);

      int tile = getSmallFontPosition(c);
      int q = getSmallFontQuadrant(c, false);

      int x = (column + i) * TileManager.HALF_TILE;
      g2d.drawImage(TileManager.getQuarterTileImage(tile, q), x, 0, null);
    }

    // Print out the value of a type (100 points, 1 key etc)
    String t = v.toString().toLowerCase();
    for (int i = 0; i < t.length(); ++i) {
      int c = (int) t.charAt(i);
      c = getAsciiPos(c);

      int tile = getSmallFontPosition(c);
      int q = getSmallFontQuadrant(c, true);

      int x = (column + i) * TileManager.HALF_TILE;
      g2d.drawImage(TileManager.getQuarterTileImage(tile, q), x,
          TileManager.HALF_TILE, null);
    }
  }

  protected int getSmallFontPosition(int c) {
    // Row 0, Col 14 + c(position in alphabet)(q = which top corner the letter
    // is in) in tile sheet
    int tile = (0 * 32 + 14) + (c / 2);
    return tile;
  }

  protected int getSmallFontQuadrant(int c, boolean isNumber) {
    // Row 0, Col 14 + c(position in alphabet)(q = which top corner the letter
    // is in) in tile sheet
    int q = 0;
    if (isNumber) {
      q = (1 - (c % 2)) + 2;
    } else {
      q = c % 2;
    }
    return q;
  }

  protected int getAsciiPos(int c) {
    switch (c) {
    case 33:
      // !
      c = 28;
      break;
    case 44:
      // ,
      c = 27;
      break;
    case 46:
      // .
      c = 26;
      break;
    case 63:
      // ?
      c = 29;
      break;
    case 32:
      // " "
      c = 30;
      break;
    default:
      if ((c >= 97) && (c <= (97 + 26))) {
        // letters a to z
        c -= 97;
      } else if ((c >= 48) && (c <= (48 + 10))) {
        // numbers 0 - 9
        c -= 48;
      } else c = 29;
      break;
    }
    return c;
  }

  public void renderSimpletextOverlay(Graphics2D g2d, String t, Point coords) {
    int x = (int) coords.getX();
    int y = (int) coords.getY();

    g2d.drawString(t, x, y);
  }

  public void keyPressedPlayer(int key) {
    if (key == KeyEvent.VK_UP) {
      player.keyUp(true);
    }
    if (key == KeyEvent.VK_LEFT) {
      player.keyLeft(true);
    }
    if (key == KeyEvent.VK_DOWN) {
      player.keyDown(true);
    }
    if (key == KeyEvent.VK_RIGHT) {
      player.keyRight(true);
    }
    if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_X) {
      player.keyJump(true);
    }
    if (key == KeyEvent.VK_Z || key == KeyEvent.VK_C) {
      player.keyFire(true);
    }
  }

  public void keyReleasedPlayer(int key) {
    if (key == KeyEvent.VK_UP) {
      player.keyUp(false);
    }
    if (key == KeyEvent.VK_LEFT) {
      player.keyLeft(false);
    }
    if (key == KeyEvent.VK_DOWN) {
      player.keyDown(false);
    }
    if (key == KeyEvent.VK_RIGHT) {
      player.keyRight(false);
    }
    if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_X) {
      player.keyJump(false);
    }
    if (key == KeyEvent.VK_Z || key == KeyEvent.VK_C) {
      player.keyFire(false);
    }
  }

  protected void reset() {
    ammo = new ArrayList<MapObject>();
    enemy = new ArrayList<MapObject>();
  }

  public abstract void render(Graphics2D g2d);

  public abstract void keyPressed(int key);

  public abstract void keyReleased(int key);

}
