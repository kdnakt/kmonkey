package lexer

import token.Token
import token.TokenType

class Lexer(private val _input: String) {
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
        '=' -> Token(TokenType.ASSIGN, ch.toString())
        '+' -> Token(TokenType.PLUS, ch.toString())
        '(' -> Token(TokenType.LPAREN, ch.toString())
        ')' -> Token(TokenType.RPAREN, ch.toString())
        '{' -> Token(TokenType.LBRACE, ch.toString())
        '}' -> Token(TokenType.RBRACE, ch.toString())
        ',' -> Token(TokenType.COMMA, ch.toString())
        ';' -> Token(TokenType.SEMICOLON, ch.toString())
        else -> Token(TokenType.ILLEGAL, ch.toString())
    }
    readChar()
    return token
}



