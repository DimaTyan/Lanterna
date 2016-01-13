package obstacles;

import com.googlecode.lanterna.terminal.Terminal;

import java.awt.*;

/**
 * Created by Dimitri on 26.12.15.
 */
public abstract class Obstacle {

    protected int x, y;
    protected char charCode;
    protected Terminal.Color color;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public char getCharCode(){
        return charCode;
    }

    public Terminal.Color getColorCode(){
        return color;
    }
}
