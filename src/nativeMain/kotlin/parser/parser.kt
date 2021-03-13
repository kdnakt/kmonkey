package parser

import ast.Identifier
import ast.LetStatement
import ast.Program
import ast.Statement
import lexer.nextToken
import token.TokenType

class Parser(val lexer: lexer.Lexer) {
    var curToken: token.Token? = null
    var peekToken: token.Token? = null

    val errors = mutableListOf<String>()

    init {
        // set curToken and peekToken
        nextToken()
        nextToken()
    }
}

fun Parser.nextToken() {
    curToken = peekToken
    peekToken = lexer.nextToken()
}

fun Parser.expectPeek(t: TokenType): Boolean {
    return if (peekTokenIs(t)) {
        nextToken()
        true
    } else {
        peekError(t)
        false
    }
}

fun Parser.peekTokenIs(t: TokenType): Boolean
    = peekToken!!.tokenType == t

fun Parser.curTokenIs(t: TokenType): Boolean
    = curToken!!.tokenType == t

fun Parser.peekError(t: TokenType) {
    errors.add("expected next token to be ${t}, got ${peekToken!!.tokenType} instead")
}

fun Parser.parseStatement(): Statement? = when(curToken!!.tokenType) {
    TokenType.LET -> parseLetStatement()
    else -> null
}

fun Parser.parseLetStatement(): LetStatement? {
    val token = curToken
    if (!expectPeek(TokenType.IDENT)) return null
    val stmt = LetStatement(token!!, Identifier(curToken!!, curToken!!.literal))
    if (!expectPeek(TokenType.ASSIGN)) return null
    while (!curTokenIs(TokenType.SEMICOLON)) {
        nextToken()
    }
    return stmt
}

fun Parser.parseProgram(): Program {
    val statements = mutableListOf<Statement>()
    while (curToken == null || curToken!!.tokenType != TokenType.EOF) {
        val stmt = parseStatement() ?: continue
        statements.add(stmt)
        nextToken()
    }
    return Program(statements)
}
