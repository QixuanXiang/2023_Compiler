public class SymbolManager_G {
    private static SymbolManager_G instance;
    private SymbolTable_G curSymbolTable;
    private int depth;

    public static SymbolManager_G getInstance() {
        if (instance == null) instance = new SymbolManager_G();
        return instance;
    }

    public SymbolManager_G() {
        this.depth = 0;
        this.curSymbolTable = new SymbolTable_G(0, 0);
    }

    public void createSymbolTable(int type) {
        this.depth++;
        SymbolTable_G symbolTable = new SymbolTable_G(depth, type);
        curSymbolTable.addNext(symbolTable);
        symbolTable.setPre(curSymbolTable);
        curSymbolTable = symbolTable;
    }

    public void traceBackSymbolTable() {
        this.depth--;
        curSymbolTable = curSymbolTable.getPre();
    }

    public void addVarSymbol(VarSymbol_G symbol) {
        if (curSymbolTable.getVarSymbol(symbol.getName()) != null ||
                curSymbolTable.getFuncSymbol(symbol.getName()) != null) {
            return;
        }
        curSymbolTable.addVarSymbol(symbol);
    }

    public void addFuncSymbol(FuncSymbol_G symbol) {
        if (curSymbolTable.getVarSymbol(symbol.getName()) != null ||
                curSymbolTable.getFuncSymbol(symbol.getName()) != null) {
            return;
        }
        curSymbolTable.addFuncSymbol(symbol);
    }

    public VarSymbol_G findVarSymbol(String name) {
        SymbolTable_G symbolTable = new SymbolTable_G(depth, curSymbolTable.getType());
        symbolTable.setPre(curSymbolTable.getPre());
        VarSymbol_G symbol = curSymbolTable.getVarSymbol(name);
        while (symbol == null) {
            symbolTable = symbolTable.getPre();
            if (symbolTable == null) {
                return null;
            }
            symbol = symbolTable.getVarSymbol(name);
        }
        return symbol;
    }

    public FuncSymbol_G findFuncSymbol(String name) {
        SymbolTable_G symbolTable = new SymbolTable_G(depth, curSymbolTable.getType());
        symbolTable.setPre(curSymbolTable.getPre());
        FuncSymbol_G symbol = curSymbolTable.getFuncSymbol(name);
        while (symbol == null) {
            symbolTable = symbolTable.getPre();
            if (symbolTable == null) {
                return null;
            }
            symbol = symbolTable.getFuncSymbol(name);
        }
        return symbol;
    }

    public int getDepth() {
        return depth;
    }
}
