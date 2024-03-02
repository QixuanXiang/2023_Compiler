import java.util.HashMap;

public class SymbolTable {
    private int id;
    private int fatherId;
    private int type;  //0 int, 1 void, 2 for
    private boolean isReturn = false;
    private HashMap<String, Symbol> directory = new HashMap<>();

    public SymbolTable(int id, int fatherId, int type) {
        this.id = id;
        this.fatherId = fatherId;
        this.type = type;
    }

    public void addSymbol(String name, Symbol symbol) {  //name = 0/1 + symbol_token, 1->func
        directory.put(name, symbol);
    }

    public void setReturn(boolean aReturn) {
        isReturn = aReturn;
    }

    public Symbol getSymbol(String name) {
        return directory.get(name);
    }

    public int getId() {
        return id;
    }

    public int getFatherId() {
        return fatherId;
    }

    public int getType() {
        return type;
    }

    public boolean isReturn() {
        return isReturn;
    }

    public HashMap<String, Symbol> getDirectory() {
        return directory;
    }
}
