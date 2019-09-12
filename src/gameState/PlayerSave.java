/*
 * Here be stuff that will carry between levels, such as, but not limited to:
 * Kills, Death, Score
 */

package gameState;

import java.util.HashMap;

import game.Game;

public class PlayerSave {
  public static final int PLAYER_KILLCOUNT = 0;
  public static final int PLAYER_DEATHCOUNT = 1;
  public static final int PLAYER_SCORE = 2;

  private static HashMap<Integer, Integer> status;
  private static HashMap<Integer, Boolean> inventory;

  public static void init(int killCount, int deathCount, int score){
    status = new HashMap<Integer, Integer>();
    status.put(PLAYER_KILLCOUNT, killCount);
    status.put(PLAYER_DEATHCOUNT, deathCount);
    status.put(PLAYER_SCORE, score);

    inventory = new HashMap<Integer, Boolean>();
    inventory.put(Game.ACTION_KEY, false);
    inventory.put(Game.ACTION_SLOWFALL, false);
  }

  public static int get(int playerStatus){
    return status.get(playerStatus);
  }

  public static void addPoint(int playerStatus) {
    int t = status.get(playerStatus);
    ++t;
    status.put(playerStatus, t);
  }

  public static void addPoint(int playerStatus, int points) {
    int t = status.get(playerStatus);
    t += points;
    status.put(playerStatus, t);
  }

  // Reset everything needed to reset when room is remade
  public static void reset(){
    inventory.put(Game.ACTION_KEY, false);
  }

  public static void hardReset(){
    init(0,0,0);
  }
  
  public static void add(int addToInventory){
    inventory.put(addToInventory, true);
  }

  public static boolean has(int hasInInventory){
    return inventory.get(hasInInventory);
  }
}
