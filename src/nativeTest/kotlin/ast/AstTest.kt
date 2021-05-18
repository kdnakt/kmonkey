package ast

import token.Token
import token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class AstTest {
    @Test
    fun testString() {
        val statements = mutableListOf<Statement>(
                LetStatement(
                        Token(TokenType.LET, "let"),
                        Identifier(
                                Token(TokenType.IDENT, "myVar"),
                                "myVar"
                        ),
                        Identifier(
                                Token(TokenType.IDENT, "anotherVar"),
                                "anotherVar"
                        )
                )
        )
        val program = Program(statements)
        assertEquals("let myVar = anotherVar;", program.string())
    }
}