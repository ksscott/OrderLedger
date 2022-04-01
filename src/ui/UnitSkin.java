package ui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import models.Tile.Coordinate;
import models.Unit;

public class UnitSkin {
	
	Unit unit;
	
	private BufferedImage image;
	private Coordinate coord;

	public UnitSkin(Unit unit, Coordinate location) {
		this.unit = unit;
		coord = location;
		
		loadImage();
	}
	
	private void loadImage() {
		boolean top = unit.player.playerIndex == 1;
		String fileName;
		String extension = ".png";
		
		switch (unit.config()) {
		default:
		case DEFAULT:
			fileName = top ? "DefaultOrange" : "DefaultBlue";
			break;
		case CANNONS:
			fileName = top ? "CannonsOrange" : "CannonsBlue";
			break;
		case SCOUT:
			fileName = top ? "ScoutOrange" : "ScoutBlue";
			break;
		case MISSILES:
			fileName = top ? "MissileOrange" : "MissileBlue";
			break;
		}

		try {
			image = ImageIO.read(new File("ShipImages/" + fileName + extension));
		} catch (IOException e) {
			System.out.println("Error opening image file: " + fileName + extension);
			e.printStackTrace();
		}
	}
	
	public void draw(Graphics g, ImageObserver observer) {
		boolean top = unit.player.playerIndex == 1;
		int flip = top ? -1 : 1;
		Image toDraw = image.getScaledInstance(BoardSkin.TILE_SIZE, BoardSkin.TILE_SIZE, Image.SCALE_SMOOTH);
		
		Rectangle area = area();
		g.drawImage(toDraw, 
				area.x + (top ? area.width : 0), 
				area.y + (top ? area.height : 0), 
				flip*area.width, 
				flip*area.height, observer);
	}
	
	public Rectangle area() {
		return new Rectangle(location().x, location().y, BoardSkin.TILE_SIZE, BoardSkin.TILE_SIZE);
	}
	
	private Point location() {
		int size = BoardSkin.TILE_SIZE;
		int xOffset = BoardSkin.ORDERS_WIDTH; // left OrderField // FIXME shouldn't be hard coded here
		int yOffset = 1 * size; // top Player name // FIXME shouldn't be hard coded here
		
		int x = (coord.c * size) + xOffset;
		int y = (coord.r * size) + yOffset;
		return new Point(x, y);
	}
}
