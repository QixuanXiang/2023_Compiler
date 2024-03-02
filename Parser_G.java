import java.util.ArrayList;

public class Parser_G {
    private final Lexer_G lexer;
    private String[] read = new String[3];
    private StringBuilder output = new StringBuilder();

    public Parser_G(Lexer_G lexer) {
        this.lexer = lexer;
        parseCompUnit();
    }

    public void parseCompUnit() {
        read = lexer.readAhead();
        while (read[0].equals("const") || (read[0].equals("int") && !read[2].equals("("))) {
            parseDecl();
            read = lexer.readAhead();
        }
        IntermediateCode.getInstance().varDefEnd();
        while (read[0].equals("void") || (read[0].equals("int") && !read[1].equals("main"))) {
            parseFuncDef();
            IntermediateCode.getInstance().funcDefEnd();
            read = lexer.readAhead();
        }
        IntermediateCode.getInstance().mainFuncDef();
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
        nextStep();
        output.append("<ConstDecl>").append("\n");
    }

    public void parseBtype() {
        nextStep();
    }

    public void parseConstDef() {
        read = lexer.readAhead();
        VarSymbol_G varSymbol = new VarSymbol_G(read[0], 0, SymbolManager_G.getInstance().getDepth());  //创建符号
        nextStep();
        read = lexer.readAhead();
        while (read[0].equals("[")) {
            nextStep();
            varSymbol.riseDim(parseConstExp()); //记录维数和每一维大小
            nextStep();
            read = lexer.readAhead();
        }
        nextStep();
        parseConstInitVal(varSymbol);
        SymbolManager_G.getInstance().addVarSymbol(varSymbol);   //符号表添加符号
        IntermediateCode.getInstance().constDef(varSymbol);  //通过符号信息生成中间代码
        output.append("<ConstDef>").append("\n");
    }

    public void parseConstInitVal(VarSymbol_G varSymbol) {
        read = lexer.readAhead();
        if (!read[0].equals("{")) {
            varSymbol.addInitVal(parseConstExp()); //记录初值
        } else {
            nextStep();
            read = lexer.readAhead();
            if (!read[0].equals("}")) {
                parseConstInitVal(varSymbol);
                read = lexer.readAhead();
                while (read[0].equals(",")) {
                    nextStep();
                    parseConstInitVal(varSymbol);
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
        nextStep();
        output.append("<VarDecl>").append("\n");
    }

    public void parseVarDef() {
        read = lexer.readAhead();
        VarSymbol_G varSymbol = new VarSymbol_G(read[0], 1, SymbolManager_G.getInstance().getDepth());  //创建符号
        nextStep();
        read = lexer.readAhead();
        while (read[0].equals("[")) {
            nextStep();
            varSymbol.riseDim(parseConstExp()); //记录维数和每一维大小
            nextStep();
            read = lexer.readAhead();
        }
        if (read[0].equals("=")) {
            nextStep();
            parseInitVal(varSymbol);
        }
        SymbolManager_G.getInstance().addVarSymbol(varSymbol);
        IntermediateCode.getInstance().constDef(varSymbol);
        output.append("<VarDef>").append("\n");
    }

    public void parseInitVal(VarSymbol_G varSymbol) {
        read = lexer.readAhead();
        if (!read[0].equals("{")) {
            varSymbol.addInitVal(parseExp()); //记录初值
        } else {
            nextStep();
            read = lexer.readAhead();
            if (!read[0].equals("}")) {
                parseInitVal(varSymbol);
                read = lexer.readAhead();
                while (read[0].equals(",")) {
                    nextStep();
                    parseInitVal(varSymbol);
                    read = lexer.readAhead();
                }
            }
            nextStep();
        }
        output.append("<InitVal>").append("\n");
    }

    public void parseFuncDef() {
        read = lexer.readAhead();
        int funcType = read[0].equals("void") ? 0 : 1;
        parseFuncType();
        read = lexer.readAhead();
        FuncSymbol_G funcSymbol = new FuncSymbol_G(read[0], funcType, SymbolManager_G.getInstance().getDepth()); //新建函数符号

        SymbolManager_G.getInstance().addFuncSymbol(funcSymbol); //将函数符号加入符号表
        SymbolManager_G.getInstance().createSymbolTable(funcType); //新建符号表
        nextStep();
        nextStep();
        read = lexer.readAhead();
        if (!read[0].equals(")")) {
            parseFuncFParams(funcSymbol); //传递函数符号来接收参数
        }
        IntermediateCode.getInstance().funcDef(funcSymbol); //生成中间代码
        nextStep();
        parseBlock(0);
        SymbolManager_G.getInstance().traceBackSymbolTable(); //返回上一级符号表
        output.append("<FuncDef>").append("\n");
    }

    public void parseMainFuncDef() {
        nextStep();
        nextStep();
        nextStep();
        nextStep();
        IntermediateCode.getInstance().mainDef(); //生成中间代码
        SymbolManager_G.getInstance().createSymbolTable(1); //新建符号表
        parseBlock(0);
        SymbolManager_G.getInstance().traceBackSymbolTable(); //返回上一级符号表
        output.append("<MainFuncDef>").append("\n");
    }

    public void parseFuncType() {
        nextStep();
        output.append("<FuncType>").append("\n");
    }

    public void parseFuncFParams(FuncSymbol_G funcSymbol) {
        parseFuncFParam(funcSymbol);
        read = lexer.readAhead();
        while (read[0].equals(",")) {
            nextStep();
            parseFuncFParam(funcSymbol);
            read = lexer.readAhead();
        }
        IntermediateCode.getInstance().paraDefEnd();
        output.append("<FuncFParams>").append("\n");
    }

    public void parseFuncFParam(FuncSymbol_G funcSymbol) {
        parseBtype();
        read = lexer.readAhead();
        VarSymbol_G varSymbol = new VarSymbol_G(read[0], 1, SymbolManager_G.getInstance().getDepth()); //新建变量符号（函数形参）
        nextStep();
        read = lexer.readAhead();
        if (read[0].equals("[")) {
            nextStep();
            nextStep();
            varSymbol.riseDim("0");
            read = lexer.readAhead();
            while (read[0].equals("[")) {
                nextStep();
                varSymbol.riseDim(parseConstExp());
                nextStep();
                read = lexer.readAhead();
            }
        }
        SymbolManager_G.getInstance().addVarSymbol(varSymbol); //符号表中添加符号
        IntermediateCode.getInstance().paraDef(varSymbol); //生成中间代码
        funcSymbol.addPara(varSymbol.getDim()); //函数符号中添加参数信息
        output.append("<FuncFParam>").append("\n");
    }

    public void parseBlock(int oldLabelNum) {
        nextStep();
        read = lexer.readAhead();
        while (!read[0].equals("}")) {
            parseBlockItem(oldLabelNum);
            read = lexer.readAhead();
        }
        nextStep();
        output.append("<Block>").append("\n");
    }

    public void parseBlockItem(int oldLabelNum) {
        IntermediateCode.getInstance().clearTemp();
        read = lexer.readAhead();
        if (read[0].equals("const") || read[0].equals("int")) {
            parseDecl();
        } else {
            parseStmt(oldLabelNum);
        }
    }

    public void parseStmt(int oldLabelNum) {
        read = lexer.readAhead();
        if (read[0].equals("{")) {
            SymbolManager_G.getInstance().createSymbolTable(0);
            IntermediateCode.getInstance().blockBegin();
            parseBlock(oldLabelNum);
            IntermediateCode.getInstance().blockEnd();
            SymbolManager_G.getInstance().traceBackSymbolTable();
        } else if (read[0].equals("if")) {
            nextStep();
            nextStep();
            String cond = parseCond();
            int labelNUm = IntermediateCode.getInstance().ifStmt(cond);
            nextStep();
            parseStmt(oldLabelNum);
            read = lexer.readAhead();
            if (read[0].equals("else")) {
                IntermediateCode.getInstance().jumpToLabel("end_if", labelNUm);
                IntermediateCode.getInstance().label("else_if", labelNUm);
                nextStep();
                parseStmt(oldLabelNum);
                IntermediateCode.getInstance().label("end_if", labelNUm);
                return;
            }
            IntermediateCode.getInstance().label("else_if", labelNUm);
        } else if (read[0].equals("for")) {
            nextStep();
            nextStep();
            read = lexer.readAhead();
            if (!read[0].equals(";")) {
                parseForStmt();
            }
            int labelNum = IntermediateCode.getInstance().forStmt();
            nextStep();
            read = lexer.readAhead();
            if (!read[0].equals(";")) {
                String cond = parseCond();
                IntermediateCode.getInstance().judgeInLoop(cond, labelNum);
            }
            nextStep();
            String before = IntermediateCode.getInstance().getIntermediateCode();
            read = lexer.readAhead();
            if (!read[0].equals(")")) {
                parseForStmt();
            }
            String after = IntermediateCode.getInstance().getIntermediateCode();
            String forStmt = after.substring(before.length());
            IntermediateCode.getInstance().updateCode(before);
            nextStep();
            parseStmt(labelNum);
            IntermediateCode.getInstance().label("loop_varUpdate", labelNum);
            IntermediateCode.getInstance().addCode(forStmt);
            IntermediateCode.getInstance().jumpToLabel("loop_begin", labelNum);
            IntermediateCode.getInstance().label("loop_end", labelNum);
        } else if (read[0].equals("break") || read[0].equals("continue")) {
            String op = read[0];
            nextStep();
            nextStep();
            IntermediateCode.getInstance().breakOrContinue(oldLabelNum, op);
        } else if (read[0].equals("return")) {
            nextStep();
            if (!read[1].equals(";")) {
                String returnExp = parseExp();
                IntermediateCode.getInstance().returnCode(returnExp);
            }
            nextStep();
        } else if (read[0].equals("printf")) {
            nextStep();
            nextStep();
            read = lexer.readAhead();
            String formatString = read[0];
            nextStep();
            read = lexer.readAhead();
            int k = 0;
            if (formatString.contains("%")) {
                String[] part = formatString.split("%");
                while (read[0].equals(",")) {
                    nextStep();
                    IntermediateCode.getInstance().writeStrCode(part[k]);
                    IntermediateCode.getInstance().clearTemp();
                    IntermediateCode.getInstance().writeCode(parseExp());
                    k++;
                    read = lexer.readAhead();
                }
                IntermediateCode.getInstance().writeStrCode(part[k]);
            } else {
                IntermediateCode.getInstance().writeStrCode(formatString);
            }
            nextStep();
            nextStep();
        } else if (lexer.isLValOrExp().equals("LVal")) {
            String left = parseLVal(true); //计算偏移量并获得最终赋值语句的左边
            nextStep();
            String right = "";
            read = lexer.readAhead();
            if (!read[0].equals("getint")) {
                right = parseExp();
                nextStep();
            } else {
                nextStep();
                nextStep();
                nextStep();
                nextStep();
                right = IntermediateCode.getInstance().readCode(); //获取最终赋值语句的右边
            }
            IntermediateCode.getInstance().assignCode(left, right);
        } else {
            if (!read[0].equals(";")) {
                parseExp();
            }
            nextStep();
        }
        output.append("<Stmt>").append("\n");
    }

    public void parseForStmt() {
        String left = parseLVal(true);
        nextStep();
        String right = parseExp();
        IntermediateCode.getInstance().assignCode(left, right);
    }

    public String parseExp() {
        return parseAddExp();
    }

    public String parseCond() {
        return parseLOrExp();
    }

    public String parseLVal(boolean isLeft) {
        read = lexer.readAhead();
        VarSymbol_G symbol = SymbolManager_G.getInstance().findVarSymbol(read[0]);  //获取左值符号
        if (isLeft) {
            symbol.setUsed(true);
        }
        read = lexer.readAhead();
        nextStep();
        read = lexer.readAhead();
        ArrayList<String> arrExpList = new ArrayList<>();
        while (read[0].equals("[")) {
            nextStep();
            arrExpList.add(parseExp()); //向表达式列表添加数组[exp]中的exp
            nextStep();
            read = lexer.readAhead();
        }
        output.append("<LVal>").append("\n");
        return IntermediateCode.getInstance().getLVal(symbol, arrExpList, isLeft);
    }

    public String parsePrimaryExp() {
        read = lexer.readAhead();
        if (read[0].equals("(")) {
            nextStep();
            String temp = parseExp();
            nextStep();
            return temp;
        } else if (read[0].charAt(0) > '9' || read[0].charAt(0) < '0') {
            return parseLVal(false);
        } else {
            return parseNumber();
        }
    }

    public String parseNumber() {
        read = lexer.readAhead();
        String temp = read[0];
        nextStep();
        return temp;
    }

    public String parseUnaryExp() {
        read = lexer.readAhead();
        if (read[0].equals("+") || read[0].equals("-") || read[0].equals("!")) {
            String op = read[0];
            nextStep();
            String right = parseUnaryExp();
            return IntermediateCode.getInstance().unaryOperation(right, op);
        } else if (read[1].equals("(") && !read[0].equals("(")) {
            FuncSymbol_G funcSymbol = SymbolManager_G.getInstance().findFuncSymbol(read[0]);
            nextStep();
            nextStep();
            read = lexer.readAhead();
            if (!read[0].equals(")")) {
                parseFuncRParams();
            }
            nextStep();
            IntermediateCode.getInstance().callCode(funcSymbol);
            return IntermediateCode.getInstance().backCode(funcSymbol);
        } else {
            return parsePrimaryExp();
        }
    }


    public void parseFuncRParams() {
        String para = parseExp();
        ArrayList<String> paraList = new ArrayList<>();
        paraList.add(para);
        read = lexer.readAhead();
        while (read[0].equals(",")) {
            nextStep();
            para = parseExp();
            paraList.add(para);
            read = lexer.readAhead();
        }
        for (String thisPara : paraList) {
            IntermediateCode.getInstance().pushCode(thisPara);
        }
        output.append("<FuncRParams>").append("\n");
    }

    public String parseMulExp() {
        String left = parseUnaryExp();
        read = lexer.readAhead();
        while (read[0].equals("*") || read[0].equals("/") || read[0].equals("%")) {
            String op = read[0];
            nextStep();
            String right = parseUnaryExp();
            left = IntermediateCode.getInstance().basicOperation(left, right, op);
            read = lexer.readAhead();
        }
        return left;
    }

    public String parseAddExp() {
        String left = parseMulExp();
        read = lexer.readAhead();
        while (read[0].equals("+") || read[0].equals("-")) {
            String op = read[0];
            nextStep();
            String right = parseMulExp();
            left = IntermediateCode.getInstance().basicOperation(left, right, op);
            read = lexer.readAhead();
        }
        return left;
    }

    public ArrayList<String> parseRelExp() {
        String left = parseAddExp();
        read = lexer.readAhead();
        int flag = 0;
        while (read[0].equals("<") || read[0].equals(">") || read[0].equals("<=") || read[0].equals(">=")) {
            flag = 1;
            String op = read[0];
            nextStep();
            String right = parseAddExp();
            left = IntermediateCode.getInstance().compareOperation(left, right, op);
            read = lexer.readAhead();
        }
        ArrayList<String> result = new ArrayList<>();
        result.add(left);
        result.add(String.valueOf(flag));
        return result;
    }

    public String parseEqExp() {
        ArrayList<String> result = parseRelExp();
        String left = result.get(0);
        read = lexer.readAhead();
        int flag = 0;
        while (read[0].equals("==") || read[0].equals("!=")) {
            flag = 1;
            String op = read[0];
            nextStep();
            String right = parseRelExp().get(0);
            left = IntermediateCode.getInstance().equalOperation(left, right, op);
            read = lexer.readAhead();
        }
        return flag == 0 && result.get(1).equals("0") ?
                IntermediateCode.getInstance().equalOperation(left, "0", "!=") : left;
    }

    public String parseLAndExp() {
        String left = parseEqExp();
        int k = 0;
        read = lexer.readAhead();
        while (read[0].equals("&&")) {
            if (k == 0) {
                IntermediateCode.getInstance().logicalOperation(left, "&&");
            }
            nextStep();
            String right = parseEqExp();
            IntermediateCode.getInstance().logicalOperation(right, "&&");
            read = lexer.readAhead();
            k++;
        }
        if (k != 0) {
            IntermediateCode.getInstance().circuiting("&&");
        }
        return left;
    }

    public String parseLOrExp() {
        String left = parseLAndExp();
        read = lexer.readAhead();
        int k = 0;
        while (read[0].equals("||")) {
            if (k == 0) {
                IntermediateCode.getInstance().logicalOperation(left, "||");
            }
            nextStep();
            String right = parseLAndExp();
            IntermediateCode.getInstance().logicalOperation(right, "||");
            read = lexer.readAhead();
            k++;
        }
        if (k != 0) {
            IntermediateCode.getInstance().circuiting("||");
        }
        return left;
    }

    public String parseConstExp() {
        return parseAddExp();
    }

    public void nextStep() {
        lexer.next();
        output.append(lexer.getLexType()).append(" ").append(lexer.getToken()).append("\n");

    }

    public String getOutput() {
        return output.toString();
    }
}
