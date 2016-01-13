package level;

import entities.DynObstacle;
import entities.FKey;
import entities.Player;
import obstacles.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;


public class ReadProp {

    FileInputStream inputStream;
    private Obstacle[][] obstacles;
    public static int width, height;
    private String level = "";
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ReadProp(){

    }

    public void loadStartLevel(boolean loadFromSaveGame, String levelName){
        try{

            System.out.println(levelName);
            Properties p = new Properties();
            level = "level_big_dense.properties";
            //File file = new File(level);
            File file = new File(level);    // LEVEL BIG SPARSE 1 IST BEARBEITET
            if(loadFromSaveGame) {
                file = new File(levelName);
            }
            //File file = new File("level.properties");

            inputStream = new FileInputStream(file);

            if(inputStream != null){
                p.load(inputStream);
            }
            else{
                throw new FileNotFoundException("property file " + file + " not found");
            }

            Enumeration<?> keysFromProp = p.propertyNames();

            height = Integer.parseInt(p.getProperty("Height"));
            width = Integer.parseInt(p.getProperty("Width"));

            obstacles = new Obstacle[height][width];

            while(keysFromProp.hasMoreElements()){

                String key = (String) keysFromProp.nextElement();
                String value = p.getProperty(key);

                if(!key.equalsIgnoreCase("Height") && !key.equalsIgnoreCase("Width")) {
                    int x = Integer.parseInt(key.split(",")[0]);
                    int y = Integer.parseInt(key.split(",")[1]);

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

            inputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public void save(Obstacle[][] obstacles, ArrayList<DynObstacle> dynPositions, Player player){
        ArrayList<FKey> savedKeys = new ArrayList<>();
        for(int yPos = 0; yPos < obstacles.length; yPos++){
            for(int xPos = 0; xPos < obstacles[yPos].length;xPos++){
                if(obstacles[yPos][xPos] instanceof FKey){
                    savedKeys.add((FKey)obstacles[yPos][xPos]);
                }
            }
        }

        try {
            Properties savedData = new Properties();
            for(FKey keys: savedKeys) {
                savedData.setProperty(keys.getX()+","+keys.getY(), "5");
            }
            for(DynObstacle dyn: dynPositions){
                savedData.setProperty(dyn.getX()+","+dyn.getY(), "4");
            }
            savedData.setProperty("hasKey", String.valueOf(player.getHasKey()));
            savedData.setProperty("lives", String.valueOf(player.getLives()));
            savedData.setProperty("level", level);
            savedData.setProperty(player.getX()+","+player.getY(), "6");
            savedData.setProperty("absX", String.valueOf(player.getAbsX()));
            savedData.setProperty("absY", String.valueOf(player.getAbsY()));
            File file = new File("savegame.properties");
            FileOutputStream fileOut = new FileOutputStream(file);
            savedData.store(fileOut, "savegame");
            System.out.println("saved game");
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void load(Player player){
        try{


            Properties p = new Properties();
            File file = new File("savegame.properties");

            //File file = new File("level_big_sparse.properties");
            //File file = new File("level_big_dense.properties");
            //File file = new File("level.properties");

            inputStream = new FileInputStream(file);

            if(inputStream != null){
                p.load(inputStream);
            }
            else{
                throw new FileNotFoundException("property file " + file + " not found");
            }

            String levelName = p.getProperty("level");
            loadStartLevel(true, levelName);
            Enumeration<?> keysFromProp = p.propertyNames();

            player.setHasKey(Boolean.valueOf(p.getProperty("hasKey")));
            player.setLives(Integer.parseInt(p.getProperty("lives")));
            player.setAbsX(Integer.parseInt(p.getProperty("absX")));
            player.setAbsY(Integer.parseInt(p.getProperty("absY")));


            while(keysFromProp.hasMoreElements()){

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

    public Obstacle[][] getObstacles() {
        return obstacles;
    }

}
