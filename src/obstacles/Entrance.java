package obstacles;

import com.googlecode.lanterna.terminal.Terminal;

public class Entrance extends Obstacle {

    // visuelle codierung des eingangs

    private static final char CHARCODE = '\u26d4';
    private static final Terminal.Color COLORCODE = Terminal.Color.RED;

    public Entrance(int x, int y){  // initialisiert die werte mit koordinaten und codierung
        this.x = x;
        this.y = y;
        charCode = 'e';
        color = COLORCODE;
    }

    public static char getCharcode(){
        return CHARCODE;
    }
    public static Terminal.Color getColorcode(){
        return COLORCODE;
    }

}
