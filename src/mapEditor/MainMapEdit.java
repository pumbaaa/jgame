package mapEditor;

import javax.swing.JFrame;

public class MainMapEdit {
  public static void main(String[] args) {
    JFrame window = new JFrame("TileMap Editor");
    window.setContentPane(new MapEditor());
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setResizable(false);
    window.pack();
    window.setVisible(true);
  }
}
