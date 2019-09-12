package game;

import javax.swing.JFrame;

public class GameMain {
  private static int WIN_S = 2;

  public static void main(String[] args) {
    if (args.length > 0) {
      try {
    	int ts = Integer.parseInt(args[0]);
    	if( ts > 0 ) {
    		WIN_S = ts;
    	}
    	} catch (NumberFormatException e) {
      }
    }
    JFrame window = new JFrame("Generic Platformer");
    window.setContentPane(new Game());
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setResizable(false);
    window.pack();
    window.setVisible(true);
  }

  public static int getScale(){
    return WIN_S;
  }
}
