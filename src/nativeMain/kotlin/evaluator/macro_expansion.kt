package evaluator

import ast.LetStatement
import ast.MacroLiteral
import ast.Program
import ast.Statement
import obj.Environment
import obj.Macro
import obj.set

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

fun addMacro(stmt: Statement, env: Environment) {
    val letStatement = stmt as LetStatement
    val macroLiteral = letStatement.value as MacroLiteral

    val macro = Macro(
        macroLiteral.parameters!!,
        macroLiteral.body,
        env
    )
    env.set(letStatement.name.value, macro)
}

fun isMacroDefinition(node: Statement): Boolean {
    if (node !is LetStatement) {
        return false
    }
    return node.value is MacroLiteral
}
