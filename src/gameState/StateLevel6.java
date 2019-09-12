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
import tileManager.TileManager;

public class StateLevel6 extends GameState {

  public StateLevel6(GameStateManager sm) {
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
    AudioPlayer.playLoop(AudioPlayer.MSX_BOSS, 0);
  }

  private void initTileMap() {
    // Load up the state tile map
    load("/assets/map/level6/level6.xml");
  }

  private void initLayers() {
    // Backgrounds, rendered in order of appearance
    layers.get(GameStateManager.LAYER_BACKGROUND)
        .add(new Background(sm, "/assets/map/level6/backgroundcave.xml"));
  }

  private void initMapObjects() {
    // The intractable and other non intractable objects that are in the state
    mapObjects = new ArrayList<MapObject>();

    // The boss
    enemy.add(new SparklyBoss(sm));
    enemy.get(enemy.size() - 1).setSpawnPoint(new Point(10, 8));
    enemy.get(enemy.size() - 1).keyLeft(true);
    enemy.get(enemy.size() - 1).keyUp(true);

    // The player
    player = new Player(sm);
    player.setSpawnPoint(new Point(18, 14));
  }

  public void reset() {
    super.reset();
    initMapObjects();
  }

  @Override
  protected void actionTeleport(MapObject mo) {
    if (PlayerSave.has(Game.ACTION_KEY)) {
      AudioPlayer.play(AudioPlayer.SFX_DOOR);
      //      AudioPlayer.b(AudioPlayer.MSX_BOSS);
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

  @Override
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
              AudioPlayer.stop(AudioPlayer.MSX_BOSS);
              AudioPlayer.play(AudioPlayer.SFX_BOSSDEATH);
              spawnDoorAndKey();
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

  private void spawnDoorAndKey() {
    // Which map objects are there in this state
    mapObjects.add(new Door(sm));
    mapObjects.get(mapObjects.size() - 1).setSpawnPoint(new Point(8, 14));
    mapObjects.get(mapObjects.size() - 1)
        .setActionOnCollision(Game.ACTION_TELEPORT);
    mapObjects.get(mapObjects.size() - 1)
        .setTeleport(GameStateManager.LEVEL_THEEND);

    // This key should be released when boss dies!
    mapObjects.add(new Key(sm));
    mapObjects.get(mapObjects.size() - 1).setSpawnPoint(new Point(11, 4));
    mapObjects.get(mapObjects.size() - 1).setActionOnCollision(Game.ACTION_KEY);
  }
  
  @Override
  protected void renderMapObjects(Graphics2D g2d) {
    // Map Objects
    for (MapObject mo : mapObjects) {
      mo.render(g2d);
    }

    // Enemies
    for (MapObject e : enemy) {
      if( !e.timeToRemove() ){
        e.render(g2d);
      }
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

  public void render(Graphics2D g2d) {
    renderBackgrounds(g2d);
    renderPlaygroundsBack(g2d);
    renderPlayground(g2d);
    renderMapObjects(g2d);
    renderPlaygroundsFront(g2d);
    renderForegrounds(g2d);
    renderHud(g2d);
    renderBossHp(g2d, enemy.get(0));
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
