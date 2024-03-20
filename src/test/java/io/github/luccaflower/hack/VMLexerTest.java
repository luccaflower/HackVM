package io.github.luccaflower.hack;

import org.junit.jupiter.api.Test;

import java.util.Queue;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

class VMLexerTest {

    Lexer<Queue<VMInstruction>> lexer = new VMLexer();
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

}