package evaluator

import ast.*
import obj.*

val NULL = NullObj()
val TRUE = BooleanObj(true)
val FALSE = BooleanObj(false)

fun eval(node: Node?, env: Environment): Obj? {
    return when (node) {
        is Program -> evalProgram(node, env)
        is ExpressionStatement -> eval(node.expression, env)
        is IntegerLiteral -> IntegerObj(node.value)
        is Bool -> nativeBooleanToBoolObject(node.value)
        is PrefixExpression -> {
            val right = eval(node.right, env)
            if (isError(right)) return right
            evalPrefixExpression(node.operator, right)
        }
        is InfixExpression -> {
            val left = eval(node.left, env)
            if (isError(left)) return left
            val right = eval(node.right, env)
            if (isError(right)) return right
            evalInfixExpression(node.operator, left, right)
        }
        is IfExpression -> evalIfExpression(node, env)
        is BlockStatement -> evalBlockStatements(node.statements, env)
        is ReturnStatement -> ReturnValue(eval(node.returnValue, env)!!)
        is LetStatement -> {
            val value = eval(node.value, env)
            if (isError(value)) return value
            env.set(node.name.value, value)
        }
        is Identifier -> evalIdentifier(node, env)
        is FunctionLiteral -> FunctionObj(node.parameters, node.body, env)
        is CallExpression -> {
            if (node.function?.tokenLiteral == "quote") {
                node.arguments?.let {
                    return quote(it[0], env)
                }
            }
            val function = eval(node.function, env)
            if (isError(function)) return function
            val args = evalExpressions(node.arguments, env)
            if (args.size == 1 && isError(args[0])) return args[0]
            return applyFunction(function, args)
        }
        is StringLiteral -> StringObj(node.value)
        is ArrayLiteral -> {
            val elements = evalExpressions(node.elements, env)
            if (elements.size == 1 && isError((elements[0]))) return elements[0]
            return ArrayObj(elements)
        }
        is IndexExpression -> {
            val left = eval(node.left, env)
            if (isError(left)) return left
            val index = eval(node.index, env)
            if (isError(index)) return index
            return evalIndexExpression(left, index)
        }
        is HashLiteral -> evalHashLiteral(node, env)
        else -> null
    }
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
    if (left?.type() == ObjectType.STRING && right?.type() == ObjectType.STRING) {
        return evalStringInfixExpression(operator, left, right)
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

fun evalStringInfixExpression(operator: String, left: Obj, right: Obj): Obj {
    if (operator != "+") {
        return ErrorObj("unknown operator: ${left.type()} $operator ${right.type()}")
    }
    val leftVal = (left as StringObj).value
    val rightVal = (right as StringObj).value
    return StringObj(leftVal + rightVal)
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
    return env.get(node.value)?:
            builtins[node.value]?:
            ErrorObj("identifier not found: ${node.value}")
}

fun evalExpressions(exps: List<Expression>?, env: Environment): List<Obj?> {
    val result = mutableListOf<Obj?>()
    if (exps == null) return result
    for (e in exps) {
        val evaluated = eval(e, env)
        if (isError(evaluated)) {
            return listOf(evaluated)
        }
        result.add(evaluated)
    }
    return result
}

fun applyFunction(fn: Obj?, args: List<Obj?>): Obj? {
    return when (fn) {
        is FunctionObj -> {
            val extendedEnv = extendFunctionEnv(fn, args)
            val evaluated = eval(fn.body, extendedEnv)
            return unwrapReturnValue(evaluated)
        }
        is Builtin -> fn.fn(args)
        else -> ErrorObj("not a function: ${fn?.type()}")
    }
}

fun extendFunctionEnv(fn: FunctionObj, args: List<Obj?>): Environment {
    val env = newEnclosedEnvironment(fn.env)
    if (fn.parameters == null) return env
    for (param in fn.parameters.withIndex()) {
        env.set(param.value.value, args[param.index])
    }
    return env
}

fun unwrapReturnValue(obj: Obj?) = when(obj) {
    is ReturnValue -> obj.value
    else -> obj
}

fun evalIndexExpression(left: Obj?, index: Obj?): Obj? {
    return when {
        left?.type() == ObjectType.ARRAY && index?.type() == ObjectType.INTEGER -> {
            evalArrayIndexExpression(left, index)
        }
        left?.type() == ObjectType.HASH -> {
            evalHashIndexExpression(left, index)
        }
        else -> ErrorObj("index operator not supported: ${left?.type()}")
    }
}

fun evalArrayIndexExpression(array: Obj, index: Obj): Obj? {
    val arrayObj = array as ArrayObj
    val idx = (index as IntegerObj).value
    val max = arrayObj.elements.size - 1
    if (idx < 0 || max < idx) {
        return NULL
    }
    return arrayObj.elements[idx.toInt()]
}

fun evalHashLiteral(node: HashLiteral, env: Environment): Obj? {
    val pairs = mutableMapOf<HashKey, HashPair>()
    node.pairs.entries.forEach {
        val key = eval(it.key, env)
        if (isError(key)) return key
        if (key !is Hashable) {
            return ErrorObj("unusable as hash key: ${key?.type()}")
        }

        val value = eval(it.value, env)
        if (isError(value)) return value
        val hashed = (key as Hashable).hashKey()
        pairs[hashed] = HashPair(key, value!!)
    }

    return Hash(pairs)
}

fun evalHashIndexExpression(hash: Obj, index: Obj?): Obj {
    val hashObj = hash as Hash
    if (index !is Hashable) {
        return ErrorObj("unusable as hash key: ${index?.type()}")
    }
    val key = index as Hashable
    val pair = hashObj.pairs[key.hashKey()] ?: return NULL
    return pair.value
}
