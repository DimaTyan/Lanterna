package obstacles;

import com.googlecode.lanterna.terminal.Terminal;

public class StatObstacle extends Obstacle {


    // visuelle codierung des statischen hindernisses
    private static final char CHARCODE = '\u26a1';
    private static final Terminal.Color COLORCODE = Terminal.Color.RED;

    public StatObstacle(int x, int y){ // initialisiert die werte mit den koordinaten und codierung
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
