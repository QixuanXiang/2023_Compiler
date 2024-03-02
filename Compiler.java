import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Compiler {
    public static void main(String[] args) throws IOException {
        String inputFileName = "testfile.txt";
        String outputFileName = "output.txt";
        String errorFIleName = "error.txt";
        String codeFileName = "code.txt";
        String mipsFileName = "mips.txt";
        Scanner inputFile = new Scanner(new FileReader(inputFileName));
        //FileWriter outputFile = new FileWriter(outputFileName);
        FileWriter errorFile = new FileWriter(errorFIleName);
        FileWriter codeFile = new FileWriter(codeFileName);
        FileWriter mipsFile = new FileWriter(mipsFileName);
        StringBuilder input = new StringBuilder();
        while (inputFile.hasNext()) {
            input.append(inputFile.nextLine());
            input.append("\n");
        }
        Lexer lexer_E = new Lexer(input.toString());
        Parser parser = new Parser(lexer_E);
        //outputFile.write(parser.getOutput());
        //outputFile.flush();
        errorFile.write(parser.getError());
        errorFile.flush();

        if (parser.getError().isEmpty()) {
            Lexer_G lexer = new Lexer_G(input.toString());
            new Parser_G(lexer);
            codeFile.write(IntermediateCode.getInstance().getIntermediateCode());
            codeFile.flush();
            MipsCode mipsCode = new MipsCode(IntermediateCode.getInstance().getIntermediateCode());
            mipsFile.write(mipsCode.getMipsCode());
            mipsFile.flush();
        }
        //outputFile.write(parser.getOutput());
        //outputFile.flush();
        //errorFile.write(parser.getError());
        //errorFile.flush();
    }
}