package ast

import token.Token

interface Node {
    val tokenLiteral: String
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
}

data class LetStatement(val token: Token, val name: Identifier)
    : Statement {
    override val tokenLiteral = token.literal
}

data class Identifier(val token: Token, val value: String)
    : Statement {
    override val tokenLiteral = token.literal
}
