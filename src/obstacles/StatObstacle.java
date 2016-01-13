package obstacles;

import com.googlecode.lanterna.terminal.Terminal;

/**
 * Created by Dimitri on 04.01.16.
 */

import java.awt.*;
public class StatObstacle extends Obstacle {

    private static final char CHARCODE = '\u26a1';
    private static final Terminal.Color COLORCODE = Terminal.Color.YELLOW;

    public StatObstacle(int x, int y){
        this.x = x;
        this.y = y;
        charCode = CHARCODE;
        color = COLORCODE;
    }

    public static char getCharcode(){
        return CHARCODE;
    }
    public static Terminal.Color getColorcode(){
        return COLORCODE;
    }
}
