package obstacles;

import com.googlecode.lanterna.terminal.Terminal;
import java.awt.*;


public class Exit extends Obstacle{

    // visuelle codierung des ausgangs

    private static final char CHARCODE = 'e';
    private static final Terminal.Color COLORCODE = Terminal.Color.GREEN;


    public Exit(int x, int y){  // initialisiert die werte mit den koordinaten und codierung
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
