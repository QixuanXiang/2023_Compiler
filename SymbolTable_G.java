import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable_G {
    private int type;  //0 void, 1 int, 2 for
    private boolean isReturn = false;
    private HashMap<String, VarSymbol_G> varSymbols;
    private HashMap<String, FuncSymbol_G> funcSymbols;
    private SymbolTable_G pre;
    private ArrayList<SymbolTable_G> nexts;
    private int depth;
    public SymbolTable_G(int depth, int type) {
        this.varSymbols = new HashMap<>();
        this.funcSymbols = new HashMap<>();
        this.nexts = new ArrayList<>();
        this.depth = depth;
        this.type = type;
    }

    public void addVarSymbol(VarSymbol_G varSymbol) {
        this.varSymbols.put(varSymbol.getName(), varSymbol);
    }

    public void addFuncSymbol(FuncSymbol_G funcSymbol) {
        this.funcSymbols.put(funcSymbol.getName(), funcSymbol);
    }

    public void setPre(SymbolTable_G pre) {
        this.pre = pre;
    }

    public SymbolTable_G getPre() {
        return pre;
    }

    public void addNext(SymbolTable_G next) {
        this.nexts.add(next);
    }


    public VarSymbol_G getVarSymbol(String name) {
        if (this.varSymbols.containsKey(name)) {
            return varSymbols.get(name);
        }
        return null;
    }

    public FuncSymbol_G getFuncSymbol(String name) {
        if (this.funcSymbols.containsKey(name)) {
            return funcSymbols.get(name);
        }
        return null;
    }


    public int getType() {
        return type;
    }

    public int getDepth() {
        return depth;
    }
}
