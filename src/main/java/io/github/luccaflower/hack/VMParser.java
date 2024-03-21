package io.github.luccaflower.hack;

import java.util.Queue;
import java.util.stream.Collectors;

public class VMParser {
    private final Queue<VMInstruction> instructions;
    private static final String init = """
            @256
            D=A
            @SP
            M=D
            """;

    public VMParser(Queue<VMInstruction> instructions) {
        this.instructions = instructions;
    }

    public String toString() {
        return init
                .concat(instructions.stream().map(VMInstruction::toString).collect(Collectors.joining("\n")))
                .lines()
                .filter(l -> !l.isBlank())
                .collect(Collectors.joining("\n"));
    }
}
