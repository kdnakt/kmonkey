package evaluator

import ast.*
import obj.*
import token.Token
import token.TokenType

fun quote(node: Node, env: Environment): Obj {
    val node = evalUnquoteCalls(node, env)
    return Quote(node)
}

fun evalUnquoteCalls(quoted: Node, env: Environment): Node? {
    fun modifier(node: Node?): Node? {
        if (!isUnquoteCall(node)) {
            return node
        }
        if (node !is CallExpression) {
            return node
        }
        if (node.arguments?.size != 1) {
            return node
        }
        val unquoted = eval(node.arguments[0], env)
        return convertObjectToASTNode(unquoted)
    }
    return modify(quoted, ::modifier)
}

fun isUnquoteCall(node: Node?): Boolean {
    if (node !is CallExpression) {
        return false
    }
    return node.function?.tokenLiteral == "unquote"
}

fun convertObjectToASTNode(obj: Obj?): Node? {
    return when (obj) {
        is IntegerObj -> {
            val t = Token(TokenType.INT, obj.value.toString())
            IntegerLiteral(t, obj.value)
        }
        is BooleanObj -> {
            val t = if (obj.value) {
                Token(TokenType.TRUE, "true")
            } else {
                Token(TokenType.FALSE, "false")
            }
            Bool(t, obj.value)
        }
        is Quote -> obj.node
        else -> null
    }
}
