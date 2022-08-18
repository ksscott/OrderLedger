package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import models.Board;
import models.Configuration;
import models.Move;
import models.Order;
import models.Player;
import models.Reconfigure;
import models.Tile.Coordinate;
import models.Unit;
import models.Move.Direction;
import networking.Server;

public class BoardSkin extends JPanel implements ActionListener, KeyListener, MouseListener {
	
	public static final int TILE_SIZE = 70;
	public static final int ORDERS_WIDTH = 3 * TILE_SIZE;
	// for some reason, the very top of the window seems to be 30 instead of 0
	public static final int WEIRD_TOP_BORDER = 30;
	
	public static final Color BACKGROUND = new Color(232, 232, 232);
	public static final Color NAMES_COLOR = new Color(30, 201, 139);
	public static final Font NAMES_FONT = new Font("Lato", Font.BOLD, 25);
	public static final Color ORDERS_COLOR = new Color(30, 201, 139);
	public static final Font ORDERS_FONT = new Font("Lato", Font.PLAIN, 20);
	
	private Board board;
	int boardWidth;
	int boardLength;
	Rectangle boardBox;
	Rectangle topPlayerBox;
	Rectangle bottomPlayerBox;
	Rectangle leftOrdersBox; // does not include pending orders
	Rectangle rightOrdersBox; // does not include pending orders
	
	private Server server;
	private Player me;
	private Player winner;
	private UnitSkin selectedUnit;
	private Set<UnitSkin> drawnUnits;
	
	public BoardSkin(Board board, Server server) {
		this.board = board;
		this.server = server;
		this.me = board.bottom; // FIXME
		this.boardWidth = board.width * TILE_SIZE;
		this.boardLength = board.length * TILE_SIZE;
		
		topPlayerBox = new Rectangle(ORDERS_WIDTH, 0, boardWidth, TILE_SIZE);
		leftOrdersBox = new Rectangle(0, bottom(topPlayerBox), ORDERS_WIDTH, boardLength);
		boardBox = new Rectangle(right(leftOrdersBox), bottom(topPlayerBox), boardWidth, boardLength);
		rightOrdersBox = new Rectangle(right(boardBox), bottom(topPlayerBox), ORDERS_WIDTH, boardLength);
		bottomPlayerBox = new Rectangle(ORDERS_WIDTH, bottom(boardBox), boardWidth, TILE_SIZE);
		
		drawnUnits = new HashSet<>();
		
		setPreferredSize(new Dimension(right(rightOrdersBox), bottom(bottomPlayerBox)));
		setBackground(BACKGROUND);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		// FIXME for some reason, you must leave and refocus on 
		/// the game window or else it won't receive key presses
//		System.out.println("Key Pressed: " + e.getKeyCode());
		if (winner != null) { return; }
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			endTurn();
			return;
		}
		if (selectedUnit == null) { return; }
		
		Order order = null;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_D:
			order = new Reconfigure(Configuration.DEFAULT);
			break;
		case KeyEvent.VK_C:
			order = new Reconfigure(Configuration.CANNONS);
			break;
		case KeyEvent.VK_S:
			order = new Reconfigure(Configuration.SCOUT);
			break;
		case KeyEvent.VK_M:
			order = new Reconfigure(Configuration.MISSILES);
			break;
		case KeyEvent.VK_UP:
			order = new Move(Move.Direction.UP);
			break;
		case KeyEvent.VK_DOWN:
			order = new Move(Move.Direction.DOWN);
			break;
		case KeyEvent.VK_LEFT:
			order = new Move(Move.Direction.LEFT);
			break;
		case KeyEvent.VK_RIGHT:
			order = new Move(Move.Direction.RIGHT);
			break;
		default:
			return;
		}
		
		System.out.println("Issuing order " + order.draw() + " to unit " + selectedUnit.unit.draw());
		board.issueOrder(selectedUnit.unit, order);
		selectedUnit = null;
		repaint();
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		if (winner != null) { return; }
		switch(e.getButton()) {
		default:
		case 1: // left click to select ship
			UnitSkin clicked = atLoc(e.getPoint());
			if (clicked != null && (server == null || clicked.unit.player.equals(me))) {
				selectedUnit = clicked;
				System.out.println("Selected a unit: " + selectedUnit.unit.draw());
			} else {
				selectedUnit = null;
			}
			break;
		case 2: // right click to give a move order // FIXME this is my mouse wheel
//			if (selectedUnit == null) { return; }
//			Move order = new Move(Move.Direction.NONE); // FIXME click to give move orders
//			board.issueOrder(selectedUnit.unit, order);
			break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		drawBackground(g);
		drawPlayers(g);
		drawOrderFields(g);
		
		drawnUnits = new HashSet<>();
		Set<Unit> allUnits = board.allUnits(); // TODO
		for (Unit unit : allUnits) {
			UnitSkin us = new UnitSkin(unit, board.coord(unit));
			us.draw(g, this);
			drawnUnits.add(us);
		}
		
		// apparently smooths animations on some systems
		Toolkit.getDefaultToolkit().sync();
	}
	
	private void endTurn() {
		System.out.println("ENDING TURN");
		
		// send orders to other server:
		List<Order> myOrders = board.getOrders(me);
		server.addUserInput(encodeOrders(myOrders));
		// get their orders from the server:
		String theirInput = server.pollDataFromServer();
		List<Order> theirOrders = decodeOrders(theirInput);
		for (Order order : theirOrders) {
			order.mirror();
		}
		board.issueOrders(board.top, theirOrders);
		
		// FIXME something's broken when issuing the other player's orders...
		
		winner = board.applyOrders();
		repaint();
	}
	
	private static String encodeOrders(List<Order> orders) {
		return orders.stream()
				.map(BoardSkin::encodeOrder)
				.collect(Collectors.joining(" "));
	}
	
	private static String encodeOrder(Order order) {
		if (order instanceof Move) {
			return ((Move) order).direction.name();
		} else if (order instanceof Reconfigure) {
			return ((Reconfigure) order).config.name();
		}
		return "unrecognized_order";
	}
	
	// FIXME copied from Game.java
	private static List<Order> decodeOrders(String input) {
		return Arrays.stream(input.split("\\s+"))
				.map(BoardSkin::decode)
				.filter(u -> u != null)
				.collect(Collectors.toList());
	}
	
	// FIXME copied from Game.java
	private static Order decode(String input) {
		String command = input.toUpperCase();
		for (Direction dir : Direction.values()) {
			if (command.equals(dir.name())) {
				return new Move(dir);
			}
		}
		for (Configuration config : Configuration.values()) {
			if (command.equals(config.name())) {
				return new Reconfigure(config);
			}
		}
		return null;
	}
	
	private UnitSkin atLoc(Point point) {
		return drawnUnits
				.stream()
				.filter(unit -> unit.area().contains(point))
				.findAny()
				.orElse(null);
	}
	
	private void drawBackground(Graphics g) {
		g.setColor(new Color(214, 214, 214));
		// paint checkered board
		for (int row = 0; row < board.length; row++) {
			for (int col = 0; col < board.width; col++) {
				// color every other tile
				if ((row + col) % 2 == 1) {
					int xOffset = 3; // because of OrderField
					int yOffset = 1; // because of TopPlayer name
					g.fillRect((col+xOffset) * TILE_SIZE, (row+yOffset) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				}
			}
		}
		
		// paint OrderFields
		g.fillRect(0, 1 * TILE_SIZE, 3 * TILE_SIZE, 3 * TILE_SIZE); // top left
		g.fillRect(0, (1+6) * TILE_SIZE, 3 * TILE_SIZE, 3 * TILE_SIZE); // bottom left
		g.fillRect((3 + board.width) * TILE_SIZE, 1 * TILE_SIZE, 3 * TILE_SIZE, 3 * TILE_SIZE); // top right
		g.fillRect((3 + board.width) * TILE_SIZE, (1+6) * TILE_SIZE, 3 * TILE_SIZE, 3 * TILE_SIZE); // bottom right
		
		// lines
		int left = left(boardBox);
		int right = right(boardBox);
		int top = top(boardBox);
		int bottom = bottom(boardBox);
		
		g.setColor(Color.BLACK);
		g.drawLine(left, top, right, top); // top border
		g.drawLine(left, bottom, right, bottom); // bottom border
		g.drawLine(left, top, left, bottom); // left border
		g.drawLine(right, top, right, bottom); // right border
	}
	
	private void drawPlayers(Graphics g) {
		String topName = board.top.name;
		 if (winner == board.top) {
			 topName += " - WINNER!";
		 }
		 String bottomName = board.bottom.name;
		 if (winner == board.bottom) {
			 bottomName += " - WINNER!";
		 }
		drawText(g, topName, NAMES_FONT, NAMES_COLOR, topPlayerBox);
		drawText(g, bottomName, NAMES_FONT, NAMES_COLOR, bottomPlayerBox);
	}
	
	private void drawOrderFields(Graphics g) {
		int height = leftOrdersBox.height / 3;
		
		// Left Orders Field
		String textTopPending = board.orders.get(board.top).drawPending();
		Rectangle rectTopPending = new Rectangle(left(leftOrdersBox), 0, ORDERS_WIDTH, TILE_SIZE);
		drawText(g, textTopPending, ORDERS_FONT, ORDERS_COLOR, rectTopPending);
		
		String textTopNear = board.orders.get(board.top).draw(new Coordinate(0,0), true);
		Rectangle rectTopNear = new Rectangle(left(leftOrdersBox), top(leftOrdersBox), ORDERS_WIDTH, height);
		drawText(g, textTopNear, ORDERS_FONT, ORDERS_COLOR, rectTopNear);
		
		String textTopMiddle = board.orders.get(board.top).draw(new Coordinate(board.length/2,0), true);
		Rectangle rectTopMiddle = new Rectangle(left(leftOrdersBox), top(leftOrdersBox) + height, ORDERS_WIDTH, height);
		drawText(g, textTopMiddle, ORDERS_FONT, ORDERS_COLOR, rectTopMiddle);
		
		String textTopFar = board.orders.get(board.top).draw(new Coordinate(board.length-1,0), true);
		Rectangle rectTopFar = new Rectangle(left(leftOrdersBox), top(leftOrdersBox) + height*2, ORDERS_WIDTH, height);
		drawText(g, textTopFar, ORDERS_FONT, ORDERS_COLOR, rectTopFar);
		
		// Right Orders Field
		String textBottomPending = board.orders.get(board.bottom).drawPending();
		Rectangle rectBottomPending = new Rectangle(left(rightOrdersBox), bottom(rightOrdersBox), ORDERS_WIDTH, TILE_SIZE);
		drawText(g, textBottomPending, ORDERS_FONT, ORDERS_COLOR, rectBottomPending);
		
		String textBottomNear = board.orders.get(board.bottom).draw(new Coordinate(board.length-1,0), false);
		Rectangle rectBottomNear = new Rectangle(left(rightOrdersBox), top(rightOrdersBox) + height*2, ORDERS_WIDTH, height);
		drawText(g, textBottomNear, ORDERS_FONT, ORDERS_COLOR, rectBottomNear);
		
		String textBottomMiddle = board.orders.get(board.bottom).draw(new Coordinate(board.length/2,0), false);
		Rectangle rectBottomMiddle = new Rectangle(left(rightOrdersBox), top(rightOrdersBox) + height, ORDERS_WIDTH, height);
		drawText(g, textBottomMiddle, ORDERS_FONT, ORDERS_COLOR, rectBottomMiddle);
		
		String textBottomFar = board.orders.get(board.bottom).draw(new Coordinate(0,0), false);
		Rectangle rectBottomFar = new Rectangle(left(rightOrdersBox), top(rightOrdersBox), ORDERS_WIDTH, height);
		drawText(g, textBottomFar, ORDERS_FONT, ORDERS_COLOR, rectBottomFar);
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
	
	private static Point point(Coordinate coord) { return new Point(coord.c, coord.r); }
	
	private static int top(Rectangle rect) { return (int) rect.getY(); }
	private static int bottom(Rectangle rect) { return (int) rect.getMaxY(); }
	private static int left(Rectangle rect) { return (int) rect.getX(); }
	private static int right(Rectangle rect) { return (int) rect.getMaxX(); }
}
