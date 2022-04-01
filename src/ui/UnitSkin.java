package ui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import models.Unit;

public class UnitSkin {
	
	private Unit unit;
	
	private BufferedImage image;
	private Point pos;

	public UnitSkin(Unit unit, Point point) {
		this.unit = unit;
		pos = point;
		
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
		int size = BoardSkin.TILE_SIZE;
		
		Image toDraw = image.getScaledInstance(BoardSkin.TILE_SIZE, BoardSkin.TILE_SIZE, Image.SCALE_SMOOTH);
		int x = pos.x * size + (top ? size : 0);
		int y = (pos.y+1) * size + (top ? size : 0);
		g.drawImage(toDraw, x, y, flip*size, flip*size, observer);
	}
}
