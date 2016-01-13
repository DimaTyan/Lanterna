package level;

import com.googlecode.lanterna.terminal.Terminal;

/**
 * Created by Dimitri on 05.01.16.
 */
public class MessageManager {

    public static void printText(Terminal t, int x, int y,String text, Terminal.Color color){

        t.moveCursor(x,y);
        t.applyForegroundColor(color);
        for(int i = 0; i < text.length(); i++){
            t.putCharacter(text.charAt(i));
        }
    }

    public static void printText(Terminal t, int x, int y,String text){

        t.moveCursor(x,y);
        for(int i = 0; i < text.length(); i++){
            t.putCharacter(text.charAt(i));
        }
    }
}
