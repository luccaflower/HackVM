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
        Lexer<VMInstruction> comment = regex("\\s*").skipAnd(string("//")).skipAnd(regex(".*")).andSkip(eol())
                .map(ignored -> new VMInstruction.Null());
        Lexer<VMInstruction> pushConstant = string("push ")
                .skipAnd(string("constant ")
                        .skipAnd(number())
                        .andSkip(eol().or(comment.map(VMInstruction::toString)))
                        .map(VMInstruction.PushConstant::new));
        Lexer<VMInstruction> pushSegment = string("push ")
                .skipAnd(regex("[a-z]+\\s"))
                .map(VMInstruction.Segment::from)
                .andThen(number())
                .andSkip(eol().or(comment.map(VMInstruction::toString)))
                .map(p -> new VMInstruction.PushSegment(p.left(), p.right()));
        Lexer<VMInstruction> pushTemp = string("push ")
                .skipAnd(string("temp "))
                .skipAnd(number())
                .andSkip(eol().or(comment.map(VMInstruction::toString)))
                .map(VMInstruction.PushTemp::new);
        Lexer<VMInstruction> pushStatic = string("push ")
                .skipAnd(string("static "))
                .skipAnd(number())
                .andSkip(eol().or(comment.map(VMInstruction::toString)))
                .map(i -> new VMInstruction.PushStatic(name, i));
        Lexer<VMInstruction> pushPointer = string("push ")
                .skipAnd(string("pointer "))
                .skipAnd(number())
                .andSkip(eol().or(comment.map(VMInstruction::toString)))
                .map(VMInstruction.PushPointer::new);
        Lexer<VMInstruction> push = pushConstant.or(pushSegment).or(pushTemp).or(pushStatic).or(pushPointer);
        Lexer<VMInstruction> popSegment = string("pop ")
                .skipAnd(regex("[a-z]+\\s"))
                .map(VMInstruction.Segment::from)
                .andThen(number())
                .andSkip(eol().or(comment.map(VMInstruction::toString)))
                .map(p -> new VMInstruction.PopSegment(p.left(), p.right()));
        Lexer<VMInstruction> popTemp = string("pop ")
                .skipAnd(string("temp "))
                .skipAnd(number())
                .andSkip(eol().or(comment.map(VMInstruction::toString)))
                .map(VMInstruction.PopTemp::new);
        Lexer<VMInstruction> popStatic = string("pop ")
                .skipAnd(string("static "))
                .skipAnd(number())
                .andSkip(eol().or(comment.map(VMInstruction::toString)))
                .map(i -> new VMInstruction.PopStatic(name, i));
        Lexer<VMInstruction> popPointer = string("pop ")
                .skipAnd(string("pointer "))
                .skipAnd(number())
                .andSkip(eol().or(comment.map(VMInstruction::toString)))
                .map(VMInstruction.PopPointer::new);
        Lexer<VMInstruction> pop = popSegment.or(popTemp).or(popStatic).or(popPointer);
        Lexer<VMInstruction> arithmetic = regex("\\w{2,3}")
                .andSkip(eol().or(comment.map(VMInstruction::toString)))
                .map(this::from);
        lexer = push.or(pop).or(arithmetic).or(comment)
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
