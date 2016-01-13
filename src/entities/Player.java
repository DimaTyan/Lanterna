package entities;

import com.googlecode.lanterna.terminal.Terminal;

/**
 * Created by Dimitri on 25.12.15.
 */
public class Player {

    private int x,y;
    private static final char PLAYERCODE = '\u263b';
    private boolean hasKey = false; // ob spieler einen schlüssel hat
    private static final Terminal.Color COLOR = Terminal.Color.YELLOW;
    private int lives = 3;  // 3 leben zu beginn
    private int absX, absY; // absolute koordinaten wie sie in der properties stehen, um den offset zu berechnen


    public Player(int x, int y){    // init der x und y werte
        this.x = x;
        this.y = y;
    }

    // -----------             BEGIN               ------------//
    // ----------- getter und setter für attribute ------------//

    public Terminal.Color getColor() {
        return COLOR;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getLives() {
        return lives;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {

        this.x = x;
    }

    public int getY() {

        return y;
    }

    public int getX() {

        return x;
    }

    public char getPlayerCode() {
        return PLAYERCODE;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    public boolean getHasKey() {

        return hasKey;
    }

    public static char getPlayercode() {
        return PLAYERCODE;
    }

    public static Terminal.Color getColorcode(){
        return COLOR;
    }


    public void setAbsX(int absX) {
        this.absX = absX;
    }

    public void setAbsY(int absY) {
        this.absY = absY;
    }

    public int getAbsX() {
        return absX;
    }

    public int getAbsY() {
        return absY;
    }

    // -----------             END                   ------------//



    public void movePlayer(Terminal terminal, int xPos, int yPos){  // bewegt den spieler an die bewegte position

        terminal.moveCursor(x, y);
        terminal.putCharacter(' '); // zum überschreiben der alten position
        setY(yPos);
        setX(xPos);
        terminal.applyForegroundColor(COLOR);
        terminal.moveCursor(xPos, yPos);
        terminal.putCharacter(PLAYERCODE);

    }

    public void setNotHitable(Terminal terminal){   // TODO
        try {
            terminal.applySGR(Terminal.SGR.ENTER_BLINK);
            movePlayer(terminal, getX(), getY());
            Thread.sleep(2000);
            terminal.applySGR(Terminal.SGR.EXIT_BLINK);
            movePlayer(terminal, getX(), getY());
        } catch (Exception e) {
        }
    }


}
