package level;

import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.Terminal;
import entities.DynObstacle;
import entities.FKey;
import entities.Player;
import obstacles.Entrance;
import obstacles.Exit;
import obstacles.StatObstacle;
import obstacles.Wall;

public class MenuKeys {

    private Terminal t;
    public MenuKeys(Terminal t){

        this.t = t;
        this.t.clearScreen();   // löscht den bildschirm
    }

    public void createKeys(){

        t.applyForegroundColor(FKey.getColorcode());
        MessageManager.printText(t,0,0, FKey.getCharcode()+" = schlüssel"); // schreibt die einzelnen symbole und ihre bedeutung
                                                                            // messagemanager ist eine klasse zum einfacheren schreiben von strings
        t.applyForegroundColor(Player.getColorcode());
        MessageManager.printText(t,0,1, Player.getPlayercode()+" = spieler");

        t.applyForegroundColor(Wall.getColorcode());
        MessageManager.printText(t,0,2, Wall.getCharcode()+" = wand");

        t.applyForegroundColor(Exit.getColorcode());
        MessageManager.printText(t,0,3, Exit.getCharcode()+" = ausgang");

        t.applyForegroundColor(Entrance.getColorcode());
        MessageManager.printText(t,0,4, Entrance.getCharcode()+" = eingang");

        t.applyForegroundColor(StatObstacle.getColorcode());
        MessageManager.printText(t,0,5, StatObstacle.getCharcode()+" = statischer Gegner");

        t.applyForegroundColor(DynObstacle.COLORCODE);
        MessageManager.printText(t,0,6, DynObstacle.CHARCODE+" = dynamischer Gegner");

        t.applyForegroundColor(Terminal.Color.WHITE);
        MessageManager.printText(t,0,7, "drücke esc um wieder zum spiel zu kommen");
        leaveKeys(t);
    }

    private void leaveKeys(Terminal t){

        boolean readingIn = true;
        while(readingIn){
            Key key = t.readInput();
            try{

                if(key.getKind() != null){
                    if(key.getKind() == Key.Kind.Escape){   // falls escape gedrückt wird, kommt man aus der legende raus
                        readingIn = false;
                    }
                }

            }catch(Exception e){}

        }
    }
}
