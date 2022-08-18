package ui;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import models.Board;
import models.Player;
import networking.Client;
import networking.Host;
import networking.Server;

public class Gui {
	
	// Tutorial: https://learncodebygaming.com/blog/how-to-make-a-video-game-in-java-2d-basics
	
	private static final JFrame window = new JFrame("Order Ledger");
	public static final Color BACKGROUND = new Color(232, 232, 232);
	
	private static boolean online;
	private static Player top;
	private static Player bottom;
	private static boolean hosting;
	private static Server server;
	private static String hostIpAddress;

	public static void guiGame() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				initWindow();
			}
		});
	}
	
	private static void initWindow() {
//		JFrame window = new JFrame("Order Ledger");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Menu menu = new Menu();
		window.add(menu);
		
		window.setResizable(false);
		window.pack(); // pack after resize to avoid troubles of unspecified nature
		window.setLocationRelativeTo(null); // center of screen
		window.setVisible(true);
	}
	
	private static void initGame(JFrame window) {
		if (online) {
			server = connect(bottom.name, hosting, hostIpAddress);
			
			String theirName = server.pollDataFromServer();
			while (theirName == null) {
				try {
					Thread.sleep(Server.POLL_INTERVAL_MILLIS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				theirName = server.pollDataFromServer();
				// FIXME allow user to quit while waiting for other user
			}
			
			top = new Player(theirName, 1); // FIXME index?
		} else { // local
			
		}
		
		Board board = new Board(top, bottom);
		BoardSkin boardSkin = new BoardSkin(board, server);
		window.add(boardSkin);
		window.addKeyListener(boardSkin);
		window.addMouseListener(boardSkin);
		
		window.setResizable(false);
		window.pack(); // pack after resize to avoid troubles of unspecified nature
		window.setLocationRelativeTo(null); // center of screen
		window.setVisible(true);
	}
	
	
	// Main Menu
	private static class Menu extends JPanel {
		
		public Menu() {
			setPreferredSize(new Dimension(600, 400));
			setBackground(BACKGROUND);
			
			Button localButton = localButton(this);
//			localButton.setBounds(25, 125, 50, 200);
			localButton.setLocation(25, 125);
			this.add(localButton);
			Button hostButton = hostButton(this);
//			onlineButton.setBounds(25, 200, 50, 200);
			hostButton.setLocation(25, 200);
			this.add(hostButton);
			Button joinButton = joinButton(this);
//			onlineButton.setBounds(25, 275, 50, 200);
			joinButton.setLocation(25, 200);
			this.add(joinButton);
			
			Label welcome = new Label("Welcome to Order Ledger");
			this.add(welcome);
		}
		
//		@Override
//		public void paintComponent(Graphics g) {
//			super.paintComponent(g);
//			drawText(g, "Welcome to Order Ledger", new Font("times", Font.PLAIN, 20), Color.BLUE, new Rectangle(200,25,200,50));
//		}
	}
	
	private static class LocalMenu extends JPanel {
		public LocalMenu() {
			setPreferredSize(new Dimension(600, 400));
			setBackground(BACKGROUND);
			
			Label topPlayerLabel = new Label("Top player name:");
			topPlayerLabel.setBounds(50,20, 150,30);
			this.add(topPlayerLabel);
			TextField topNameField = new TextField("", 15);
			topNameField.setBounds(50,50, 150,30);
			this.add(topNameField);
			
			Label bottomPlayerLabel = new Label("Bottom player name:");
			bottomPlayerLabel.setBounds(50,120, 150,30);
			this.add(bottomPlayerLabel);
			TextField bottomNameField = new TextField("", 15);
			bottomNameField.setBounds(50,150, 150,30);
			this.add(bottomNameField);
			
			Button goButton = new Button("Go");
			goButton.setLocation(225, 200);
			goButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					top = new Player(topNameField.getText(), 1);
					bottom = new Player(bottomNameField.getText(), 2);
					window.remove(LocalMenu.this);
					initGame(window);
				}
			});
			this.add(goButton);
			
			Button backButton = new Button("Back");
			backButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					window.remove(LocalMenu.this);
					Menu menu = new Menu();
					window.add(menu);
					
					window.setResizable(false);
					window.pack(); // pack after resize to avoid troubles of unspecified nature
					window.setLocationRelativeTo(null); // center of screen
					window.setVisible(true);
				}
			});
			this.add(backButton);
		}
	}
	
	private static class OnlineMenu extends JPanel {
		public OnlineMenu() {
			setPreferredSize(new Dimension(600, 400));
			setBackground(BACKGROUND);
			
			Label nameLabel = new Label("Player name:");
			nameLabel.setBounds(50,20, 150,30);
			this.add(nameLabel);
			TextField nameField = new TextField("", 15);
			nameField.setBounds(50,50, 150,30);
			this.add(nameField);
			
			Label ipLabel = new Label("Enter host IP address:");
			ipLabel.setBounds(50,120, 150,30);
			TextField ipField = new TextField("", 10);
			ipField.setBounds(50,150, 150,30);
			if (!hosting) {
				this.add(ipLabel);
				this.add(ipField);
			}
			
			Button goButton = new Button("Go");
			goButton.setLocation(new Point(225, 200));
			goButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					bottom = new Player(nameField.getText(), 2); // FIXME Indexes!! ??
					hostIpAddress = ipField.getText();
					window.remove(OnlineMenu.this);
					initGame(window);
				}
			});
			this.add(goButton);
			
			Button backButton = new Button("Back");
			backButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					window.remove(OnlineMenu.this);
					Menu menu = new Menu();
					window.add(menu);
					
					window.setResizable(false);
					window.pack(); // pack after resize to avoid troubles of unspecified nature
					window.setLocationRelativeTo(null); // center of screen
					window.setVisible(true);
				}
			});
			this.add(backButton);
		}
	}
	
	private static Button localButton(Menu menu) {
		Button localButton = new Button("Local");
		localButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				online = false;
				window.remove(menu);
				window.add(new LocalMenu());
				
				window.setResizable(false);
				window.pack(); // pack after resize to avoid troubles of unspecified nature
				window.setLocationRelativeTo(null); // center of screen
				window.setVisible(true);
			}
		});
		return localButton;
	}
	
	private static Button hostButton(Menu menu) {
		Button hostButton = new Button("Host");
		hostButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				online = true;
				hosting = true;
				window.remove(menu);
				window.add(new OnlineMenu());
				
				window.setResizable(false);
				window.pack(); // pack after resize to avoid troubles of unspecified nature
				window.setLocationRelativeTo(null); // center of screen
				window.setVisible(true);
			}
		});
		return hostButton;
	}
	
	private static Button joinButton(Menu menu) {
		Button joinButton = new Button("Join");
		joinButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				online = true;
				hosting = false;
				window.remove(menu);
				window.add(new OnlineMenu());
				
				window.setResizable(false);
				window.pack(); // pack after resize to avoid troubles of unspecified nature
				window.setLocationRelativeTo(null); // center of screen
				window.setVisible(true);
			}
		});
		return joinButton;
	}
	
	private static Server connect(String playerName, boolean hosting, String hostIpAddress) {
		Server server = null;
		if (hosting) {
			server = new Host(playerName);
		} else { // client
			server = new Client(playerName, hostIpAddress);
		}
		boolean connected = server.start();
		// FIXME allow user to quit during connection
		return connected ? server : null;
	}
	
	private static void drawText(Graphics g, String text, Font font, Color color, Rectangle container) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setColor(color);
		g2d.setFont(font);
		FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
		
		int x = container.x + (container.width - metrics.stringWidth(text)) / 2;
		int y = container.y + ((container.height - metrics.getHeight()) / 2) + metrics.getAscent();
		g2d.drawString(text, x, y);
	}
}
