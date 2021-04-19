package lexer

import token.Token
import token.TokenType
import token.lookUpIdent

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
    skipWhitespace()
    val token = when(ch) {
        null -> Token(TokenType.EOF, "")
        '=' -> {
            if (peekChar() == '=') {
                val prev = ch
                readChar()
                Token(TokenType.EQ, prev.toString() + ch.toString())
            } else {
                newToken(TokenType.ASSIGN, ch)
            }
        }
        '+' -> newToken(TokenType.PLUS, ch)
        '(' -> newToken(TokenType.LPAREN, ch)
        ')' -> newToken(TokenType.RPAREN, ch)
        '{' -> newToken(TokenType.LBRACE, ch)
        '}' -> newToken(TokenType.RBRACE, ch)
        ',' -> newToken(TokenType.COMMA, ch)
        ';' -> newToken(TokenType.SEMICOLON, ch)
        '!' -> {
            if (peekChar() == '=') {
                val prev = ch
                readChar()
                Token(TokenType.NOT_EQ, prev.toString() + ch.toString())
            } else {
                newToken(TokenType.BANG, ch)
            }
        }
        '-' -> newToken(TokenType.MINUS, ch)
        '/' -> newToken(TokenType.SLASH, ch)
        '*' -> newToken(TokenType.ASTERISK, ch)
        '<' -> newToken(TokenType.LT, ch)
        '>' -> newToken(TokenType.GT, ch)
        '"' -> Token(TokenType.STRING, readString())
        '[' -> newToken(TokenType.LBRACKET, ch)
        ']' -> newToken(TokenType.RBRACKET, ch)
        else -> {
            when {
                isLetter(ch) -> {
                    val ident = readIdentifier()
                    return Token(lookUpIdent(ident), ident)
                }
                isDigit(ch) -> {
                    return Token(TokenType.INT, readNumber())
                }
                else -> {
                    newToken(TokenType.ILLEGAL, ch)
                }
            }
        }
    }
    readChar()
    return token
}

private fun Lexer.skipWhitespace() {
    while (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
        readChar()
    }
}

private fun Lexer.peekChar(): Char? {
    return if (readPos >= input.size) {
        null
    } else {
        input[readPos]
    }
}

private fun Lexer.readIdentifier(): String {
    val p = pos
    while (isLetter(ch)) {
        readChar()
    }
    return input.concatToString(p, pos)
}

private fun Lexer.readNumber(): String {
    val p = pos
    while (isDigit(ch)) {
        readChar()
    }
    return input.concatToString(p, pos)
}

private fun Lexer.readString(): String {
    val p = pos + 1
    while (true) {
        readChar()
        if (ch == null || ch == '"') {
            break
        }
    }
    return input.concatToString(p, pos)
}

fun newToken(t: TokenType, c: Char?): Token
    = Token(t, c.toString())

fun isLetter(c: Char?): Boolean {
    if (c == null) return false
    return 'a' <= c && c <= 'z'
            || 'A' <= c && c <= 'Z'
            || c == '_'
}

fun isDigit(c: Char?): Boolean {
    if (c == null) return false
    return '0' <= c && c <= '9'
}