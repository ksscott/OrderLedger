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
import java.util.Set;

import javax.swing.JPanel;

import models.Board;
import models.Tile.Coordinate;
import models.Unit;

public class BoardSkin extends JPanel implements ActionListener, KeyListener {
	
	public static final int TILE_SIZE = 70;
	
	private Board board;
	
	public BoardSkin(Board board) {
		this.board = board;
		// length + 2 to draw player names:
		setPreferredSize(new Dimension(TILE_SIZE * (board.width+6), TILE_SIZE * (board.length+2)));
		setBackground(new Color(232, 232, 232));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		drawBackground(g);
		drawPlayers(g);
		drawOrderFields(g);
		
		Set<Unit> allUnits = board.allUnits(); // TODO
		for (Unit unit : allUnits) {
			UnitSkin us = new UnitSkin(unit, point(board.coord(unit)));
			us.draw(g, this);
		}
		
		// apparently smooths animations on some systems
		Toolkit.getDefaultToolkit().sync();
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
		Point topLeft = new Point(3 * TILE_SIZE, 1 * TILE_SIZE);
		Point topRight = new Point((3 + board.width) * TILE_SIZE, 1 * TILE_SIZE);
		Point bottomLeft = new Point(3 * TILE_SIZE, (1 + board.length) * TILE_SIZE);
		Point bottomRight = new Point((3 + board.width) * TILE_SIZE, (1 + board.length) * TILE_SIZE);
		
		g.setColor(Color.BLACK);
		g.drawLine(topLeft.x, topLeft.y, topRight.x, topRight.y); // top border
		g.drawLine(bottomLeft.x, bottomLeft.y, bottomRight.x, bottomRight.y); // bottom border
		g.drawLine(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y); // left border
		g.drawLine(topRight.x, topRight.y, bottomRight.x, bottomRight.y); // right border
	}
	
	private void drawPlayers(Graphics g) {
		Rectangle rectTop = new Rectangle(3 * TILE_SIZE, 0, TILE_SIZE * board.width, TILE_SIZE);
		drawText(g, board.top.name, new Font("Lato", Font.BOLD, 25), new Color(30, 201, 139), rectTop);
		
		Rectangle rectBottom = new Rectangle(3 * TILE_SIZE, TILE_SIZE * (board.length+1), TILE_SIZE * board.width, TILE_SIZE);
		drawText(g, board.bottom.name, new Font("Lato", Font.BOLD, 25), new Color(30, 201, 139), rectBottom);
	}
	
	private void drawOrderFields(Graphics g) {
		// TODO
		Rectangle rectTopNear = new Rectangle();
		// drawText();
		Rectangle rectTopMiddle = new Rectangle();
		// drawText();
		Rectangle rectTopFar = new Rectangle();
		// drawText();
		
		Rectangle rectBottomNear = new Rectangle();
		// drawText();
		Rectangle rectBottomMiddle = new Rectangle();
		// drawText();
		Rectangle rectBottomFar = new Rectangle();
		// drawText();
	}
	
	private void drawText(Graphics g, String text, Font font, Color color, Rectangle container) {
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
	
	private static Point point(Coordinate coord) {
		return new Point(coord.c, coord.r);
	}
}
