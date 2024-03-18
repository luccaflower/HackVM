package io.github.luccaflower.hack;

public sealed interface VMInstruction permits VMInstruction.PushConstant, VMInstruction.PushSegment,
        VMInstruction.AddConst, VMInstruction.AddVar {
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
    record AddConst(short val) implements VMInstruction {}
    record AddVar(short val) implements VMInstruction {}

    public enum Segment {
        LCL;
        static Segment from(String name) {
            return switch (name) {
                case "local" -> LCL;
                default -> throw new IllegalStateException("Unexpected value: " + name);
            };
        }
    }
}
