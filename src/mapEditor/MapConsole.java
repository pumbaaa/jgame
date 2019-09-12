package mapEditor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class MapConsole {
  private Graphics2D g;
  private Font hack, hackBoldItalic;
  private int mode, type;
  private int mapX, mapY;
  private int tileX, tileY;
  private int tileSheet;

  private String name = "";
  private String info = "";

  private String inputString = "";

  private Point headerOffset;
  private Point mapCoordsOffset;
  private Point tileCoordsOffset;
  private Point menuOffset;
  private Point subMenuOffset;
  private Point consoleOffset;

  private ArrayList<String> consoleLines = new ArrayList<String>();

  public MapConsole( Graphics2D g, Point mapCoordsOffset, Point menuOffset, Point subMenuOffset,
      Point consoleOffset, Point headerOffset, Point tileCoordsOffset){
    this.g = g;

    this.headerOffset = headerOffset;
    this.mapCoordsOffset = mapCoordsOffset;
    this.menuOffset = menuOffset;
    this.subMenuOffset = subMenuOffset;
    this.consoleOffset = consoleOffset;
    this.tileCoordsOffset = tileCoordsOffset;

    hack = new Font("Arial", Font.PLAIN, 12);
    hackBoldItalic = new Font("Arial", Font.ITALIC + Font.BOLD, 12);
  }
 

  public void setMode(int mode) {
    this.mode = mode;
  }

  public void setType(int type) {
    this.type = type;
  }

  public void displayHeader() {
    int x = (int) headerOffset.getX();
    int y = (int) headerOffset.getY();
    
    g.setFont(hack);
    g.setColor(Color.WHITE);
    int offset = x;
    String modeText = "";
    switch (mode) {
    case 0:
      modeText = "Console";
      break;
    case 1:
      modeText = "Editor";
      break;
    case 2:
      modeText = "Type";
      break;
    case 3:
      modeText = "File";
      break;
    default:
      modeText = "Ops";
      break;
    }
    g.drawString(modeText + " Mode", offset, y);

    offset += g.getFontMetrics().stringWidth(modeText + " Mode") + 40;

    g.drawString(name, offset, y);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  private void displayConsole() {
    int x = (int) consoleOffset.getX();
    int y = (int) consoleOffset.getY();
    
    g.setFont(hackBoldItalic);
    g.setColor(Color.YELLOW);
    g.drawString(inputString, x, y - 20);
    g.setFont(hack);
    g.setColor(Color.WHITE);
    for (int i = 0; i < consoleLines.size(); ++i) {
      if (i < 6) {
        g.drawString(consoleLines.get(consoleLines.size() - 1 - i),
            x, y + (i * 20));
      } else {
        break;
      }
    }

  }

  public void updateCurrentTileSheet(int tileSheet) {
    this.tileSheet = tileSheet;
    addToConsole("Current Tile Sheet #" + tileSheet);
  }

  public void updateMapCoords(int mapX, int mapY) {
    this.mapX = mapX;
    this.mapY = mapY;
  }

  public void updateTileSheetCoords(int tileX, int tileY) {
    this.tileX = tileX;
    this.tileY = tileY;
  }

  private void displayCoordinates() {
    int mx = (int) mapCoordsOffset.getX();
    int my = (int) mapCoordsOffset.getY();

    int x = (int) tileCoordsOffset.getX();
    int y = (int) tileCoordsOffset.getY();
    
    String t = (mapX + ":" + mapY);
    g.setFont(hack);
    g.setColor(Color.WHITE);
    g.drawString(t, mx, my);

    String u = (tileX + ":" + tileY);
    g.setFont(hack);
    g.setColor(Color.WHITE);
    g.drawString(u, x, y);
    g.drawString("Sheet #" + tileSheet, x,
        y - 16);

  }

  public void addToConsole(String newLine) {
    consoleLines.add(newLine);
  }

  public void render() {
    displayHeader();
    displayMenu();
    displayCoordinates();
    displayConsole();
    displayInfo();
  }

  private void displayInfo() {
    int x = (int) consoleOffset.getX();
    int y = (int) consoleOffset.getY();
    
    g.setFont(hack);
    g.setColor(Color.WHITE);
    g.drawString(info, x, y - 45);
  }

  private void displayMenu() {
    int x = (int) menuOffset.getX();
    int y = (int) menuOffset.getY();
    
    g.setFont(hack);
    g.setColor(Color.WHITE);
    int offset = x;
    if (mode == 0) {
      displaySubMenu();
      g.setFont(hackBoldItalic);
    } else {
      g.setFont(hack);
    }
    String t = "F6: Console Mode";
    g.drawString(t, offset, y);
    offset += g.getFontMetrics().stringWidth(t) + 20;
    if (mode == 1) {
      displaySubMenu();
      g.setFont(hackBoldItalic);
    } else {
      g.setFont(hack);
    }
    t = "F7: Editor Mode";
    g.drawString(t, offset, y);
    offset += g.getFontMetrics().stringWidth(t) + 20;
    if (mode == 2) {
      displaySubMenu();
      g.setFont(hackBoldItalic);
    } else {
      g.setFont(hack);
    }
    t = "F8: Type Mode";
    g.drawString(t, offset, y);
    offset += g.getFontMetrics().stringWidth(t) + 20;
    if (mode == 3) {
      displaySubMenu();
      g.setFont(hackBoldItalic);
    } else {
      g.setFont(hack);
    }
    t = "F9: File Mode";
    g.drawString(t, offset, y);
  }

  private void displaySubMenu() {
    switch (mode) {
    case 0:
      displaySubMenu0();
      break;
    case 1:
      displaySubMenu1();
      break;
    case 2:
      displaySubMenu2();
      break;
    case 3:
      displaySubMenu3();
      break;
    default:
      break;
    }
  }

  private void displaySubMenu0() {
    int x = (int) subMenuOffset.getX();
    int y = (int) subMenuOffset.getY();
    
    g.setFont(hack);
    g.setColor(Color.WHITE);
    int offset = x;
    g.drawString("[N]ew Room", offset, y);
    offset += g.getFontMetrics().stringWidth("[N]ew Room") + 20;
    g.drawString("[I]nformation", offset, y);
    offset += g.getFontMetrics().stringWidth("[I]nformation") + 20;
    g.drawString("[R]ename room", offset, y);
    offset += g.getFontMetrics().stringWidth("[R]ename room") + 20;
    g.drawString("[1-x] Active Tile Sheet", offset, y);
  }

  private void displaySubMenu1() {
    int x = (int) subMenuOffset.getX();
    int y = (int) subMenuOffset.getY();
    
    g.setFont(hack);
    g.setColor(Color.WHITE);
    int offset = x;
    g.drawString("Use arrow keys to move in tile sheet", offset,
        y);
  }

  private void displaySubMenu2() {
    int x = (int) subMenuOffset.getX();
    int y = (int) subMenuOffset.getY();
    
    g.setColor(Color.WHITE);
    int offset = x;
    if (type == 1) {
      g.setFont(hackBoldItalic);
    } else {
      g.setFont(hack);
    }
    g.drawString("1: Solid block", offset, y);
    offset += g.getFontMetrics().stringWidth("1: Solid block") + 20;
    if (type == 0) {
      g.setFont(hackBoldItalic);
    } else {
      g.setFont(hack);
    }
    g.drawString("2: Transparent block", offset, y);
  }

  private void displaySubMenu3() {
    int x = (int) subMenuOffset.getX();
    int y = (int) subMenuOffset.getY();
    
    g.setColor(Color.WHITE);
    g.setFont(hack);
    int offset = x;
    g.drawString("[L]oad Room map", offset, y);
    offset += g.getFontMetrics().stringWidth("L: Load Room map") + 20;
    g.drawString("[S]ave Room map", offset, y);
  }

  public String getInput() {
    return inputString;
  }

  public void addInput(int k) {
    if (k == KeyEvent.VK_BACK_SPACE) {
      if (inputString.length() > 0) {
        inputString = inputString.substring(0, inputString.length() - 1);
      }
    } else {
      inputString += Character.toLowerCase((char) k);
    }
  }

  public void resetInput() {
    inputString = "";
  }
}
