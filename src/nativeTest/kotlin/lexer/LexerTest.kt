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
            let add = fn(x, y) {
              x + y;
            };
            let result = add(five, ten);
        """.trimIndent()
        val tests = listOf(
            TokenType.LET to "let",
            TokenType.IDENT to "five",
            TokenType.ASSIGN to "=",
            TokenType.INT to "5",
            TokenType.SEMICOLON to ";",
            TokenType.LET to "let",
            TokenType.IDENT to "add",
            TokenType.ASSIGN to "=",
            TokenType.FUNCTION to "fn",
            TokenType.LPAREN to "(",
            TokenType.IDENT to "x",
            TokenType.COMMA to ",",
            TokenType.IDENT to "y",
            TokenType.RPAREN to ")",
            TokenType.LBRACE to "{",
            TokenType.IDENT to "x",
            TokenType.PLUS to "+",
            TokenType.IDENT to "y",
            TokenType.SEMICOLON to ";",
            TokenType.RBRACE to "}",
            TokenType.SEMICOLON to ";",
            TokenType.LET to "let",
            TokenType.IDENT to "result",
            TokenType.ASSIGN to "=",
            TokenType.IDENT to "add",
            TokenType.LPAREN to "(",
            TokenType.IDENT to "five",
            TokenType.COMMA to ",",
            TokenType.IDENT to "ten",
            TokenType.RPAREN to ")",
            TokenType.SEMICOLON to ";",
            TokenType.EOF to "",
        )
        val lexer = Lexer(input)

        for (test in tests) {
            val token = lexer.nextToken()
            assertEquals(
                Token(test.first, test.second), token)
        }
    }

}