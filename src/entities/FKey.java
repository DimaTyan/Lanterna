package entities;

import com.googlecode.lanterna.terminal.Terminal;
import obstacles.Obstacle;

/**
 * Created by Dimitri on 26.12.15.
 */
public class FKey extends Obstacle{

    private static final char CHARCODE = '\ua117';
    private static final Terminal.Color COLORCODE = Terminal.Color.YELLOW;

    public FKey(int x, int y){
        this.x = x;
        this.y = y;
        color = COLORCODE;
        charCode = CHARCODE;
    }

    public static char getCharcode(){
        return CHARCODE;
    }
    public static Terminal.Color getColorcode(){
        return COLORCODE;
    }

}
