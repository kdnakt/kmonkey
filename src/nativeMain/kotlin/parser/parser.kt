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
        TokenType.LPAREN to Precedence.CALL,
)

class Parser(val lexer: lexer.Lexer, trace: Boolean = false) {
    var curToken: token.Token? = null
    var peekToken: token.Token? = null

    val errors = mutableListOf<String>()
    val prefixParseFns = mapOf<TokenType, () -> Expression?>(
        TokenType.IDENT to ::parseIdentifier,
        TokenType.INT to ::parseIntegerLiteral,
        TokenType.MINUS to ::parsePrefixExpression,
        TokenType.BANG to ::parsePrefixExpression,
        TokenType.TRUE to ::parseBooleanExpression,
        TokenType.FALSE to ::parseBooleanExpression,
        TokenType.LPAREN to ::parseGroupedExpression,
        TokenType.IF to ::parseIfExpression,
        TokenType.FUNCTION to ::parseFunctionExpression,
        TokenType.STRING to ::parseStringLiteral,
        TokenType.LBRACKET to ::parseArrayLiteral,
    )
    val infixParseFns = mapOf<TokenType, (Expression?) -> Expression>(
        TokenType.PLUS to ::parseInfixExpression,
        TokenType.MINUS to ::parseInfixExpression,
        TokenType.ASTERISK to ::parseInfixExpression,
        TokenType.SLASH to ::parseInfixExpression,
        TokenType.EQ to ::parseInfixExpression,
        TokenType.NOT_EQ to ::parseInfixExpression,
        TokenType.GT to ::parseInfixExpression,
        TokenType.LT to ::parseInfixExpression,
        TokenType.LPAREN to ::parseCallExpression,
    )

    init {
        isTrace = trace
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
    val trace = trace("parseExpressionStatement")
    try {
        val stmt = ExpressionStatement(curToken!!, parseExpression(Precedence.LOWEST))
        if (peekTokenIs(TokenType.SEMICOLON)) nextToken()
        return stmt
    } finally {
        untrace(trace)
    }
}

fun Parser.parseExpression(precedence: Precedence): Expression? {
    val trace = trace("parseExpression")
    try {
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
    } finally {
        untrace(trace)
    }
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

fun Parser.parseInfixExpression(left: Expression?): Expression {
    val trace = trace("parseInfixExpression")
    try {
        val token = curToken!!
        val precedence = curPrecedence()
        nextToken()
        return InfixExpression(token, left, token.literal, parseExpression(precedence)!!)
    } finally {
        untrace(trace)
    }
}

fun Parser.curPrecedence(): Precedence {
    val p = precedences[curToken!!.tokenType]
    if (p != null) return p
    return Precedence.LOWEST
}

fun Parser.parsePrefixExpression(): Expression {
    val trace = trace("parsePrefixExpression")
    try {
        val token = curToken!!
        nextToken()
        return PrefixExpression(
                token,
                token.literal,
                parseExpression(Precedence.PREFIX)
        )
    } finally {
        untrace(trace)
    }
}

fun Parser.parseBooleanExpression(): Expression {
    return Bool(curToken!!, curTokenIs(TokenType.TRUE))
}

fun Parser.parseGroupedExpression(): Expression? {
    nextToken()
    val exp = parseExpression(Precedence.LOWEST)
    if (!expectPeek(TokenType.RPAREN)) {
        return null
    }
    return exp
}

fun Parser.parseIfExpression(): Expression? {
    val token = curToken!!
    if (!expectPeek(TokenType.LPAREN)) {
        return null
    }
    nextToken()
    val condition = parseExpression(Precedence.LOWEST)!!

    if (!expectPeek(TokenType.RPAREN)) {
        return null
    }
    if (!expectPeek(TokenType.LBRACE)) {
        return null
    }
    val consequence = parseBlockStatement()
    var alternative: BlockStatement? = null
    if (peekTokenIs(TokenType.ELSE)) {
        nextToken()

        if (!expectPeek(TokenType.LBRACE)) {
            return null
        }
        alternative = parseBlockStatement()
    }
    return IfExpression(token, condition, consequence, alternative)
}

fun Parser.parseBlockStatement(): BlockStatement {
    val token = curToken!!
    val statements = mutableListOf<Statement>()
    nextToken()
    while (!curTokenIs(TokenType.RBRACE) && !curTokenIs(TokenType.EOF)) {
        val stmt = parseStatement()
        if (stmt != null) {
            statements.add(stmt)
        }
        nextToken()
    }
    return BlockStatement(token, statements)
}

fun Parser.parseFunctionExpression(): Expression? {
    val token = curToken!!
    if (!expectPeek(TokenType.LPAREN)) return null
    val params = parseFunctionParameters()
    if (!expectPeek(TokenType.LBRACE)) return null
    return FunctionLiteral(token, params, parseBlockStatement())
}

fun Parser.parseFunctionParameters(): List<Identifier>? {
    val identifiers = mutableListOf<Identifier>()
    if (peekTokenIs(TokenType.RPAREN)) {
        nextToken()
        return identifiers
    }

    nextToken()
    identifiers.add(Identifier(curToken!!, curToken!!.literal))
    while (peekTokenIs(TokenType.COMMA)) {
        nextToken()
        nextToken()
        identifiers.add(Identifier(curToken!!, curToken!!.literal))
    }

    if (!expectPeek(TokenType.RPAREN)) return null

    return identifiers
}

fun Parser.parseCallExpression(function: Expression?): Expression {
    return CallExpression(curToken!!, function,
            parseExpressionList(TokenType.RPAREN))
}

fun Parser.parseStringLiteral(): Expression {
    return StringLiteral(curToken!!, curToken!!.literal)
}

fun Parser.parseArrayLiteral(): Expression {
    return ArrayLiteral(curToken!!, parseExpressionList(TokenType.RBRACKET))
}

fun Parser.parseExpressionList(end: TokenType): List<Expression>? {
    val list = mutableListOf<Expression>()
    if (peekTokenIs(end)) {
        nextToken()
        return list
    }
    nextToken()
    list.add(parseExpression(Precedence.LOWEST)!!)
    while (peekTokenIs(TokenType.COMMA)) {
        nextToken()
        nextToken()
        list.add(parseExpression(Precedence.LOWEST)!!)
    }
    if (!expectPeek(end)) {
        return null
    }
    return list
}
