package repl

import evaluator.defineMacros
import evaluator.eval
import evaluator.expandMacros
import obj.newEnvironment
import parser.parseProgram

val PROMPT = ">> "

fun start(trace: Boolean) {
    val env = newEnvironment()
    val macroEnv = newEnvironment()
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

        defineMacros(program, macroEnv)
        eval(expandMacros(program, macroEnv), env)?.let {
            println(it.inspect())
        }
    }
}

fun printParseErrors(errors: List<String>) {
    println("Woops! parser errors:")
    errors.forEach { println("\t$it") }
}
