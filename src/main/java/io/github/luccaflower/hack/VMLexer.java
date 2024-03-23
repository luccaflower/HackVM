package io.github.luccaflower.hack;

import java.util.Queue;

import static io.github.luccaflower.hack.Lexer.*;
import static io.github.luccaflower.hack.Lexer.string;

public class VMLexer implements Lexer<Queue<VMInstruction>> {
    private int eqCount = 0;
    private int ltCount = 0;
    private int gtCount = 0;
    private final Lexer<Queue<VMInstruction>> lexer;
    public VMLexer(String name) {
        var comment = regex("\\s*").skipAnd(string("//")).skipAnd(regex(".*")).andSkip(eol())
                .<VMInstruction>map(ignored -> new VMInstruction.Null());
        var pushConstant = string("constant ")
                .skipAnd(number())
                .<VMInstruction>map(VMInstruction.PushConstant::new);
        var pushSegment = regex("[a-z]+\\s")
                .map(VMInstruction.Segment::from)
                .andThen(number())
                .<VMInstruction>map(p -> new VMInstruction.PushSegment(p.left(), p.right()));
        var pushTemp = string("temp ")
                .skipAnd(number())
                .<VMInstruction>map(VMInstruction.PushTemp::new);
        var pushStatic = string("push ")
                .skipAnd(string("static "))
                .skipAnd(number())
                .<VMInstruction>map(i -> new VMInstruction.PushStatic(name, i));
        var pushPointer = string("pointer ")
                .skipAnd(number())
                .<VMInstruction>map(VMInstruction.PushPointer::new);
        var push = string("push ").skipAnd(pushConstant.or(pushSegment).or(pushTemp).or(pushStatic).or(pushPointer));
        var popSegment = regex("[a-z]+\\s")
                .map(VMInstruction.Segment::from)
                .andThen(number())
                .<VMInstruction>map(p -> new VMInstruction.PopSegment(p.left(), p.right()));
        var popTemp = string("temp ")
                .skipAnd(number())
                .<VMInstruction>map(VMInstruction.PopTemp::new);
        var popStatic = string("static ")
                .skipAnd(number())
                .<VMInstruction>map(i -> new VMInstruction.PopStatic(name, i));
        var popPointer = string("pointer ")
                .skipAnd(number())
                .<VMInstruction>map(VMInstruction.PopPointer::new);
        var pop = string("pop ").skipAnd(popSegment.or(popTemp).or(popStatic).or(popPointer));
        var arithmetic = regex("\\w{2,3}")
                .map(this::from);
        lexer = push.or(pop).or(arithmetic).andSkip(eol().or(comment.map(VMInstruction::toString))).or(comment)
                .repeating()
                .andSkip(eof());
    }

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
