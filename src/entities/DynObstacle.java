package entities;

import com.googlecode.lanterna.terminal.Terminal;
import obstacles.Obstacle;

import java.util.Random;


public class DynObstacle extends Obstacle {

    int x, y;
    Random r = new Random();
    private int dir;

    public static final char CHARCODE = '\u2603';   // zeichen für gegner
    public static final Terminal.Color COLORCODE = Terminal.Color.WHITE;    // farbe des gegner

    public DynObstacle(int x, int y) {  // initialisiert die werte

        this.x = x;
        this.y = y;
        super.x = x;
        super.y = y;
        charCode = CHARCODE;
        color = COLORCODE;

    }

    public int getRandom() {    // random zahl generierung für richtungen für die bewegungen
        dir = r.nextInt(4);
        return dir;
    }

    public void set() { // setzt die neue position abhängig vom generierten zufallsschritt

        // 0->up  1->right  2->down 3->left

        switch (dir) {
            case 0:
                y = y - 1;
                break;
            case 1:
                x = x + 1;
                break;
            case 2:
                y = y + 1;
                break;
            case 3:
                x = x - 1;
                break;
        }

    }
    public void removeOldPos(Terminal t,int xOffset, int yOffset){  // entfernt die alte position eines  dynamischen gegners
        t.moveCursor(x-xOffset, y-yOffset);
        t.putCharacter(' ');
    }

    public void draw(Terminal t, int xOffset, int yOffset){ // zeichnet den neuen gegner
        t.moveCursor(x-xOffset, y-yOffset); //xOffset und yOffset in Field erklärt
        t.applyForegroundColor(COLORCODE);
        t.putCharacter(CHARCODE);
    }

    public void setX(int x){    // getter und setter für gegner
        this.x = x;
    }

    public void setY(){
        this.y = y;
    }


    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }
}
