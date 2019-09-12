package gameState;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import audioPlayer.AudioPlayer;
import game.Game;

public class GameStateManager {
	public static final int LAYER_BACKGROUND = 0;
	public static final int LAYER_PLAYGROUND_BEHIND_MAPOBJECTS = 2;
	public static final int LAYER_PLAYGROUND_INFRONTOF_MAPOBJECTS = 3;
	public static final int LAYER_FOREGROUND = 4;

	public static final int LEVEL_MENU = 0;
	public static final int LEVEL_1 = 10;
	public static final int LEVEL_2 = 11;
	public static final int LEVEL_3 = 12;
	public static final int LEVEL_4 = 13;
	public static final int LEVEL_5 = 14;
	public static final int LEVEL_6 = 15;
	public static final int LEVEL_INFO = 97;
	public static final int LEVEL_THEEND = 98;
	public static final int LEVEL_LOADING = 99;

	// Game States container
	HashMap<Integer, GameState> gameStates;
	// ArrayList<GameState> gameStates;
	private int currentGameState;
	private Game game;

	public GameStateManager(Game game) {
		// This is to be able to access the shake-functionality
		this.game = game;
		// Initiate the player status and inventory manager
		initAudio();
		PlayerSave.init(0, 0, 0);
		gameStates = new HashMap<Integer, GameState>();
		// gameStates = new ArrayList<GameState>();
		// Set the game state in which the game will launch
		// Initiate and populate the array in which the game states reside
		initStates();
		setState(LEVEL_LOADING);
		setState(LEVEL_MENU);
	}

	private void initAudio() {
		AudioPlayer.load(AudioPlayer.SFX_BOOM);
		AudioPlayer.load(AudioPlayer.SFX_ENEMYDEATH);
		AudioPlayer.load(AudioPlayer.SFX_DEATH);
		AudioPlayer.load(AudioPlayer.SFX_DOOR);
		AudioPlayer.load(AudioPlayer.SFX_BOSSDEATH);
		AudioPlayer.load(AudioPlayer.SFX_SELECT);
		AudioPlayer.load(AudioPlayer.SFX_MENUOPTION);

		AudioPlayer.load(AudioPlayer.SFX_SSH);
		AudioPlayer.load(AudioPlayer.SFX_SSH_SHORT);
		AudioPlayer.load(AudioPlayer.SFX_FIRE);
		AudioPlayer.load(AudioPlayer.SFX_FIRE2);
		AudioPlayer.load(AudioPlayer.SFX_SPAWN);
		AudioPlayer.load(AudioPlayer.SFX_PICKUP);
		AudioPlayer.load(AudioPlayer.SFX_BLOPP);
		AudioPlayer.load(AudioPlayer.SFX_WOEW);
		AudioPlayer.load(AudioPlayer.SFX_SHOOT);

		AudioPlayer.load(AudioPlayer.MSX_OVERWORLD);
		AudioPlayer.load(AudioPlayer.MSX_BOSS);

		AudioPlayer.load(AudioPlayer.MSX_END);
		AudioPlayer.load(AudioPlayer.MSX_UNDERWORLD);
		AudioPlayer.load(AudioPlayer.MSX_MAIN_MENU);
	}

	private void initStates() {
		// Initiate the list of game states
		gameStates.put(LEVEL_MENU, null);
		gameStates.put(LEVEL_1, null);
		gameStates.put(LEVEL_2, null);
		gameStates.put(LEVEL_3, null);
		gameStates.put(LEVEL_4, null);
		gameStates.put(LEVEL_5, null);
		gameStates.put(LEVEL_6, null);
		gameStates.put(LEVEL_THEEND, null);
		gameStates.put(LEVEL_INFO, null);
		gameStates.put(LEVEL_LOADING, null);
		loadState(LEVEL_LOADING);
	}

	// Call this method do shake the screen
	public void shakeScreen(){
		game.shakeScreen();
	}
	
	// call the current states (levels) update/render/key-presses
	public void update() {
		gameStates.get(currentGameState).update();
	}

	public void render(Graphics2D graphics) {
		gameStates.get(currentGameState).render(graphics);
	}

	public void keyPressed(KeyEvent key) {
		if (key.getKeyCode() == KeyEvent.VK_ESCAPE || key.getKeyCode() == KeyEvent.VK_Q) {
			System.exit(0);
		}
		gameStates.get(currentGameState).keyPressed(key.getKeyCode());
	}

	public void keyReleased(KeyEvent key) {
		gameStates.get(currentGameState).keyReleased(key.getKeyCode());
	}

	// Load a state to memory
	private void loadState(int state) {
		switch (state) {
		case LEVEL_MENU:
			gameStates.put(state, new StateMainMenu(this));
			break;
		case LEVEL_1:
			gameStates.put(state, new StateLevel1(this));
			break;
		case LEVEL_2:
			gameStates.put(state, new StateLevel2(this));
			break;
		case LEVEL_3:
			gameStates.put(state, new StateLevel3(this));
			break;
		case LEVEL_4:
			gameStates.put(state, new StateLevel4(this));
			break;
		case LEVEL_5:
			gameStates.put(state, new StateLevel5(this));
			break;
		case LEVEL_6:
			gameStates.put(state, new StateLevel6(this));
			break;
		case LEVEL_INFO:
			gameStates.put(state, new StateInfo(this));
			break;
		case LEVEL_THEEND:
			gameStates.put(state, new StateTheEnd(this));
			break;
		case LEVEL_LOADING:
			gameStates.put(state, new StateLoading(this));
			break;
		}
	}

	// Free resources from a stage
	private void unloadState(int state) {
		if (state != currentGameState) {
			gameStates.put(state, null);
		}
	}

	// get current level (state)
	public int getState() {
		return currentGameState;
	}

	// set level (state)
	public void setState(int state) {
		if (state != LEVEL_LOADING) {
			currentGameState = LEVEL_LOADING;
			loadState(state);
			int t = currentGameState;
			unloadState(t);
			currentGameState = state;
		}
	}

	// return the width (in tiles) of the current level
	public int getWidth() {
		return gameStates.get(currentGameState).getNumTilesWidth();
	}

	// return the height (in tiles) of the current level
	public int getHeight() {
		return gameStates.get(currentGameState).getNumTilesHeight();
	}

	// Return the tile number of x, y on the current level map
	public int getTileId(Point mapCoords) {
		return gameStates.get(currentGameState).getImageId(mapCoords);
	}

	public int getTileId(double num) {
		Point mapCoords = new Point((int) num % getWidth(), (int) num % getHeight());
		return gameStates.get(currentGameState).getImageId(mapCoords);
	}

	public int getTileId(int num) {
		Point mapCoords = new Point((int) num % getWidth(), (int) num % getHeight());
		return gameStates.get(currentGameState).getImageId(mapCoords);
	}

	// Return the tile number of x, y on the current level map
	public int getTileType(Point mapCoords) {
		return gameStates.get(currentGameState).getType(mapCoords);
	}

	public int getTileType(double num) {
		Point mapCoords = new Point((int) num % getWidth(), (int) num % getHeight());
		return gameStates.get(currentGameState).getType(mapCoords);
	}

	public int getTileType(int num) {
		Point mapCoords = new Point((int) num % getWidth(), (int) num % getHeight());
		return gameStates.get(currentGameState).getType(mapCoords);
	}
}
