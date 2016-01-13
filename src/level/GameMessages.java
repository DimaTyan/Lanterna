package level;


public enum GameMessages {

    LOST("you lost :( RIP"),
    WON("you won :)"),
    RESUME("Resume"),
    SAVE("Save"),
    LOAD("Load"),
    KEYS("Keys"),
    NEWGAME("New Game");

    private String desc;

    private GameMessages(String s) {
        this.desc = s;
    }

    public String toString(){
        return desc;
    }

}
