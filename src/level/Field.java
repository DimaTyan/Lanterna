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
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Field {


    private boolean update = true;
    private ReadProp rp;
    private SwingTerminal terminal;
    private ScheduledExecutorService executor;
    private int spawnX, spawnY;
    private Obstacle[][] obstacles;
    private Player p;
    private boolean didFinish = false;
    private int width;
    private int height;
    private boolean dead = false;
    private boolean pause = false;
    private ArrayList<DynObstacle> dynEnem = new ArrayList<>();
    int terminalWidth;   // da linke obere ecke (0,0)
    int terminalHeight;
    private int xOffset;
    private int yOffset;
    private int absoluteX, absoluteY;
    private boolean moveDynNotVisible = false;


    public Field()  {

        TerminalAppearance ta = TerminalAppearance.DEFAULT_APPEARANCE.withFont(TerminalAppearance.DEFAULT_NORMAL_FONT.deriveFont(20f));
        terminal = TerminalFacade.createSwingTerminal(ta);  // TerminalFacade.createTerminal(); ???????
        terminal.getTerminalSize().setColumns(80);
        terminal.getTerminalSize().setRows(30);
        terminal.enterPrivateMode();
        terminal.setCursorVisible(false);
        terminal.getJFrame().setResizable(false);
        terminal.getJFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        terminalWidth = terminal.getTerminalSize().getColumns()-16;
        terminalHeight = terminal.getTerminalSize().getRows();

        rp = new ReadProp();

        //handleMenuOption(true);

        rp.loadStartLevel(false, "");
        width = rp.getWidth();
        height = rp.getHeight();
        obstacles = rp.getObstacles();
        if(obstacles == null) System.out.println("an error occured while displaying the opre");

        executor = Executors.newScheduledThreadPool(1);

        setAbsoluteValues();
        p = new Player(absoluteX, absoluteY);
        p.setAbsX(absoluteX);
        p.setAbsY(absoluteY);
        spawnObstacles();
        p.setX(absoluteX - xOffset);
        p.setY(absoluteY - yOffset);
        terminal.applyForegroundColor(p.getColor());
        terminal.moveCursor(p.getX(), p.getY());
        printPlayerStatus();
        executor.scheduleAtFixedRate(helloRunnable, 0, 1, TimeUnit.SECONDS);

        while (update) {
            Key key = terminal.readInput();

            try {
                if (dead) {
                    terminal.clearScreen();
                    MessageManager.printText(terminal, terminalWidth/2, terminalHeight/2, GameMessages.LOST.toString(), Terminal.Color.RED);
                    // maybe print game status before death or so
                    break;
                }
                if (key.getKind() != null) {
                    moveHandler(key);
                    printPlayerStatus();

                    if (key.getKind() == Key.Kind.Escape) {
                        handleMenuOption(false);
                    }

                }
                if (didFinish) {
                    terminal.clearScreen();
                    MessageManager.printText(terminal, terminalWidth/2, terminalHeight/2, GameMessages.WON.toString(), Terminal.Color.GREEN);
                    executor.shutdown();
                    break;
                }
                printPlayerStatus();
            } catch (Exception e) {//e.printStackTrace();
            }
        }
    }

    Runnable helloRunnable = new Runnable() {
        public void run() {
            controlEnem();
        }
    };

    private void handleMenuOption(boolean newgame){

        GameMenu g = new GameMenu(terminal);
        g.createMenu(newgame);
        pause = true;
        g.optionSelected();
        switch (g.getOption()) {
            case 0:
                break;
            case 1:
                rp.save(obstacles, dynEnem, p);
                break;
            case 2:
                rp.load(p);
                dynEnem.clear();
                setAbsoluteValues();
                obstacles = rp.getObstacles();
                break;
            case 3:
                MenuKeys mKeys = new MenuKeys(terminal);
                mKeys.createMenu();
                break;
            case 4: System.exit(0);break;
        }
        if(!newgame) {
            terminal.clearScreen();
            spawnObstacles();
            spawnPlayer();
            //paintDyn();
            pause = false;
        }
    }


    private void controlEnem() {

        if (!dead && !pause) {
            for (DynObstacle enem : dynEnem) {

                int r = enem.getRandom();   // direction is set, cooridinates aren't

                int x = enem.getX() - xOffset;
                int y = enem.getY() - yOffset;

                boolean isFree = false;
                while (!isFree) {
                    if (r == 0) {
                        if (isCollidingDyn(x, y - 1)) {
                            r = enem.getRandom();
                        } else {
                            isFree = true;

                        }

                    } else if (r == 1) {
                        if (isCollidingDyn(x+1, y)) {
                            r = enem.getRandom();
                        } else {
                            isFree = true;

                        }

                    } else if (r == 2) {
                        if (isCollidingDyn(x, y+1)) {
                            r = enem.getRandom();
                        }else {
                            isFree = true;
                        }

                    } else if (r == 3) {
                        if (isCollidingDyn(x-1, y)) {
                            r = enem.getRandom();
                        }else {
                            isFree = true;
                        }
                    }
                }

                obstacles[enem.getY()][enem.getX()] = null;

                if (!dead) {
                    //obstacles[enem.getX()][enem.getY()] = null;

                    if(isDynObstVisible(enem)) {
                        //System.out.println("dyn is visible");
                        enem.removeOldPos(terminal,xOffset, yOffset);
                        enem.set();
                        enem.draw(terminal,xOffset,yOffset);
                        obstacles[enem.getY()][enem.getX()] = enem; // speicherung der koordinaten [y-werte][x-werte]
                    }
                    else{
                        //System.out.println("dyn is not visible");
                        enem.set();
                    }
                }
            }
        }
    }

    private void printPlayerStatus() {
        String status = "Inventar: ";
        if (p.getHasKey()) {
            status+=FKey.getCharcode();
        }
        MessageManager.printText(terminal, terminalWidth + 1, 0, status, Terminal.Color.WHITE);
        status = "Leben: " + p.getLives();
        MessageManager.printText(terminal, terminalWidth + 1 ,1, status, Terminal.Color.WHITE);
    }

    private void spawnPlayer() {
        p.movePlayer(terminal, p.getX(),p .getY());
    }

    private void moveHandler(Key key) {
        if (key.getKind() == Key.Kind.ArrowDown) {
            if (!isColliding(p.getX(), p.getY() + 1)) {
                if (onKey(p.getX(), p.getY() + 1)) {
                    p.setHasKey(true);
                }

                p.setAbsY(p.getAbsY()+1);
                absoluteY= absoluteY+1;
                p.movePlayer(terminal, p.getX(), p.getY() + 1);

            }else System.out.println("wall");

        } else if (key.getKind() == Key.Kind.ArrowUp) {

            if (!isColliding(p.getX(), p.getY() - 1)) {
                if (onKey(p.getX(), p.getY() + 1)) {
                    p.setHasKey(true);
                }
                absoluteY = absoluteY-1;
                p.setAbsY(p.getAbsY()-1);
                p.movePlayer(terminal, p.getX(), p.getY() - 1);

            }else System.out.println("wall");


        } else if (key.getKind() == Key.Kind.ArrowLeft) {

            if (!isColliding(p.getX() - 1, p.getY())) {
                if (onKey(p.getX() - 1, p.getY())) {
                    p.setHasKey(true);
                }
                absoluteX = absoluteX - 1;
                p.setAbsX(p.getAbsX()-1);
                p.movePlayer(terminal, p.getX() - 1, p.getY());
            }else System.out.println("wall");


        } else if (key.getKind() == Key.Kind.ArrowRight) {

            if (!isColliding(p.getX() + 1, p.getY())) {
                if (onKey(p.getX() + 1, p.getY())) {
                    p.setHasKey(true);
                }
                absoluteX = absoluteX + 1;
                p.setAbsX(p.getAbsX()+1);
                p.movePlayer(terminal, p.getX() + 1, p.getY());
            }else System.out.println("wall");

        }

        /*System.out.println("p.getX()" + p.getX());
        System.out.println("p.getY()" + p.getY());*/

        if(!isVisible(p.getX(), p.getY())){
            //render new map

            int x = p.getX();   //  koordinaten im terminal
            int y = p.getY();
            spawnObstacles();

            //absolute position aus der property

            //System.out.println("absolute y: " + absoluteY);
            //System.out.println("absolute x: " + absoluteX);

            if(x > terminalWidth-1){
                System.out.println("test");
                p.setX(0);
            }
            else if(x < 0){
                p.setX(terminalWidth-1);
            }
            if(y > terminalHeight-1){
                p.setY(0);
            }
            else if(y < 0){
                p.setY(terminalHeight-1);
            }

            spawnPlayer();

        }
    }

    private boolean onKey(int x, int y) {

        x = x+xOffset;

        y = y+yOffset;

        if (obstacles[y][x] != null) {
            if (obstacles[y][x] instanceof FKey) {
                obstacles[y][x]=null;
                p.setHasKey(true);
                return true;
            }
        }
        return false;
    }

    private boolean isColliding(int x, int y) {

        x = x+xOffset;
        y = y+yOffset;
        //System.out.println("x: " + x + "y:" +y);

        if (obstacles[y][x] != null && !(obstacles[y][x] instanceof FKey)) {

            Obstacle obstacle = obstacles[y][x];
            /*int obstX, obstY;
            obstX = obstacle.getX() - xOffset;
            obstY = obstacle.getY() - yOffset;*/

            if (obstacle instanceof Exit && p.getHasKey()) {
                System.out.println("key found");
                didFinish = true;
                return false;
            } else if (obstacle instanceof StatObstacle || obstacle instanceof DynObstacle) {
                System.out.println("ran into enemy");
                p.setLives(p.getLives() - 1);
                if (p.getLives() == 0) dead = true;
                /*pause = true;
                p.setNotHitable(terminal);
                pause = false;*/
                printPlayerStatus();
                return true;
            }
            return true;
        }
        return false;
    }

    private boolean isCollidingDyn(int x, int y) {

        //int x = x1 - xOffset;
        //int y = y1 - yOffset;

        if (p.getX() == x && p.getY() == y) {

            if (p.getLives() == 0) {
                dead = true;
                executor.shutdown();
            } else {
                p.setLives(p.getLives() - 1);
                System.out.println("hit by dynamic");
                if (p.getLives() == 0) {
                    dead = true;
                    executor.shutdown();
                    terminal.clearScreen();
                } else {

                    printPlayerStatus();
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

                    int x = xCoord - xOffset;
                    int y = yCoord - yOffset;

                    if((x <terminalWidth && y < terminalHeight) && x >= 0 && y >= 0) {

                        terminal.moveCursor(x, y);
                        terminal.applyForegroundColor(obstacle.getColorCode());
                        terminal.putCharacter(obstacle.getCharCode());


                    }
                    if (obstacle instanceof Entrance) {
                        spawnX = xCoord;
                        spawnY = yCoord;
                    } else if (obstacle instanceof DynObstacle) {
                        //dynEnem.clear();
                        dynEnem.add((DynObstacle) obstacle);
                    }
                }
            }
        }
    }


    private void setAbsoluteValues(){

        for (int yCoord = 0; yCoord < obstacles.length; yCoord++) {
            for (int xCoord = 0; xCoord < obstacles[yCoord].length; xCoord++) {

                //System.out.println("x:"+xCoord+"    y:"+yCoord);
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

    private boolean isVisible(int x, int y){

        if(x <= terminalWidth-1 && y <= terminalHeight-1 && x >= 0 && y >= 0){
            return true;
        }
        return false;
    }

    private boolean isDynObstVisible(DynObstacle dyn){


        if(dyn.getX()-xOffset < terminalWidth && dyn.getX()-xOffset >= 0 && dyn.getY()-yOffset < terminalHeight && dyn.getY()-yOffset >= 0){
            return true;
        }
        //System.out.println("not visible");
        return false;
    }

    private void paintDyn(){
        for(DynObstacle enem: dynEnem){

            //System.out.println("x:"+enem.getX() + "|" + "y:"+enem.getY());
            terminal.moveCursor(enem.getX()-xOffset,enem.getY()-yOffset);
            terminal.applyForegroundColor(enem.getColorCode());
            terminal.putCharacter(enem.getCharCode());
        }
    }


}
