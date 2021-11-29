package Model;

public final class Data {
    
    public static final int COL = 24;
    public static final int ROW = 24;

    public static final String DRAW = "[DRAW]";
    public static final String WIN = "[WIN]";
    public static final String NORMAL = "[NORMAL]";

    public final static String X_VALUE = "[X]";
    public final static String O_VALUE = "[O]";
    public final static String EMPTY_VALUE = "";

    public final static String SMS = "[SMS]";
    public final static String CARO = "[CARO]";

    /**
     * 
     */
    public static boolean round = true; 
    public static Cell matrix[][] = new Cell[Data.COL][Data.ROW];
    
}
