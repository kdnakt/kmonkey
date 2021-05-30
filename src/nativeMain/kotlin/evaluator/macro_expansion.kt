package evaluator

import ast.*
import obj.*

fun defineMacros(program: Program, env: Environment) {
    val definitions = mutableListOf<Int>()
    for ((i, stmt) in program.statements.withIndex()) {
        if (isMacroDefinition(stmt)) {
            addMacro(stmt, env)
            definitions.add(i)
        }
    }

    for (defIndex in definitions.reversed()) {
        program.statements.removeAt(defIndex)
    }
}

private fun addMacro(stmt: Statement, env: Environment) {
    val letStatement = stmt as LetStatement
    val macroLiteral = letStatement.value as MacroLiteral

    val macro = Macro(
        macroLiteral.parameters!!,
        macroLiteral.body,
        env
    )
    env.set(letStatement.name.value, macro)
}

private fun isMacroDefinition(node: Statement): Boolean {
    if (node !is LetStatement) {
        return false
    }
    return node.value is MacroLiteral
}

fun expandMacros(program: Node?, env: Environment): Node? {
    fun expandNode(node: Node?): Node? {
        if (node !is CallExpression) {
            return node
        }
        val macro = isMacroCall(node, env) ?: return node
        val args = quoteArgs(node)
        val evalEnv = extendMacroEnc(macro, args)
        val evaluated = eval(macro.body, evalEnv)

        val quote = evaluated as Quote
        return quote.node
    }
    return modify(program, ::expandNode)
}

private fun isMacroCall(exp: CallExpression, env: Environment): Macro? {
    if (exp.function !is Identifier) {
        return null
    }
    val obj = env.get(exp.function.value)?: return null
    if (obj is Macro) {
        return obj
    }
    return null
}

private fun quoteArgs(exp: CallExpression): List<Quote> {
    val args = mutableListOf<Quote>()
    if (exp.arguments == null) return args
    for (a in exp.arguments) {
        args.add(Quote(a))
    }
    return args
}

private fun extendMacroEnc(macro: Macro, args: List<Quote>): Environment {
    val extended = newEnclosedEnvironment(macro.env)
    for ((idx, param) in macro.parameters.withIndex()) {
        extended.set(param.value, args[idx])
    }
    return extended
}
