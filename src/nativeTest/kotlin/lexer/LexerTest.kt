package lexer

import token.Token
import token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class LexerTest {

    @Test
    fun testNextToken() {
        val input =
        """
            let five = 5;
        """.trimIndent()
        val tests = listOf(
            TokenType.LET to "let",
            TokenType.IDENT to "five",
            TokenType.ASSIGN to "=",
            TokenType.INT to "5",
            TokenType.SEMICOLON to ";"
        )
        val lexer = Lexer(input)

        for (test in tests) {
            val token = lexer.nextToken()
            assertEquals(
                Token(test.first, test.second), token)
        }
    }

}