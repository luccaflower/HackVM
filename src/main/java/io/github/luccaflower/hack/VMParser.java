package io.github.luccaflower.hack;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import static io.github.luccaflower.hack.Lexer.*;
import static io.github.luccaflower.hack.Lexer.string;

public class VMParser implements Lexer<Queue<VMInstruction>> {
    public static final String LABEL_PATTERN = "[a-zA-Z_\\-0-9]+";
    private int eqCount = 0;
    private int ltCount = 0;
    private int gtCount = 0;
    private FunctionState functionState = new NoFunction();
    private final Lexer<Queue<VMInstruction>> lexer;
    private final Map<String, Integer> returnLabels = new HashMap<>();
    public VMParser(String name) {
        var comment = regex("\\s*").skipAnd(string("//")).skipAnd(regex(".*").andSkip(eol()))
                .<VMInstruction>map(ignored -> new VMInstruction.Null());
        lexer = string("push ").skipAnd(string("constant ").skipAnd(number())
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
                .or(string("label ")
                        .skipAnd(regex(LABEL_PATTERN))
                        .map(n -> functionState.toString().concat(n))
                        .map(VMInstruction.Label::new))
                .or(string("goto ")
                        .skipAnd(regex(LABEL_PATTERN))
                        .map(n -> functionState.toString().concat(n))
                        .map(VMInstruction.GoTo::new))
                .or(string("if-goto ")
                        .skipAnd(regex(LABEL_PATTERN))
                        .map(n -> functionState.toString().concat(n))
                        .map(VMInstruction.IfGoTo::new))
                .or(string("function ")
                        .skipAnd(regex(LABEL_PATTERN).andSkip(string(" ")))
                        .andThen(number())
                        .map(p -> {
                            this.functionState = this.functionState.define(name, p.left());
                            return new VMInstruction.DefineFunction(p.left(), p.right());
                        }))
                .or(string("call ")
                        .skipAnd(regex(LABEL_PATTERN).andSkip(string(" ")))
                        .andThen(number())
                        .map(p -> {
                            var count = returnLabels.merge(p.left(), 0, (n, c) -> c + 1);
                            return new VMInstruction.CallFunction(p.left(), p.right(), "%s.ret.%d".formatted(p.left(), count));
                        }))
                .or(string("return")
                        .map(ignored -> {
                            functionState = functionState.doReturn();
                            return new VMInstruction.Return();
                        }))
                .andSkip(eol().or(comment.map(VMInstruction::toString)))
                .or(comment)
                .repeating()
                .andSkip(eof());
    }

    @Override
    public Parsed<Queue<VMInstruction>> tryParse(CharSequence in) throws ParseException {
        var stripped = in.toString()
                .replace("\t", "")
                .lines()
                .filter(l -> !l.isBlank())
                .map(String::strip)
                .collect(Collectors.joining("\n"));
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

    private record NoFunction() implements FunctionState {
        @Override
        public FunctionState define(String file, String name) {
            return new Function(file, name);
        }

        @Override
        public FunctionState doReturn() {
            throw new IllegalStateException("return without function not allowed");
        }

        @Override
        public String toString() {
            return "";
        }

    }
    private record Function(String file, String name) implements FunctionState {
        @Override
        public FunctionState define(String file, String name) {
            throw new IllegalStateException("Cannot nest functions");
        }

        @Override
        public FunctionState doReturn() {
            return new NoFunction();
        }

        @Override
        public String toString() {
            return "%s.%s$".formatted(file, name);
        }

    }

    private interface FunctionState {
        FunctionState define(String file, String name);
        FunctionState doReturn();
    }

}
