import java.util.ArrayList;

public class VarSymbol_G {
    private final String name;    // 当前单词所对应的字符串。
    private int dim;        // 0 -> a, 1 -> a[], 2 -> a[][]
    private final int varType;        // 0 -> const, 1 -> var
    private int depth;
    private int size1;
    private int size2;
    private ArrayList<String> initValList;
    private boolean isUsed;



    public VarSymbol_G(String name, int type, int depth) {
        this.name = name;
        this.dim = 0;
        this.varType = type;
        this.depth = depth;
        this.initValList = new ArrayList<>();
        this.isUsed = false;
    }


    public void riseDim(String size) {
        if (this.dim == 0) {
            this.size1 = Integer.parseInt(size);
        } else if (this.dim == 1) {
            this.size2 = Integer.parseInt(size);
        }
        this.dim++;
    }

    public void addInitVal(String initVal) {
        this.initValList.add(initVal);
    }

    public String getName() {
        return name;
    }

    public int getDim() {
        return dim;
    }

    public int getVarType() {
        return varType;
    }

    public int getDepth() {
        return depth;
    }

    public int getSize1() {
        return size1;
    }

    public int getSize2() {
        return size2;
    }

    public ArrayList<String> getInitValList() {
        return initValList;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }
}
