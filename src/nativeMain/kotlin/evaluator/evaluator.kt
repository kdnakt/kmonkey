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
    is InfixExpression -> {
        val left = eval(node.left)
        val right = eval(node.right)
        evalInfixExpression(node.operator, left, right)
    }
    is IfExpression -> evalIfExpression(node!!)
    is BlockStatement -> evalStatements(node.statements)
    else -> null
}

fun evalPrefixExpression(operator: String, right: Obj?): Obj? = when(operator) {
    "!" -> evalBangOperator(right)
    "-" -> evalMinusPrefixOperatorExpression(right)
    else -> NULL
}

fun evalBangOperator(right: Obj?): Obj = when(right) {
    TRUE -> FALSE
    FALSE -> TRUE
    NULL -> TRUE
    else -> FALSE
}

fun evalMinusPrefixOperatorExpression(right: Obj?): Obj {
    if (right?.type() != ObjectType.INTEGER) {
        return NULL
    }
    val value = (right as IntegerObj).value
    return IntegerObj(-value)
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

fun evalInfixExpression(operator: String, left: Obj?, right: Obj?): Obj {
    if (left?.type() == ObjectType.INTEGER && right?.type() == ObjectType.INTEGER) {
        return evalIntegerInfixExpression(operator, left, right)
    }
    return when (operator) {
        "==" -> nativeBooleanToBoolObject(left == right)
        "!=" -> nativeBooleanToBoolObject(left != right)
        else -> NULL
    }
}

fun evalIntegerInfixExpression(operator: String, left: Obj, right: Obj): Obj {
    val leftVal = (left as IntegerObj).value
    val rightVal = (right as IntegerObj).value
    return when (operator) {
        "+" -> IntegerObj(leftVal + rightVal)
        "-" -> IntegerObj(leftVal - rightVal)
        "*" -> IntegerObj(leftVal * rightVal)
        "/" -> IntegerObj(leftVal / rightVal)
        "<" -> nativeBooleanToBoolObject(leftVal < rightVal)
        ">" -> nativeBooleanToBoolObject(leftVal > rightVal)
        "==" -> nativeBooleanToBoolObject(leftVal == rightVal)
        "!=" -> nativeBooleanToBoolObject(leftVal != rightVal)
        else -> NULL
    }
}

fun evalIfExpression(ie: IfExpression): Obj? {
    val condition = eval(ie.condition)
    return when {
        isTruthy(condition) -> {
            eval(ie.consequence)
        }
        ie.alternative != null -> {
            eval(ie.alternative)
        }
        else -> NULL
    }
}

fun isTruthy(obj: Obj?): Boolean {
    return when(obj) {
        NULL -> false
        TRUE -> true
        FALSE -> false
        else -> true
    }
}