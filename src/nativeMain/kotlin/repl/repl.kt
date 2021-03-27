package repl

import lexer.nextToken
import parser.parseProgram
import platform.posix.scanf
import token.TokenType

val PROMPT = ">> "

fun start(trace: Boolean) {
    while (true) {
        print(PROMPT)
        val str: String = readLine() ?: return
        val lexer = lexer.Lexer(str)
        val parser = parser.Parser(lexer, trace)
        val program = parser.parseProgram()
        if (parser.errors.isNotEmpty()) {
            printParseErrors(parser.errors)
            continue
        }

        println(program.string())
    }
}

fun printParseErrors(errors: List<String>) {
    println("Woops! parser errors:")
    errors.forEach { println("\t$it") }
}
