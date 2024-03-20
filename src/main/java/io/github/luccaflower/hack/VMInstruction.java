package io.github.luccaflower.hack;

public sealed interface VMInstruction permits VMInstruction.Null, VMInstruction.PushConstant, VMInstruction.PushSegment,
        VMInstruction.Add {
    record PushConstant(short val) implements VMInstruction {
        @Override
        public String toString() {
            return """
                    @%d
                    D=A
                    @SP
                    A=M
                    M=D
                    @SP
                    M=M+1
                    """.formatted(val);
        }
    }

    record PushSegment(Segment segment, short val) implements VMInstruction {
        @Override
        public String toString() {
            return """
                    @%d
                    D=A
                    @%s
                    A=A+D
                    D=M
                    @SP
                    A=M
                    M=D
                    @SP
                    M=M-1
                    """.formatted(val, segment);
        }
    }

    record PopSegment(Segment segment, short val) {
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
                    @SP
                    A=M
                    D=M
                    @SP
                    M=M-1
                    @R13
                    A=M
                    M=D
                    """.formatted(val, segment);
        }
    }
    record Add() implements VMInstruction {
        @Override
        public String toString() {
            return """
                    @SP
                    AM=M-1
                    D=M
                    @SP
                    AM=M-1
                    M=M+D
                    @SP
                    M=M+1
                    """;
        }
    }

    record Subtract() {
        @Override
        public String toString() {
            return """
                    @SP
                    AM=M-1
                    D=M
                    @SP
                    AM=M-1
                    M=M-D
                    @SP
                    M=M+1
                    """;
        }
    }

    record Negative() {
        @Override
        public String toString() {
            return """
                    @SP
                    AM=M-1
                    D=0-M
                    """;
        }
    }

    record And() {
        @Override
        public String toString() {
            return """
                    @SP
                    AM=M-1
                    D=M
                    @SP
                    AM=M-1
                    M=M&D
                    """;
        }
    }

    record Or() {
        @Override
        public String toString() {
            return """
                    @SP
                    AM=M-1
                    D=M
                    @SP
                    AM=M-1
                    M=M|D
                    """;
        }
    }

    record Equal() {
        @Override
        public String toString() {
            return """
                    @SP
                    AM=M-1
                    D=M
                    @R13
                    M=D
                    @SP
                    AM=M-1
                    @R13
                    D=M-D
                    @EQUAL
                    D;JEQ
                    @NOT_EQUAL
                    0;JMP
                    (EQUAL)
                    D=-1
                    @END_EQUAL
                    0;JMP
                    (NOT_EQUAL)
                    D=0
                    (END_EQUAL)
                    """;
        }
    }
    record Null() implements VMInstruction {
        @Override
        public String toString() {
            return "";
        }
    }
    enum Segment {
        LCL;
        static Segment from(String name) {
            return switch (name) {
                case "local" -> LCL;
                default -> throw new IllegalStateException("Unexpected value: " + name);
            };
        }
    }
}
