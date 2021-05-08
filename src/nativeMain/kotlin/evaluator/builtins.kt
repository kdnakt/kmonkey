package evaluator

import obj.*

fun builtinLen(args: List<Obj?>): Obj? {
    if (args.size != 1) {
        return ErrorObj("wrong number of arguments. got=${args.size}, want=1")
    }
    return when (val arg = args[0]) {
        is StringObj -> IntegerObj(arg.value.length.toLong())
        is ArrayObj -> IntegerObj(arg.elements.size.toLong())
        else -> ErrorObj("argument to `len` not supported, got ${arg?.type()}")
    }
}

fun builtinFirst(args: List<Obj?>): Obj? {
    if (args.size != 1) {
        return ErrorObj("wrong number of arguments. got=${args.size}, want=1")
    }
    if (args[0]?.type() != ObjectType.ARRAY) {
        return ErrorObj("argument to `first` must be ARRAY, got ${args[0]?.type()}")
    }
    val array = args[0] as ArrayObj
    if (array.elements.isNotEmpty()) {
        return array.elements.first()
    }
    return NULL
}


fun builtinLast(args: List<Obj?>): Obj? {
    if (args.size != 1) {
        return ErrorObj("wrong number of arguments. got=${args.size}, want=1")
    }
    if (args[0]?.type() != ObjectType.ARRAY) {
        return ErrorObj("argument to `last` must be ARRAY, got ${args[0]?.type()}")
    }
    val array = args[0] as ArrayObj
    if (array.elements.isNotEmpty()) {
        return array.elements.last()
    }
    return NULL
}

fun builtinRest(args: List<Obj?>): Obj? {
    if (args.size != 1) {
        return ErrorObj("wrong number of arguments. got=${args.size}, want=1")
    }
    if (args[0]?.type() != ObjectType.ARRAY) {
        return ErrorObj("argument to `rest` must be ARRAY, got ${args[0]?.type()}")
    }
    val array = args[0] as ArrayObj
    if (array.elements.isEmpty()) {
        return NULL
    }
    return ArrayObj(array.elements.subList(1, array.elements.size))
}

fun builtinPush(args: List<Obj?>): Obj? {
    if (args.size != 2) {
        return ErrorObj("wrong number of arguments. got=${args.size}, want=2")
    }
    if (args[0]?.type() != ObjectType.ARRAY) {
        return ErrorObj("argument to `push` must be ARRAY, got ${args[0]?.type()}")
    }
    val array = args[0] as ArrayObj
    return ArrayObj(array.elements + args[1])
}

fun builtinPuts(args: List<Obj?>): Obj? {
    for (arg in args) {
        println(arg?.inspect())
    }
    return NULL
}

val builtins = mapOf(
    "len" to Builtin(::builtinLen),
    "first" to Builtin(::builtinFirst),
    "last" to Builtin(::builtinLast),
    "rest" to Builtin(::builtinRest),
    "push" to Builtin(::builtinPush),
    "puts" to Builtin(::builtinPuts),
)
