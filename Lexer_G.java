public class Lexer_G {
    private final String source;
    private int curPos = 0;
    private StringBuilder token;
    private LexType lexType;
    private int lineNum = 1;
    private int formatNum = 0;
    private boolean legal = true;
    private int number;
    private StringBuilder recordToken; //记录当前token
    private int recordPos;
    private LexType recordLextype;
    private int recordLineNum;

    public Lexer_G(String input) {
        this.source = input;
    }

    enum LexType {
        IDENFR, INTCON, STRCON, MAINTK, CONSTTK, INTTK, BREAKTK, CONTINUETK, IFTK, ELSETK,
        NOT, AND, OR, FORTK, GETINTTK, PRINTFTK, RETURNTK, PLUS, MINU, VOIDTK,
        MULT, DIV, MOD, LSS, LEQ, GRE, GEQ, EQL, NEQ, NOTE,
        ASSIGN, SEMICN, COMMA, LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE
    }

    public boolean next() {
        token = new StringBuilder();
        space(); //处理空格、\t、\n
        if (curPos == source.length()) {
            return false;
        }

        char c = source.charAt(curPos);
        if (isNonDigit(c)) { //标识符或保留字
            getIdentOrReserve();
        } else if (Character.isDigit(c)) { //数字
            getNumber();
        } else if (c == '/') { //注释或除号
            getAnnotation();
        } else if (c == '\"') { //双引号字符串
            getStr();
        } else { //符号
            getChar();
        }
        return true;
    }

    public String[] readAhead() { //预读下三个词
        recordToken = token; //记录当前token
        recordPos = curPos;
        recordLextype = lexType;
        recordLineNum = lineNum;
        String[] words = new String[3];
        next();
        String word1 = getToken();
        next();
        String word2 = getToken();
        next();
        String word3 = getToken();
        words[0] = word1;
        words[1] = word2;
        words[2] = word3;
        token = recordToken;
        curPos = recordPos;
        lexType = recordLextype;
        lineNum = recordLineNum;
        return words;
    }

    public String isLValOrExp() { //判断是LVal还是Exp
        StringBuilder recordToken = token; //记录当前token
        int recordPos = curPos;
        LexType recordLextype = lexType;
        while (next() && !getToken().equals(";") && !getToken().equals("="));
        String first = getToken();
        token = recordToken;
        curPos = recordPos;
        lexType = recordLextype;
        if (first.equals(";")) {
            return "Exp";
        } else if (first.equals("=")) {
            return "LVal";
        }
        return "";
    }

    public void space() {
        if (curPos < source.length()) {
            char c = source.charAt(curPos);
            if (c == ' ' || c == '\t' || c == '\n') {
                while (curPos < source.length() && (source.charAt(curPos) == ' ' ||
                        source.charAt(curPos) == '\t' || source.charAt(curPos) == '\n')) {
                    if (source.charAt(curPos) == '\n') {
                        lineNum++;
                    }
                    curPos++;
                }
            }
        }
    }

    public boolean isNonDigit(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private void getIdentOrReserve() {
        char c = source.charAt(curPos++);
        token.append(c);
        while (curPos < source.length() &&
                (isNonDigit(source.charAt(curPos)) || Character.isDigit(source.charAt(curPos)))) {
            c = source.charAt(curPos++);
            token.append(c);
        }
        reserve(); // 查关键字表
    }

    public void getNumber() {
        char c = source.charAt(curPos++);
        token.append(c);
        while (curPos < source.length() && Character.isDigit(source.charAt(curPos))) {
            c = source.charAt(curPos++);
            token.append(c);
        }
        lexType = LexType.INTCON; // 设置单词类别
        number = Integer.valueOf(token.toString());
    }

    public void getAnnotation() {
        char c = source.charAt(curPos++);
        token.append(c);
        if (curPos < source.length() && source.charAt(curPos) == '/') { //第二个 /
            c = source.charAt(curPos++);
            token.append(c);
            while (curPos < source.length() && source.charAt(curPos) != '\n') {
                // 非换行字符
                c = source.charAt(curPos++);
                token.append(c);
            }
            if (curPos < source.length()) { // \n 或 直接结束
                curPos++;
                lineNum++; // 单行注释末尾的\n
            }
            lexType = LexType.NOTE;
            next();
        } else if (curPos < source.length() && source.charAt(curPos) == '*') {
            // /* 跨行注释 用状态机判断
            c = source.charAt(curPos++);
            token.append(c);
            while (curPos < source.length()) {  // 状态转换循环（直至末尾）
                while (curPos < source.length() && source.charAt(curPos) != '*') {
                    // 非*字符 对应状态q5
                    c = source.charAt(curPos++);
                    token.append(c);
                    if (c == '\n') lineNum++; // 多行注释中 每行最后的回车
                }
                // *
                while (curPos < source.length() && source.charAt(curPos) == '*') {
                    // *字符 对应状态q6 如果没有转移到q7，则会在循环中转移到q5
                    c = source.charAt(curPos++);
                    token.append(c);
                }
                if (curPos < source.length() && source.charAt(curPos) == '/' && c == '*') {
                    // /字符 对应状态q7
                    c = source.charAt(curPos++);
                    token.append(c);
                    lexType = LexType.NOTE;
                    break;
                }
            }
            next();
        } else { //除号
            reserve();
        }
    }

    public void getStr() {
        formatNum = 0;
        legal = true;
        char c = source.charAt(curPos++);
        token.append(c);
        while (curPos < source.length() && source.charAt(curPos) != '"') {
            c = source.charAt(curPos++);
            token.append(c);
            if (c == '%' && curPos < source.length()) {
                if (source.charAt(curPos) == 'd') {
                    formatNum++;
                } else {
                    legal = false;
                }
            } else if (c == '\\' && curPos < source.length()) {
                if (source.charAt(curPos) != 'n') {
                    legal = false;
                }
            } else if (!(c == ' ' || c == '!' || (c >= '(' && c <= '~'))) {
                legal = false;
            }
        }
        if (source.charAt(curPos) == '"') {
            c = source.charAt(curPos++);
            token.append(c);
            lexType = LexType.STRCON;
        }
    }

    public void getChar() {
        char c = source.charAt(curPos++);
        token.append(c);
        if (c == '&') {
            if (curPos < source.length() && source.charAt(curPos) == '&') {
                c = source.charAt(curPos++);
                token.append(c);
            }
        } else if (c == '|') {
            if (curPos < source.length() && source.charAt(curPos) == '|') {
                c = source.charAt(curPos++);
                token.append(c);
            }
        } else if (c == '<') {
            if (curPos < source.length() && source.charAt(curPos) == '=') {
                c = source.charAt(curPos++);
                token.append(c);
            }
        } else if (c == '>') {
            if (curPos < source.length() && source.charAt(curPos) == '=') {
                c = source.charAt(curPos++);
                token.append(c);
            }
        } else if (c == '=') {
            if (curPos < source.length() && source.charAt(curPos) == '=') {
                c = source.charAt(curPos++);
                token.append(c);
            }
        } else if (c == '!') {
            if (curPos < source.length() && source.charAt(curPos) == '=') {
                c = source.charAt(curPos++);
                token.append(c);
            }
        }
        reserve();
    }

    public void reserve() {
        switch (token.toString()) {
            case "main":
                lexType = LexType.MAINTK;
                break;
            case "const":
                lexType = LexType.CONSTTK;
                break;
            case "int":
                lexType = LexType.INTTK;
                break;
            case "break":
                lexType = LexType.BREAKTK;
                break;
            case "continue":
                lexType = LexType.CONTINUETK;
                break;
            case "if":
                lexType = LexType.IFTK;
                break;
            case "else":
                lexType = LexType.ELSETK;
                break;
            case "!":
                lexType = LexType.NOT;
                break;
            case "&&":
                lexType = LexType.AND;
                break;
            case "||":
                lexType = LexType.OR;
                break;
            case "for":
                lexType = LexType.FORTK;
                break;
            case "getint":
                lexType = LexType.GETINTTK;
                break;
            case "printf":
                lexType = LexType.PRINTFTK;
                break;
            case "return":
                lexType = LexType.RETURNTK;
                break;
            case "+":
                lexType = LexType.PLUS;
                break;
            case "-":
                lexType = LexType.MINU;
                break;
            case "void":
                lexType = LexType.VOIDTK;
                break;
            case "*":
                lexType = LexType.MULT;
                break;
            case "/":
                lexType = LexType.DIV;
                break;
            case "%":
                lexType = LexType.MOD;
                break;
            case "<":
                lexType = LexType.LSS;
                break;
            case "<=":
                lexType = LexType.LEQ;
                break;
            case ">":
                lexType = LexType.GRE;
                break;
            case ">=":
                lexType = LexType.GEQ;
                break;
            case "==":
                lexType = LexType.EQL;
                break;
            case "!=":
                lexType = LexType.NEQ;
                break;
            case "=":
                lexType = LexType.ASSIGN;
                break;
            case ";":
                lexType = LexType.SEMICN;
                break;
            case ",":
                lexType = LexType.COMMA;
                break;
            case "(":
                lexType = LexType.LPARENT;
                break;
            case ")":
                lexType = LexType.RPARENT;
                break;
            case "[":
                lexType = LexType.LBRACK;
                break;
            case "]":
                lexType = LexType.RBRACK;
                break;
            case "{":
                lexType = LexType.LBRACE;
                break;
            case "}":
                lexType = LexType.RBRACE;
                break;
            default:
                lexType = LexType.IDENFR;
                break;
        }
    }

    public void readRecord() {
        token = recordToken;
        curPos = recordPos;
        lexType = recordLextype;
        lineNum = recordLineNum;
    }

    public String getToken() {
        return this.token.toString();
    }

    public String getLexType() {
        return this.lexType.toString();
    }

    public int getLineNum() {
        return this.lineNum;
    }

    public boolean isLegal() {
        return legal;
    }

    public int getFormatNum() {
        return formatNum;
    }
}
