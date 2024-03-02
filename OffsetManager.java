import java.util.HashMap;

public class OffsetManager {
    private static OffsetManager instance;
    private OffsetTable curOffsetTable;

    public static OffsetManager getInstance() {
        if (instance == null) instance = new OffsetManager();
        return instance;
    }

    public OffsetManager() {
        HashMap<String, Integer> mainOffset = new HashMap<>();
        HashMap<String, String> mainMarkList = new HashMap<>();
        this.curOffsetTable = new OffsetTable(0, mainOffset, mainMarkList);
    }

    public void createSymbolTable(int preOffset, HashMap<String, Integer> preList, HashMap<String, String> preMark) {
        OffsetTable offsetTable = new OffsetTable(preOffset, preList, preMark);
        offsetTable.setPre(offsetTable);
        curOffsetTable = offsetTable;
    }

    public void traceBackSymbolTable() {
        curOffsetTable = curOffsetTable.getPre();
    }

    public void addVar(String name) {
        curOffsetTable.getOffsetList().put(name, curOffsetTable.getCurOffset());
    }

    public void addMark(String name, String regS) {
        curOffsetTable.getMarkList().put(name, regS);
    }

    public boolean containsMark(String name) {
        return curOffsetTable.getMarkList().containsKey(name);
    }

    public String getRegS(String name) {
        return curOffsetTable.getMarkList().get(name);
    }
    public HashMap<String, String> getMaskList() {
        return curOffsetTable.getMarkList();
    }

    public void setVar(String name, int setOffset) {
        curOffsetTable.getOffsetList().put(name, setOffset);
    }

    public void nextOffset() {
        curOffsetTable.nextOffset();
    }

    public void preOffset() {
        curOffsetTable.preOffset();
    }

    public int getOffset(String name) {
        return curOffsetTable.getOffsetList().get(name);
    }

    public boolean contains(String name) {
        return curOffsetTable.getOffsetList().containsKey(name);
    }

    public HashMap<String, Integer> getOffsetList() {
        return curOffsetTable.getOffsetList();
    }

    public int getCurOffset() {
        return curOffsetTable.getCurOffset();
    }
}
