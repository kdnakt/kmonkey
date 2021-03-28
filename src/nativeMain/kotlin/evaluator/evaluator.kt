package evaluator

import ast.*
import obj.*

val NULL = NullObj()
val TRUE = BooleanObj(true)
val FALSE = BooleanObj(false)

fun eval(node: Node?): Obj? = when(node) {
    is Program -> evalStatements(node.statements)
    is ExpressionStatement -> eval(node.expression)
    is IntegerLiteral -> IntegerObj(node.value)
    is Bool -> nativeBooleanToBoolObject(node.value)
    is PrefixExpression -> {
        val right = eval(node.right)
        evalPrefixExpression(node.operator, right)
    }
    else -> null
}

fun evalPrefixExpression(operator: String, right: Obj?): Obj? = when(operator) {
    "!" -> evalBangOperator(right)
    else -> NULL
}

fun evalBangOperator(right: Obj?): Obj = when(right) {
    TRUE -> FALSE
    FALSE -> TRUE
    NULL -> TRUE
    else -> FALSE
}

fun nativeBooleanToBoolObject(value: Boolean): Obj {
    return if (value) {
        TRUE
    } else {
        FALSE
    }
}

fun evalStatements(stmts: List<Statement>): Obj? {
    var result: Obj? = null
    for (stmt in stmts) {
        result = eval(stmt)
    }
    return result
}
