package io.github.luccaflower.hack;

import java.util.Queue;

public final class VMLexer {
    private VMLexer() {}

    private static final Lexer<VMInstruction> PUSH_CONSTANT = Lexer.string("push ")
            .skipAnd(Lexer.string("constant ").skipAnd(Lexer.number()).map(VMInstruction.PushConstant::new));
    private static final Lexer<VMInstruction> PUSH_SEGMENT = Lexer.regex("[a-z]+\\s")
            .map(VMInstruction.Segment::from)
            .andThen(Lexer.number())
            .map(p -> new VMInstruction.PushSegment(p.left(), p.right()));
    private static final Lexer<VMInstruction> PUSH = PUSH_CONSTANT
            .or(PUSH_SEGMENT);


    public static Lexer<Queue<VMInstruction>> LEXER = PUSH
            .repeating()
            .andSkip(Lexer.eof());
}
