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
        lexer = string("push ").skipAnd(string("constant ")
                .skipAnd(number())
                .<VMInstruction>map(VMInstruction.PushConstant::new)
                .or(regex("[a-z]+\\s")
                        .map(VMInstruction.Segment::from)
                        .andThen(number())
                        .map(p -> new VMInstruction.PushSegment(p.left(), p.right())))
                .or(string("temp ")
                        .skipAnd(number())
                        .map(VMInstruction.PushTemp::new))
                .or(string("static ")
                        .skipAnd(number())
                        .map(i -> new VMInstruction.PushStatic(name, i)))
                .or(string("pointer ")
                        .skipAnd(number())
                        .map(VMInstruction.PushPointer::new)))
                .or(string("pop ").skipAnd(regex("[a-z]+\\s")
                        .map(VMInstruction.Segment::from)
                        .andThen(number())
                        .<VMInstruction>map(p1 -> new VMInstruction.PopSegment(p1.left(), p1.right()))
                        .or(string("temp ")
                                .skipAnd(number())
                                .map(VMInstruction.PopTemp::new))
                        .or(string("static ")
                                .skipAnd(number())
                                .map(i1 -> new VMInstruction.PopStatic(name, i1)))
                        .or(string("pointer ")
                                .skipAnd(number())
                                .map(VMInstruction.PopPointer::new))))
                .or(regex("\\w{2,3}")
                        .map(this::arithmeticFrom))
                .andSkip(eol().or(comment.map(VMInstruction::toString)))
                .or(comment)
                .repeating()
                .andSkip(eof());
    }

    @Override
    public Parsed<Queue<VMInstruction>> tryParse(CharSequence in) throws ParseException {
        var stripped = in.toString().strip();
        return lexer.tryParse(stripped);
    }

    private VMInstruction arithmeticFrom(String name) {
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
