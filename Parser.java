import java.util.ArrayList;
import java.util.Objects;

public class Parser {
    private final Lexer lexer;
    private String[] read = new String[3];
    private StringBuilder output = new StringBuilder();
    private StringBuilder errorInfo = new StringBuilder();
    private ArrayList<SymbolTable> symbolTableList = new ArrayList<>();
    private SymbolTable curSymbolTable;
    private ArrayList<Symbol> fParas = new ArrayList<>(); //存储函数的形参
    private ArrayList<ArrayList<Integer>> rParasList = new ArrayList<>(); //调用函数时存储读入的参数的类型
    private ArrayList<Integer> curParas = new ArrayList<>(); //记录当前读入参数的类型
    private int rParaDiv = 0; //读入参数的维数
    private boolean errorBlock = false;  //是否是错误的block(重复命名的函数），错误的block不建表且不关心内部错误
    private int id = 0;
    private boolean returnFlag = false; // int 类型函数最后一行是否有 return 语句
    private boolean isVarDefSu; //定义变量时记录其是否有错（非重定义错误），有错则变量类型未知（-10）
    private boolean isFuncDefSu; //定义函数时记录其是否有错（非重定义错误）
    private boolean isRPara; //标识符判断是否是参数

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        parseCompUnit();
    }

    public void parseCompUnit() {
        addSymbolTable(0, 1);
        read = lexer.readAhead();
        while (read[0].equals("const") || (read[0].equals("int") && !read[2].equals("("))) {
            parseDecl();
            read = lexer.readAhead();
        }
        while (read[0].equals("void") || (read[0].equals("int") && !read[1].equals("main"))) {
            parseFuncDef();
            read = lexer.readAhead();
        }
        parseMainFuncDef();
        output.append("<CompUnit>").append("\n");
    }

    public void parseDecl() {
        read = lexer.readAhead();
        if (read[0].equals("const")) {
            parseConstDecl();
        } else {
            parseVarDecl();
        }
    }

    public void parseConstDecl() {
        nextStep();
        parseBtype();
        parseConstDef();
        read = lexer.readAhead();
        while (read[0].equals(",")) {
            nextStep();
            parseConstDef();
            read = lexer.readAhead();
        }
        missingChar(read, ";");
        output.append("<ConstDecl>").append("\n");
    }

    public void parseBtype() {
        nextStep();
    }

    public void parseConstDef() {
        read = lexer.readAhead();
        String token = read[0];
        nextStep();
        read = lexer.readAhead();
        int div = 0;  //变量的维数
        isVarDefSu = true;
        while (read[0].equals("[")) {
            nextStep();
            parseConstExp();
            read = lexer.readAhead();
            missingChar(read, "]");
            read = lexer.readAhead();
            div++;
        }
        nextStep();
        parseConstInitVal();
        addSymbol(token, div, 1);
        output.append("<ConstDef>").append("\n");
    }

    public void parseConstInitVal() {
        read = lexer.readAhead();
        if (!read[0].equals("{")) {
            parseConstExp();
        } else {
            nextStep();
            read = lexer.readAhead();
            if (!read[0].equals("}")) {
                parseConstInitVal();
                read = lexer.readAhead();
                while (read[0].equals(",")) {
                    nextStep();
                    parseConstInitVal();
                    read = lexer.readAhead();
                }
            }
            nextStep();
        }
        output.append("<ConstInitVal>").append("\n");
    }

    public void parseVarDecl() {
        parseBtype();
        parseVarDef();
        read = lexer.readAhead();
        while (read[0].equals(",")) {
            nextStep();
            parseVarDef();
            read = lexer.readAhead();
        }
        missingChar(read, ";");
        output.append("<VarDecl>").append("\n");
    }

    public void parseVarDef() {
        read = lexer.readAhead();
        String token = read[0];
        nextStep();
        read = lexer.readAhead();
        int div = 0;
        isVarDefSu = true;
        while (read[0].equals("[")) {
            nextStep();
            parseConstExp();
            read = lexer.readAhead();
            missingChar(read, "]");
            read = lexer.readAhead();
            div++;
        }
        if (read[0].equals("=")) {
            nextStep();
            parseInitVal();
        }
        addSymbol(token, div, 0);
        output.append("<VarDef>").append("\n");
    }

    public void parseInitVal() {
        read = lexer.readAhead();
        if (!read[0].equals("{")) {
            parseExp();
        } else {
            nextStep();
            read = lexer.readAhead();
            if (!read[0].equals("}")) {
                parseInitVal();
                read = lexer.readAhead();
                while (read[0].equals(",")) {
                    nextStep();
                    parseInitVal();
                    read = lexer.readAhead();
                }
            }
            nextStep();
        }
        output.append("<InitVal>").append("\n");
    }

    public void parseFuncDef() {
        read = lexer.readAhead();
        int retype = read[0].equals("void") ? 1 : 0;
        isFuncDefSu = true;
        parseFuncType();
        read = lexer.readAhead();
        String token = read[0];
        nextStep();
        nextStep();
        fParas.clear();
        read = lexer.readAhead();
        if (!read[0].equals(")") && !read[0].equals("{")) {
            parseFuncFParams();
        }
        read = lexer.readAhead();
        missingChar(read, ")");
        addFuncSymbol(token, retype, fParas);
        addSymbolTable(curSymbolTable.getId(), retype);
        if (!errorBlock) {
            for (Symbol para : fParas) {
                addSymbol(para.getToken(), para.getType(), para.getCon());
            }
        }
        parseBlock();
        if (!errorBlock && (!returnFlag && retype == 0)) {
            error("g");
        }
        returnFlag = false;
        deleteSymbolTable();
        errorBlock = false;
        output.append("<FuncDef>").append("\n");
    }

    public void parseMainFuncDef() {
        nextStep();
        nextStep();
        nextStep();
        read = lexer.readAhead();
        missingChar(read, ")");
        addSymbolTable(curSymbolTable.getId(), 0);
        parseBlock();
        if (!returnFlag) {
            error("g");
        }
        returnFlag = false;
        deleteSymbolTable();
        output.append("<MainFuncDef>").append("\n");
    }

    public void parseFuncType() {
        nextStep();
        output.append("<FuncType>").append("\n");
    }

    public void parseFuncFParams() {
        parseFuncFParam();
        read = lexer.readAhead();
        while (read[0].equals(",")) {
            nextStep();
            parseFuncFParam();
            read = lexer.readAhead();
        }
        output.append("<FuncFParams>").append("\n");
    }

    public void parseFuncFParam() {
        parseBtype();
        read = lexer.readAhead();
        String token = read[0];
        nextStep();
        int div = 0;
        isVarDefSu = true;
        read = lexer.readAhead();
        if (read[0].equals("[")) {
            nextStep();
            read = lexer.readAhead();
            missingChar(read, "]");
            read = lexer.readAhead();
            div++;
            while (read[0].equals("[")) {
                nextStep();
                parseConstExp();
                read = lexer.readAhead();
                missingChar(read, "]");
                read = lexer.readAhead();
                div++;
            }
        }
        Symbol symbol = new Symbol(token, div, 0);
        fParas.add(symbol);
        output.append("<FuncFParam>").append("\n");
    }

    public void parseBlock() {
        nextStep();
        read = lexer.readAhead();
        while (!read[0].equals("}")) {
            parseBlockItem();
            read = lexer.readAhead();
        }
        nextStep();
        output.append("<Block>").append("\n");
    }

    public void parseBlockItem() {
        read = lexer.readAhead();
        if (read[0].equals("const") || read[0].equals("int")) {
            parseDecl();
        } else {
            if (read[0].equals("{")) { //stmt 为 block 语句，建立类型为 void 的表
                addSymbolTable(curSymbolTable.getId(), 1);
            }
            parseStmt();
        }
    }

    public void parseStmt() {
        returnFlag = false;
        read = lexer.readAhead();
        if (read[0].equals("{")) {
            parseBlock();
            deleteSymbolTable(); //block 结束删除符号表
        } else if (read[0].equals("if")) {
            nextStep();
            nextStep();
            parseCond();
            read = lexer.readAhead();
            missingChar(read, ")");
            addSymbolTable(curSymbolTable.getId(), 1);
            read = lexer.readAhead();
            boolean flag = !read[0].equals("{");
            parseStmt();
            if (flag) {
                deleteSymbolTable();
            }
            read = lexer.readAhead();
            if (read[0].equals("else")) {
                nextStep();
                addSymbolTable(curSymbolTable.getId(), 1);
                read = lexer.readAhead();
                flag = !read[0].equals("{");
                parseStmt();
                if (flag) {
                    deleteSymbolTable();
                }
            }
        } else if (read[0].equals("for")) { //
            nextStep();
            nextStep();
            read = lexer.readAhead();
            if (!read[0].equals(";")) {
                parseForStmt();
            }
            nextStep();
            read = lexer.readAhead();
            if (!read[0].equals(";")) {
                parseCond();
            }
            nextStep();
            read = lexer.readAhead();
            if (!read[0].equals(")")) {
                parseForStmt();
            }
            nextStep();
            addSymbolTable(curSymbolTable.getId(), 2);
            read = lexer.readAhead();
            boolean flag = !read[0].equals("{");
            parseStmt();
            if (flag) {
                deleteSymbolTable();
            }
        } else if (read[0].equals("break") || read[0].equals("continue")) {
            nextStep();
            errorBrCoRe(2);
            read = lexer.readAhead();
            missingChar(read, ";");
        } else if (read[0].equals("return")) {  //
            nextStep();
            read = lexer.readAhead();
            if (!read[0].equals(";") && !read[0].equals("}") && !read[0].equals("{") && !read[0].equals("const")
                    && !read[0].equals("int") && !read[0].equals("break") && !read[0].equals("continue")
                    && !read[0].equals("if") && !read[0].equals("else") && !read[0].equals("for")
                    && !read[0].equals("printf") && !read[0].equals("return")) {
                parseExp();
                errorBrCoRe(0);
            }
            read = lexer.readAhead();
            missingChar(read, ";");
            read = lexer.readAhead();
            if (curSymbolTable.getType() == 0) {
                returnFlag = true;
            }
        } else if (read[0].equals("printf")) {
            nextStep();
            nextStep();
            nextStep();
            if (!lexer.isLegal()) {
                error("a");
            }
            read = lexer.readAhead();
            int formatNum = 0;
            while (read[0].equals(",")) {
                nextStep();
                parseExp();
                formatNum++;
                read = lexer.readAhead();
            }
            if (formatNum != lexer.getFormatNum()) {
                error("l");
            }
            missingChar(read, ")");
            read = lexer.readAhead();
            missingChar(read, ";");
        } else if (lexer.isLValOrExp().equals("LVal")) {
            parseLVal(true);
            nextStep();
            read = lexer.readAhead();
            if (!read[0].equals("getint")) {
                parseExp();
                read = lexer.readAhead();
                missingChar(read, ";");
            } else {
                nextStep();
                nextStep();
                read = lexer.readAhead();
                missingChar(read, ")");
                read = lexer.readAhead();
                missingChar(read, ";");
            }
        } else {
            if (!read[0].equals(";")) {
                parseExp();
            }
            read = lexer.readAhead();
            missingChar(read, ";");
        }
        output.append("<Stmt>").append("\n");
    }

    public void parseForStmt() {
        parseLVal(true);
        nextStep();
        parseExp();
        output.append("<ForStmt>").append("\n");
    }

    public void parseExp() {
        parseAddExp();
        output.append("<Exp>").append("\n");
    }

    public void parseCond() {
        parseLOrExp();
        output.append("<Cond>").append("\n");
    }

    public void parseLVal(boolean isAssign) {
        read = lexer.readAhead();
        String token = read[0];
        nextStep();
        if (isRPara) {
            rParaDiv = isDef(token, isAssign);
        } else {
            isDef(token, isAssign);
        }
        read = lexer.readAhead();
        while (read[0].equals("[")) {
            rParaDiv--;
            nextStep();
            isRPara = false;
            parseExp();
            isRPara = true;
            read = lexer.readAhead();
            missingChar(read, "]");
            read = lexer.readAhead();
        }
        output.append("<LVal>").append("\n");
    }

    public void parsePrimaryExp() {
        read = lexer.readAhead();
        if (read[0].equals("(")) {
            nextStep();
            parseExp();
            read = lexer.readAhead();
            missingChar(read, ")");
        } else if (read[0].charAt(0) > '9' || read[0].charAt(0) < '0') {
            parseLVal(false);
        } else {
            parseNumber();
        }
        output.append("<PrimaryExp>").append("\n");
    }

    public void parseNumber() {
        nextStep();
        output.append("<Number>").append("\n");
    }

    public void parseUnaryExp() {
        read = lexer.readAhead();
        if (read[0].equals("+") || read[0].equals("-") || read[0].equals("!")) {
            parseUnaryOp();
            parseUnaryExp();
        } else if (read[1].equals("(") && !read[0].equals("(")) {
            read = lexer.readAhead();
            String token = read[0];
            nextStep();
            nextStep();
            read = lexer.readAhead();
            ArrayList<Integer> newRPara = new ArrayList<>();
            rParasList.add(newRPara);
            curParas = rParasList.get(rParasList.size() - 1);
            if (!read[0].equals(")") && !read[0].equals(";")) { //
                parseFuncRParams();
            }
            rParaDiv = isCallCorrect(token); //函数调用正确且为 int 型维数为0， 否则为 -1
            rParasList.remove(curParas);
            if (!rParasList.isEmpty()) {
                curParas = rParasList.get(rParasList.size() - 1);
            }
            read = lexer.readAhead();
            missingChar(read, ")");
        } else {
            parsePrimaryExp();
        }
        output.append("<UnaryExp>").append("\n");
    }

    public void parseUnaryOp() {
        nextStep();
        output.append("<UnaryOp>").append("\n");
    }

    public void parseFuncRParams() {
        rParaDiv = 0;
        isRPara = true;
        parseExp();
        curParas.add(rParaDiv);
        read = lexer.readAhead();
        while (read[0].equals(",")) {
            nextStep();
            rParaDiv = 0;
            parseExp();
            curParas.add(rParaDiv);
            read = lexer.readAhead();
        }
        output.append("<FuncRParams>").append("\n");
    }

    public void parseMulExp() {
        parseUnaryExp();
        read = lexer.readAhead();
        while (read[0].equals("*") || read[0].equals("/") || read[0].equals("%")) {
            output.append("<MulExp>").append("\n");
            nextStep();
            parseUnaryExp();
            read = lexer.readAhead();
        }
        output.append("<MulExp>").append("\n");
    }

    public void parseAddExp() {
        parseMulExp();
        read = lexer.readAhead();
        while (read[0].equals("+") || read[0].equals("-")) {
            output.append("<AddExp>").append("\n");
            nextStep();
            parseMulExp();
            read = lexer.readAhead();
        }
        output.append("<AddExp>").append("\n");
    }

    public void parseRelExp() {
        parseAddExp();
        read = lexer.readAhead();
        while (read[0].equals("<") || read[0].equals(">") || read[0].equals("<=") || read[0].equals(">=")) {
            output.append("<RelExp>").append("\n");
            nextStep();
            parseAddExp();
            read = lexer.readAhead();
        }
        output.append("<RelExp>").append("\n");
    }

    public void parseEqExp() {
        parseRelExp();
        read = lexer.readAhead();
        while (read[0].equals("==") || read[0].equals("!=")) {
            output.append("<EqExp>").append("\n");
            nextStep();
            parseRelExp();
            read = lexer.readAhead();
        }
        output.append("<EqExp>").append("\n");
    }

    public void parseLAndExp() {
        parseEqExp();
        read = lexer.readAhead();
        while (read[0].equals("&&")) {
            output.append("<LAndExp>").append("\n");
            nextStep();
            parseEqExp();
            read = lexer.readAhead();
        }
        output.append("<LAndExp>").append("\n");
    }

    public void parseLOrExp() {
        parseLAndExp();
        read = lexer.readAhead();
        while (read[0].equals("||")) {
            output.append("<LOrExp>").append("\n");
            nextStep();
            parseLAndExp();
            read = lexer.readAhead();
        }
        output.append("<LOrExp>").append("\n");
    }

    public void parseConstExp() {
        parseAddExp();
        output.append("<ConstExp>").append("\n");
    }

    public void nextStep() {
        lexer.next();
        output.append(lexer.getLexType()).append(" ").append(lexer.getToken()).append("\n");
    }

    public void addSymbolTable(int fatherId, int type) {
        if (!errorBlock) {
            SymbolTable symbolTable = new SymbolTable(id++, fatherId, type);
            symbolTableList.add(symbolTable);
            curSymbolTable = symbolTableList.get(symbolTableList.size() - 1);

        }
    }

    public void deleteSymbolTable() {
        if (!errorBlock) {
            symbolTableList.remove(curSymbolTable);
            curSymbolTable = symbolTableList.get(symbolTableList.size() - 1);
        }
    }

    public void addSymbol(String token, int div, int con) {
        if (curSymbolTable.getDirectory().containsKey("0" + token)
                || curSymbolTable.getDirectory().containsKey("1" + token)) {
            error("b");
        } else if (!errorBlock) {
            Symbol symbol;
            if (isVarDefSu) {
                symbol = new Symbol(token, div, con);
            } else {
                symbol = new Symbol(token, -10, con);
            }
            curSymbolTable.addSymbol("0" + token, symbol);
        }
    }

    public void addFuncSymbol(String token, int retype, ArrayList<Symbol> paras) {
        if (curSymbolTable.getDirectory().containsKey("0" + token)
                || curSymbolTable.getDirectory().containsKey("1" + token)) {
            error("b");
            errorBlock = true;
        } else {
            Symbol symbol = new Symbol(token, retype);
            for (Symbol para : paras) {
                symbol.getFunc().addPara(para.getType());
            }
            if (!isFuncDefSu) {
                symbol.getFunc().paraNum = -1;
            }
            curSymbolTable.addSymbol("1" + token, symbol);
        }
    }

    public void missingChar(String[] read, String mark) {
        if (read[0].equals(mark)) {
            nextStep();
        } else {
            if (mark.equals(";")) {
                error("i");
            } else if (mark.equals(")")) {
                isFuncDefSu = false;
                error("j");
            } else {
                isVarDefSu = false;
                error("k");
            }
        }
    }

    public void errorBrCoRe(int type) {
        boolean flag = false;
        for (int i = symbolTableList.size() - 1; i >= 0; i--) {
            if (symbolTableList.get(i).getType() == type) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            if (type == 2) {
                error("m");
            } else if (type == 0) {
                error("f");
            }
        }
    }

    public int isDef(String token, boolean isVal) {
        for (int i = symbolTableList.size() - 1; i >= 0; i--) {
            if (symbolTableList.get(i).getDirectory().containsKey("0" + token)) {
                if (symbolTableList.get(i).getDirectory().get("0" + token).getCon() == 1 && isVal) {
                    error("h");
                }
                return symbolTableList.get(i).getDirectory().get("0" + token).getType();
            }
        }
        error("c");
        return -10; //未定义的变量类型未知
    }

    public int isCallCorrect(String token) {
        for (int i = symbolTableList.size() - 1; i >= 0; i--) {
            if (symbolTableList.get(i).getDirectory().containsKey("1" + token)) {
                Symbol symbol = symbolTableList.get(i).getDirectory().get("1" + token);
                ArrayList<Integer> fParas = symbol.getFunc().paraTypeList;
                int paraNum = symbol.getFunc().paraNum;
                if (paraNum != curParas.size() && paraNum != -1) { // 传入参数数量和函数形参数量不同且函数定义无错则报错
                    error("d");
                    return -1;
                }
                for (int j = 0; j < paraNum; j++) {
                    if (!Objects.equals(fParas.get(j), curParas.get(j)) && curParas.get(j) > -10) { //如果传入参数类型和函数形参类型不同则报错（传入参数未定义或类型未知不报错）
                        error("e");
                        return -1;
                    }
                }
                if (symbol.getFunc().retype == 0) {
                    return 0;
                }
                return -1;
            }
        }
        error("c");
        return -1;
    }

    public void error(String type) {
        errorInfo.append(lexer.getLineNum()).append(" ").append(type).append("\n");
    }

    public String getOutput() {
        return output.toString();
    }

    public String getError() {
        return errorInfo.toString();
    }
}
