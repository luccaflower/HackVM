package io.github.luccaflower.hack;

public sealed interface VMInstruction permits VMInstruction.Add, VMInstruction.And, VMInstruction.Equal, VMInstruction.GreaterThan, VMInstruction.LessThan, VMInstruction.Negative, VMInstruction.Not, VMInstruction.Null, VMInstruction.Or, VMInstruction.PopSegment, VMInstruction.PushConstant, VMInstruction.PushSegment, VMInstruction.Subtract {
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
                    M=M+1
                    """.formatted(val, segment);
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
                    @SP
                    AM=M-1
                    D=M
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

    record Subtract() implements VMInstruction {
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
                    @SP
                    AM=M-1
                    D=M
                    @SP
                    AM=M-1
                    M=M&D
                    @SP
                    M=M+1
                    """;
        }
    }

    record Or() implements VMInstruction {
        @Override
        public String toString() {
            return """
                    @SP
                    AM=M-1
                    D=M
                    @SP
                    AM=M-1
                    M=M|D
                    @SP
                    M=M+1
                    """;
        }
    }

    record Equal(int count) implements VMInstruction{
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
                    D=M
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
                    @SP
                    A=M
                    M=D
                    @SP
                    M=M+1
                    """.replace("{COUNT}", String.valueOf(count));
        }
    }
    record GreaterThan(int count) implements VMInstruction{
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
                    D=M
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
                    @SP
                    A=M
                    M=D
                    @SP
                    M=M+1
                    """.replace("{COUNT}", String.valueOf(count));
        }
    }
    record LessThan(int count) implements VMInstruction{
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
                    D=M
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
                    @SP
                    A=M
                    M=D
                    @SP
                    M=M+1
                    """.replace("{COUNT}", String.valueOf(count));
        }
    }
    record Null() implements VMInstruction {
        @Override
        public String toString() {
            return "";
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
    enum Segment {
        LCL,
        ARG,
        THIS,
        THAT;
        static Segment from(String name) {
            return switch (name) {
                case "local" -> LCL;
                case "argument" -> ARG;
                case "this" -> THIS;
                case "that" -> THAT;
                default -> throw new IllegalStateException("Unexpected value: " + name);
            };
        }
    }

}
