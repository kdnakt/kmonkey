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

data class Program(val statements: MutableList<Statement>): Node {
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

data class PrefixExpression(
        val token: Token,
        val operator: String,
        val right: Expression?,
): Expression {
    override val tokenLiteral = token.literal
    override fun string(): String {
        val sb = StringBuilder("(")
        sb.append(operator)
        if (right != null) sb.append(right.string())
        sb.append(")")
        return sb.toString()
    }
}

data class InfixExpression(
        val token: Token,
        val left: Expression?,
        val operator: String,
        val right: Expression,
): Expression {
    override val tokenLiteral = token.literal
    override fun string(): String {
        val sb = StringBuilder("(")
        if (left != null) sb.append(left.string())
        sb.append(" $operator ")
        sb.append(right.string())
        sb.append(")")
        return sb.toString()
    }
}

data class Bool(
        val token: Token,
        val value: Boolean,
): Expression {
    override val tokenLiteral = token.literal
    override fun string() = token.literal
}

data class IfExpression(
        val token: Token,
        val condition: Expression,
        val consequence: BlockStatement,
        val alternative: BlockStatement?,
): Expression {
    override val tokenLiteral = token.literal
    override fun string(): String {
        val sb = StringBuilder("if")
        sb.append(condition.string())
        sb.append(" ")
        sb.append(consequence.string())

        if (alternative != null) {
            sb.append("else ")
            sb.append(alternative.string())
        }

        return sb.toString()
    }
}

data class BlockStatement(
        val token: Token,
        val statements: MutableList<Statement>,
): Statement {
    override val tokenLiteral = token.literal
    override fun string(): String {
        val sb = StringBuilder()
        for (s in statements) {
            sb.append(s.string())
        }
        return sb.toString()
    }
}

data class FunctionLiteral(
        val token: Token,
        val parameters: List<Identifier>?,
        val body: BlockStatement
): Expression {
    override val tokenLiteral = token.literal
    override fun string(): String {
        val sb = StringBuilder()
        sb.append(tokenLiteral)
        sb.append("(")
        parameters?.joinTo(sb, ", ",
                transform = { it.string() })
        sb.append(")")
        sb.append(body.string())
        return sb.toString()
    }
}

data class CallExpression(
        val token: Token,
        val function: Expression?,
        val arguments: List<Expression>?,
): Expression {
    override val tokenLiteral = token.literal
    override fun string(): String {
        val sb = StringBuilder()
        sb.append(function?.string())
        sb.append("(")
        arguments?.joinTo(sb, ", ",
                transform = { it.string() })
        sb.append(")")
        return sb.toString()
    }
}

data class StringLiteral(
        val token: Token,
        val value: String,
): Expression {
    override val tokenLiteral = token.literal
    override fun string() = token.literal
}

data class ArrayLiteral(
        val token: Token,
        val elements: List<Expression>?,
): Expression {
    override val tokenLiteral = token.literal
    override fun string(): String {
        val sb = StringBuilder("[")
        elements?.joinTo(sb, ", ",
                transform = { it.string() })
        sb.append("]")
        return sb.toString()
    }
}

data class IndexExpression(
        val token: Token,
        val left: Expression,
        val index: Expression,
): Expression {
    override val tokenLiteral = token.literal
    override fun string(): String {
        val sb = StringBuilder("(")
        sb.append(left.string())
        sb.append("[")
        sb.append(index.string())
        sb.append("])")
        return sb.toString()
    }
}

data class HashLiteral(
    val token: Token, // '{'
    val pairs: Map<Expression, Expression>
): Expression {
    override val tokenLiteral = token.literal
    override fun string(): String {
        val sb = StringBuilder("{")
        pairs.map { "${it.key.string()}:${it.value.string()}" }
            .joinTo(sb, ", ")
        sb.append("}")
        return sb.toString()
    }
}
