package level;

import entities.DynObstacle;
import entities.FKey;
import entities.Player;
import obstacles.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;


public class FileManager {

    // klasse stellt methoden zum lesen und speichern von dateien bereit

    private FileInputStream inputStream;    // einlesedaten in dem objekt gespeichert
    private Obstacle[][] obstacles;     // 2D array was die eingelesenen koordinaten und codierungen aus den properties enthält
    private int width, height;    // höhe und breite des spielfeldes
    private String level = "";  // levelname


    // mehr getter!

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Obstacle[][] getObstacles() {
        return obstacles;
    }

    public void loadStartLevel(boolean loadFromSaveGame, String levelName){
        try{

            System.out.println(levelName);
            Properties p = new Properties();
            level = "level_small.properties";   //lädt das hier angegebene level
            //File file = new File(level);
            File file = new File(level);    // datei wird in file gespeichert
            if(loadFromSaveGame) {
                file = new File(levelName);
            }
            //File file = new File("level.properties");

            inputStream = new FileInputStream(file);

            if(inputStream != null){    // wenn die datei existiert, dann lade diese
                p.load(inputStream);
            }
            else{
                throw new FileNotFoundException("property file " + file + " not found");
            }

            Enumeration<?> keysFromProp = p.propertyNames();    // enum hat alle keys aus der properties

            height = Integer.parseInt(p.getProperty("Height")); // lade höhe und breite aus datei mit keys Height und Width
            width = Integer.parseInt(p.getProperty("Width"));

            obstacles = new Obstacle[height][width];    // erzeugt damit dann 2D array

            while(keysFromProp.hasMoreElements()){  // solange es noch ungelese keys gibt wird die datei gelesen

                String key = (String) keysFromProp.nextElement();   // key als String
                String value = p.getProperty(key);                 // value als String

                if(!key.equalsIgnoreCase("Height") && !key.equalsIgnoreCase("Width")) { // height und width sollen ignoriert werden, da schon eingelesen
                    int x = Integer.parseInt(key.split(",")[0]);
                    int y = Integer.parseInt(key.split(",")[1]);

                    // speichert die jeweiligen objekte an die stelle wie sie in der properties codiert ist

                    if (value.equals("0"))      obstacles[y][x] = new Wall(x, y);    // wand
                    else if (value.equals("1")) obstacles[y][x] = new Entrance(x, y);   // eingang
                    else if (value.equals("2")) obstacles[y][x] = new Exit(x, y);   // ausgang
                    else if (value.equals("3")) obstacles[y][x] = new StatObstacle(x, y);    //statische hindernis
                    if(!loadFromSaveGame) {
                        if (value.equals("4")) obstacles[y][x] = new DynObstacle(x, y);
                        else if (value.equals("5")) obstacles[y][x] = new FKey(x, y);   // schlüssel
                    }
                }
            }

            inputStream.close();    // schließt den lesestrom
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    // save speichert die position und leben des spielers und ob dieser einen schlüssel eingesammelt hat
    // die position der dynamischen objekte werden ebenfalls gespeichert
    // speichert die position der schlüssel

    public void save(Obstacle[][] obstacles, ArrayList<DynObstacle> dynPositions, Player player){

        ArrayList<FKey> savedKeys = new ArrayList<>();

        for(int yPos = 0; yPos < obstacles.length; yPos++){
            for(int xPos = 0; xPos < obstacles[yPos].length;xPos++){
                if(obstacles[yPos][xPos] instanceof FKey){
                    savedKeys.add((FKey)obstacles[yPos][xPos]); // holt die schlüssel objekte aus dem array und speichert deren koordinaten
                }
            }
        }

        try {
            Properties savedData = new Properties();
            for(FKey keys: savedKeys) {
                savedData.setProperty(keys.getX()+","+keys.getY(), "5");    // gehe durch schlüssel-liste und speichere diese in der gleichen codierung
            }
            for(DynObstacle dyn: dynPositions){
                savedData.setProperty(dyn.getX()+","+dyn.getY(), "4");  // das gleiche für die dynamischen objekte
            }
            savedData.setProperty("hasKey", String.valueOf(player.getHasKey()));    // speichert ob der spieler den schlüssel hat
            savedData.setProperty("lives", String.valueOf(player.getLives()));  // leben des spielers
            savedData.setProperty("level", level);  // das gespielte level
            savedData.setProperty(player.getX()+","+player.getY(), "6");    // position des spielers
            savedData.setProperty("absX", String.valueOf(player.getAbsX()));    // absolute position des spielers,
            savedData.setProperty("absY", String.valueOf(player.getAbsY()));    // um den offset richtig
            File file = new File("savegame.properties");    // savegame heißt savegame.properties
            FileOutputStream fileOut = new FileOutputStream(file);  // datenausgabe-strom
            savedData.store(fileOut, "savegame");   // speichere die datei mit beschreibung "savegame"
            System.out.println("saved game");
            fileOut.close();    // schließe ausgabestrom
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // lädt das savegame

    public void load(Player player){
        try{


            Properties p = new Properties();
            File file = new File("savegame.properties");    // lade die datei savegame

            //File file = new File("level_big_sparse.properties");
            //File file = new File("level_big_dense.properties");
            //File file = new File("level.properties");

            inputStream = new FileInputStream(file);    // lese die datei

            if(inputStream != null){
                p.load(inputStream);
            }
            else{
                throw new FileNotFoundException("property file " + file + " not found");
            }

            String loadedLevel = p.getProperty("level");
            this.level = loadedLevel;
            loadStartLevel(true, loadedLevel);    // lade das ursprüngliche level ohne dynamishce gegner und schlüssel,
                                                // da diese in savegame gespeichert sind
            Enumeration<?> keysFromProp = p.propertyNames();

            player.setHasKey(Boolean.valueOf(p.getProperty("hasKey"))); // setze die entsprechenden attribute aus savegame
            player.setLives(Integer.parseInt(p.getProperty("lives")));
            player.setAbsX(Integer.parseInt(p.getProperty("absX")));
            player.setAbsY(Integer.parseInt(p.getProperty("absY")));


            while(keysFromProp.hasMoreElements()){  // äquivalent zu methode loadLevel();

                String key = (String) keysFromProp.nextElement();
                String value = p.getProperty(key);

                if(!key.equalsIgnoreCase("hasKey") && !key.equalsIgnoreCase("lives") && !key.equalsIgnoreCase("level")&&
                        !key.equalsIgnoreCase("absX") && !key.equalsIgnoreCase("absY")) {


                    int x = Integer.parseInt(key.split(",")[0]);
                    int y = Integer.parseInt(key.split(",")[1]);

                    if (value.equals("4")) obstacles[y][x] = new DynObstacle(x, y);
                    else if (value.equals("5")) obstacles[y][x] = new FKey(x, y);
                    else if (value.equals("6")) {
                        System.out.println("---------------loaded player pos");
                        player.setX(x);
                        player.setY(y);
                    }
                }
            }
            inputStream.close();

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



}
