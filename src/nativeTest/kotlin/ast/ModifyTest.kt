package ast

import token.Token
import token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

val token = Token(TokenType.INT, "dummy")
val ident = Identifier(token, "dummy")
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

class ModifyTest {
    @Test
    fun testModify() {
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
            LetStatement(token, ident, one()) to LetStatement(token, ident, two()),
            FunctionLiteral(token, listOf(ident), BlockStatement(token, mutableListOf(ExpressionStatement(token, one()))))
                to FunctionLiteral(token, listOf(ident), BlockStatement(token, mutableListOf(ExpressionStatement(token, two())))),
            ArrayLiteral(token, listOf(one(), one())) to ArrayLiteral(token, listOf(two(), two()))
        )

        for (test in tests) {
            val modified = modify(test.key, ::turnOneIntoTwo)
            assertEquals(test.value, modified)
        }
    }

    @Test
    fun testModifyHashLiteral() {
        val hashLiteral = HashLiteral(token, mapOf(
            one() to one(),
            one() to one(),
        ))
        modify(hashLiteral, ::turnOneIntoTwo)

        for (pair in hashLiteral.pairs) {
            val key = pair.key as IntegerLiteral
            assertEquals(2, key.value)
            val value = pair.value as IntegerLiteral
            assertEquals(2, value.value)
        }
    }
}