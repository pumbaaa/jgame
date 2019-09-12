package mapObject;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import audioPlayer.AudioPlayer;
import gameState.GameStateManager;
import tileManager.TileManager;

public class SlowFall extends MapObject{

  public SlowFall(GameStateManager sm) {
    super(sm);

    spawnPoint.setLocation(new Point2D.Double(50.0, 50.0));
    coords.setLocation(spawnPoint);

    hitBox.setSize(16, 16);

    animation = new Animation[1];
    animation[0] = new Animation(((11 * 32) + 2), 1);
  }

  @Override
  public void update(){
  }
  
  @Override
  public boolean kill() {
    AudioPlayer.play(AudioPlayer.SFX_PICKUP);
    removeMe();
    return true;
  }

  @Override
  public void render(Graphics2D g2d) {
    double x = coords.getX();
    double y = coords.getY();

    // Draw the mushroom

    g2d.drawImage(TileManager.getTileImage(animation[currentAnim].getFrame()),
        (int) (x - TileManager.HALF_TILE), (int) (y - TileManager.HALF_TILE), null);

    drawHitBox(g2d);
  }
}
