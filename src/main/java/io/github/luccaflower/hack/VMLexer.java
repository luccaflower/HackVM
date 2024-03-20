package io.github.luccaflower.hack;

import java.util.Queue;

public class VMLexer implements Lexer<Queue<VMInstruction>> {
    public VMLexer() {}

    private static final Lexer<VMInstruction> COMMENT = Lexer.regex("\\s*").skipAnd(Lexer.string("//")).skipAnd(Lexer.regex(".*")).andSkip(Lexer.eol())
            .map(ignored -> new VMInstruction.Null());
    private static final Lexer<VMInstruction> PUSH_CONSTANT = Lexer.string("push ")
            .skipAnd(Lexer.string("constant ").skipAnd(Lexer.number()).andSkip(Lexer.eol().or(COMMENT.map(VMInstruction::toString)))
                    .map(VMInstruction.PushConstant::new));
    private static final Lexer<VMInstruction> PUSH_SEGMENT = Lexer.regex("[a-z]+\\s").andSkip(Lexer.eol())
            .map(VMInstruction.Segment::from)
            .andThen(Lexer.number())
            .andSkip(Lexer.eol().or(COMMENT.map(VMInstruction::toString)))
            .map(p -> new VMInstruction.PushSegment(p.left(), p.right()));

    private static final Lexer<VMInstruction> ADD = Lexer.string("add").andSkip(Lexer.eol().or(COMMENT.map(VMInstruction::toString)))
            .map(ignored -> new VMInstruction.Add());
    private static final Lexer<VMInstruction> PUSH = PUSH_CONSTANT
            .or(PUSH_SEGMENT);


    private static Lexer<Queue<VMInstruction>> LEXER = PUSH.or(ADD).or(COMMENT)
            .repeating()
            .andSkip(Lexer.eof());

    @Override
    public Parsed<Queue<VMInstruction>> tryParse(CharSequence in) throws ParseException {
        var stripped = in.toString().strip();
        return LEXER.tryParse(stripped);
    }
}
