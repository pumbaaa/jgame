package tileManager;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;

import javax.imageio.ImageIO;

// Manages which tile sheet are active, and retrieving the correct tile
// when asked for
public class TileManager implements Serializable {
  private static final long serialVersionUID = 2;

  private static class Tile {

    private BufferedImage image;

    public Tile(BufferedImage image) {
      this.image = image;
    }

    public BufferedImage getImage() {
      return image;
    }
  }

  // tileSets contains the images of all tile sheets.
  // First [] is the id for the tile sheet
  // Second and third [] is the coordinates where you can find a specific tile
  // private static Tile[][][] tileSets;
  private static HashMap<Integer, Tile[][]> sets = new HashMap<Integer, Tile[][]>();

  // Which is the currently activated set of tiles
  private static int current;

  public static int TILE_SIZE = 16;
  public static int HALF_TILE = TILE_SIZE / 2;

  // The width and height of a tile sheet source image, should be the same size
  // for all source sheets
  private static int columns;
  private static int rows;

  // How many sets are loaded into the system
  public static void init() {
    // Load the tile sheets to a hash map
    load(0, "/assets/sheet/32x32_16x16px_tech.png");
    load(1, "/assets/sheet/32x32_16x16px.png");
  }

  // Load the specified tile sheets
  private static void load(int id, String fileName) {
    BufferedImage tileSheet;
    try {

      // Load the tile sheet to a buffered image
      tileSheet = ImageIO.read(TileManager.class.getResourceAsStream(fileName));
      columns = tileSheet.getWidth() / TileManager.TILE_SIZE;
      rows = tileSheet.getHeight() / TileManager.TILE_SIZE;
      Tile[][] t = new Tile[columns][rows];

      // Columns and Rows of source tile sheet
      // Save the sub image to the correct tile position
      for (int x = 0; x < columns; ++x) {
        for (int y = 0; y < rows; ++y) {
          t[x][y] = new Tile(tileSheet.getSubimage(x * TILE_SIZE, y * TILE_SIZE,
              TILE_SIZE, TILE_SIZE));
        }
      }
      sets.put(id, t);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  public static BufferedImage getTileImage(Point coords, int set) {
    // Get the image for the specified Tile
    BufferedImage image;
    int x = (int) coords.getX();
    int y = (int) coords.getY();
    if (x >= 0 && x < columns && y >= 0 && y < rows) {
      image = sets.get(set)[x][y].getImage();

    } else {
      // If the coordinates are outside of the tile sheet, return a default tile
      image = sets.get(set)[1][0].getImage();
    }
    return image;
  }

  public static BufferedImage getTileImage(int num, int set) {
    // Calculate the sheet x and y
    int x = num % columns;
    int y = num / columns;
    return getTileImage(new Point(x, y), set);
  }

  // Gets a quarter of a TILE_SIZE, where the second parameter 'q' tells which
  // corner to grab. 0 = top left (default if q is out of range), 1 = top right,
  // 2 = bottom left, 3 = bottom right
  public static BufferedImage getQuarterTileImage(Point coords, int q,
      int set) {
    BufferedImage image;
    int x = (int) coords.getX();
    int y = (int) coords.getY();
    int xOffset, yOffset;
    switch (q) {
    case 1:
      xOffset = HALF_TILE;
      yOffset = 0;
      break;
    case 2:
      xOffset = HALF_TILE;
      yOffset = HALF_TILE;
      break;
    case 3:
      xOffset = 0;
      yOffset = HALF_TILE;
      break;
    default:
      xOffset = 0;
      yOffset = 0;
      break;
    }
    if (x >= 0 && x < columns && y >= 0 && y < rows) {
      image = sets.get(set)[x][y].getImage().getSubimage(xOffset, yOffset,
          HALF_TILE, HALF_TILE);
    } else {
      // If the coordinates are outside of the tile sheet, return a default tile
      image = sets.get(set)[1][0].getImage().getSubimage(xOffset, yOffset,
          HALF_TILE, HALF_TILE);
      ;
    }
    return image;
  }

  public static BufferedImage getQuarterTileImage(int num, int q, int set) {
    // Convert to map x, y and retrieve image
    int x = num % columns;
    int y = num / columns;
    return getQuarterTileImage(new Point(x, y), q, set);
  }

  // Custom getters for if a tile is from another sheet or not
  public static BufferedImage getTileImage(Point coords) {
    return getTileImage(coords, current);
  }

  public static BufferedImage getTileImage(int num) {
    // Calculate the sheet x and y
    int x = num % columns;
    int y = num / columns;
    return getTileImage(new Point(x, y), current);
  }

  // Gets a quarter of a TILE_SIZE, where the second parameter 'q' tells which
  // corner to grab. 0 = top left (default if q is out of range), 1 = top right,
  // 2 = bottom left, 4 = bottom
  // right
  public static BufferedImage getQuarterTileImage(Point coords, int q) {
    return getQuarterTileImage(coords, current);
  }

  public static BufferedImage getQuarterTileImage(int num, int q) {
    // Convert to map x, y and retrieve image
    int x = num % columns;
    int y = num / columns;
    return getQuarterTileImage(new Point(x, y), q, current);
  }

  // Setters and getters for changing the current TileSet
  public static void setTileSet(int set) {
    if (set >= 0 && set < sets.size()) {
      current = set;
    }
  }

  public static int getTileSet() {
    return current;
  }

  public static int getNumSheets() {
    return sets.size();
  }
}
