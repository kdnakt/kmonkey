package lexer

import token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class LexerTest {

    @Test
    fun testNextToken() {
        val input = "=+(){},;"
        val tests = mapOf(
            TokenType.ASSIGN to "=",
            TokenType.PLUS to "+",
            TokenType.LPAREN to "(",
            TokenType.RPAREN to ")",
            TokenType.LBRACE to "{",
            TokenType.RBRACE to "}",
            TokenType.COMMA to ",",
            TokenType.SEMICOLON to ";"
        )
        val lexer = Lexer(input)

        for (test in tests) {
            val token = lexer.nextToken()
            assertEquals(test.key, token.tokenType,
                "Wrong tokenType")
            assertEquals(test.value, token.literal,
                "Wrong literal")
        }
    }

}