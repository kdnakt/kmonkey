import kotlinx.cli.*

fun main(args: Array<String>) {
    val argsParser = ArgParser("kmonkey")
    val trace by argsParser.option(ArgType.Boolean, shortName = "t")
            .default(false)
    println("Hello! This is the Monkey programming language!")
    println("Feel free to type in commands")
    repl.start(trace)
}