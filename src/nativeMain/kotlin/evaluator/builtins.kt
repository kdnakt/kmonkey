package evaluator

import obj.*

fun builtinLen(args: List<Obj?>): Obj {
    if (args.size != 1) {
        return ErrorObj("wrong number of arguments. got=${args.size}, want=1")
    }
    return when (val arg = args[0]) {
        is StringObj -> IntegerObj(arg.value.length.toLong())
        else -> ErrorObj("argument to `len` not supported, got ${arg?.type()}")
    }
}

val builtins = mapOf(
    "len" to Builtin(::builtinLen),
)
