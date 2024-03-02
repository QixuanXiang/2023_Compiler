import java.util.ArrayList;
import java.util.HashMap;

public class IntermediateCode {
    private static IntermediateCode instance;
    private StringBuilder intermediateCode;
    private ArrayList<String> temporaryVar;
    private HashMap<String, Integer> labelNum = new HashMap<>();
    SymbolManager_G manager = SymbolManager_G.getInstance();


    public static IntermediateCode getInstance() {
        if (instance == null) instance = new IntermediateCode();
        return instance;
    }

    public IntermediateCode() {
        this.intermediateCode = new StringBuilder();
        this.temporaryVar = new ArrayList<>();
        this.labelNum.put("falseLabel", 0);
        this.labelNum.put("trueLabel", 0);
        this.labelNum.put("else_if", 0);
        this.labelNum.put("end_if", 0);
        this.labelNum.put("loop_begin", 0);
        this.labelNum.put("loop_end", 0);
        this.labelNum.put("loop_varUpdate", 0);
    }

    public String createTemporaryVar() {
        String newTempVar = "&t" + (temporaryVar.size() + 1);
        temporaryVar.add(newTempVar);
        return newTempVar;
    }

    public void deleteTemporaryVar(String name) {
        temporaryVar.remove(name);
    }

    public void varDefEnd() {
        intermediateCode.append("varDef End\n");
    }

    public void funcDefEnd() {
        intermediateCode.append("funcDef End\n");
    }

    public void mainFuncDef() {
        intermediateCode.append("mainFunc Def\n");
    }

    public void blockBegin() {
        intermediateCode.append("block begin\n");
    }

    public void blockEnd() {
        intermediateCode.append("block end\n");
    }


    public void constDef(VarSymbol_G varSymbol) {
        String name_depth = varSymbol.getName() + "_" + manager.getDepth();
        if (varSymbol.getVarType() == 0) {
            intermediateCode.append("const ");
        } else {
            intermediateCode.append("var ");
        }
        if (varSymbol.getDim() >= 1) {
            intermediateCode.append("arr ");
        }
        intermediateCode.append("int ").append(name_depth);
        if (varSymbol.getDim() == 1) {
            intermediateCode.append("[").append(varSymbol.getSize1()).append("]");
        } else if (varSymbol.getDim() == 2) {
            intermediateCode.append("[").append(varSymbol.getSize2() * varSymbol.getSize1()).append("]");
        }

        if (!varSymbol.getInitValList().isEmpty()) {
            if (varSymbol.getDim() == 0) {
                intermediateCode.append(" = ").append(varSymbol.getInitValList().get(0)).append("\n");
            } else {
                intermediateCode.append("\n");
                for (int i = 0; i < varSymbol.getInitValList().size(); i++) {
                    intermediateCode.append(name_depth).append("[").append(i).append("] = ");
                    intermediateCode.append(varSymbol.getInitValList().get(i)).append("\n");
                }
            }
        } else {
            intermediateCode.append("\n");
        }
    }

    public void funcDef(FuncSymbol_G funcSymbol) {
        String name_depth = funcSymbol.getName() + "_" + funcSymbol.getDepth();
        if (funcSymbol.getFuncType() == 0) {
            intermediateCode.append("void ").append(name_depth).append("()").append(funcSymbol.getParaNum()).append("\n");
        } else {
            intermediateCode.append("int ").append(name_depth).append("()").append(funcSymbol.getParaNum()).append("\n");
        }
    }

    public void mainDef() {
        intermediateCode.append("int main()\n");
    }

    public void paraDef(VarSymbol_G varSymbol) {
        String name_depth = varSymbol.getName() + "_" + manager.getDepth();
        intermediateCode.append("para int ").append(name_depth);
        if (varSymbol.getDim() == 1) {
            intermediateCode.append("[]");
        } else if (varSymbol.getDim() == 2) {
            intermediateCode.append("[]").append("[").append(varSymbol.getSize2()).append("]");
        }
        intermediateCode.append("\n");
    }

    public void paraDefEnd() {
        intermediateCode.append("para end\n");
    }


    public String readCode() {
        String temp = createTemporaryVar();
        intermediateCode.append(temp).append(" = read\n");
        return temp;
    }

    public String getLVal(VarSymbol_G symbol, ArrayList<String> offset, boolean isLeft) {
        String name = symbol.getName() + "_" + symbol.getDepth();
        if (symbol.getDim() == offset.size()) { //变量维数和数组的参数相等，即得到的是值而非数组
            if (offset.isEmpty()) {
                /*if (!isLeft && (symbol.getVarType() == 0 || (!symbol.isUsed() && !symbol.getInitValList().isEmpty()
                        && isDigit(symbol.getInitValList().get(0))))) {*/
                if (!isLeft && symbol.getVarType() == 0) {
                    return String.valueOf(symbol.getInitValList().get(0));
                }
                return name;
            } else if (offset.size() == 1) {
                if (!isLeft && symbol.getVarType() == 0 && isDigit(offset.get(0))) {
                    return String.valueOf(symbol.getInitValList().get(Integer.parseInt(offset.get(0))));
                }
                if (!isLeft) {
                    String temp;
                    if (offset.get(0).charAt(0) == '&') {
                        temp = offset.get(0);
                    } else {
                        temp = createTemporaryVar();
                    }
                    intermediateCode.append(temp).append(" = ").append(name).
                            append("[").append(offset.get(0)).append("]\n");
                    return temp;
                } else {
                    return name + "[" + offset.get(0) + "]";
                }
            } else {
                String temp1;
                if (isDigit(offset.get(0))) {
                    int result = Integer.parseInt(offset.get(0)) * symbol.getSize2();
                    temp1 = String.valueOf(result);
                } else {
                    if (offset.get(0).charAt(0) == '&') {
                        temp1 = offset.get(0);
                    } else {
                        temp1 = createTemporaryVar();
                    }
                    intermediateCode.append(temp1).append(" = ");
                    intermediateCode.append(offset.get(0)).append(" * ").append(symbol.getSize2()).append("\n");
                }
                String temp2;
                if (isDigit(temp1) && isDigit(offset.get(1))) {
                    int result = Integer.parseInt(temp1) + Integer.parseInt(offset.get(1));
                    temp2 = String.valueOf(result);
                } else {
                    if (offset.get(1).charAt(0) == '&') {
                        temp2 = offset.get(1);
                    } else {
                        temp2 = createTemporaryVar();
                    }
                    intermediateCode.append(temp2).append(" = ");
                    intermediateCode.append(temp1).append(" + ").append(offset.get(1)).append("\n");
                }
                if (!isLeft && symbol.getVarType() == 0 && isDigit(temp2)) {
                    return String.valueOf(symbol.getInitValList().get(Integer.parseInt(temp2)));
                }
                if (!isLeft) {
                    String temp;
                    if (temp2.charAt(0) == '&') {
                        temp = temp2;
                    } else {
                        temp = createTemporaryVar();
                    }
                    intermediateCode.append(temp).append(" = ").
                            append(name).append("[").append(temp2).append("]\n");
                    return temp;
                } else {
                    return name + "[" + temp2 + "]";
                }
            }
        } else {
            if (symbol.getDim() == 1 && offset.isEmpty()) {
                return name + "[]";
            } else if (symbol.getDim() == 2 && offset.isEmpty()) {
                return name + "[][]";
            } else {
                return name + "[" + offset.get(0) + "]" + "[" + symbol.getSize2() + "]";
            }
        }
    }

    public void assignCode(String left, String right) {
        intermediateCode.append(left).append(" = ").append(right).append("\n");
    }

    public int ifStmt(String cond) {
        labelNum.replace("else_if", labelNum.get("else_if"), labelNum.get("else_if") + 1);
        labelNum.replace("end_if", labelNum.get("end_if"), labelNum.get("end_if") + 1);
        intermediateCode.append("beq ").append(cond).append(" 0 else_if").append(labelNum.get("else_if")).append("\n");
        return labelNum.get("else_if");
    }
/*
    public int ifStmt(String cond) {
        labelNum.replace("else_if", labelNum.get("else_if"), labelNum.get("else_if") + 1);
        labelNum.replace("end_if", labelNum.get("end_if"), labelNum.get("end_if") + 1);
        intermediateCode.append("beq ").append(cond).append(" $0 else_if").append(labelNum.get("else_if")).append("\n");
        return labelNum.get("else_if");
    }
*/

    public int forStmt() {
        labelNum.replace("loop_begin", labelNum.get("loop_begin"), labelNum.get("loop_begin") + 1);
        labelNum.replace("loop_end", labelNum.get("loop_end"), labelNum.get("loop_end") + 1);
        label("loop_begin", labelNum.get("loop_begin"));
        return labelNum.get("loop_begin");
    }

    public void judgeInLoop(String cond, int labelNum) {
        intermediateCode.append("beq ").append(cond).append(" 0 loop_end").append(labelNum).append("\n");
    }
/*
    public void judgeInLoop(String cond, int labelNum) {
        intermediateCode.append("beq ").append(cond).append(" $0 loop_end").append(labelNum).append("\n");
    }
*/
    public void breakOrContinue(int labelNum, String op) {
        if (op.equals("break")) {
            jumpToLabel("loop_end", labelNum);
        } else if (op.equals("continue")) {
            jumpToLabel("loop_varUpdate", labelNum);
        }
    }

    public String unaryOperation(String right, String op) {
        if (!op.equals("!")) {
            if (op.equals("-")) {
                String left = "0";
                return basicOperation(left, right, op);
            } else {
                return right;
            }
        } else {
            String temp = allocateRegForResult("0", right);
            intermediateCode.append("seq ").append(temp).append(" ").append(right).append(" 0\n");
            return temp;
        }
    }

    public String basicOperation(String left, String right, String op) {
        String temp;
        if (isDigit(left) && isDigit(right)) {
            int result;
            result = switch (op) {
                case "+" -> Integer.parseInt(left) + Integer.parseInt(right);
                case "-" -> Integer.parseInt(left) - Integer.parseInt(right);
                case "*" -> Integer.parseInt(left) * Integer.parseInt(right);
                case "/" -> Integer.parseInt(left) / Integer.parseInt(right);
                case "%" -> Integer.parseInt(left) % Integer.parseInt(right);
                default -> 0;
            };
            return String.valueOf(result);
        }

        if (isDigit(right)) {
            if ((op.equals("+") || op.equals("-")) && right.equals("0")) {
                return left;
            }
            if ((op.equals("*") || op.equals("/")) && right.equals("1")) {
                return left;
            }
            if (op.equals("*") && right.equals("0")) {
                return "0";
            }
            if (op.equals("*") && isPowerOf2(right)) {
                temp = allocateRegForResult(left, right);
                intermediateCode.append(temp).append(" = ").
                        append(left).append(" < ").append(power2(right)).append("\n");
                return temp;
            }
            /*
            if (op.equals("/") && Integer.parseInt(right) > 0) {
                ArrayList<String> ml = divide(right);
                String m = ml.get(0);
                String l = ml.get(1);
                temp = allocateRegForResult(left, right);
                intermediateCode.append(temp).append(" = ").
                        append(left).append(" *h ").append(m).append("\n");
                intermediateCode.append(temp).append(" = ").
                        append(temp).append(" >1 ").append(l).append("\n");
                return temp;
            }
            */
        }
        temp = allocateRegForResult(left, right);
        intermediateCode.append(temp).append(" = ").
                append(left).append(" ").append(op).append(" ").append(right).append("\n");
        return temp;
    }

    public String compareOperation(String left, String right, String op) {
        String temp = allocateRegForResult(left, right);
        switch (op) {
            case "<" -> intermediateCode.append("slt ");
            case ">" -> intermediateCode.append("sgt ");
            case "<=" -> intermediateCode.append("sle ");
            case ">=" -> intermediateCode.append("sge ");
            default -> intermediateCode.append("\n");
        }
        intermediateCode.append(temp).append(" ").append(left).append(" ").append(right).append("\n");
        return temp;
    }

    public String equalOperation(String left, String right, String op) {
        String temp = allocateRegForResult(left, right);
        intermediateCode.append("seq ").append(temp).append(" ").append(left).append(" ").append(right).append("\n");
        if (op.equals("!=")) {
            intermediateCode.append("seq ").append(temp).append(" ").append(temp).append(" 0").append("\n");
        }
        return temp;
    }
/*
    public String equalOperation(String left, String right, String op) {
        String temp = allocateRegForResult(left, right);
        if (op.equals("==")) {
            intermediateCode.append("seq ").append(temp).append(" ").append(left).append(" ").append(right).append("\n");
        } else if (op.equals("!=")) {
            intermediateCode.append("sne ").append(temp).append(" ").append(left).append(" ").append(right).append("\n");
        }
        return temp;
    }
 */

    public void logicalOperation(String left, String op) {
        if (op.equals("&&")) {
            intermediateCode.append("beq ").append(left).append(", 0 falseLabel").append(labelNum.get("falseLabel")).append("\n");
        } else if (op.equals("||")) {
            intermediateCode.append("beq ").append(left).append(", 1 trueLabel").append(labelNum.get("trueLabel")).append("\n");
        }
        temporaryVar.remove(left);
    }
/*
    public void logicalOperation(String left, String op) {
        if (op.equals("&&")) {
            intermediateCode.append("beq ").append(left).append(", $0 falseLabel").append(labelNum.get("falseLabel")).append("\n");
        } else if (op.equals("||")) {
            intermediateCode.append("beq ").append(left).append(", 1 trueLabel").append(labelNum.get("trueLabel")).append("\n");
        }
        temporaryVar.remove(left);
    }

 */

    public String circuiting(String op) {
        String temp = createTemporaryVar();
        if (op.equals("&&")) {
            label("falseLabel", labelNum.get("falseLabel"));
            labelNum.replace("falseLabel", labelNum.get("falseLabel"), labelNum.get("falseLabel") + 1);
        } else if (op.equals("||")) {
            label("trueLabel", labelNum.get("trueLabel"));
            labelNum.replace("trueLabel", labelNum.get("trueLabel"), labelNum.get("trueLabel") + 1);
        }
        return temp;
    }

    public void jumpToLabel(String label, int labelNum) {
        intermediateCode.append("jump ").append(label).append(labelNum).append("\n");
    }

    public void label(String label, int labelNum) {
        intermediateCode.append(label).append(labelNum).append(" :\n");
    }

    public void writeCode(String formatString) {
        intermediateCode.append("write ").append(formatString).append("\n");
    }

    public void writeStrCode(String formatString) {
        if (formatString.charAt(0) == 'd') {
            formatString = formatString.substring(1);
        }
        formatString = formatString.replaceAll("\"", "");
        if (!formatString.isEmpty()) {
            intermediateCode.append("write \"").append(formatString).append("\"").append("\n");
        }
    }

    public void pushCode(String para) {
        intermediateCode.append("push ").append(para).append("\n");
    }

    public void callCode(FuncSymbol_G funcSymbol) {
        String name = funcSymbol.getName() + "_" + funcSymbol.getDepth();
        intermediateCode.append("call ").append(name).append("\n");
    }

    public String backCode(FuncSymbol_G funcSymbol) {
        if (funcSymbol.getFuncType() == 0) {
            return null;
        }
        String temp = createTemporaryVar();
        intermediateCode.append(temp).append(" = RET").append("\n");
        return temp;
    }

    public void returnCode(String temp) {
        intermediateCode.append("ret ").append(temp).append("\n");
    }

    public boolean isDigit(String exp) {
        if (exp.charAt(0) == '-' || exp.charAt(0) == '+') {
            exp = exp.substring(1);
        }
        for (int i = 0; i < exp.length(); i++) {
            if (!Character.isDigit(exp.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean isPowerOf2(String num) {
        long intNum = Integer.parseInt(num);
        long j = 1;
        while (j < intNum) {
            j = j * 2;
        }
        return j == intNum;
    }

    public int power2(String num) {
        long intNum = Integer.parseInt(num);
        int i = 0;
        long j = 1;
        while (j < intNum) {
            j = j * 2;
            i++;
        }
        return i;
    }

    public ArrayList<String> divide(String num) {
        long intNum = Integer.parseInt(num);
        ArrayList<String> back = new ArrayList<>();
        for (long i = 0; i <= 30 ; i++) {
            long low = 1;
            long high = 1;
            for (int j = 0; j < 30 + i; j++) {
                low = low * 2;
            }
            for (int k = 0; k < i; k++) {
                high = high * 2;
            }
            high = low + high;
            for (long m = 0; intNum * m <= high; m++) {
                if (intNum * m >= low && intNum * m <= high) {
                    back.add(String.valueOf(m));
                    back.add(String.valueOf(i));
                    return back;
                }
            }
        }
        return null;
    }

    public String allocateRegForResult(String left, String right) {
        String temp;
        if (left.charAt(0) == '&') {
            temp = left;
            if (right.charAt(0) == '&') {
                deleteTemporaryVar(right);
            }
        } else if (right.charAt(0) == '&') {
            temp = right;
        } else {
            temp = createTemporaryVar();
        }
        return temp;
    }

    public void clearTemp() {
        temporaryVar.clear();
        intermediateCode.append("clear temp\n");
    }

    public void updateCode(String newCode) {
        intermediateCode = new StringBuilder();
        intermediateCode.append(newCode);
    }

    public void addCode(String newCode) {
        intermediateCode.append(newCode);
    }

    public String getIntermediateCode() {
        return intermediateCode.toString();
    }

}
