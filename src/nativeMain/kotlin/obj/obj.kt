package obj

import ast.BlockStatement
import ast.Identifier

interface Obj {
    fun type(): ObjectType
    fun inspect(): String
}

enum class ObjectType {
    INTEGER,
    BOOLEAN,
    NULL,
    RETURN_VALUE,
    ERROR,
    FUNCTION,
}

data class IntegerObj(val value: Long): Obj {
    override fun type() = ObjectType.INTEGER
    override fun inspect() = value.toString()
}

data class BooleanObj(val value: Boolean): Obj {
    override fun type() = ObjectType.BOOLEAN
    override fun inspect() = value.toString()
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
