package level;


public enum GameMessages {  // enum für menü-optionen

    LOST("you lost :( RIP"),
    WON("you won :)"),
    RESUME("Resume"),
    SAVE("Save"),
    LOAD("Load"),
    KEYS("Keys"),
    NEWGAME("New Game"),
    QUIT("Quit");

    private String desc;

    private GameMessages(String s) {
        this.desc = s;
    }

    public String toString(){
        return desc;
    }

}
