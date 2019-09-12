package gameState;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import audioPlayer.AudioPlayer;
import background.Background;
import game.Game;
import mapObject.*;

public class StateLevel5 extends GameState {

  public StateLevel5(GameStateManager sm) {
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
  }

  private void initTileMap() {
    // Load up the state tile map
    load("/assets/map/level5/level5.xml");
  }

  private void initLayers() {
    // Backgrounds, rendered in order of appearance
    layers.get(GameStateManager.LAYER_BACKGROUND)
        .add(new Background(sm, "/assets/map/level5/backgroundcave.xml"));
  }

  private void initMapObjects() {
    // The intractable and other non intractable objects that are in the state
    mapObjects = new ArrayList<MapObject>();

    // Which map objects are there in this state
    mapObjects.add(new Door(sm));
    mapObjects.get(mapObjects.size() - 1).setSpawnPoint(new Point(18, 14));
    mapObjects.get(mapObjects.size() - 1)
        .setActionOnCollision(Game.ACTION_TELEPORT);
    mapObjects.get(mapObjects.size() - 1).setTeleport(GameStateManager.LEVEL_6);

    mapObjects.add(new Key(sm));
    mapObjects.get(mapObjects.size() - 1).setSpawnPoint(new Point(2, 3));
    mapObjects.get(mapObjects.size() - 1).setActionOnCollision(Game.ACTION_KEY);

    mapObjects.add(new SpikeBlock(sm));
    mapObjects.get(mapObjects.size() - 1).setSpawnPoint(new Point(10, 6));
    mapObjects.add(new SpikeBlock(sm));
    mapObjects.get(mapObjects.size() - 1).setSpawnPoint(new Point(11, 6));
    mapObjects.add(new SpikeBlock(sm));
    mapObjects.get(mapObjects.size() - 1).setSpawnPoint(new Point(12, 6));


    // Add some enemies and define their movements and spawn points
    enemy.add(new Mushroom(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(3, 3));
    enemy.add(new Mushroom(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(3, 3));
    enemy.get(enemy.size() - 1).keyLeft(true);
    enemy.add(new Tank(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(14, 5));
    enemy.get(enemy.size() - 1).keyRight(true);
    enemy.add(new Tank(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(10, 8));
    enemy.get(enemy.size() - 1).keyLeft(true);
    enemy.add(new Tank(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(16, 5));
    enemy.get(enemy.size() - 1).keyRight(true);
    enemy.add(new Tank(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(8, 10));
    enemy.get(enemy.size() - 1).keyRight(true);

    // The player
    player = new Player(sm);
    player.setSpawnPoint(new Point(18, 13));
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
      AudioPlayer.stop(AudioPlayer.MSX_UNDERWORLD);
//      AudioPlayer.unload(AudioPlayer.MSX_UNDERWORLD);
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
