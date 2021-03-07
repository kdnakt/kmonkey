package lexer

import token.Token
import token.TokenType

class Lexer(_input: String) {
    val input = _input.toCharArray()
    var pos: Int = 0
    var readPos: Int = 0
    var ch: Char? = null
    init {
        readChar()
    }
}

private fun Lexer.readChar() {
    ch = if (readPos >= input.size) {
        null
    } else {
        input[readPos]
    }
    pos = readPos++
}

fun Lexer.nextToken(): Token {
    val token = when(ch) {
        null -> Token(TokenType.EOF, "")
        '=' -> newToken(TokenType.ASSIGN, ch)
        '+' -> newToken(TokenType.PLUS, ch)
        '(' -> newToken(TokenType.LPAREN, ch)
        ')' -> newToken(TokenType.RPAREN, ch)
        '{' -> newToken(TokenType.LBRACE, ch)
        '}' -> newToken(TokenType.RBRACE, ch)
        ',' -> newToken(TokenType.COMMA, ch)
        ';' -> newToken(TokenType.SEMICOLON, ch)
        else -> newToken(TokenType.ILLEGAL, ch)
    }
    readChar()
    return token
}

fun newToken(t: TokenType, c: Char?): Token
    = Token(t, c.toString())
