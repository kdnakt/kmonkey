package ast

fun modify(node: Node?, modifier: (Node?) -> Node): Node {
    val newNode = when(node) {
        is Program -> {
            for ((i, stmt) in node.statements.withIndex()) {
                node.statements[i] = modify(stmt, modifier) as Statement
            }
            return node
        }
        is ExpressionStatement -> {
            ExpressionStatement(node.token, modify(node.expression, modifier) as Expression)
        }
        is InfixExpression -> {
            InfixExpression(node.token,
                modify(node.left, modifier) as Expression,
                node.operator,
                modify(node.right, modifier) as Expression
            )
        }
        is PrefixExpression -> {
            PrefixExpression(node.token, node.operator,
                modify(node.right, modifier) as Expression
            )
        }
        is IndexExpression -> {
            IndexExpression(node.token,
                modify(node.left, modifier) as Expression,
                modify(node.index, modifier) as Expression
            )
        }
        is IfExpression -> {
            val alternative = if (node.alternative != null){
                modify(node.alternative, modifier) as BlockStatement
            } else {
                null
            }
            IfExpression(node.token,
                modify(node.condition, modifier) as Expression,
                modify(node.consequence, modifier) as BlockStatement,
                alternative
            )
        }
        is BlockStatement -> {
            for ((i, stmt) in node.statements.withIndex()) {
                node.statements[i] = modify(stmt, modifier) as Statement
            }
            return node
        }
        is ReturnStatement -> {
            ReturnStatement(node.token,
                modify(node.returnValue, modifier) as Expression
            )
        }
        is LetStatement -> {
            LetStatement(node.token, node.name,
                modify(node.value, modifier) as Expression
            )
        }
        else -> node
    }
    return modifier(newNode)
}
