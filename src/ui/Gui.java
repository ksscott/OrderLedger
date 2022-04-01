package ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import models.Board;
import models.Player;

public class Gui {
	
	// Tutorial: https://learncodebygaming.com/blog/how-to-make-a-video-game-in-java-2d-basics
	
	private static void initWindow() {
		JFrame window = new JFrame("Order Ledger");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Player top = new Player("TopPlayer", 1);
		Player bottom = new Player("BottomPlayer", 2);
		Board board = new Board(top, bottom);
		BoardSkin boardSkin = new BoardSkin(board);
		window.add(boardSkin);
		window.addKeyListener(boardSkin);
		window.addMouseListener(boardSkin);
		
		window.setResizable(false);
		window.pack(); // pack after resize to avoid troubles of unspecified nature
		window.setLocationRelativeTo(null); // center of screen
		window.setVisible(true);
	}
	
	public static void guiGame() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				initWindow();
			}
		});
	}
}
