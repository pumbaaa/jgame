package gameState;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import audioPlayer.AudioPlayer;
import background.Background;
import game.Game;
import mapObject.*;

public class StateTheEnd extends GameState {

  private int timerPage = 0;
  private int timerPages = 4;
  private int timerCounter = 0;
  private int timerLimit = 1000;

  public StateTheEnd(GameStateManager sm) {
    super(sm);
    // Initiate resources
    initResources();
  }

  private void initResources() {
    initAudio();
    initTileMap();
    initLayers();
    initMapObjects();
  }

  private void initAudio() {
//    AudioPlayer.load(AudioPlayer.MSX_END);
    AudioPlayer.playLoop(AudioPlayer.MSX_END, 0);
  }

  private void initTileMap() {
    // Load up the state tile map
    load("/assets/map/theend/theend.xml");
  }

  private void initLayers() {
    // Backgrounds, rendered in order of appearance
    layers.get(GameStateManager.LAYER_BACKGROUND)
        .add(new Background(sm, "/assets/map/theend/backgroundnight.xml"));
    layers.get(GameStateManager.LAYER_BACKGROUND)
    .get(layers.get(GameStateManager.LAYER_BACKGROUND).size() - 1)
    .setVec(new Point2D.Double(0.0, 0.05));
  }

  private void initMapObjects() {
    // The intractable and other non intractable objects that are in the state
    mapObjects = new ArrayList<MapObject>();

    // Which map objects are there in this state
    mapObjects.add(new Door(sm));
    mapObjects.get(mapObjects.size() - 1).setSpawnPoint(new Point(2, 5));

    mapObjects.add(new Key(sm));
    mapObjects.get(mapObjects.size() - 1).setSpawnPoint(new Point(2, 7));

    mapObjects.add(new SlowFall(sm));
    mapObjects.get(mapObjects.size() - 1).setSpawnPoint(new Point(2, 9));

    mapObjects.add(new SpikeBlock(sm));
    mapObjects.get(mapObjects.size() - 1).setSpawnPoint(new Point(2, 11));

    // The player
    player = new Player(sm);
    player.setSpawnPoint(new Point(2, 13));

    // Add some enemies and define their movements and spawn points
    enemy.add(new Mushroom(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(12, 6));
    enemy.get(enemy.size() - 1).keyLeft(true);

    enemy.add(new Tank(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(12, 8));
    enemy.get(enemy.size() - 1).keyRight(true);

    enemy.add(new SparklyBoss(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(11, 11));
    enemy.get(enemy.size() - 1).keyRight(true);

  }

  public void reset() {
    super.reset();
    ammo = new ArrayList<MapObject>();

    initMapObjects();
  }

  @Override
  protected void actionTeleport(MapObject mo) {
    if (PlayerSave.has(Game.ACTION_KEY)) {
      AudioPlayer.play(AudioPlayer.SFX_DOOR);
      sm.setState(mo.getTeleport());
    }
  }

  public void update() {
    updateBackground();
    updatePlaygroundBack();
    updatePlaygroundFront();
    updateForeground();
  }

  public void render(Graphics2D g2d) {
    renderBackgrounds(g2d);
    renderPlaygroundsBack(g2d);
    renderPlayground(g2d);
    endScreens(g2d);

    renderPlaygroundsFront(g2d);
    renderForegrounds(g2d);
    renderHud(g2d);
  }

  private void endScreens(Graphics2D g2d) {
    switch (timerPage) {
    case 0:
      renderMapObjects(g2d);
      renderObjectInformation(g2d);
      break;
    case 1:
      renderCredits(g2d);
      break;
    case 2:
      renderAdditional(g2d);
      break;
    case 3:
      renderAdditional2(g2d);
      break;
    default:
      break;
    }
    nextPage(false);
  }

  private void nextPage(boolean force) {
    ++timerCounter;
    if (timerCounter >= timerLimit || force) {
      ++timerPage;
      timerPage = timerPage % timerPages;
      timerCounter = 0;
    }
  }

  private void renderObjectInformation(Graphics2D g2d) {
    renderSmallFont(g2d, 4, 5, "Ms Door");
    renderSmallFont(g2d, 4, 7, "Mr Key");
    renderSmallFont(g2d, 4, 9, "SlowFall");
    renderSmallFont(g2d, 4, 11, "SpikR");
    renderSmallFont(g2d, 4, 13, "Robot Overlord");

    renderSmallFont(g2d, 14, 6, "Mush Mush");
    renderSmallFont(g2d, 14, 8, "Tankzor");
    renderSmallFont(g2d, 14, 11, "Sparkly Jr");

    renderSmallFont(g2d, 5, 14, "press fire for main menu", false, false);
    renderSmallFont(g2d, 5, 14, "press jump for next page", false, true);
  }

  private void renderCredits(Graphics2D g2d) {
    String[] t = { "Credits", "", "Programming",
        "    Pumbaa aka Erik Holstensson", "", "Pixly Graphics",
        "    Pumbaa aka Erik Holstensson", "", "Cheeky Sound Effects",
        "    Pumbaa aka Erik Holstensson", "", "Awesome Music",
        "    Esau aka David Enheden", "",
        "All music used with permission from the", "creator.", "",
        "For more awesome music,", "visit www.amigatraktor.com", "" };
    renderTextOverlaySmall(g2d, t, new Point(2, 3));
    renderSmallFont(g2d, 5, 14, "press fire for main menu", false, false);
    renderSmallFont(g2d, 5, 14, "press jump for next page", false, true);
  }

  private void renderAdditional(Graphics2D g2d) {
    String[] t = {
        "for every human still alive,",
        "a robot will be built,",
        "to kill the breed within the hive,",
        "and free the world of guilt.",
        "",
        "to live and prosper on this world,",
        "is the only goal,",
        "for all the humans still not hurled,",
        "down the fire hole.",
        "",
        "the robot lords will brutally kill,",
        "every fleshie still not burned,",
        "there are no more free will,",
        "when the humans are overturned."
    };
    renderTextOverlaySmall(g2d, t, new Point(2, 4));
    renderSmallFont(g2d, 5, 14, "press fire for main menu", false, false);
    renderSmallFont(g2d, 5, 14, "press jump for next page", false, true);
  }

    private void renderAdditional2(Graphics2D g2d) {
    String[] t = {
        "You are the robot overlord", "",
        "Your task is to dominate the",
        "world.", "",
        "To succeed in the mission, you",
        "have to complete the stages, and",
        "kill Sparlky Jr and his minions",
        "They are the only robots which",
        "still symphetize with the humans.", "",
        "Thank you for playing the game!",
        "Have a nice day!"
    };
    renderTextOverlaySmall(g2d, t, new Point(2, 3));
    renderSmallFont(g2d, 5, 14, "press fire for main menu", false, false);
    renderSmallFont(g2d, 5, 14, "press jump for next page", false, true);

    renderTextOverlay(g2d, "TERMINATE ALL HUMANS!", new Point(2, 11), 14);
  }

  public void keyPressed(int key) {
    if (key == KeyEvent.VK_Z || key == KeyEvent.VK_C) {
      AudioPlayer.stop(AudioPlayer.MSX_END);
//      AudioPlayer.unload(AudioPlayer.MSX_END);
      sm.setState(GameStateManager.LEVEL_MENU);
    }
    if (key == KeyEvent.VK_X || key == KeyEvent.VK_SPACE) {
      nextPage(true);
    }
  }

  public void keyReleased(int key) {
  }
}
