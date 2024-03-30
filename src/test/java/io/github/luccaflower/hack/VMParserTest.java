package io.github.luccaflower.hack;

import org.junit.jupiter.api.Test;

import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;

class VMParserTest {

    Lexer<Queue<VMInstruction>> lexer = new VMParser("thing");
    @Test
    void parsePushConst() throws Lexer.ParseException {
        assertThat(lexer.parse("push constant 1")).first().isEqualTo(new VMInstruction.PushConstant((short) 1));
    }

    @Test
    void add() throws Lexer.ParseException {
        assertThat(lexer.parse("add")).first().isEqualTo(new VMInstruction.Add());
    }

    @Test
    void comments() throws Lexer.ParseException {
        assertThat(lexer.parse("//stuff")).first().isEqualTo(new VMInstruction.Null());
    }

    @Test
    void commentsAfterInstructions() throws Lexer.ParseException {
        assertThat(lexer.parse("add //stuff")).first().isEqualTo(new VMInstruction.Add());
    }

    @Test
    void labelAfterFunctionPrependsFunctionToLabel() throws Lexer.ParseException {
        var input = """
                function test 2
                label name
                """;
        Queue<VMInstruction> result = lexer.parse(input);
        result.remove();

        assertThat(result.remove()).isEqualTo(new VMInstruction.Label("thing.test$name"));
    }

}