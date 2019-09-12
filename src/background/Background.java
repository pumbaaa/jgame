package background;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

import game.Game;
import gameState.GameStateManager;
import ioManager.IoManager;
import tileManager.TileManager;
import tileMap.RoomMap;

public class Background {
  private GameStateManager sm;
  private RoomMap room;
  private String fileName;

  // Current Position (the total map x)
  private Point2D.Double coords = new Point2D.Double(0.0, 0.0);
  // Position Movement Vector
  private Point2D.Double delta = new Point2D.Double(0.0, 0.0);
  // The speed of movement;
  @SuppressWarnings("unused")
  private double s;

  public Background(GameStateManager sm, String fileName) {
    this.sm = sm;
    this.fileName = fileName;
    load();
    setPos(new Point2D.Double(0.0, 0.0));
    setVec(new Point2D.Double(0.0, 0.0));
    s = 1.0;
  }

  private void load(){
    IoManager io = new IoManager(fileName);
    io.load();
    room = io.getRoom();
  }
  
  public void setPos(Point2D.Double coords) {
    this.coords.setLocation(coords.getX() % Game.WIN_W,
        coords.getY() % Game.WIN_H);
  }

  public void setVec(Point2D.Double delta) {
    this.delta.setLocation(delta.getX() % Game.WIN_W,
        delta.getY() % Game.WIN_H);
  }

  // Default update is auto movement with wrap
  public void update() {
    double x = coords.getX();
    double y = coords.getY();
    double dx = delta.getX();
    double dy = delta.getY();

    x += dx;
    if (x < 0.0) {
      x += (sm.getWidth() * TileManager.TILE_SIZE);
    }
    if (x > (sm.getWidth() * TileManager.TILE_SIZE)) {
      x -= (sm.getWidth() * TileManager.TILE_SIZE);
    }

    y += dy;
    if (y < 0.0) {
      y += (sm.getWidth() * TileManager.TILE_SIZE);
    }
    if (y > (sm.getWidth() * TileManager.TILE_SIZE)) {
      y -= (sm.getWidth() * TileManager.TILE_SIZE);
    }

    coords.setLocation(x, y);
  }

  public void render(Graphics2D g2d) {
    double x = coords.getX();
    double y = coords.getY();
    int ts = TileManager.TILE_SIZE;

    // Screen Tile Size (+2 for buffer above, below, left and right) (in
    // tiles)
    int scrTileW = Game.WIN_COLS + 2;
    int scrTileH = Game.WIN_ROWS + 2;

    // Map position, where on the map to start reading tiles (in tiles)
    int cMapPosX = (int) (x / TileManager.TILE_SIZE);
    int cMapPosY = (int) (y / TileManager.TILE_SIZE);

    // Wrap map
    if (cMapPosX < 0) {
      cMapPosX += room.getWidth() + Game.WIN_COLS;
    }
    if (cMapPosX >= room.getWidth() + Game.WIN_COLS) {
      cMapPosX -= room.getWidth() - Game.WIN_COLS;
    }
    if (cMapPosY < 0) {
      cMapPosY += room.getHeight() + Game.WIN_ROWS;
    }
    if (cMapPosY >= room.getWidth() + Game.WIN_ROWS) {
      cMapPosY = room.getWidth() - Game.WIN_ROWS;
    }

    for (int h = 0; h < scrTileH; ++h) {
      for (int w = 0; w < scrTileW; ++w) {
        Point tmpos = new Point(((cMapPosX + w) % room.getColumns()),
            ((cMapPosY + h) % room.getRows()));
        if (room.getImageId(tmpos) != 0) {
          int tx = (w * ts) - (int) (x % TileManager.TILE_SIZE);
          int ty = (h * ts) - (int) (y % TileManager.TILE_SIZE);
          g2d.drawImage(TileManager.getTileImage(room.getImageId(tmpos)),
              tx, ty, null);
        }
      }
    }
  }
}
