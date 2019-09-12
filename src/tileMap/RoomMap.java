package tileMap;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;

import tileManager.TileManager;

public class RoomMap implements Serializable {
  private static final long serialVersionUID = 2;

  private class TileInfo {

    // The tile id of the graphic to show on this tile
    private int imageId;

    // Map tile type (solid, dangerous, normal (no action), teleport etc
    private int type;

    // From which tile sheet set
    private int set;

    public TileInfo(int imageId, int type) {
      this.imageId = imageId;
      this.type = type;
      set = 0;
    }

    public int getImageId() {
      return imageId;
    }

    public void setImageId(int imageId) {
      this.imageId = imageId;
    }

    public int getType() {
      return type;
    }

    public void setType(int type) {
      this.type = type;
    }

    public int getSet() {
      return set;
    }

    public void setSet(int set) {
      this.set = set;
    }
  }

  TileInfo[][] room;

  private String name;
  private String info;
  private int columns;
  private int rows;

  public RoomMap(String name, int columns, int rows, String info) {
    this.name = name;
    this.columns = columns;
    this.rows = rows;
    this.info = info;
    room = new TileInfo[columns][rows];
    for (int x = 0; x < columns; ++x) {
      for (int y = 0; y < rows; ++y) {
        room[x][y] = new TileInfo(0, 0);
      }
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String roomInfo) {
    this.info = roomInfo;
  }

  // getWidth and getHeight two are deprecated
  public int getWidth() {
    return getColumns();
  }

  public int getHeight() {
    return getRows();
  }

  public int getRows() {
    return rows;
  }

  public void setRows(int rows) {
    this.rows = rows;
  }

  public int getColumns() {
    return columns;
  }

  public void setColumns(int columns) {
    this.columns = columns;
  }

  public void setTile(int tileImageId, int type, Point coords) {
    setImageId(coords, tileImageId);
    setType(coords, type);
  }

  // Return the tile sheet number of a tile on the map
  // Supports coordinates on screen( Point2d and (int, int) and col/row on
  // tile map
  public int getType(int x, int y) {
    return getType(new Point(x, y));
  }

  public int getType(Point2D.Double coords) {
    int xt = (int) (coords.getX() / TileManager.TILE_SIZE);
    int yt = (int) (coords.getY() / TileManager.TILE_SIZE);
    return getType(new Point(xt, yt));
  }

  public int getType(Point coords) {
    int type = 0;
    int x = (int) coords.getX();
    int y = (int) coords.getY();
    if (x >= 0 && x < columns && y >= 0
        && y < rows) {
      type = room[(int) coords.getX()][(int) coords.getY()].getType();
    }
    return type;
  }

  // Sets the type of a tile (Solid, transparent etc)
  // Supports setting type by (int, int) and Point)
  public void setType(int x, int y, int type) {
    setType(new Point(x, y), type);
  }

  public void setType(Point coords, int type) {
    int x = (int) coords.getX();
    int y = (int) coords.getY();
    if (x >= 0 && x <= columns && y >= 0 && y < columns) {
      room[x][y].setType(type);
    }
  }

  // Return the tile sheet number of a tile on the map
  // Supports Point2D.double, Point, and (int and int)
  public int getImageId(Point2D.Double coords) {
    int xt = (int) (coords.getX() / TileManager.TILE_SIZE);
    int yt = (int) (coords.getY() / TileManager.TILE_SIZE);
    return getImageId(new Point(xt, yt));
  }

  public int getImageId(int x, int y) {
    return getImageId(new Point(x, y));
  }

  public int getImageId(Point coords) {
    int tile = 0;
    if (coords.getX() >= 0 && coords.getX() < columns && coords.getY() >= 0
        && coords.getY() < columns) {
      tile = room[(int) coords.getX()][(int) coords.getY()].getImageId();
    }
    return tile;
  }

  // Set the imageId of a tile in the map
  // Supports Point and (int, int)
  public void setImageId(int x, int y, int imageId) {
    setImageId(new Point(x, y), imageId);
  }

  public void setImageId(Point coords, int tileImageId) {
    int x = (int) coords.getX();
    int y = (int) coords.getY();
    if (x >= 0 && x <= columns && y >= 0 && y < columns) {
      room[x][y].setImageId(tileImageId);
    }
  }
  
  public int getSet(Point coords) {
    int x = (int) coords.getX();
    int y = (int) coords.getY();
    return room[x][y].getSet();
  }

  public int getSet(int x, int y) {
    return getSet(new Point(x, y));
  }

  public void setSet(int x, int y, int set) {
    room[x][y].setSet(set);
  }

}
