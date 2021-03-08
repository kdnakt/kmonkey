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

    COMMA(","),
    SEMICOLON(";"),

    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),

    FUNCTION("FUNCTION"),
    LET("LET"),
}

val keywords = mapOf(
    "fn" to TokenType.FUNCTION,
    "let" to TokenType.LET
)

fun lookUpIdent(ident: String): TokenType {
    if (keywords.containsKey(ident)) {
        return keywords.getValue(ident)
    }
    return TokenType.IDENT
}