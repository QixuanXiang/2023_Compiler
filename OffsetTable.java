import java.util.ArrayList;
import java.util.HashMap;

public class OffsetTable {
    private HashMap<String, Integer> offsetList;
    private HashMap<String, String> markList;
    private int curOffset;
    private OffsetTable pre;
    public OffsetTable(int preOffset, HashMap<String, Integer> preOffsetList, HashMap<String, String> preMarkList) {
        this.offsetList = preOffsetList;
        this.curOffset = preOffset;
        this.markList = preMarkList;
        this.pre = null;
    }

    public void setPre(OffsetTable offsetTable) {
        this.pre = offsetTable;
    }

    public OffsetTable getPre() {
        return this.pre;
    }
    public void addVar(String name) {
        offsetList.put(name, curOffset);
    }

    public void setVar(String name, int setOffset) {
        offsetList.put(name, setOffset);
    }

    public void nextOffset() {
        curOffset = curOffset + 4;
    }

    public void preOffset() {
        curOffset = curOffset - 4;
    }

    public int getOffset(String name) {
        return offsetList.get(name);
    }

    public HashMap<String, String> getMarkList() {
        return markList;
    }

    public boolean contains(String name) {
        return offsetList.containsKey(name);
    }

    public int getCurOffset() {
        return curOffset;
    }

    public HashMap<String, Integer> getOffsetList() {
        return offsetList;
    }
}
