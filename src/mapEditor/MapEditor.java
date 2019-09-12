package mapEditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.Point;

import javax.swing.JPanel;
import ioManager.IoManager;
import tileManager.TileManager;
import tileMap.RoomMap;

public class MapEditor extends JPanel
    implements Runnable, KeyListener, MouseListener {
  private static final long serialVersionUID = 1L;

  private static final int W = 1080;
  private static final int H = 600;
  private Thread thread;

  private Color trans = new Color(0.5f, 0.5f, 0.5f, 0.5f);

  private boolean roomIsLoaded;
  // private boolean roomIsSaved;
  private RoomMap room;
  private boolean keyHasBeenPressed;

  private boolean inputMode = false;

  private Point headerOffset = new Point(10, 20);
  private Point tileSheetOffset = new Point(440, 20);
  private Point mapAreaOffset = new Point(10, 100);
  private Point mapCoordsOffset = new Point(10, 80);
  private Point tileCoordsOffset = new Point(360, 120);
  private Point menuOffset = new Point(10, 40);
  private Point subMenuOffset = new Point(10, 60);
  private Point consoleOffset = new Point(10, 420);
  private Point selectedTilePreviewOffset = new Point(360, 150);
  private Point mapCoords = new Point(0, 0);

  private int selectedTile, selectedType;

  private int activeTileSheet;

  private int mode;
  private boolean quit;

  private Font hack = new Font("Arial", Font.PLAIN, 12);

  private MapConsole mc;

  private BufferedImage bufferedImage;
  private Graphics2D g;

  private int columns = 20;
  private int rows = 16;

  public MapEditor() {
    setPreferredSize(new Dimension(W, H));
    setFocusable(true);
    requestFocus();
  }

  // Waits for the map editor to be loaded, and starts a new thread for the
  // map editor
  public void addNotify() {
    super.addNotify();
    if (thread == null) {
      thread = new Thread(this);
      addKeyListener(this);
      addMouseListener(this);
      thread.start();
    }
  }

  public void initialize() {
    // Create the double buffer-image to draw on before drawing onto screen
    bufferedImage = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
    g = bufferedImage.createGraphics();
    mc = new MapConsole(g, mapCoordsOffset, menuOffset, subMenuOffset,
        consoleOffset, headerOffset, tileCoordsOffset);

    // The game will Exit by choosing Exit in the menus or closing the game
    // window. Might implement changing quit-variable to true, for a cleaner
    // exit further on.
    quit = false;

    // Initiate the TileManager
    TileManager.init();
    newRoom();
  }

  public void run() {
    initialize();

    // Limit the game to n FPS
    int fpsLimit = 60;
    long fpsTimer = 1000 / fpsLimit;
    long timerBegin;
    long timerElapsed;
    long timerWait;

    // Main game loop
    while (!quit) {
      timerBegin = System.nanoTime();

      update();
      renderBuffer();
      renderScreen();

      // Determine if system is ready for the next frame (in nano seconds)
      timerElapsed = System.nanoTime() - timerBegin;
      timerWait = fpsTimer - timerElapsed / 1000000;

      if (timerWait < 0) {
        timerWait = 0;
      }
      try {
        Thread.sleep(timerWait);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void setInputMode(boolean fileInputMode) {
    this.inputMode = fileInputMode;
    mc.addToConsole(fileInputMode ? "Input Mode On" : "Input Mode Off");
  }

  private void load() {
    if (!mc.getInput().equals("")) {
      IoManager io = new IoManager("/assets/mapedit/" + mc.getInput() + ".xml");
      System.out.println(mc.getInput() + ".xml");
      if (io.load()) {
        room = io.getRoom();
        mc.addToConsole("Room + '" + room.getName() + "' loaded.");
        setRoomName(room.getName());
        setRoomInfo(room.getInfo());
        setRoomLoaded(true);
        // setRoomSaved(false);
      } else {
        mc.addToConsole(
            "Couldn't load the room specified. Please check the name.");
      }
    } else {
      mc.addToConsole("First enter the room name you want to load.");
      mc.addToConsole("(Press space to enter input mode)");
    }
  }

  private void save() {
    if (room != null && !room.getName().equals("")) {
      IoManager io = new IoManager(room, room.getName() + ".xml");

      if (io.save()) {

        mc.addToConsole("Room '" + room.getName() + "' saved.");
        // setRoomSaved(true);
      } else {
        mc.addToConsole("Failed to save room '" + room.getName() + ".");
      }
    } else {
      mc.addToConsole("First enter the room name you want to save.");
      mc.addToConsole("(Press space to enter input mode)");
    }
  }

  private void newRoom() {
    String t = "";
    if (mc.getInput().equals("")) {
      t = "roomname";
    } else {
      t = mc.getInput();
    }
    room = new RoomMap(t, columns, rows, "Add information for the room here.");
    setRoomLoaded(true);
    // setRoomSaved(false);
    mc.setName(room.getName());
    mc.setInfo(room.getInfo());
    mc.addToConsole(
        "New room created: '" + room.getName() + "', '" + room.getInfo() + "' ["
            + room.getColumns() + ":" + room.getRows() + "]");
  }

  /*
   * private void setRoomSaved(boolean roomIsSaved) { this.roomIsSaved =
   * roomIsSaved; }
   */

  private void setRoomLoaded(boolean roomIsLoaded) {
    this.roomIsLoaded = roomIsLoaded;
  }

  private void update() {
  }

  private void drawTileSheet() {
    int tx = (int) tileSheetOffset.getX();
    int ty = (int) tileSheetOffset.getY();

    for (int x = 0; x < 32; ++x) {
      for (int y = 0; y < 32; ++y) {
        g.drawImage(TileManager.getTileImage(new Point(x, y)),
            (x * TileManager.TILE_SIZE) + tx, (y * TileManager.TILE_SIZE) + ty,
            null);
      }
    }
    g.setColor(Color.WHITE);
    g.drawRect(tx + ((selectedTile % 32) * TileManager.TILE_SIZE) - 1,
        ty + ((selectedTile / 32) * TileManager.TILE_SIZE) - 1,
        TileManager.TILE_SIZE + 1, TileManager.TILE_SIZE + 1);
  }

  private void updateCoords(int x, int y) {
    int mx = (int) mapAreaOffset.getX();
    int my = (int) mapAreaOffset.getY();

    int tx = (int) tileSheetOffset.getX();
    int ty = (int) tileSheetOffset.getY();

    int mapX = (int) ((x - mx) / TileManager.TILE_SIZE);
    int mapY = (int) ((y - my) / TileManager.TILE_SIZE);

    mapCoords.setLocation(mapX, mapY); 
    if (mapX >= 0 && mapX < columns && mapY >= 0 && mapY < rows) {
      mc.updateMapCoords(mapX, mapY);
    }

    int col = 32;
    int row = 32;
    int tileX = (int) ((x - tx) / TileManager.TILE_SIZE);
    int tileY = (int) ((y - ty) / TileManager.TILE_SIZE);
    if (tileX >= 0 && tileX < col && tileY >= 0 && tileY < row) {
      mc.updateTileSheetCoords(tileX, tileY);
    }

  }

  private void setRoomInfo(String info) {
    room.setInfo(info);
    mc.setInfo(room.getInfo());
    mc.addToConsole("Room info changed to: '" + room.getInfo() + "'.");
  }

  private void setRoomName() {
    if (!mc.getInput().equals("")) {
      setRoomName(mc.getInput());
    } else {
      mc.addToConsole("Please state a name to change too first.");
      mc.addToConsole("(Press space to enter input mode)");
    }
  }

  private void setRoomName(String name) {
    room.setName(name);
    mc.setName(room.getName());
    mc.addToConsole("Room name changed to: '" + room.getName() + "'.");
  }

  private void nextTileSheet() {
    int activeSheet = (activeTileSheet + 1) % TileManager.getNumSheets();
    setTileSheet(activeSheet);
  }

  private void prevTileSheet() {
    int activeSheet = 0;
    if (activeTileSheet > 0) {
      activeSheet = (++activeTileSheet) % TileManager.getNumSheets();
    } else {
      activeSheet += TileManager.getNumSheets();
      activeSheet = (--activeSheet) % TileManager.getNumSheets();
    }
    setTileSheet(activeSheet);
  }

  private void setTileSheet(int activeSheet) {
    TileManager.setTileSet(activeSheet);
    activeTileSheet = TileManager.getTileSet();
    mc.updateCurrentTileSheet(activeTileSheet);
  }

  private void setTile() {
    int mapX = (int) mapCoords.getX(); 
    int mapY = (int) mapCoords.getY(); 
    
    if (mapX >= 0 && mapX < columns && mapY >= 0 && mapY < rows) {
      room.setImageId(mapX, mapY, selectedTile);
      room.setSet(mapX, mapY, activeTileSheet);
    }
  }

  private void clearTile() {
    int mapX = (int) mapCoords.getX(); 
    int mapY = (int) mapCoords.getY(); 
    
    if (mapX >= 0 && mapX < columns && mapY >= 0 && mapY < rows) {
      room.setImageId(mapX, mapY, 0);
      room.setSet(mapX, mapY, activeTileSheet);
    }
  }

  private void setType() {
    int mapX = (int) mapCoords.getX(); 
    int mapY = (int) mapCoords.getY(); 
    
    if (mapX >= 0 && mapX < columns && mapY >= 0 && mapY < rows) {
      room.setType(mapX, mapY, selectedType);
    }
  }

  private void setType(int type) {
    this.selectedType = type;
    mc.setType(type);
  }

  private void clearType() {
    int mapX = (int) mapCoords.getX(); 
    int mapY = (int) mapCoords.getY(); 
    
    if (mapX >= 0 && mapX < columns && mapY >= 0 && mapY < rows) {
      room.setType(mapX, mapY, 0);
    }
  }

  private void clearScreen() {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, W, H);
  }

  private void keyPressedInputMode(int k) {
    // Input Mode
    if ((k >= KeyEvent.VK_A && k <= KeyEvent.VK_Z)
        || (k >= KeyEvent.VK_0 && k <= KeyEvent.VK_9)
        || k == KeyEvent.VK_BACK_SPACE) {
      mc.addInput(k);
    }
    // Exit InputMode
    if (k == KeyEvent.VK_ESCAPE || k == KeyEvent.VK_ENTER) {
      setInputMode(false);
    }
  }

  private void keyPressedConsoleMode(int k) {
    setMode(k);
    switch (k) {
    case KeyEvent.VK_N:
      newRoom();
      break;
    case KeyEvent.VK_I:
      setRoomInfo(room.getInfo());
      break;
    case KeyEvent.VK_R:
      setRoomName();
      break;
    default:
      break;
    }
  }

  private void keyPressedEditMode(int k) {
    setMode(k);
    switch (k) {
    case KeyEvent.VK_UP:
      prevTileLine();
      break;
    case KeyEvent.VK_DOWN:
      nextTileLine();
      break;
    case KeyEvent.VK_RIGHT:
      nextTile();
      break;
    case KeyEvent.VK_LEFT:
      prevTile();
      break;
    default:
      break;
    }
  }

  private void keyPressedTypeMode(int k) {
    setMode(k);
    switch (k) {
    // Load room
    case KeyEvent.VK_1:
      setType(1);
      break;
    // New room
    case KeyEvent.VK_2:
      setType(0);
      break;
    default:
      break;
    }
  }

  private void keyPressedFileMode(int k) {
    setMode(k);
    switch (k) {
    // Load room
    case KeyEvent.VK_L:
      load();
      break;
    // Save room
    case KeyEvent.VK_S:
      save();
      break;
    default:
      break;
    }
  }

  private void keyPressedChangeTileSheet(int k) {
    switch (k) {
    case KeyEvent.VK_1:
      setTileSheet(0);
      break;
    case KeyEvent.VK_2:
      setTileSheet(1);
      break;
    case KeyEvent.VK_PAGE_DOWN:
      nextTileSheet();
      break;
    case KeyEvent.VK_PAGE_UP:
      prevTileSheet();
      break;
    }
  }

  // Let the state manager handle all the key presses
  public void keyPressed(KeyEvent key) {
    int k = key.getKeyCode();
    if (!keyHasBeenPressed) {
      if (k == KeyEvent.VK_Q) {
        System.exit(0);
      } else if (k == KeyEvent.VK_SPACE) {
        setInputMode(true);
      } else {
        if (!inputMode) {
          switch (mode) {
          case 0:
            keyPressedConsoleMode(k);
            break;
          case 1:
            keyPressedEditMode(k);
            break;
          case 2:
            keyPressedTypeMode(k);
            break;
          case 3:
            keyPressedFileMode(k);
            break;
          default:
            break;
          }
          keyPressedChangeTileSheet(k);
        } else {
          keyPressedInputMode(k);
        }

      }
    }
    if (k != 16) {
      keyHasBeenPressed = true;
    }
  }

  private void nextTile() {
    selectedTile = (selectedTile + 1) % (32 * 32);
  }

  private void prevTile() {
    --selectedTile;
    if (selectedTile < 0) {
      selectedTile += (32 * 32);
    } else {
      selectedTile = selectedTile % (32 * 32);
    }
  }

  private void nextTileLine() {
    selectedTile = (selectedTile + 32) % (32 * 32);

  }

  private void prevTileLine() {
    selectedTile = selectedTile - 32;
    if (selectedTile < 0) {
      selectedTile += (32 * 32);
    } else {
      selectedTile = selectedTile % (32 * 32);
    }
  }

  private void setMode(int k) {
    if (k >= KeyEvent.VK_F5 && k <= KeyEvent.VK_F8) {
      mode = k - KeyEvent.VK_F5;
    }
    mc.setMode(mode);
  }

  public void keyReleased(KeyEvent k) {
    // k.getKeyCode() == KeyEvent.VK_0;
    keyHasBeenPressed = false;
  }

  public void keyTyped(KeyEvent k) {
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
    int x = (int) e.getPoint().getLocation().getX();
    int y = (int) e.getPoint().getLocation().getY();
    updateCoords(x, y);
    selectOnTileSheet(e.getButton(), x, y);
    mousePressedEditMode(e.getButton(), x, y);
    mousePressedTypeMode(e.getButton(), x, y);
  }

  private void selectOnTileSheet(int button, int x, int y) {
    int tx = (int) tileSheetOffset.getX();
    int ty = (int) tileSheetOffset.getY();

    if (button == 1) {
      if (x >= tx && x < (tx + (32 * TileManager.TILE_SIZE)) && y >= ty
          && y < (ty + (32 * TileManager.TILE_SIZE))) {
        int xt = (int) ((x - tx) / TileManager.TILE_SIZE);
        int yt = (int) ((y - ty) / TileManager.TILE_SIZE);
        selectedTile = (yt * 32) + xt;
      }
    }
  }

  private void mousePressedEditMode(int button, int x, int y) {
    int mx = (int) mapAreaOffset.getX();
    int my = (int) mapAreaOffset.getY();

    if (mode == 1) {
      if (button == 1) {
        if (x >= mx && x < (mx + (columns * TileManager.TILE_SIZE)) && y >= my
            && y < (my + (rows * TileManager.TILE_SIZE))) {
          setTile();
        }
      } else if (button == 3) {
        if (x >= mx && x < (mx + (columns * TileManager.TILE_SIZE)) && y >= my
            && y < (my + (rows * TileManager.TILE_SIZE))) {
          clearTile();
        }
      }
    }
  }

  private void mousePressedTypeMode(int button, int x, int y) {
    int mx = (int) mapAreaOffset.getX();
    int my = (int) mapAreaOffset.getY();

    if (mode == 2) {
      if (button == 1) {
        if (x >= mx && x < (mx + (columns * TileManager.TILE_SIZE)) && y >= my
            && y < (my + (rows * TileManager.TILE_SIZE))) {
          setType();
        }
      } else if (button == 3) {
        if (x >= mx && x < (mx + (columns * TileManager.TILE_SIZE)) && y >= my
            && y < (my + (rows * TileManager.TILE_SIZE))) {
          clearType();
        }
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  // Redraws the tile map on the canvas
  private void renderBuffer() {
    clearScreen();
    drawTileSheet();
    drawTileSheetGrid();
    drawRoom();
    drawRoomGrid();
    drawCurrentlySelectedTile();
    mc.render();
  }

  // Draw the room tiles on the canvas
  private void drawRoom() {
    int mx = (int) mapAreaOffset.getX();
    int my = (int) mapAreaOffset.getY();

    // Only draw if the room is loaded
    if (roomIsLoaded) {
      for (int x = 0; x < columns; ++x) {
        for (int y = 0; y < rows; ++y) {
          int xt = mx + (x * TileManager.TILE_SIZE);
          int yt = my + (y * TileManager.TILE_SIZE);

          // Draw the tile image id
          g.drawImage(TileManager.getTileImage(room.getImageId(x, y),
              room.getSet(x, y)), xt, yt, null);
          // Draw the tile Type
          g.setFont(hack);
          g.setColor(Color.WHITE);
          g.drawString("" + room.getType(x, y), xt + TileManager.HALF_TILE / 2,
              yt + hack.getSize());
        }
      }
    }
  }

  private void drawRoomGrid() {
    int mx = (int) mapAreaOffset.getX();
    int my = (int) mapAreaOffset.getY();

    g.setColor(trans);
    for (int x = 0; x <= columns; ++x) {
      g.drawLine(mx + (x * TileManager.TILE_SIZE), my,
          mx + (x * TileManager.TILE_SIZE),
          my + (rows * TileManager.TILE_SIZE));
      g.drawLine(mx + (x * TileManager.TILE_SIZE) - 1, my,
          mx + (x * TileManager.TILE_SIZE) - 1,
          my + (rows * TileManager.TILE_SIZE));
    }
    for (int y = 0; y <= rows; ++y) {
      g.drawLine(mx, my + (y * TileManager.TILE_SIZE),
          mx + (columns * TileManager.TILE_SIZE),
          my + (y * TileManager.TILE_SIZE));
      g.drawLine(mx, my + (y * TileManager.TILE_SIZE) - 1,
          mx + (columns * TileManager.TILE_SIZE),
          my + (y * TileManager.TILE_SIZE) - 1);
    }
  }

  private void drawTileSheetGrid() {
    int tx = (int) tileSheetOffset.getX();
    int ty = (int) tileSheetOffset.getY();

    g.setColor(trans);
    int col = 32;
    int row = 32;
    for (int x = 0; x <= col; ++x) {
      g.drawLine(tx + (x * TileManager.TILE_SIZE), ty,
          tx + (x * TileManager.TILE_SIZE), ty + (row * TileManager.TILE_SIZE));
      g.drawLine(tx + (x * TileManager.TILE_SIZE) - 1, ty,
          tx + (x * TileManager.TILE_SIZE) - 1,
          ty + (row * TileManager.TILE_SIZE));
    }
    for (int y = 0; y <= row; ++y) {
      g.drawLine(tx, ty + (y * TileManager.TILE_SIZE),
          tx + (col * TileManager.TILE_SIZE), ty + (y * TileManager.TILE_SIZE));
      g.drawLine(tx, ty + (y * TileManager.TILE_SIZE) - 1,
          tx + (col * TileManager.TILE_SIZE),
          ty + (y * TileManager.TILE_SIZE) - 1);
    }
  }

  private void drawCurrentlySelectedTile() {
    int x = (int) selectedTilePreviewOffset.getX();
    int y = (int) selectedTilePreviewOffset.getY();

    g.drawImage(TileManager.getTileImage(selectedTile), x, y, null);
    g.setColor(Color.WHITE);
    g.setFont(hack);
    g.drawString("" + selectedType, x, y - 10);
  }

  // Draw the buffered image to the canvas
  private void renderScreen() {
    Graphics g = getGraphics();
    // Scale the image up to preferred size
    g.drawImage(bufferedImage, 0, 0, W, H, 0, 0, W, H, null);
    g.dispose();
  }
}
