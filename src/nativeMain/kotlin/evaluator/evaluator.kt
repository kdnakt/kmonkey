package evaluator

import ast.*
import obj.*

val NULL = NullObj()
val TRUE = BooleanObj(true)
val FALSE = BooleanObj(false)

fun eval(node: Node?): Obj? = when(node) {
    is Program -> evalProgram(node)
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
    is IfExpression -> evalIfExpression(node)
    is BlockStatement -> evalBlockStatements(node.statements)
    is ReturnStatement -> ReturnValue(eval(node.returnValue)!!)
    else -> null
}

fun evalPrefixExpression(operator: String, right: Obj?): Obj = when(operator) {
    "!" -> evalBangOperator(right)
    "-" -> evalMinusPrefixOperatorExpression(right)
    else -> ErrorObj("unknown operator: $operator${right?.type()}")
}

fun evalBangOperator(right: Obj?): Obj = when(right) {
    TRUE -> FALSE
    FALSE -> TRUE
    NULL -> TRUE
    else -> FALSE
}

fun evalMinusPrefixOperatorExpression(right: Obj?): Obj {
    if (right?.type() != ObjectType.INTEGER) {
        return ErrorObj("unknown operator: -${right?.type()}")
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

fun evalProgram(program: Program): Obj? {
    var result: Obj? = null
    for (stmt in program.statements) {
        result = eval(stmt)
        when (result) {
            is ReturnValue -> return result.value
            is ErrorObj -> return result
        }
    }
    return result
}

fun evalBlockStatements(stmts: List<Statement>): Obj? {
    var result: Obj? = null
    for (stmt in stmts) {
        result = eval(stmt)
        if (result != null) {
            val rt = result.type()
            if (rt == ObjectType.RETURN_VALUE
                    || rt == ObjectType.ERROR) {
                return result
            }
        }
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
        else -> {
            val msg = "${left?.type()} $operator ${right?.type()}"
            if (left?.type() != right?.type()) {
                ErrorObj("type mismatch: $msg")
            } else {
                ErrorObj("unknown operator: $msg")
            }
        }
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
        else -> ErrorObj("unknown operator: ${left.type()} $operator ${right.type()}")
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
