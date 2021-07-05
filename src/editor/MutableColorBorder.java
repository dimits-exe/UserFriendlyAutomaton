package editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.border.LineBorder;

class MutableColorBorder extends LineBorder {
	private static final long serialVersionUID = 1649237099464980877L;
	
	private Color currentColor;

	public MutableColorBorder(Color color) {
		super(color);
		currentColor = color;
	}

	public void setColor(Color newColor) {
		currentColor = newColor;
	}
	
	@Override
	public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) {
		g.setColor(currentColor);
	    super.lineColor = currentColor;
	    super.paintBorder(c, g, x, y, width, height);
	}

}
