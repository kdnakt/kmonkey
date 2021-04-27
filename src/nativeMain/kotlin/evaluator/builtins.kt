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

val builtins = mapOf(
    "len" to Builtin(::builtinLen),
    "first" to Builtin(::builtinFirst),
)
