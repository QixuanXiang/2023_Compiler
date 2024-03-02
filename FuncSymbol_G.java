import java.util.ArrayList;

public class FuncSymbol_G {
    private final String name;    // 当前单词所对应的字符串
    private final int symbolType; // 0 -> var, 1 -> func
    private int funcType; // 0 -> void, 1 -> int
    private int depth;
    private int paraNum; // 参数数量
    private ArrayList<Integer> paraDimList; // 参数维数

    public FuncSymbol_G(String name, int funcType, int depth) {
        this.name = name;
        this.symbolType = 1;
        this.funcType = funcType;
        this.depth = depth;
        this.paraNum = 0;
        this.paraDimList = new ArrayList<>();
    }

    public void addPara(int dim) {
        paraNum++;
        paraDimList.add(dim);
    }

    public String getName() {
        return name;
    }

    public int getSymbolType() {
        return symbolType;
    }

    public int getFuncType() {
        return funcType;
    }

    public int getDepth() {
        return depth;
    }

    public int getParaNum() {
        return paraNum;
    }

    public ArrayList<Integer> getParaDimList() {
        return paraDimList;
    }
}
