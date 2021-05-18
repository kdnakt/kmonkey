package ast

import token.Token
import token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class ModifyTest {
    @Test
    fun testModify() {
        val token = Token(TokenType.INT, "dummy")
        fun one(): Expression = IntegerLiteral(token, 1)
        fun two(): Expression = IntegerLiteral(token, 2)

        fun turnOneIntoTwo(node: Node?): Node {
            if (node !is IntegerLiteral) {
                return node!!
            }
            if (node.value != 1L) {
                return node
            }
            return IntegerLiteral(node.token, 2)
        }

        val tests = mapOf<Node, Node>(
            one() to two(),
            Program(mutableListOf(ExpressionStatement(token, one()))) to Program(mutableListOf(ExpressionStatement(token, two()))),
            InfixExpression(token, one(), "+", two()) to InfixExpression(token, two(), "+", two()),
            InfixExpression(token, two(), "+", one()) to InfixExpression(token, two(), "+", two()),
            PrefixExpression(token, "-", one()) to PrefixExpression(token, "-", two()),
            IndexExpression(token, one(), two()) to IndexExpression(token, two(), two()),
            IfExpression(token, one(),
                BlockStatement(token, mutableListOf(ExpressionStatement(token, one()))),
                BlockStatement(token, mutableListOf(ExpressionStatement(token, one())))) to
                    IfExpression(token, two(),
                        BlockStatement(token, mutableListOf(ExpressionStatement(token, two()))),
                        BlockStatement(token, mutableListOf(ExpressionStatement(token, two())))),
            ReturnStatement(token, one()) to ReturnStatement(token, two()),
            LetStatement(token, Identifier(token, "dummy"), one()) to LetStatement(token, Identifier(token, "dummy"), two()),
        )

        for (test in tests) {
            val modified = modify(test.key, ::turnOneIntoTwo)
            assertEquals(test.value, modified)
        }
    }
}