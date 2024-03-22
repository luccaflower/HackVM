package io.github.luccaflower.hack;

import java.util.Queue;

public class VMLexer implements Lexer<Queue<VMInstruction>> {
    private int eqCount = 0;
    private int ltCount = 0;
    private int gtCount = 0;
    public VMLexer() {}

    private final Lexer<VMInstruction> comment = Lexer.regex("\\s*").skipAnd(Lexer.string("//")).skipAnd(Lexer.regex(".*")).andSkip(Lexer.eol())
            .map(ignored -> new VMInstruction.Null());
    private final Lexer<VMInstruction> pushConstant = Lexer.string("push ")
            .skipAnd(Lexer.string("constant ").skipAnd(Lexer.number()).andSkip(Lexer.eol().or(comment.map(VMInstruction::toString)))
                    .map(VMInstruction.PushConstant::new));
    private final Lexer<VMInstruction> pushSegment = Lexer.string("push ")
            .skipAnd(Lexer.regex("[a-z]+\\s"))
            .andSkip(Lexer.eol())
            .map(VMInstruction.Segment::from)
            .andThen(Lexer.number())
            .andSkip(Lexer.eol().or(comment.map(VMInstruction::toString)))
            .map(p -> new VMInstruction.PushSegment(p.left(), p.right()));

    private final Lexer<VMInstruction> popSegment = Lexer.string("pop")
            .skipAnd(Lexer.regex("[a-z]+\\s"))
            .andSkip(Lexer.eol())
            .map(VMInstruction.Segment::from)
            .andThen(Lexer.number())
            .andSkip(Lexer.eol().or(comment.map(VMInstruction::toString)))
            .map(p -> new VMInstruction.PopSegment(p.left(), p.right()));

    private final Lexer<VMInstruction> arithmetic = Lexer.regex("\\w{2,3}").andSkip(Lexer.eol().or(comment.map(VMInstruction::toString)))
            .map(this::from);
    private final Lexer<VMInstruction> push = pushConstant
            .or(pushSegment);



    private final Lexer<Queue<VMInstruction>> lexer = push.or(arithmetic).or(comment)
            .repeating()
            .andSkip(Lexer.eof());

    @Override
    public Parsed<Queue<VMInstruction>> tryParse(CharSequence in) throws ParseException {
        var stripped = in.toString().strip();
        return lexer.tryParse(stripped);
    }

    private VMInstruction from(String name) {
        return switch (name) {
            case "add" -> new VMInstruction.Add();
            case "sub" -> new VMInstruction.Subtract();
            case "neg" -> new VMInstruction.Negative();
            case "eq" -> new VMInstruction.Equal(eqCount++);
            case "lt" -> new VMInstruction.LessThan(ltCount++);
            case "gt" -> new VMInstruction.GreaterThan(gtCount++);
            case "and" -> new VMInstruction.And();
            case "or" -> new VMInstruction.Or();
            case "not" -> new VMInstruction.Not();
            default -> throw new IllegalArgumentException("Unexpected value: " + name);
        };
    }
}
