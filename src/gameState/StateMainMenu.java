package gameState;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import audioPlayer.AudioPlayer;
import background.Background;
import mapObject.Animation;
import tileManager.TileManager;

public class StateMainMenu extends GameState {
  private ArrayList<Option> options;
  private int selectedOption;
  private Animation marker;

  public class Option {

    private int id;
    private String text;
    private int nextState;

    public Option(int id, String text, int nextState) {
      this.id = id;
      this.text = text;
      this.nextState = nextState;
    }

    public int getId() {
      return id;
    }

    public String getText() {
      return text;
    }

    public int getNextState() {
      return nextState;
    }

    public void setNextState(int nextState) {
      this.nextState = nextState;
    }
  }

  public StateMainMenu(GameStateManager sm) {
    super(sm);
    // Initiate resources
    initResources();
//    AudioPlayer.load(AudioPlayer.MSX_MAIN_MENU);
    AudioPlayer.playLoop(AudioPlayer.MSX_MAIN_MENU, 0);
  }

  private void initResources() {
    // Load up the game state tile map
    load("/assets/map/menu/playground.xml");

    // Backgrounds, playground, and foreground, rendered in order of appearance
    layers.get(GameStateManager.LAYER_BACKGROUND)
        .add(new Background(sm, "/assets/map/menu/backgroundbluesky.xml"));
    layers.get(GameStateManager.LAYER_BACKGROUND)
        .add(new Background(sm, "/assets/map/menu/backgroundsun.xml"));
    layers.get(GameStateManager.LAYER_FOREGROUND)
        .add(new Background(sm, "/assets/map/menu/backgroundclouds.xml"));

    // Set the backgrounds in motion
    layers.get(GameStateManager.LAYER_BACKGROUND).get(1)
        .setVec(new Point2D.Double(0.0, 0.0));
    layers.get(GameStateManager.LAYER_FOREGROUND).get(0)
        .setVec(new Point2D.Double(0.25, 0.0));

    // Which map objects are there in this state
    // Which menu options are there
    initOptions();
  }

  // Which menu options are there
  private void initOptions() {
    options = new ArrayList<Option>();
    options.add(new Option(0, "GAME  INFO", GameStateManager.LEVEL_INFO));
    options.add(new Option(1, "START GAME", GameStateManager.LEVEL_1));
    options.add(new Option(2, "EXIT  GAME", 0));
    selectedOption = 1;
    marker = new Animation(2, 5);
    marker.setDelayLimit(15);
  }

  public void reset() {
    initResources();
  }

  public void update() {
    updateBackground();
    updatePlaygroundBack();
    updatePlaygroundFront();
    updateForeground();
  }

  private void renderMenuOptions(Graphics2D g2d) {
    // Display the menu options and update the marker for current choice
    g2d.setFont(new Font("Arial Black", Font.PLAIN, 16));
    int yOffset = 0;
    for (Option option : options) {
      int x1 = (room.getColumns() / 2) - (option.getText().length() / 2) - 1;
      int x2 = (room.getColumns() / 2) + (option.getText().length() / 2);
      int x = (room.getColumns() / 2) - ((option.getText().length() / 2));

      int y = (room.getRows() / 2) + (yOffset);

      renderTextOverlay(g2d, option.getText(), new Point(x, y), 10);

      if (selectedOption == yOffset) {
        g2d.drawImage(TileManager.getTileImage(marker.getFrame()),
            x1 * TileManager.TILE_SIZE, y * TileManager.TILE_SIZE, null);
        g2d.drawImage(TileManager.getTileImage(marker.getFrame()),
            x2 * TileManager.TILE_SIZE, y * TileManager.TILE_SIZE, null);
      }
      ++yOffset;
    }
  }

  public void render(Graphics2D g2d) {
    renderBackgrounds(g2d);
    renderPlaygroundsBack(g2d);
    renderPlayground(g2d);
    renderMenuOptions(g2d);
    renderPlaygroundsFront(g2d);
    renderForegrounds(g2d);
  }

  private void selectOption() {
    // Start the game
    for (int id = 0; id < options.size(); ++id) {
      if (selectedOption == id) {
        if (options.get(selectedOption).getId() == 2) {
          try {
            AudioPlayer.play(AudioPlayer.SFX_SELECT);
            // Here be a delay for some fancy effect, like fade out or something
            Thread.sleep(700);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          System.exit(0);
        }
        if (id != 0) {
          AudioPlayer.stop(AudioPlayer.MSX_MAIN_MENU);
//          AudioPlayer.unload(AudioPlayer.MSX_MAIN_MENU);
        }
        try {
          AudioPlayer.play(AudioPlayer.SFX_SELECT);
          // Here be a delay for some fancy effect, like fade out or something
          Thread.sleep(700);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        sm.setState(options.get(selectedOption).getNextState());
      }
    }

  }

  private void nextOption() {
    ++selectedOption;
    if (selectedOption >= options.size()) {
      selectedOption = 0;
    }
    AudioPlayer.play(AudioPlayer.SFX_MENUOPTION);
  }

  private void prevOption() {
    --selectedOption;
    if (selectedOption < 0) {
      selectedOption = options.size() - 1;
    }
    AudioPlayer.play(AudioPlayer.SFX_MENUOPTION);
  }

  public void keyPressed(int key) {
    if (key == KeyEvent.VK_DOWN) {
      nextOption();
    }
    if (key == KeyEvent.VK_UP) {
      prevOption();
    }
    if (key == KeyEvent.VK_Z || key == KeyEvent.VK_C) {
      selectOption();
    }
  }

  public void keyReleased(int key) {
  }
}
