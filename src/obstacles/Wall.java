package obstacles;

import com.googlecode.lanterna.terminal.Terminal;

import java.awt.*;

/**
 * Created by Dimitri on 26.12.15.
 */
public class Wall extends Obstacle{

    // visuelle codierung der wände

    int x,y;
    private static final char CHARCODE = '\u2630';
    private static final Terminal.Color COLORCODE= Terminal.Color.MAGENTA ;

    public Wall(int x, int y){ // initialisiert die werte mit den koordinaten und codierung
        this.x = x;
        this.y = y;
        charCode = CHARCODE;
        color = this.COLORCODE;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public static char getCharcode(){
        return CHARCODE;
    }
    public static Terminal.Color getColorcode(){
        return COLORCODE;
    }
}
