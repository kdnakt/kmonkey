package parser

import ast.*
import lexer.nextToken
import token.TokenType

enum class Precedence {
    LOWEST,
    EQUALS, // ==
    LESSGREATER, // < or >
    SUM, // +
    PRODUCT, // *
    PREFIX, // -X or !X
    CALL, // myFunc()
}

val precedences = mapOf(
        TokenType.EQ to Precedence.EQUALS,
        TokenType.NOT_EQ to Precedence.EQUALS,
        TokenType.LT to Precedence.LESSGREATER,
        TokenType.GT to Precedence.LESSGREATER,
        TokenType.PLUS to Precedence.SUM,
        TokenType.MINUS to Precedence.SUM,
        TokenType.SLASH to Precedence.PRODUCT,
        TokenType.ASTERISK to Precedence.PRODUCT,
)

class Parser(val lexer: lexer.Lexer) {
    var curToken: token.Token? = null
    var peekToken: token.Token? = null

    val errors = mutableListOf<String>()
    val prefixParseFns = mapOf<TokenType, () -> Expression>(
        TokenType.IDENT to ::parseIdentifier,
        TokenType.INT to ::parseIntegerLiteral,
        TokenType.MINUS to ::parsePrefixExpression,
    )
    val infixParseFns = mapOf<TokenType, (Expression) -> Expression>(
        TokenType.PLUS to ::parseInfixExpression,
        TokenType.MINUS to ::parseInfixExpression,
    )

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
    TokenType.RETURN -> parseReturnStatement()
    else -> parseExpressionStatement()
}

fun Parser.parseLetStatement(): LetStatement? {
    val token = curToken!!
    if (!expectPeek(TokenType.IDENT)) return null
    val ident = Identifier(curToken!!, curToken!!.literal)
    if (!expectPeek(TokenType.ASSIGN)) return null
    nextToken()
    val stmt = LetStatement(token, ident, parseExpression(Precedence.LOWEST))
    while (!curTokenIs(TokenType.SEMICOLON)) {
        nextToken()
    }
    return stmt
}

fun Parser.parseReturnStatement(): ReturnStatement {
    val token = curToken!!
    nextToken()
    val stmt = ReturnStatement(token, parseExpression(Precedence.LOWEST))
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

fun Parser.parseExpressionStatement(): ExpressionStatement {
    val stmt = ExpressionStatement(curToken!!, parseExpression(Precedence.LOWEST))
    if (peekTokenIs(TokenType.SEMICOLON)) nextToken()
    return stmt
}

fun Parser.parseExpression(precedence: Precedence): Expression? {
    val prefix = prefixParseFns[curToken!!.tokenType]
    if (prefix == null) {
        noPrefixParseFnError(curToken!!.tokenType)
        return null
    }
    var leftExp = prefix()
    while (!peekTokenIs(TokenType.SEMICOLON) && precedence < peekPrecedence()) {
        val infix = infixParseFns[peekToken!!.tokenType] ?: return leftExp
        nextToken()
        leftExp = infix(leftExp)
    }
    return leftExp
}

fun Parser.noPrefixParseFnError(t: TokenType) {
    errors.add("no prefix parse function for $t found")
}

fun Parser.peekPrecedence(): Precedence {
    val p = precedences[peekToken!!.tokenType]
    if (p != null) return p
    return Precedence.LOWEST
}

fun Parser.parseIdentifier(): Expression {
    return Identifier(curToken!!, curToken!!.literal)
}

fun Parser.parseIntegerLiteral(): Expression {
    val token = curToken!!
    val value = token.literal.toLong()
    return IntegerLiteral(token, value)
}

fun Parser.parseInfixExpression(left: Expression): Expression {
    val token = curToken!!
    val precedence = curPrecedence()
    nextToken()
    return InfixExpression(token, left, token.literal, parseExpression(precedence)!!)
}

fun Parser.curPrecedence(): Precedence {
    val p = precedences[curToken!!.tokenType]
    if (p != null) return p
    return Precedence.LOWEST
}

fun Parser.parsePrefixExpression(): Expression {
    val token = curToken!!
    nextToken()
    return PrefixExpression(
            token,
            token.literal,
            parseExpression(Precedence.PREFIX)
    )
}
