package repl

import lexer.nextToken
import platform.posix.scanf
import token.TokenType

val PROMPT = ">> "

fun start(trace: Boolean) {
    while (true) {
        print(PROMPT)
        val str: String = readLine() ?: return
        val lexer = lexer.Lexer(str)
        var t = lexer.nextToken()
        while (t.tokenType != TokenType.EOF) {
            println(t)
            t = lexer.nextToken()
        }

    }
}
