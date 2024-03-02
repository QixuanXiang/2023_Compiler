import java.util.ArrayList;

public class Symbol {
    public class Func {
        public int retype; // 0 -> int, 1 -> void
        public int paraNum = 0; // 参数数量
        public ArrayList<Integer> paraTypeList = new ArrayList<>(); // 参数类型
        public Func(int retype) {
            this.retype = retype;
        }

        public void addPara(int type){
            paraNum++;
            paraTypeList.add(type);
        }
    }

    private final String token; 	// 当前单词所对应的字符串。
    private int type; 		// 0 -> a, 1 -> a[], 2 -> a[][], -1 -> func， -10 -> 类型未知
    private int con;		// 1 -> const, 0 -> var
    private Func func;

    public Symbol(String token, int type, int con) {
        this.token = token;
        this.type = type;
        this.con = con;
    }

    public Symbol(String token, int retype) {
        this.token = token;
        this.type = -1;
        this.con = 0;
        this.func = new Func(retype);
    }

    public String getToken() {
        return token;
    }

    public int getType() {
        return type;
    }

    public int getCon() {
        return con;
    }

    public Func getFunc() {
        return func;
    }
}
