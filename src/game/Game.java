package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JPanel;

import audioPlayer.AudioPlayer;
import gameState.GameStateManager;
import tileManager.TileManager;

@SuppressWarnings("serial")
public class Game extends JPanel implements Runnable, KeyListener {

	// Window Width, Height and Scale.
	public static final int WIN_W = 320;
	public static final int WIN_H = 256;
	public static final int WIN_S = GameMain.getScale();
	public static final int WIN_COLS = WIN_W / TileManager.TILE_SIZE;
	public static final int WIN_ROWS = WIN_H / TileManager.TILE_SIZE;

	public static final int ACTION_TELEPORT = 1;
	public static final int ACTION_KEY = 2;
	public static final int ACTION_SLOWFALL = 3;
	public static final int ACTION_DAMAGE = 4;

	private boolean shake;
	private double shakeReturn;
	private double shakeScale;
	private int shakeTimer;
	private int shakeTimerInit;
	private Point2D.Double shakeCoords;

	private Thread thread;

	private boolean quit;

	private BufferedImage bufferedImage;
	private Graphics2D graphics;

	private GameStateManager sm;

	// Prepare window for the game
	public Game() {
		setPreferredSize(new Dimension(WIN_W * WIN_S, WIN_H * WIN_S));
		setFocusable(true);
		requestFocus();
	}

	// Waits for the game to be loaded, and starts a new thread for the
	// game
	public void addNotify() {
		super.addNotify();
		if (thread == null) {
			thread = new Thread(this);
			addKeyListener(this);
			thread.start();
		}
	}

	private void initialize() {
		// Create the double buffer-image to draw on before drawing onto screen
		bufferedImage = new BufferedImage(WIN_W * WIN_S, WIN_H * WIN_S, BufferedImage.TYPE_INT_RGB);
		graphics = bufferedImage.createGraphics();

		// The game will Exit by choosing Exit in the menus or closing the game
		// window. Might implement changing quit-variable to true, for a cleaner
		// exit further on.
		quit = false;

		// Initiate the TileManager
		TileManager.init();
		// Initiate the SoundPlayer
		AudioPlayer.init();

		// Start the game state manager
		sm = new GameStateManager(this);

		// Triggers and values for shaking the screen
		shake = false;
		shakeReturn = 0.8;
		shakeCoords = new Point2D.Double(0.0, 0.0);
		shakeTimer = 0;
		shakeTimerInit = 10;
		shakeScale = 7.0;
	}

	public void run() {
		initialize();

		// Limit the game to n fps
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

	// Let the state manager handle movements updates etc
	private void update() {
		sm.update();
	}

	private void renderBuffer() {
		// Clear screen (set to black)
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, WIN_W * WIN_S, WIN_H * WIN_S);

		// Let the state manager handle the rendering
		sm.render(graphics);
	}

	private void doShake() {
		double dx = shakeCoords.getX();
		double dy = shakeCoords.getY();
		if (shake) {
			Random r = new Random();
			dx = (r.nextDouble() - 1.0 ) * shakeScale;
			dy = (r.nextDouble() - 1.0 ) * shakeScale;
			if (shakeTimer > 0) {
				--shakeTimer;
			} else {
				shake = false;
			}
		} else {
			if (dx > 0.0) {
				dx -= shakeReturn;
				if (dx <= 0.0) {
					dx = 0.0;
				}
			}
			if (dx < 0.0) {
				dx += shakeReturn;
				if (dx >= 0.0) {
					dx = 0.0;
				}
			}
			if (dy > 0.0) {
				dy -= shakeReturn;
				if (dy <= 0.0) {
					dy = 0.0;
				}
			}
			if (dy < 0.0) {
				dy += shakeReturn;
				if (dy >= 0.0) {
					dy = 0.0;
				}
			}
		}
		shakeCoords.setLocation(dx, dy);
	}

	// Call to shake screen
	public void shakeScreen() {
		shake = true;
		shakeTimer = shakeTimerInit;
	}

	// Draw the buffered image to the game canvas
	private void renderScreen() {
		Graphics g = getGraphics();
		// Shake screen if needed
		doShake();
		int shx = (int) shakeCoords.getX();
		int shy = (int) shakeCoords.getY();

		// Scale the image up to preferred size, and draw it on the screen
		g.drawImage(bufferedImage, shx, shy, WIN_W * WIN_S + shx, WIN_H * WIN_S + shy, 0, 0, WIN_W, WIN_H, null);
		g.dispose();
	}

	// Let the state manager handle all the key presses
	public void keyPressed(KeyEvent key) {
		sm.keyPressed(key);
	}

	public void keyReleased(KeyEvent key) {
		sm.keyReleased(key);
	}

	public void keyTyped(KeyEvent key) {
	}

}
