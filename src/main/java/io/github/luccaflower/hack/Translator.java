package io.github.luccaflower.hack;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Translator {
    public static void main(String[] args) throws IOException {
        String filename;
        if (args.length == 0) {
            filename = ".";
        } else {
            filename = args[0];
        }
        File file = new File(filename);
        String assembly;
        String name;
        if (file.isDirectory()) {
            name = Arrays.stream(file.getCanonicalPath().split(FileSystems.getDefault().getSeparator()))
                    .toList()
                    .getLast();
            var instructions = Arrays.stream(Objects.requireNonNull(file.listFiles()))
                    .filter(f -> f.getName().endsWith(".vm"))
                    .flatMap(f -> parse(f.getName()).stream())
                    .collect(Collectors.toCollection(ArrayDeque::new));
            instructions.addFirst(new VMInstruction.CallFunction("Sys.init", 0, "sysReturn"));
            assembly = new VMCodeWriter(instructions).write();
        } else if (!filename.endsWith(".vm")) {
            throw new IllegalArgumentException("Invalid filename");
        } else {
            name = filename.replace(".vm", "");
            assembly = processSingleFile(filename);
        }
        String outputFilename = name + ".asm";
        File outputFile = new File(outputFilename);
        if (outputFile.exists() && !(outputFile.delete())) {
            throw new IllegalStateException("Failed to overwrite existing hack file");
        }
        try (var output = new FileWriter(outputFilename, false)) {
            output.write(assembly);
        }
    }

    private static String processSingleFile(String filename) {
        String machineCode;
        Queue<VMInstruction> lexed;
        lexed = parse(filename);
        machineCode = new VMCodeWriter(lexed).write();
        return machineCode;
    }

    private static Queue<VMInstruction> parse(String filename) {
        Queue<VMInstruction> lexed;
        try (var input = new BufferedReader(new InputStreamReader(new FileInputStream(filename)))) {
            var vmCode = input.lines().collect(Collectors.joining("\n"));
            lexed = new VMParser(filename.replace(".vm", "")).parse(vmCode);
        } catch (IOException | Lexer.ParseException e) {
            throw new RuntimeException("Failed to process file %s".formatted(filename), e);
        }
        return lexed;
    }


}
