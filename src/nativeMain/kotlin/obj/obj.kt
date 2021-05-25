package obj

import ast.BlockStatement
import ast.Identifier
import ast.Node

interface Obj {
    fun type(): ObjectType
    fun inspect(): String
}

interface Hashable {
    fun hashKey(): HashKey
}

enum class ObjectType {
    INTEGER,
    BOOLEAN,
    NULL,
    RETURN_VALUE,
    ERROR,
    FUNCTION,
    STRING,
    BUILTIN,
    ARRAY,
    HASH,
    QUOTE,
    MACRO,
}

data class IntegerObj(val value: Long): Obj, Hashable {
    override fun type() = ObjectType.INTEGER
    override fun inspect() = value.toString()
    override fun hashKey() = HashKey(type(), value)
}

data class BooleanObj(val value: Boolean): Obj, Hashable {
    override fun type() = ObjectType.BOOLEAN
    override fun inspect() = value.toString()
    override fun hashKey(): HashKey {
        val longValue = if (value) {
            1L
        } else {
            0L
        }
        return HashKey(type(), longValue)
    }
}

class NullObj(): Obj {
    override fun type() = ObjectType.NULL
    override fun inspect() = "null"
}

data class ReturnValue(val value: Obj): Obj {
    override fun type() = ObjectType.RETURN_VALUE
    override fun inspect() = value.inspect()
}

data class ErrorObj(val message: String): Obj {
    override fun type() = ObjectType.ERROR
    override fun inspect() = "ERROR:$message"
}

data class FunctionObj(
        val parameters: List<Identifier>?,
        val body: BlockStatement,
        val env: Environment,
): Obj {
    override fun type() = ObjectType.FUNCTION
    override fun inspect(): String {
        val sb = StringBuilder("fn(")
        parameters?.joinTo(sb, ", ",
                transform = { it.string() })
        sb.append(") {\n")
        sb.append(body.string())
        sb.append("\n}")
        return sb.toString()
    }
}

data class StringObj(val value: String): Obj, Hashable {
    override fun type() = ObjectType.STRING
    override fun inspect() = value
    override fun hashKey() = HashKey(type(), value.hashCode().toLong())
}

data class Builtin(val fn: (List<Obj?>) -> Obj?): Obj {
    override fun type() = ObjectType.BUILTIN
    override fun inspect() = "builtin function"
}

data class ArrayObj(val elements: List<Obj?>): Obj {
    override fun type() = ObjectType.ARRAY
    override fun inspect(): String {
        val sb = StringBuilder("[")
        elements.joinTo(sb, ", ",
            transform = { it?.inspect() ?: "" })
        sb.append("]")
        return sb.toString()
    }
}

data class HashKey(
    val type: ObjectType,
    val value: Long,
)

data class HashPair(
    val key: Obj,
    val value: Obj,
)

data class Hash(
    val pairs: Map<HashKey, HashPair>
): Obj {
    override fun type() = ObjectType.HASH
    override fun inspect(): String {
        val sb = StringBuilder("{")
        pairs.values.joinTo(sb, ", ",
            transform = { "${it.key.inspect()}: ${it.value.inspect()}" })
        sb.append("}")
        return sb.toString()
    }
}

data class Quote(
    val node: Node?,
): Obj {
    override fun type() = ObjectType.QUOTE
    override fun inspect() = "QUOTE(${node?.string()})"
}

data class Macro(
    val parameters: List<Identifier>,
    val body: BlockStatement,
    val env: Environment,
): Obj {
    override fun type() = ObjectType.MACRO
    override fun inspect(): String {
        val sb = StringBuilder("macro(")
        parameters.joinTo(sb, transform = { it.string() })
        sb.append(") {\n")
        sb.append(body.string())
        sb.append("\n}")
        return sb.toString()
    }
}
