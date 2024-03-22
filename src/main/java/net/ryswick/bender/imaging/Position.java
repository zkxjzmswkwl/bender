package net.ryswick.bender.imaging;

import java.awt.Rectangle;

import lombok.Getter;
import lombok.Setter;

public class Position {
    @Getter @Setter private int x;
    @Getter @Setter private int y;
    @Getter @Setter private int width;
    @Getter @Setter private int height;

    public Position(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle toRectangle() {
        return new Rectangle(x, y, width, height);
    }
}
