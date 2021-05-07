package repl

import evaluator.eval
import lexer.newToken
import lexer.nextToken
import obj.newEnvironment
import parser.parseProgram
import platform.posix.scanf
import token.TokenType

val PROMPT = ">> "

fun start(trace: Boolean) {
    val env = newEnvironment()
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

        eval(program, env)?.let {
            println(it.inspect())
        }
    }
}

fun printParseErrors(errors: List<String>) {
    println("Woops! parser errors:")
    errors.forEach { println("\t$it") }
}
