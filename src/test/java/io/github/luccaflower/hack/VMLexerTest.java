package io.github.luccaflower.hack;

import org.junit.jupiter.api.Test;

import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;

class VMLexerTest {

    Lexer<Queue<VMInstruction>> lexer = VMLexer.LEXER;
    @Test
    void parsePushConst() throws Lexer.ParseException {
        assertThat(lexer.parse("push constant 1")).first().isEqualTo(new VMInstruction.PushConstant((short) 1));
    }

}