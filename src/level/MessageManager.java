package level;

import com.googlecode.lanterna.terminal.Terminal;


public class MessageManager {

    // klasse stellt 2 methoden bereit, einmal zum schreiben mit und ohne angabe der farbe
    // erleichtert string ausgaben auf dem terminal

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
