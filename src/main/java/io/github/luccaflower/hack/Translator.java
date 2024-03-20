package io.github.luccaflower.hack;

import java.io.*;
import java.util.stream.Collectors;

public class Translator {
    public static void main(String[] args) throws IOException, Lexer.ParseException {
        String filename;
        if (args.length == 0) {
            filename = "Prog.vm";
        } else {
            filename = args[0];
        }
        if (!filename.endsWith(".vm")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        String machineCode;
        try (var input = new BufferedReader(new InputStreamReader(new FileInputStream(filename)))) {
            machineCode = assemble(input);
        }

        String outputFilename = filename.replace(".vm", ".asm");
        File outputFile = new File(outputFilename);
        if (outputFile.exists() && !(outputFile.delete())) {
            throw new IllegalStateException("Failed to overwrite existing hack file");
        }
        try (var output = new FileWriter(outputFilename, false)) {
            output.write(machineCode);
        }
    }

    public static String assemble(BufferedReader input) throws Lexer.ParseException {
        String assembly;
        var vmCode = input.lines().collect(Collectors.joining("\n"));
        var lexed = new VMLexer().parse(vmCode);
        assembly = new VMParser(lexed).toString();
        return assembly;
    }


}
