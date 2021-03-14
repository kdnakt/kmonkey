package ast

import token.Token

interface Node {
    val tokenLiteral: String
    fun string(): String
}

interface Statement: Node {
}

interface Expression: Node {
}

data class Program(val statements: List<Statement>): Node {
    override val tokenLiteral = when {
        statements.isNotEmpty() -> statements[0].tokenLiteral
        else -> ""
    }
    override fun string(): String {
        val sb = StringBuilder()
        for (s in statements) {
            sb.append(s.string())
        }
        return sb.toString()
    }
}

data class LetStatement(val token: Token, val name: Identifier,
        val value: Expression?): Statement {
    override val tokenLiteral = token.literal
    override fun string(): String {
        val sb = StringBuilder("$tokenLiteral ")
        sb.append(name.string())
        sb.append(" = ")
        if (value != null) sb.append(value.string())
        sb.append(";")
        return sb.toString()
    }
}

data class ReturnStatement(val token: Token, val returnValue: Expression?)
    : Statement {
    override val tokenLiteral = token.literal
    override fun string(): String {
        val sb = StringBuilder("$tokenLiteral ")
        if (returnValue != null) sb.append(returnValue.string())
        sb.append(";")
        return sb.toString()
    }
}

data class ExpressionStatement(val token: Token, val expression: Expression?)
    : Statement {
    override val tokenLiteral = token.literal
    override fun string(): String = expression!!.string()
}

data class Identifier(val token: Token, val value: String)
    : Expression {
    override val tokenLiteral = token.literal
    override fun string(): String = value
}

data class IntegerLiteral(val token: Token, val value: Long)
    : Expression {
    override val tokenLiteral = token.literal
    override fun string(): String = value.toString()
}
