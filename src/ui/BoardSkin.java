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
	
	public static final int TILE_SIZE = 55;
	
	private Board board;
	
	public BoardSkin(Board board) {
		this.board = board;
		// length + 2 to draw player names:
		setPreferredSize(new Dimension(TILE_SIZE * board.width, TILE_SIZE * (board.length+2)));
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
		for (int row = 0; row < board.length; row++) {
			for (int col = 0; col < board.width; col++) {
				// color every other tile
				if ((row + col) % 2 == 1) {
					// row+1 because of TopPlayer name
					g.fillRect(col * TILE_SIZE, (row+1) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				}
			}
		}
	}
	
	private void drawPlayers(Graphics g) {
		String topPlayer = board.top.name;
		String bottomPlayer = board.bottom.name;
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setColor(new Color(30, 201, 139));
		g2d.setFont(new Font("Lato", Font.BOLD, 25));
		FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
		
		Rectangle rectTop = new Rectangle(0, 0, TILE_SIZE * board.width, TILE_SIZE);
		int xTop = rectTop.x + (rectTop.width - metrics.stringWidth(topPlayer)) / 2;
		int yTop = rectTop.y + ((rectTop.height - metrics.getHeight()) / 2) + metrics.getAscent();
		g2d.drawString(topPlayer, xTop, yTop);
		
		Rectangle rectBottom = new Rectangle(0, TILE_SIZE * (board.length+1), TILE_SIZE * board.width, TILE_SIZE);
		int xBottom = rectBottom.x + (rectBottom.width - metrics.stringWidth(bottomPlayer)) / 2;
		int yBottom = rectBottom.y + ((rectBottom.height - metrics.getHeight()) / 2) + metrics.getAscent();
		g2d.drawString(bottomPlayer, xBottom, yBottom);
	}
	
	private static Point point(Coordinate coord) {
		return new Point(coord.c, coord.r);
	}
}
