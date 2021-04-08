package evaluator

import ast.*
import obj.*

val NULL = NullObj()
val TRUE = BooleanObj(true)
val FALSE = BooleanObj(false)

fun eval(node: Node?, env: Environment): Obj? = when(node) {
    is Program -> evalProgram(node, env)
    is ExpressionStatement -> eval(node.expression, env)
    is IntegerLiteral -> IntegerObj(node.value)
    is Bool -> nativeBooleanToBoolObject(node.value)
    is PrefixExpression -> {
        val right = eval(node.right, env)
        if (isError(right)) right
        evalPrefixExpression(node.operator, right)
    }
    is InfixExpression -> {
        val left = eval(node.left, env)
        if (isError(left)) left
        val right = eval(node.right, env)
        if (isError(right)) right
        evalInfixExpression(node.operator, left, right)
    }
    is IfExpression -> evalIfExpression(node, env)
    is BlockStatement -> evalBlockStatements(node.statements, env)
    is ReturnStatement -> ReturnValue(eval(node.returnValue, env)!!)
    is LetStatement -> {
        val value = eval(node.value, env)
        if (isError(value)) value
        env.set(node.name.value, value)
    }
    is Identifier -> evalIdentifier(node, env)
    is FunctionLiteral -> FunctionObj(node.parameters, node.body, env)
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

fun evalProgram(program: Program, env: Environment): Obj? {
    var result: Obj? = null
    for (stmt in program.statements) {
        result = eval(stmt, env)
        when (result) {
            is ReturnValue -> return result.value
            is ErrorObj -> return result
        }
    }
    return result
}

fun evalBlockStatements(stmts: List<Statement>, env: Environment): Obj? {
    var result: Obj? = null
    for (stmt in stmts) {
        result = eval(stmt, env)
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

fun evalIfExpression(ie: IfExpression, env: Environment): Obj? {
    val condition = eval(ie.condition, env)
    return when {
        isTruthy(condition) -> {
            eval(ie.consequence, env)
        }
        ie.alternative != null -> {
            eval(ie.alternative, env)
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

fun isError(obj: Obj?): Boolean {
    if (obj == null) return false
    return obj.type() == ObjectType.ERROR
}

fun evalIdentifier(node: Identifier, env: Environment): Obj {
    return env.get(node.value)?: ErrorObj("identifier not found: ${node.value}")
}
