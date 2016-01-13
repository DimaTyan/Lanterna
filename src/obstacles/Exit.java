package obstacles;

import com.googlecode.lanterna.terminal.Terminal;
import java.awt.*;
/**
 * Created by Dimitri on 26.12.15.
 */
public class Exit extends Obstacle{

    private static final char CHARCODE = '\u2690';
    private static final Terminal.Color COLORCODE = Terminal.Color.GREEN;

    public Exit(int x, int y){
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
