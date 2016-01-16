package level;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminal;
import com.googlecode.lanterna.terminal.swing.TerminalAppearance;
import entities.DynObstacle;
import entities.FKey;
import entities.Player;
import obstacles.Entrance;
import obstacles.Exit;
import obstacles.Obstacle;
import obstacles.StatObstacle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Field {

    /*

    Scrollen
    Beim Scrollen ist es so, dass ein Offset berechnet werden muss. Das heißt,
    dass wenn der Spieler an einer Position beginnt, die für das Terminal zu groß ist,
    dann muss ein Offset berechnet werden. Dieser ist vielfaches der Terminalgröße und
    berechnet sich mithilfe der absolute Spieler Position, wie sie in der Properties definiert ist.
    Das heißt, dass diese Position durch die Terminalgröße geteilt wird(für X und Y Offset berechnen).
    Damit hat man dann x*TerminalBreite und y*TerminalHöhe. Damit kann man dann die nötigen Zeichne darstellen,
    die dann in das Termianl passen und die für den spieler sichtbar sind.
    Wenn der Spieler an einen der vier Ränder kommt und in einen neuen Bereich kommt,
    dann musser der Offset erneut berechnet werden und dann wird ein neuer Bereich gezeichnet.

     */

    // field verwaltet das gesamte feld

    private boolean update = true;  // gameloop für ständige abfrage nach bewegung und steuerung der gegner
    private FileManager fp;         // verantwwortlich für laden und speichern der dateien
    private SwingTerminal terminal; // terminal...
    private ScheduledExecutorService executor;  // klasse für geplante ausführung von anweisungen für den dynamischen gegner
    private int spawnX, spawnY;
    private Obstacle[][] obstacles; // alle objekte des spiels
    private Player p;   // spieler
    private boolean didFinish = false;  // boolean zum feststellen, ob der spieler gewonnen hat
    private int width;
    private int height;
    private boolean dead = false;   // spieler hat verloren, wenn true
    private boolean pause = false;  // pause true, wenn im menü, hält gameloop an
    private ArrayList<DynObstacle> dynEnem = new ArrayList<>(); // dynamischen objekte in arraylist
    int terminalWidth;   // breite des terminals
    int terminalHeight;  // höhe des terminals
    private int xOffset;    // xOffset
    private int yOffset;    // yOffset  --> erklärung in readme
    private int absoluteX, absoluteY;   // absolutex und y für angabe, wie sie in der properties stehen


    public Field()  {

        TerminalAppearance ta = TerminalAppearance.DEFAULT_APPEARANCE.withFont(TerminalAppearance.DEFAULT_NORMAL_FONT.deriveFont(20f)); // vergrößert die schrift
        terminal = TerminalFacade.createSwingTerminal(ta);  // erstellt das terminal und übernimmt die appearance aus ta objekt
        terminal.getTerminalSize().setColumns(80);  // setzt die maximale breite des terminals
        terminal.getTerminalSize().setRows(30);     // setzt die maximale höhe des terminals
        terminal.enterPrivateMode();                // zum zeichnen notwendig
        terminal.setCursorVisible(false);           // cursor nicht sichtbar
        terminal.getJFrame().setResizable(false);   // kein resizen erlaubt, da scrollen implementiert werden soll
        terminal.getJFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    // schließt das programm, wenn auf X gedrückt wird

        terminalWidth = terminal.getTerminalSize().getColumns()-16;  // das eigentliche spielfeld
                                                                     // -16 ist ein rand für die spielinformation
        terminalHeight = terminal.getTerminalSize().getRows();       // höhe

        fp = new FileManager();

        //handleMenuOption(true);

        fp.loadStartLevel(false, "");   // lade level
        width = fp.getWidth();  // hol die breite und höhe
        height = fp.getHeight();
        obstacles = fp.getObstacles();  // holt die ganzen objekte
        if(obstacles == null) System.out.println("an error occured while displaying the previous error");

        executor = Executors.newScheduledThreadPool(1);

        setAbsoluteValues();    // setz die absolute werte
        p = new Player(absoluteX, absoluteY);   // spieler wird gesetzt
        p.setAbsX(absoluteX);
        p.setAbsY(absoluteY);
        spawnObstacles();   // zeichne die objekte
        addDynEnems();
        p.setX(absoluteX - xOffset);
        p.setY(absoluteY - yOffset);
        terminal.applyForegroundColor(p.getColor());
        terminal.moveCursor(p.getX(), p.getY());
        printStatus();
        executor.scheduleAtFixedRate(runEnemy, 0, 1, TimeUnit.SECONDS); // führe jede sekunge runEnemy aus-> bewegt dynmischen gegner

        while (update) {
            Key key = terminal.readInput(); // lese die spieler eingabe

            try {
                if (dead) { // wenn spieler stirbt, dann zeige ende
                    terminal.clearScreen();
                    MessageManager.printText(terminal, terminalWidth/2, terminalHeight/2, GameMessages.LOST.toString(), Terminal.Color.RED);
                    // maybe print game status before death or so
                    break;
                }

                if (key.getKind() != null) {    // wenn die eingabe nicht null war, dann bewege den spieler abhängig von der  eingebenen taste
                    moveHandler(key);   // movehandler regelt die tasteneingabe
                    printStatus();  // gib status aus

                    if (key.getKind() == Key.Kind.Escape) {
                        handleMenuOption(false);    // ruft menü auf
                    }

                }
                if (didFinish) {    // wenn der spieler gewinnt, dann zeige gewinner ende
                    terminal.clearScreen();
                    MessageManager.printText(terminal, terminalWidth/2, terminalHeight/2, GameMessages.WON.toString(), Terminal.Color.GREEN);
                    executor.shutdown();
                    break;
                }
                printStatus();
            } catch (Exception e) {//e.printStackTrace();
            }
        }
    }

    Runnable runEnemy = new Runnable() {
        public void run() {
            controlEnem();
        }   // interface was die zu ausführende methode definiert
    };

    private void handleMenuOption(boolean newgame){     // regelt die menüeingabe

        GameMenu g = new GameMenu(terminal);
        g.createMenu(newgame);      // erstellt das menü
        pause = true;
        g.optionSelected();     // golt sich die menüeingabe
        switch (g.getOption()) {
            case 0:
                break;  // wenn 0, dann fortsetzen
            case 1:
                fp.save(obstacles, dynEnem, p); // 1 -> speichern
                break;
            case 2: // 2-> lade spielstand, ein spielstand lade ich hoch, dieser kann durch speichern überschrieben werden
                terminal.clearScreen();
                fp.load(p);
                dynEnem.clear();
                setAbsoluteValues();
                obstacles = fp.getObstacles();
                addDynEnems();
                break;
            case 3: // 3-> zeigt die legende
                MenuKeys mKeys = new MenuKeys(terminal);
                mKeys.createKeys();
                break;
            case 4: System.exit(0);break;   // beendet das spiel
        }
        if(!newgame) {
            terminal.clearScreen();
            spawnObstacles();
            spawnPlayer();
            //paintDyn();
            pause = false;
        }
    }


    private void controlEnem() {    // gegner steuerung. liegt in dieser klasse, da spieler objekt dafür benötigt wird

        if (!dead && !pause) {
            for (DynObstacle enem : dynEnem) {

                System.out.println(dynEnem.size());
                int r = enem.getRandom();   // direction is set, cooridinates aren't
                int x = enem.getX() - xOffset;  // hole die x und y koords, und ziehe den offset ab
                int y = enem.getY() - yOffset;  // offset ist die anzahl der terminallängen und breiten die benötigt werden, damit
                                                // die objekte auf die terminalgröße passen

                boolean isFree = false;
                while (!isFree) {               // gegnerbewegung ist random, erreichen den spieler aber
                    if (r == 0) {   // 0 -> nach oben
                        if (isCollidingDyn(x, y - 1)) { // random bewegung durch generierung von random zahlen
                            r = enem.getRandom();
                        } else {
                            isFree = true;

                        }
                    } else if (r == 1) {    // 1-> nach rechts
                        if (isCollidingDyn(x+1, y)) {
                            r = enem.getRandom();
                        } else {
                            isFree = true;

                        }

                    } else if (r == 2) {    // 2-> nach unten
                        if (isCollidingDyn(x, y+1)) {
                            r = enem.getRandom();
                        }else {
                            isFree = true;
                        }

                    } else if (r == 3) {    // 3-> nach links
                        if (isCollidingDyn(x-1, y)) {
                            r = enem.getRandom();
                        }else {
                            isFree = true;
                        }
                    }
                }

                obstacles[enem.getY()][enem.getX()] = null; // überschreibe die alte position mit null

                if (!dead) {
                    //obstacles[enem.getX()][enem.getY()] = null;

                    if(isDynObstVisible(enem)){
                        enem.removeOldPos(terminal, x, y);
                    }
                    enem.set();
                    if(isDynObstVisible(enem)) {
                        //System.out.println("dyn is visible");
                        enem.removeOldPos(terminal, x,y);   // wenn der spieler sichtbar ist, soll er gezeichnet werden
                        //enem.set();
                        enem.draw(terminal,xOffset,yOffset);
                        obstacles[enem.getY()][enem.getX()] = enem; // speicherung der koordinaten [y-werte][x-werte]
                    }
                    // wenn der gegner nicht in das terminal "passt", sollen die koordianten trotzdem gesetzt werden
                    // da der spieler erreicht werden muss
                    // es wird nicht gezeichnet, da die gegner sonst an den jeweiligen rändern des terminal gezeichnet werden

                    printEnemy();
                }
            }
        }
    }

    private void printStatus() {
        String status = "Inventar: ";
        if (p.getHasKey()) {
            status+=FKey.getCharcode(); // zeigt schlüssel im inventar wenn eingesammelt wird
        }
        MessageManager.printText(terminal, terminalWidth + 1, 0, status, Terminal.Color.WHITE);
        status = "Leben: " + p.getLives();  // anzahl der leben
        MessageManager.printText(terminal, terminalWidth + 1 ,1, status, Terminal.Color.WHITE);

    }

    private void printEnemy(){


        //zeigt die position der dynamischen gegner an

        for(int row = 0; row < dynEnem.size(); row++){
            for(int col = 0; col < 17; col++){
                MessageManager.printText(terminal, terminalWidth + col ,2+row, " ", Terminal.Color.WHITE);
            }
        }
        for(int i = 0; i < dynEnem.size(); i++){
            String enemyPos = dynEnem.get(i).getX() + "|" + dynEnem.get(i).getY();
            MessageManager.printText(terminal, terminalWidth + 1 ,2+i, enemyPos, Terminal.Color.WHITE);
        }
    }

    private void spawnPlayer() {
        p.movePlayer(terminal, p.getX(),p .getY());
    }   // setzt den spieler

    private void moveHandler(Key key) { // regelt die bewegung des spielers
        if (key.getKind() == Key.Kind.ArrowDown) {  // wenn nach die untere pfeiltaste gedrückt wird, bewege den spieler um 1 nach unten (duh!)
            if (!isColliding(p.getX(), p.getY() + 1)) { // prüft, ob eine kollision dadurch entsteht
                if (onKey(p.getX(), p.getY() + 1)) {    // prüft ob der spieler auf einem schlüssel steht
                    p.setHasKey(true);  // wenn ja setze hasKey auf true
                }

                p.setAbsY(p.getAbsY()+1);   // setze die absoluten koordinaten

                p.movePlayer(terminal, p.getX(), p.getY() + 1); // bewege den spieler

            }else System.out.println("wall");

        } else if (key.getKind() == Key.Kind.ArrowUp) { // analog für andere richtungen, hier nach oben

            if (!isColliding(p.getX(), p.getY() - 1)) {
                if (onKey(p.getX(), p.getY() + 1)) {
                    p.setHasKey(true);
                }

                p.setAbsY(p.getAbsY()-1);
                p.movePlayer(terminal, p.getX(), p.getY() - 1);

            }else System.out.println("wall");


        } else if (key.getKind() == Key.Kind.ArrowLeft) {   // bewegung nach links

            if (!isColliding(p.getX() - 1, p.getY())) {
                if (onKey(p.getX() - 1, p.getY())) {
                    p.setHasKey(true);
                }

                p.setAbsX(p.getAbsX()-1);
                p.movePlayer(terminal, p.getX() - 1, p.getY());
            }else System.out.println("wall");


        } else if (key.getKind() == Key.Kind.ArrowRight) {  // bewegung nach rechts

            if (!isColliding(p.getX() + 1, p.getY())) {
                if (onKey(p.getX() + 1, p.getY())) {
                    p.setHasKey(true);
                }
                p.setAbsX(p.getAbsX()+1);
                p.movePlayer(terminal, p.getX() + 1, p.getY());
            }else System.out.println("wall");

        }

        /*System.out.println("p.getX()" + p.getX());
        System.out.println("p.getY()" + p.getY());*/

        if(!isVisible(p.getX(), p.getY())){ // wenn der spieler nicht mehr sichtbar ist render den neuen teil der map
            //render new map

            int x = p.getX();   //  koordinaten im terminal
            int y = p.getY();
            spawnObstacles();   // berechne neuen offset und zeichne die objekte neu, da ein neuer bereich geladen wird

            //absolute position aus der property

            if(x > terminalWidth-1){    // prüft an welchem rand sich der spieler befindet
                                        // und positioniert den spieler an den teil des gerenderten bereichs
                System.out.println("test");
                p.setX(0);
            }
            else if(x < 0){     // wenn der spieler nach links läuft, kommt er ganz rechts raus
                p.setX(terminalWidth-1);
            }
            if(y > terminalHeight-1){   // analog für oben
                p.setY(0);
            }
            else if(y < 0){ // analog für unten
                p.setY(terminalHeight-1);
            }

            spawnPlayer();

        }
    }

    private boolean onKey(int x, int y) {   // prüft ob der spieler auf einem schlüssel steht

        x = x+xOffset;
        y = y+yOffset;

        if (obstacles[y][x] != null) {
            if (obstacles[y][x] instanceof FKey) {
                obstacles[y][x]=null;
                p.setHasKey(true);  // prüft abhängig vom offset, ob der spieler auf der position des schlüssels steht
                return true;
            }
        }
        return false;
    }

    private boolean isColliding(int x, int y) { // kollisionsabfrage

        x = x+xOffset;
        y = y+yOffset;

        if (obstacles[y][x] != null && !(obstacles[y][x] instanceof FKey)) {

            Obstacle obstacle = obstacles[y][x];

            if (obstacle instanceof Exit && p.getHasKey()) {    // wenn der spieler den schüssel hat und auf den ausgang läuft, dann beende das spiel
                System.out.println("key found");
                didFinish = true;
                return false;
            } else if (obstacle instanceof StatObstacle || obstacle instanceof DynObstacle) {   // wenn das hindernis ein gegner ist, dann ziehe leben ab
                System.out.println("ran into enemy");
                p.setLives(p.getLives() - 1);
                if (p.getLives() == 0) dead = true;
                /*pause = true;
                p.setNotHitable(terminal);
                pause = false;*/
                printStatus();
                return true;
            }
            return true;    // alles andere ist entweder eine wand oder eingang, durch die man nicht durchlaufen kann
        }
        return false;
    }

    private boolean isCollidingDyn(int x, int y) {   // kollisionsbehandlung für dynmisches objekt


        if (p.getX() == x && p.getY() == y) {   // wenn der spieler keine leben mehr hat, beende spiel

            if (p.getLives() == 0) {
                dead = true;
                executor.shutdown();
            } else {    // ansonsten ziehe leben ab
                p.setLives(p.getLives() - 1);
                System.out.println("hit by dynamic");
                if (p.getLives() == 0) {
                    dead = true;
                    executor.shutdown();
                    terminal.clearScreen();
                } else {

                    printStatus();
                    /*pause = true;
                    try{p.setNotHitable(terminal);
                    pause = false;
                    }catch (Exception e){}*/
                }
            }
            return true;

        } else if (obstacles[y+yOffset][x+xOffset] != null) {
            return true;
        }
        return false;
    }


    private void spawnObstacles() {

        terminal.clearScreen();
        for (int yCoord = 0; yCoord < obstacles.length; yCoord++) {
            for (int xCoord = 0; xCoord < obstacles[yCoord].length; xCoord++) {

                if (obstacles[yCoord][xCoord] != null) {

                    Obstacle obstacle = obstacles[yCoord][xCoord];


                    int xSection = p.getAbsX()/terminalWidth;
                    int ySection = p.getAbsY()/terminalHeight;

                    xOffset = xSection * terminalWidth;
                    yOffset = ySection * terminalHeight;

                    int x = xCoord - xOffset;   // gehe durch jedes objekt im 2d array und berechne dessen position abhängig vom offset
                    int y = yCoord - yOffset;

                    if((x <terminalWidth && y < terminalHeight) && x >= 0 && y >= 0) {  // wenn die x und y positionen der objekte ohne offset im termina liegen, sollen diese gezeichnet werden

                        terminal.moveCursor(x, y);
                        terminal.applyForegroundColor(obstacle.getColorCode());
                        terminal.putCharacter(obstacle.getCharCode());

                    }
                    if (obstacle instanceof Entrance) {
                        spawnX = xCoord;
                        spawnY = yCoord;
                    }
                }
            }
        }
    }

    private void addDynEnems(){
        for (int yCoord = 0; yCoord < obstacles.length; yCoord++) {
            for (int xCoord = 0; xCoord < obstacles[yCoord].length; xCoord++) {

                if (obstacles[yCoord][xCoord] != null) {

                    Obstacle obstacle = obstacles[yCoord][xCoord];
                    if (obstacle instanceof DynObstacle) {
                        dynEnem.add((DynObstacle) obstacle);
                    }
                }
            }
        }
    }


    private void setAbsoluteValues(){   // setze start werte des spielers

        for (int yCoord = 0; yCoord < obstacles.length; yCoord++) {
            for (int xCoord = 0; xCoord < obstacles[yCoord].length; xCoord++) {

                if (obstacles[yCoord][xCoord] != null) {

                    Obstacle obstacle = obstacles[yCoord][xCoord];

                    if (obstacle instanceof Entrance) {

                        absoluteX = xCoord;
                        absoluteY = yCoord;

                    }
                }
            }
        }
    }

    private boolean isVisible(int x, int y){    // sichtbar wenn innerhalb des terminals

        if(x <= terminalWidth-1 && y <= terminalHeight-1 && x >= 0 && y >= 0){
            return true;
        }
        return false;
    }

    private boolean isDynObstVisible(DynObstacle dyn){  // sichtbar wenn innerhalb des terminals


        if(dyn.getX()-xOffset < terminalWidth && dyn.getX()-xOffset >= 0 && dyn.getY()-yOffset < terminalHeight && dyn.getY()-yOffset >= 0){
            return true;
        }

        return false;
    }

}
