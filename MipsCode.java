import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MipsCode {
    private StringBuilder mipsCode = new StringBuilder();
    private String intermediateCode;
    private ArrayList<String> dataSentences = new ArrayList<>();
    private HashMap<String, String> formatStrings = new HashMap<>();
    private ArrayList<String> mainFuncSentences = new ArrayList<>();
    private ArrayList<ArrayList<String>> funcs = new ArrayList<>();
    private int paraNum = 0;
    private int totalPara;
    private int divNum = 0;
    private ArrayList<String> regTs = new ArrayList<>();
    private ArrayList<String> regSs = new ArrayList<>();
    private HashMap<String, String> match = new HashMap<>();
    private OffsetManager manager = OffsetManager.getInstance();
    private final String sp = "$sp";
    private final String v0 = "$v0";
    private final String ra = "$ra";
    private final String a0 = "$a0";
    private final String s5 = "$s6";
    private final String s6 = "$s7";

    public MipsCode(String intermediateCode) {
        this.intermediateCode = intermediateCode;
        createMipsCode();
    }

    public String createRegT() {
        int k = 0;
        while (k <= 4) {
            String newRegT = "$t" + k;
            if (!regTs.contains(newRegT)) {
                regTs.add(newRegT);
                return newRegT;
            }
            k++;
        }
        return null;
    }

    public void deleteRegT(String name) {
        regTs.remove(name);
    }

    public void clearRegT() {
        regTs.clear();
        match.clear();
    }

    public void addMatch(String left, String right) {
        match.put(left, right);
    }

    public void deleteMatch(String temp) {
        match.remove(temp);
    }

    public String findTemp(String temp) {
        for (String s : match.keySet()) {
            if (match.get(s).equals(temp)) {
                return s;
            }
        }
        return null;
    }


    public void createMipsCode() {
        String[] dataText = this.intermediateCode.split("varDef End\n");
        if (!dataText[0].isEmpty()) {
            String[] data = dataText[0].split("\n");
            dataSentences.addAll(Arrays.asList(data));
        }
        String[] funcMainFunc = dataText[1].split("mainFunc Def\n");
        if (!funcMainFunc[0].isEmpty()) {
            String[] functions = funcMainFunc[0].split("funcDef End\n");
            for (String function : functions) {
                ArrayList<String> funcSentences = new ArrayList<>(Arrays.asList(function.split("\n")));
                funcs.add(funcSentences);
            }
        }
        String[] mainFunc = funcMainFunc[1].split("\n");
        mainFuncSentences.addAll(Arrays.asList(mainFunc));
        for (ArrayList<String> func : funcs) {
            for (String funcSentence : func) {
                if (funcSentence.contains("\"")) {
                    String str = funcSentence.substring(6);
                    if (!formatStrings.containsKey(str)) {
                        String formatName = "formatStr" + formatStrings.size();
                        formatStrings.put(str, formatName);
                    }
                }
            }
        }
        for (String mainFuncSentence : mainFuncSentences) {
            if (mainFuncSentence.contains("\"")) {
                String str = mainFuncSentence.substring(6);
                if (!formatStrings.containsKey(str)) {
                    String formatName = "formatStr" + formatStrings.size();
                    formatStrings.put(str, formatName);
                }
            }
        }
        createDataCode();
        createMainFuncCode();
        createFuncCode();
    }

    public void createDataCode() {
        ArrayList<String> init = new ArrayList<>();
        mipsCode.append(".data").append("\n");
        int k = 0;
        for (String dataSentence : dataSentences) {
            String[] read = dataSentence.split(" ");
            if (read[0].equals("const") || read[0].equals("var")) {
                k = 0;
                if (!init.isEmpty()) {
                    for (String s : init) {
                        mipsCode.append(" ").append(s);
                    }
                    init.clear();
                }
                mipsCode.append("\n");
                if (read[1].equals("arr")) {
                    mipsCode.append("   ").append(read[3].split("\\[")[0]).append(": .word");
                    int num = getArrSize(read[3]);
                    for (int i = 0; i < num; i++) {
                        init.add("0");
                    }
                } else {
                    mipsCode.append("   ").append(read[2].split("\\[")[0]).append(": .word");
                    if (read.length >= 4) {
                        mipsCode.append(" ").append(read[4]);
                    } else {
                        mipsCode.append(" ").append("0");
                    }
                }
            } else {
                init.set(k, read[2]);
                k = k + 1;
            }

        }
        if (!init.isEmpty()) {
            for (String s : init) {
                mipsCode.append(" ").append(s);
            }
            init.clear();
        }
        mipsCode.append("\n");
        for (String formatString : formatStrings.keySet()) {
            mipsCode.append("   ").append(formatStrings.get(formatString));
            mipsCode.append(": .asciiz ").append(formatString).append("\n");
        }
        mipsCode.append("\n");
    }

    public void createMainFuncCode() {
        mipsCode.append(".text").append("\n");
        for (String mainFuncSentence : mainFuncSentences) {
            if (mainFuncSentence.equals("int main()")) {
                mipsCode.append(".main:").append("\n");
            } else {
                String[] read = mainFuncSentence.split(" ");
                intermediateToMips(read, true);
            }
        }
        mipsCode.append("   li $v0, 10\n");
        mipsCode.append("   syscall\n");
        mipsCode.append("\n");
    }

    public void createFuncCode() {
        for (ArrayList<String> func : funcs) {
            HashMap<String, Integer> empty = new HashMap<>();
            HashMap<String, String> emptyMark = new HashMap<>();
            manager.createSymbolTable(0, empty, emptyMark);
            paraNum = 0;
            for (String funcSentence : func) {
                String[] read = funcSentence.split(" ");
                if (funcSentence.contains("()") && !funcSentence.contains("\"")) {
                    String funcName = read[1].split("\\(")[0];
                    totalPara = Integer.parseInt(read[1].split("\\)")[1]);
                    mipsCode.append(funcName).append(":\n");
                    func.remove(funcSentence);
                    break;
                }
            }
            for (String funcSentence : func) {
                String[] read = funcSentence.split(" ");
                intermediateToMips(read, false);
            }
            manager.traceBackSymbolTable();
            mipsCode.append("   jr $ra").append("\n");
            regSs.clear();
        }
        mipsCode.append("\n");
    }

    public void intermediateToMips(String[] read, boolean isMain) {
        if (read[0].equals("const") || read[0].equals("var")) {
            varDef(read);
        } else if (read[0].charAt(0) == '&') {
            operation(read);
        } else if (read[0].equals("write")) {
            StringBuilder str = new StringBuilder(read[1]);
            for (int i = 2; i < read.length; i++) {
                str.append(" ");
                str.append(read[i]);
            }
            write(str.toString());
        } else if (read[0].equals("push")) {
            push(read[1]);
        } else if (read[0].equals("call")) {
            call(read[1]);
        } else if (read[0].equals("ret")) {
            funcReturn(read[1], isMain);
        } else if (read[0].equals("para")) {
            paraDef(read);
        } else if (read[0].equals("clear")) {
            clearRegT();
        } else if (read[0].equals("block")) {
            block(read[1]);
        } else if (read[0].equals("jump")) {
            jumpToLabel(read[1]);
        } else if (read[0].equals("slt") || read[0].equals("sle") ||
                read[0].equals("sgt") || read[0].equals("sge") || read[0].equals("seq")) {
            compareAssign(read);
        } else if (read[0].equals("beq")) {
            compareJump(read);
        } else if (read[1].equals(":")) {
            label(read[0]);
        } else {
            assign(read);
        }
    }

    public void varDef(String[] read) {
        String varName;
        String varInit;
        int varSize;
        if (read[1].equals("int")) {
            varName = read[2];
            manager.addVar(varName);
            manager.nextOffset();
            if (read.length > 3) {
                varInit = read[4];
                varInit = handleVal(varInit, false);
                sw(varInit, -manager.getOffset(varName) + "(" + sp + ")");
                deleteRegT(varInit);
            }
        } else if (read[1].equals("arr")) {
            varName = getArrName(read[3]);
            varSize = getArrSize(read[3]);
            for (int i = 0; i < varSize - 1; i++) {
                manager.nextOffset();
            }
            manager.addVar(varName);
            manager.nextOffset();
        }
    }

    public void paraDef(String[] read) {
        if (read[1].equals("end")) {
            paraNum = 0;
            return;
        }
        String paraName = read[2];
        if (paraName.contains("[]")) {
            String newName = paraName.split("\\[")[0];
            manager.setVar(newName, -12 - 4 * (totalPara - paraNum));
            manager.addMark(newName, "space");
        } else {
            manager.setVar(paraName, -12 - 4 * (totalPara - paraNum));
        }
        paraNum++;
    }

    public void assign(String[] read) {
        String varName;
        String right = read[2];
        if (read[0].contains("[")) {
            String para = getArrPara(read[0]);
            para = handleVal(para, false);
            right = handleVal(right, false);
            varName = getArrName(read[0]);
            handleAssignToArr(varName, para, right);
            deleteRegT(para);
            deleteRegT(right);
            deleteMatch(para);
            deleteMatch(right);
        } else {
            right = handleVal(right, false);
            handleValAssign(read[0], right);
            deleteRegT(right);
            deleteMatch(right);
        }
    }

    public void operation(String[] read) {
        if (read.length == 3) {
            if (read[2].equals("read")) {
                handleRead(read[0]);
            } else if (read[2].equals("RET")) {
                handleBack(read[0]);
            } else {
                handleArrRight(read[0], read[2]);
            }
        } else {
            String left = read[2];
            String right = read[4];
            left = handleVal(left, false);
            String op = read[3];
            if (op.equals("+") || op.equals("-") || op.equals("<")) {
                right = handleVal(right, true);
            } else {
                right = handleVal(right, false);
            }
            deleteRegT(left);
            deleteRegT(right);
            deleteMatch(left);
            deleteMatch(right);
            String mipsTemp = createRegT();
            if (right.charAt(0) == '$') {
                switch (op) {
                    case "+" -> add(mipsTemp, left, right);
                    case "-" -> sub(mipsTemp, left, right);
                    case "*" -> mul(mipsTemp, left, right);
                    case "*h" -> mulh(mipsTemp, left, right);
                    case "/" -> div(mipsTemp, left, right);
                    case "%" -> mod(mipsTemp, left, right);
                }
            } else {
                if (op.equals("-")) {
                    int intRight = -Integer.parseInt(right);
                    right = String.valueOf(intRight);
                    addi(mipsTemp, left, right);
                } else {
                    switch (op) {
                        case "+" -> addi(mipsTemp, left, right);
                        case "*" -> mul(mipsTemp, left, right);
                        case "<" -> sll(mipsTemp, left, right);
                        case ">" -> srl(mipsTemp, left, right);
                        case ">1" -> sra(mipsTemp, left, right);
                    }
                }
            }
            addMatch(mipsTemp, read[0]);
        }
    }

    public void write(String para) {
        if (isDigit(para)) {
            li(a0, para);
            mipsCode.append("   li $v0, 1").append("\n");
        } else if (manager.contains(para)) {
            int offset = -manager.getOffset(para); //*******************88888
            lw(a0, offset + "(" + sp + ")");
            mipsCode.append("   li $v0, 1").append("\n");
        } else if (para.charAt(0) == '&') {
            String temp = findTemp(para);
            move(a0, temp);
            mipsCode.append("   li $v0, 1").append("\n");
        } else if (formatStrings.containsKey(para)) {
            la(a0, formatStrings.get(para));
            mipsCode.append("   li $v0, 4").append("\n");

        } else {
            lw(a0, para);
            regTs.remove(para);
            mipsCode.append("   li $v0, 1").append("\n");
        }
        mipsCode.append("   syscall").append("\n");
    }

    public void push(String para) {
        if (para.contains("[]")) {
            para = para.split("\\[")[0];
            String regT = createRegT();
            if (manager.contains(para)) {
                String arrOffset = String.valueOf(manager.getOffset(para));
                if (manager.containsMark(para)) {
                    subi(regT, sp, arrOffset);
                    lw(regT, "(" + regT + ")");
                } else {
                    subi(regT, sp, arrOffset);
                }
            } else {
                la(regT, para);
            }
            sw(regT, -manager.getCurOffset() + "(" + sp + ")");
            manager.nextOffset();
            deleteRegT(regT);
        } else if (para.contains("[")) {
            String newPara = para.split("\\[")[0];
            String offset1 = para.split("\\[")[1].split("]")[0];
            String offset2 = para.split("\\[")[2].split("]")[0];
            offset1 = handleVal(offset1, false);
            sll(offset1, offset1, "2");
            mul(offset1, offset1, offset2);
            if (manager.contains(newPara)) {
                String arrOffset = String.valueOf(manager.getOffset(newPara));
                if (manager.containsMark(newPara)) {
                    String regT = createRegT();
                    subi(regT, sp, arrOffset);
                    lw(regT, "(" + regT + ")");
                    add(offset1, regT, offset1);
                    deleteRegT(regT);
                } else {
                    subi(offset1, offset1, arrOffset);
                    add(offset1, sp, offset1);
                }
            } else {
                String reg = createRegT();
                la(reg, newPara);
                add(offset1, reg, offset1);
                deleteRegT(reg);
            }
            sw(offset1, -manager.getCurOffset() + "(" + sp + ")");
            manager.nextOffset();
            deleteRegT(offset1);
        } else {
            String temp = handleVal(para, false);
            sw(temp, -manager.getCurOffset() + "(" + sp + ")");
            manager.nextOffset();
            deleteRegT(temp);
        }
    }

    public void call(String name) {
        sw(ra, -manager.getCurOffset() + "(" + sp + ")");
        manager.nextOffset();
        for (int i = 0; i <= 0; i++) {
            sw("$t" + i, -manager.getCurOffset() + "(" + sp + ")");
            manager.nextOffset();
        }
        sw(sp, -manager.getCurOffset() + "(" + sp + ")");
        manager.nextOffset();
        subi(sp, sp, String.valueOf(manager.getCurOffset()));
        mipsCode.append("   ").append("jal ").append(name).append("\n");
        addi(sp, sp, "4");
        lw(sp, "(" + sp + ")");
        manager.preOffset();
        for (int i = 0; i >= 0; i--) {
            manager.preOffset();
            lw("$t" + i, -manager.getCurOffset() + "(" + sp + ")");
        }
        manager.preOffset();
        lw(ra, -manager.getCurOffset() + "(" + sp + ")");
    }

    public void funcReturn(String result, boolean isMain) {
        if (!isMain) {
            String returnTemp = handleVal(result, true);
            if (returnTemp.charAt(0) == '$') {
                move(v0, returnTemp);
            } else {
                li(v0, returnTemp);
            }
            mipsCode.append("   jr $ra").append("\n");
        }
    }

    public void block(String read) {
        if (read.equals("begin")) {
            manager.createSymbolTable(manager.getCurOffset(), manager.getOffsetList(), manager.getMaskList());
        } else if (read.equals("end")) {
            manager.traceBackSymbolTable();
        }
    }

    public void label(String labelName) {
        mipsCode.append("   ").append(labelName).append(":\n");
    }

    public void jumpToLabel(String labelName) {
        j(labelName);
    }

    public void compareAssign(String[] read) {
        String left = handleVal(read[2], false);
        String right;
        if (isDigit(read[3])) {
            int num = Integer.parseInt(read[3]);
            if (num < 32768 && num >=-32768) {
                right = handleVal(read[3], true);
            } else {
                right = handleVal(read[3], false);
            }
        } else {
            right = handleVal(read[3], false);
        }
        String result;
        if (match.containsKey(s5)) {
            addMatch(s6, read[1]);
            result = s6;
        } else {
            addMatch(s5, read[1]);
            result = s5;
        }
        if (right.charAt(0) == '$') {
            switch (read[0]) {
                case "sle" -> sle(result, left, right);
                case "slt" -> slt(result, left, right);
                case "sge" -> sge(result, left, right);
                case "sgt" -> sgt(result, left, right);
                case "seq" -> seq(result, left, right);
                case "sne" -> sne(result, left, right);
                default -> throw new IllegalStateException("Unexpected value: " + read[0]);
            }
        } else {
            switch (read[0]) {
                case "sle" -> sle(result, left, right);
                case "slt" -> slti(result, left, right);
                case "sge" -> sge(result, left, right);
                case "sgt" -> sgt(result, left, right);
                case "seq" -> seq(result, left, right);
                case "sne" -> sne(result, left, right);
                default -> throw new IllegalStateException("Unexpected value: " + read[0]);
            }
        }
        deleteRegT(left);
        deleteRegT(right);
    }

    public void compareJump(String[] read) {
        beq(s5, read[2], read[3]);
        deleteMatch(s5);
    }

    public String getArrName(String name) {
        return name.split("\\[")[0];
    }

    public boolean isDigit(String name) {
        if (name.charAt(0) == '-' || name.charAt(0) == '+') {
            name = name.substring(1);
        }
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isDigit(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public int getArrSize(String name) {
        String step1 = name.split("\\[")[1];
        String step2 = step1.split("]")[0];
        return Integer.parseInt(step2);
    }

    public String getArrPara(String name) {
        String step1 = name.split("\\[")[1];
        String step2 = step1.split("]")[0];
        return step2;
    }

    public void handleArrRight(String imTemp, String arr) {
        String arrName = getArrName(arr);
        String para = getArrPara(arr);
        String regT = createRegT();
        para = handleVal(para, false);
        sll(para, para, "2");
        if (manager.contains(arrName)) {
            String arrOffset = String.valueOf(manager.getOffset(arrName));
            if (manager.containsMark(arrName)) {
                subi(regT, sp, arrOffset);
                lw(regT, "(" + regT + ")");
                add(para, regT, para);
            } else {
                subi(para, para, arrOffset);
                add(para, sp, para);
            }
            lw(regT, "(" + para + ")");
        } else {
            lw(regT, arrName + "(" + para + ")");
        }
        deleteMatch(para);
        deleteRegT(para);
        addMatch(regT, imTemp);
    }


    public String handleVal(String para, boolean imm) {
        if (isDigit(para)) {
            if (!imm) {
                String temp = createRegT();
                li(temp, para);
                return temp;
            } else {
                return para;
            }
        } else if (manager.contains(para)) {
            int offset = -manager.getOffset(para);
            String temp = createRegT();
            lw(temp, offset + "(" + sp + ")");
            return temp;
        } else if (para.charAt(0) != '&') {
            String temp = createRegT();
            lw(temp, para);
            return temp;
        } else {
            String temp = findTemp(para);
            deleteMatch(temp);
            return temp;
        }
    }

    public void handleAssignToArr(String val, String offset, String right) {
        sll(offset, offset, "2");
        if (manager.contains(val)) {
            String arrOffset = String.valueOf(manager.getOffset(val));
            if (manager.containsMark(val)) {
                String regT = createRegT();
                subi(regT, sp, arrOffset);
                lw(regT, "(" + regT + ")");
                add(offset, regT, offset);
                deleteRegT(regT);
            } else {
                subi(offset, offset, arrOffset);
                add(offset, sp, offset);
            }
            sw(right, "(" + offset + ")");
        } else {
            sw(right, val + "(" + offset + ")");
        }
    }

    public void handleValAssign(String val, String right) {
        if (manager.contains(val)) {
            int valOffset = -manager.getOffset(val);
            sw(right, valOffset + "(" + sp + ")");
        } else {
            sw(right, val);
        }
    }

    public void handleRead(String inTemp) {
        String newTemp = createRegT();
        mipsCode.append("   li $v0, 5").append("\n");
        mipsCode.append("   syscall").append("\n");
        move(newTemp, v0);
        addMatch(newTemp, inTemp);
    }

    public void handleBack(String inTemp) {
        String newTemp = createRegT();
        move(newTemp, v0);
        addMatch(newTemp, inTemp);
    }


    public void sw(String stored, String addr) {
        mipsCode.append("   ").append("sw ").append(stored).append(", ");
        mipsCode.append(addr).append("\n");
    }

    public void lw(String store, String addr) {
        mipsCode.append("   ").append("lw ").append(store).append(", ");
        mipsCode.append(addr).append("\n");
    }

    public void sll(String result, String input, String length) {
        mipsCode.append("   ").append("sll ").append(result).append(", ");
        mipsCode.append(input).append(", ").append(length).append("\n");
    }

    public void srl(String result, String input, String length) {
        mipsCode.append("   ").append("srl ").append(result).append(", ");
        mipsCode.append(input).append(", ").append(length).append("\n");
    }

    public void sra(String result, String input, String length) {
        mipsCode.append("   ").append("sra ").append(result).append(", ");
        mipsCode.append(input).append(", ").append(length).append("\n");
        mipsCode.append("   bge ").append(result).append(", $0, div").append(divNum).append("\n");
        addi(result, result, "1");
        String labelName = "div" + divNum++;
        label(labelName);
    }

    public void add(String result, String input1, String input2) {
        mipsCode.append("   ").append("addu ").append(result).append(", ");
        mipsCode.append(input1).append(", ").append(input2).append("\n");
    }

    public void addi(String result, String input1, String input2) {
        mipsCode.append("   ").append("addiu ").append(result).append(", ");
        mipsCode.append(input1).append(", ").append(input2).append("\n");
    }

    public void sub(String result, String input1, String input2) {
        mipsCode.append("   ").append("subu ").append(result).append(", ");
        mipsCode.append(input1).append(", ").append(input2).append("\n");
    }

    public void subi(String result, String input1, String input2) {
        mipsCode.append("   ").append("subiu ").append(result).append(", ");
        mipsCode.append(input1).append(", ").append(input2).append("\n");
    }

    public void mul(String result, String input1, String input2) {
        mipsCode.append("   ").append("mul ").append(result).append(", ");
        mipsCode.append(input1).append(", ").append(input2).append("\n");
    }

    public void mulh(String result, String input1, String input2) {
        mipsCode.append("   ").append("mult ").append(input1).append(", ").append(input2).append("\n");
        mipsCode.append("   ").append("mfhi ").append(result).append("\n");
        mipsCode.append("   ").append("mflo ").append(input2).append("\n");
        sll(result, result, "2");
        srl(input2, input2, "30");
        add(result, result, input2);
    }

    public void div(String result, String input1, String input2) {
        mipsCode.append("   ").append("div ").append(input1).append(", ").append(input2).append("\n");
        mipsCode.append("   ").append("mflo ").append(result).append("\n");
    }

    public void mod(String result, String input1, String input2) {
        mipsCode.append("   ").append("div ").append(input1).append(", ").append(input2).append("\n");
        mipsCode.append("   ").append("mfhi ").append(result).append("\n");
    }

    public void li(String temp, String imm) {
        mipsCode.append("   ").append("li ").append(temp).append(", ").append(imm).append("\n");
    }

    public void move(String left, String right) {
        mipsCode.append("   ").append("move ").append(left).append(", ").append(right).append("\n");
    }

    public void la(String left, String right) {
        mipsCode.append("   ").append("la ").append(left).append(", ").append(right).append("\n");
    }

    public void j(String labelName) {
        mipsCode.append("   ").append("j ").append(labelName).append("\n");
    }

    public void sle(String result, String left, String right) {
        mipsCode.append("   ").append("sle ").append(result).append(", ").append(left).append(", ").append(right);
        mipsCode.append("\n");
    }

    public void slt(String result, String left, String right) {
        mipsCode.append("   ").append("slt ").append(result).append(", ").append(left).append(", ").append(right);
        mipsCode.append("\n");
    }

    public void slti(String result, String left, String right) {
        mipsCode.append("   ").append("slti ").append(result).append(", ").append(left).append(", ").append(right);
        mipsCode.append("\n");
    }

    public void sge(String result, String left, String right) {
        mipsCode.append("   ").append("sge ").append(result).append(", ").append(left).append(", ").append(right);
        mipsCode.append("\n");
    }

    public void sgt(String result, String left, String right) {
        mipsCode.append("   ").append("sgt ").append(result).append(", ").append(left).append(", ").append(right);
        mipsCode.append("\n");
    }

    public void seq(String result, String left, String right) {
        mipsCode.append("   ").append("seq ").append(result).append(", ").append(left).append(", ").append(right);
        mipsCode.append("\n");
    }

    public void sne(String result, String left, String right) {
        mipsCode.append("   ").append("sne ").append(result).append(", ").append(left).append(", ").append(right);
        mipsCode.append("\n");
    }

    public void beq(String left, String right, String labelNum) {
        mipsCode.append("   ").append("beq ").append(left).append(", ").append(right).append(", ").append(labelNum);
        mipsCode.append("\n");
    }

    public String getMipsCode() {
        return mipsCode.toString();
    }
}
