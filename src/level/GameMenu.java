package level;

import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.Terminal;

public class GameMenu { // klasse für das menü

    private Terminal ter;
    private int cursorY;
    private int centerX, centerY;
    private int option;

    public GameMenu(Terminal t){
        this.ter = t;
    }

    public void createMenu(boolean newgame){    // erzeugt menü mit optionen zur auswahl
        ter.clearScreen();
        ter.setCursorVisible(true);
        ter.applyForegroundColor(Terminal.Color.WHITE);
        centerX = ((ter.getTerminalSize().getColumns()-1)/2)-4;
        centerY = (ter.getTerminalSize().getRows()-1)/2;
        if(newgame) {
            MessageManager.printText(ter, centerX, centerY, GameMessages.NEWGAME.toString());
        }
        else MessageManager.printText(ter, centerX, centerY, GameMessages.RESUME.toString());
        MessageManager.printText(ter,centerX, centerY+1 ,GameMessages.SAVE.toString());
        MessageManager.printText(ter,centerX, centerY+2, GameMessages.LOAD.toString());
        MessageManager.printText(ter,centerX, centerY+3, GameMessages.KEYS.toString());
        MessageManager.printText(ter,centerX, centerY+4, GameMessages.QUITANDSAVE.toString());
        MessageManager.printText(ter,centerX, centerY+5, GameMessages.QUIT.toString());

        ter.moveCursor(centerX, centerY);
        cursorY = centerY;
    }

    public boolean optionSelected(){


        boolean selecting = true;
        while(selecting) {
            Key key = ter.readInput();
            try {

                if (key.getKind() != null) {
                    if (key.getKind() == Key.Kind.ArrowDown) {

                        if(cursorY == centerY+5) cursorY = centerY+5;  // holt sich abhängig vom cursor die ausgewählte option
                        else cursorY++;
                        ter.moveCursor(centerX,cursorY);

                    } else if (key.getKind() == Key.Kind.ArrowUp) {
                        if(cursorY == centerY) cursorY = centerY;
                        else cursorY--;
                        ter.moveCursor(centerX, cursorY);

                    } else if (key.getKind() == Key.Kind.Enter) {
                        selecting = false;
                        if(cursorY == centerY) option = 0;
                        else if(cursorY == centerY+1) option = 1;   // centerY ist die erste option centerY+1 die zweite usw...
                        else if(cursorY == centerY+2) option = 2;
                        else if(cursorY == centerY+3) option = 3;
                        else if(cursorY == centerY+4) option = 4;
                        else if(cursorY == centerY+5) option = 5;
                        ter.setCursorVisible(false);
                        return true;
                    }
                }

            } catch (Exception e){} // e.printStackTrace();
        }
        return false;
    }

    public void showLevels(){
        ter.clearScreen();
    }

    public int getOption(){
        return option;
    }   // gibt die gewählte option wieder
}
