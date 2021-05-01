package token

data class Token(
    val tokenType: TokenType,
    val literal: String,
)

enum class TokenType(val tokenType: String) {
    ILLEGAL("ILLEGAL"),
    EOF("EOF"),

    IDENT("IDENT"),
    INT("INT"),

    ASSIGN("="),
    PLUS("+"),
    MINUS("-"),
    BANG("!"),
    ASTERISK("*"),
    SLASH("/"),

    EQ("=="),
    NOT_EQ("!="),
    LT("<"),
    GT(">"),

    COMMA(","),
    SEMICOLON(";"),

    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    LBRACKET("["),
    RBRACKET("]"),

    FUNCTION("FUNCTION"),
    LET("LET"),
    TRUE("TRUE"),
    FALSE("FALSE"),
    IF("IF"),
    ELSE("ELSE"),
    RETURN("RETURN"),

    STRING("STRING"),

    COLON(":"),
}

val keywords = mapOf(
        "fn" to TokenType.FUNCTION,
        "let" to TokenType.LET,
        "true" to TokenType.TRUE,
        "false" to TokenType.FALSE,
        "if" to TokenType.IF,
        "else" to TokenType.ELSE,
        "return" to TokenType.RETURN,
)

fun lookUpIdent(ident: String): TokenType {
    if (keywords.containsKey(ident)) {
        return keywords.getValue(ident)
    }
    return TokenType.IDENT
}