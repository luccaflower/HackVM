package io.github.luccaflower.hack;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface VMInstruction {

    record Pop() {
        @Override
        public String toString() {
            return """
                    @SP
                    AM=M-1
                    D=M
                    """;
        }
    }

    record Push() {
        @Override
        public String toString() {
            return """
                    @SP
                    A=M
                    M=D
                    @SP
                    M=M+1
                    """;
        }
    }
    record Label(String name) implements VMInstruction {
        @Override
        public String toString() {
            return """
                    (%s)
                    """.formatted(name);
        }
    }

    record GoTo(String name) implements VMInstruction {
        @Override
        public String toString() {
            return """
                    @%s
                    0;JMP
                    """.formatted(name);
        }
    }

    record IfGoTo(String name) implements VMInstruction {
        @Override
        public String toString() {
            return """
                    %s
                    @%s
                    D;JNE
                    """.formatted(new Pop(), name);
        }
    }
    record PushConstant(short val) implements VMInstruction {
        @Override
        public String toString() {
            return """
                    @%d
                    D=A
                    %s
                    """.formatted(val, new Push());
        }
    }

    record PushSegment(Segment segment, short val) implements VMInstruction {
        @Override
        public String toString() {
            return """
                    @%d
                    D=A
                    @%s
                    A=M+D
                    D=M
                    %s
                    """.formatted(val, segment, new Push());
        }
    }

    record PopSegment(Segment segment, short val) implements VMInstruction {
        @Override
        public String toString() {
            return """
                    @%d
                    D=A
                    @%s
                    A=M+D
                    D=A
                    @R13
                    M=D
                    %s
                    @R13
                    A=M
                    M=D
                    """.formatted(val, segment, new Pop());
        }
    }

    enum Segment {
        LCL,
        ARG,
        THIS,
        THAT;
        static Segment from(String name) {
            return switch (name.strip()) {
                case "local" -> LCL;
                case "argument" -> ARG;
                case "this" -> THIS;
                case "that" -> THAT;
                default -> throw new IllegalStateException("Unexpected value: " + name);
            };
        }
    }

    record PushTemp(short val) implements VMInstruction {
        @Override
        public String toString() {
            return """
                    @R%d
                    D=M
                    %s
                    """.formatted(5+val, new Push());
        }
    }

    record PopTemp(short val) implements VMInstruction {
        @Override
        public String toString() {
            return """
                    %s
                    @R%d
                    M=D
                    """.formatted(new Pop(), 5+val);
        }
    }
    record PushStatic(String name, short val) implements VMInstruction {
        @Override
        public String toString() {
            return """
                    @%s.%d
                    D=M
                    %s
                    """.formatted(name, val, new Push());
        }
    }

    record PopStatic(String name, short val) implements VMInstruction {
        @Override
        public String toString() {
            return """
                    %s
                    @%s.%d
                    M=D
                    """.formatted(new Pop(), name, val);
        }
    }

    record PushPointer(short val) implements VMInstruction {

        @Override
        public String toString() {
            return """
                    @%d
                    D=M
                    %s
                    """.formatted(3+val, new Push());
        }
    }

    record PopPointer(short val) implements VMInstruction {
        @Override
        public String toString() {
            return """
                    %s
                    @%d
                    M=D
                    """.formatted(new Pop(), 3+val);
        }
    }

    record Add() implements VMInstruction {
        @Override
        public String toString() {
            return """
                    %s
                    @SP
                    AM=M-1
                    M=M+D
                    @SP
                    M=M+1
                    """.formatted(new Pop());
        }
    }

    record Subtract() implements VMInstruction {
        @Override
        public String toString() {
            return """
                    %s
                    @SP
                    AM=M-1
                    M=M-D
                    @SP
                    M=M+1
                    """.formatted(new Pop());
        }
    }

    record Negative() implements VMInstruction {
        @Override
        public String toString() {
            return """
                    @SP
                    AM=M-1
                    D=!M
                    M=D+1
                    @SP
                    M=M+1
                    """;
        }
    }

    record And() implements VMInstruction {
        @Override
        public String toString() {
            return """
                    %s
                    @SP
                    AM=M-1
                    M=M&D
                    @SP
                    M=M+1
                    """.formatted(new Pop());
        }
    }

    record Or() implements VMInstruction {
        @Override
        public String toString() {
            return """
                    %s
                    @SP
                    AM=M-1
                    M=M|D
                    @SP
                    M=M+1
                    """.formatted(new Pop());
        }
    }

    record Equal(int count) implements VMInstruction{
        @Override
        public String toString() {
            return """
                    %s
                    @R13
                    M=D
                    %s
                    @R13
                    D=D-M
                    @EQUAL_{COUNT}
                    D;JEQ
                    @NOT_EQUAL_{COUNT}
                    0;JMP
                    (EQUAL_{COUNT})
                    D=-1
                    @END_EQUAL_{COUNT}
                    0;JMP
                    (NOT_EQUAL_{COUNT})
                    D=0
                    (END_EQUAL_{COUNT})
                    %s
                    """.replace("{COUNT}", String.valueOf(count))
                    .formatted(new Pop(), new Pop(), new Push());
        }
    }
    record GreaterThan(int count) implements VMInstruction{
        @Override
        public String toString() {
            return """
                    %s
                    @R13
                    M=D
                    %s
                    @R13
                    D=D-M
                    @GT_{COUNT}
                    D;JGT
                    @NOT_GT_{COUNT}
                    0;JMP
                    (GT_{COUNT})
                    D=-1
                    @END_GT_{COUNT}
                    0;JMP
                    (NOT_GT_{COUNT})
                    D=0
                    (END_GT_{COUNT})
                    %s
                    """.replace("{COUNT}", String.valueOf(count))
                    .formatted(new Pop(), new Pop(), new Push());
        }
    }
    record LessThan(int count) implements VMInstruction{
        @Override
        public String toString() {
            return """
                    %s
                    @R13
                    M=D
                    %s
                    @R13
                    D=D-M
                    @LT_{COUNT}
                    D;JLT
                    @NOT_LT_{COUNT}
                    0;JMP
                    (LT_{COUNT})
                    D=-1
                    @END_LT_{COUNT}
                    0;JMP
                    (NOT_LT_{COUNT})
                    D=0
                    (END_LT_{COUNT})
                    %s
                    """.replace("{COUNT}", String.valueOf(count))
                    .formatted(new Pop(), new Pop(), new Push());
        }
    }
    record Not() implements VMInstruction {
        @Override
        public String toString() {
            return """
                    @SP
                    AM=M-1
                    M=!M
                    @SP
                    M=M+1
                    """;
        }
    }

    record DefineFunction(String name, int locals) implements VMInstruction {
        @Override
        public String toString() {
            var label = new Label(name);
            return label.toString().concat("\n")
                    .concat(IntStream.range(0, locals())
                            .mapToObj(i -> List.of(new PushConstant((short)0), new PopSegment(Segment.LCL, (short) i)))
                            .flatMap(Collection::stream)
                            .map(VMInstruction::toString)
                            .collect(Collectors.joining("\n")));
        }
    }
    record Null() implements VMInstruction {
        @Override
        public String toString() {
            return "";
        }
    }
}
