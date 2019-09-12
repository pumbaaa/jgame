package gameState;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import audioPlayer.AudioPlayer;
import background.Background;
import game.Game;
import mapObject.*;

public class StateLevel4 extends GameState {

  public StateLevel4(GameStateManager sm) {
    super(sm);
    // Initiate resources
    initResources();
  }

  private void initResources() {
    initTileMap();
    initLayers();
    initMapObjects();
    initAudio();
  }

  private void initAudio() {
    AudioPlayer.playLoop(AudioPlayer.MSX_UNDERWORLD, 0);
  }

  private void initTileMap() {
    // Load up the state tile map
    load("/assets/map/level4/level4.xml");
  }

  private void initLayers() {
    // Backgrounds, rendered in order of appearance
    layers.get(GameStateManager.LAYER_BACKGROUND)
        .add(new Background(sm, "/assets/map/level4/backgroundcave.xml"));
  }

  private void initMapObjects() {
    // The intractable and other non intractable objects that are in the state
    mapObjects = new ArrayList<MapObject>();

    // Which map objects are there in this state
    mapObjects.add(new Door(sm));
    mapObjects.get(mapObjects.size() - 1).setSpawnPoint(new Point(18, 13));
    mapObjects.get(mapObjects.size() - 1)
        .setActionOnCollision(Game.ACTION_TELEPORT);
    mapObjects.get(mapObjects.size() - 1).setTeleport(GameStateManager.LEVEL_5);

    mapObjects.add(new Key(sm));
    mapObjects.get(mapObjects.size() - 1).setSpawnPoint(new Point(1, 10));
    mapObjects.get(mapObjects.size() - 1).setActionOnCollision(Game.ACTION_KEY);

    mapObjects.add(new SpikeBlock(sm));
    mapObjects.get(mapObjects.size() - 1).setSpawnPoint(new Point(10, 8));
    mapObjects.get(mapObjects.size() - 1).setActionOnCollision(Game.ACTION_DAMAGE);

    // Add some enemies and define their movements and spawn points
    enemy.add(new Mushroom(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(8, 10));
    enemy.get(enemy.size() - 1).keyLeft(true);
    enemy.add(new Tank(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(6, 6));
    enemy.get(enemy.size() - 1).keyRight(true);
    enemy.add(new Tank(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(14, 6));
    enemy.get(enemy.size() - 1).keyLeft(true);
    enemy.add(new Tank(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(8, 12));
    enemy.get(enemy.size() - 1).keyLeft(true);
    enemy.add(new Mushroom(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(1, 6));

    // The player
    player = new Player(sm);
    player.setSpawnPoint(new Point(9, 13));
  }

  public void reset() {
    super.reset();
    ammo = new ArrayList<MapObject>();

    initMapObjects();
  }
  
  @Override
  protected void actionTeleport(MapObject mo){
    if (PlayerSave.has(Game.ACTION_KEY)) {
      AudioPlayer.play(AudioPlayer.SFX_DOOR);
      sm.setState(mo.getTeleport());
    }
  }

  public void update() {
    updateBackground();
    updatePlaygroundBack();
    updateMapObjects();
    updatePlaygroundFront();
    updateForeground();
  }

  public void render(Graphics2D g2d) {
    renderBackgrounds(g2d);
    renderPlaygroundsBack(g2d);
    renderPlayground(g2d);
    renderMapObjects(g2d);
    renderPlaygroundsFront(g2d);
    renderForegrounds(g2d);
    renderHud(g2d);
  }

  public void keyPressed(int key) {
    if (!player.timeToRemove()) {
      keyPressedPlayer(key);
    } else {
      if (key == KeyEvent.VK_Z || key == KeyEvent.VK_C) {
        reset();
      }
    }
  }

  public void keyReleased(int key) {
    keyReleasedPlayer(key);
  }
}
