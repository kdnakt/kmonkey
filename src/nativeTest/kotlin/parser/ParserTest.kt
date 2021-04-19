package parser

import ast.*
import lexer.Lexer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ParserTest {

    @Test
    fun testLetStatements() {
        data class Test<T>(val input: String,
                        val expectedIdentifier: String,
                        val expectedValue: T)
        val tests = listOf(
                Test<Long>("let x = 5;", "x", 5),
                Test<Long>("let y = 10;", "y", 10),
                Test<Long>("let foobar = 838383;", "foobar", 838383),
        )

        for (test in tests) {
            val lexer = Lexer(test.input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()
            checkParseErrors(parser)

            assertEquals(1, program.statements.size)
            val stmt = program.statements[0]
            testLetStatement(stmt, test.expectedIdentifier)
            val value = (stmt as LetStatement).value
            testLiteralExpression(value, test.expectedValue)
        }
    }

    @Test
    fun testReturnStatement() {
        data class Test<T>(val input: String,
                           val expectedRetVal: T)
        val tests = listOf(
                Test<Long>("return 10;", 10),
                Test<String>("return a;", "a"),
        )
        for (test in tests) {
            val lexer = Lexer(test.input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()
            checkParseErrors(parser)

            assertEquals(1, program.statements.size)
            val stmt = program.statements[0] as ReturnStatement
            assertEquals("return", stmt.tokenLiteral)
            testLiteralExpression(stmt.returnValue, test.expectedRetVal)
        }
    }

    @Test
    fun testParsingInfixExpressions() {
        data class Test<T>(val input: String,
                           val leftVal: T,
                           val operator: String,
                           val rightVal: T)
        val tests = listOf(
                Test<Long>("5 + 5", 5, "+", 5),
                Test<Long>("5 - 5", 5, "-", 5),
                Test<Long>("5 * 5", 5, "*", 5),
                Test<Long>("5 / 5", 5, "/", 5),
                Test<Long>("5 > 5", 5, ">", 5),
                Test<Long>("5 < 5", 5, "<", 5),
                Test<Long>("5 == 5", 5, "==", 5),
                Test<Long>("5 != 5", 5, "!=", 5),
                Test<Boolean>("true == true", true, "==", true),
                Test<Boolean>("true != false", true, "!=", false),
        )

        for (test in tests) {
            val lexer = Lexer(test.input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()
            checkParseErrors(parser)

            assertEquals(1, program.statements.size,
                    "wrong program.statements count")
            val stmt = program.statements[0] as ExpressionStatement
            testInfixExpression(stmt.expression, test.leftVal, test.operator, test.rightVal)
        }
    }

    @Test
    fun testParsingPrefixExpressions() {
        data class Test(val input: String,
                        val operator: String,
                        val integerVal: Long)
        val tests = listOf(
                Test("!5", "!", 5),
                Test("-15", "-", 15),
        )
        for (test in tests) {
            val lexer = Lexer(test.input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()
            checkParseErrors(parser)

            assertEquals(1, program.statements.size)
            val stmt = program.statements[0] as ExpressionStatement
            val exp = stmt.expression as PrefixExpression
            assertEquals(test.operator, exp.operator)
            testIntegerLiteral(exp.right, test.integerVal)
        }
    }

    @Test
    fun testOperatorPrecedenceParsing() {
        data class Test(val input: String, val expected: String)
        val tests = listOf(
                Test("-a * b", "((-a) * b)"),
                Test("!-a", "(!(-a))"),
                Test("a + b + c", "((a + b) + c)"),
                Test("a + b - c", "((a + b) - c)"),
                Test("a * b * c", "((a * b) * c)"),
                Test("a * b / c", "((a * b) / c)"),
                Test("a + b / c", "(a + (b / c))"),
                Test("a + b * c + d / e - f", "(((a + (b * c)) + (d / e)) - f)"),
                Test("3 + 4; -5 * 5", "(3 + 4)((-5) * 5)"),
                Test("5 > 4 == 3 < 4", "((5 > 4) == (3 < 4))"),
                Test("5 < 4 != 3 < 4", "((5 < 4) != (3 < 4))"),
                Test("3 + 4 * 5 == 3 * 1 + 4 * 5", "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))"),
                Test("true", "true"),
                Test("false", "false"),
                Test("3 > 5 == false", "((3 > 5) == false)"),
                Test("1 + (2 + 3) + 4", "((1 + (2 + 3)) + 4)"),
                Test("-(5 + 5)", "(-(5 + 5))"),
                Test("a + add(b * c) + d", "((a + add((b * c))) + d)"),
                Test("add(a, b, 1, 2 * 3, 4 + 5, add(6, 7 * 8))", "add(a, b, 1, (2 * 3), (4 + 5), add(6, (7 * 8)))")
        )
        for (test in tests) {
            val lexer = Lexer(test.input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()
            checkParseErrors(parser)

            assertEquals(test.expected, program.string())
        }
    }

    @Test
    fun testIdentifierExpression() {
        val input = "foobar;"
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        checkParseErrors(parser)

        assertEquals(1, program.statements.size)
        val stmt = program.statements[0] as ExpressionStatement
        val ident = stmt.expression as Identifier
        assertEquals("foobar", ident.value)
        assertEquals("foobar", ident.tokenLiteral)
    }

    @Test
    fun testIntegerLiteralExpression() {
        val input = "5;"
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        checkParseErrors(parser)

        assertEquals(1, program.statements.size)
        val stmt = program.statements[0] as ExpressionStatement
        val literal = stmt.expression as IntegerLiteral
        assertEquals(5, literal.value)
        assertEquals("5", literal.tokenLiteral)
    }

    @Test
    fun testBoolExpression() {
        data class Test(val input: String, val expected: Boolean)
        val tests = listOf(
                Test("true", true),
                Test("false", false),
        )

        for (test in tests) {
            val lexer = Lexer(test.input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()
            checkParseErrors(parser)

            assertEquals(1, program.statements.size)
            val stmt = program.statements[0] as ExpressionStatement
            val bool = stmt.expression as Bool
            assertEquals(test.expected, bool.value)
            assertEquals(test.input, bool.tokenLiteral)
        }
    }

    @Test
    fun testIfStatement() {
        val input = "if (x < y) { x }"
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        checkParseErrors(parser)

        assertEquals(1, program.statements.size)
        val stmt = program.statements[0] as ExpressionStatement
        val exp = stmt.expression as IfExpression
        testInfixExpression(exp.condition, "x", "<", "y")
        assertEquals(1, exp.consequence.statements.size)
        val consequence = exp.consequence.statements[0] as ExpressionStatement
        testIdentifier(consequence.expression, "x")
        assertNull(exp.alternative)
    }

    @Test
    fun testFunctionLiteralParsing() {
        val input = "fn(x, y) { x + y; }"
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        checkParseErrors(parser)

        assertEquals(1, program.statements.size)
        val stmt = program.statements[0] as ExpressionStatement
        val function = stmt.expression as FunctionLiteral

        assertEquals(2, function.parameters?.size)
        testLiteralExpression(function.parameters!![0], "x")
        testLiteralExpression(function.parameters!![1], "y")

        assertEquals(1, function.body.statements.size)
        val bodyStmt = function.body.statements[0] as ExpressionStatement
        testInfixExpression(bodyStmt.expression, "x", "+", "y")
    }

    @Test
    fun testFunctionParameterParsing() {
        data class Test(val input: String, val expectedParams: List<String>)
        val tests = listOf(
                Test("fn() {};", listOf()),
                Test("fn(x) {};", listOf("x")),
                Test("fn(x, y, z) {};", listOf("x", "y", "z"))
        )
        for (test in tests) {
            val lexer = Lexer(test.input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()
            checkParseErrors(parser)

            assertEquals(1, program.statements.size)
            val stmt = program.statements[0] as ExpressionStatement
            val function = stmt.expression as FunctionLiteral
            assertEquals(test.expectedParams.size, function.parameters?.size)
            for (i in test.expectedParams.indices) {
                testLiteralExpression(function.parameters!![i], test.expectedParams[i])
            }
        }
    }

    @Test
    fun testCallExpressionParsing() {
        val input = "add(1, 2 * 3, 4 + 5)"
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        checkParseErrors(parser)

        assertEquals(1, program.statements.size)
        val stmt = program.statements[0] as ExpressionStatement
        val exp = stmt.expression as CallExpression
        testIdentifier(exp.function, "add")
        assertEquals(3, exp.arguments!!.size)
        testLiteralExpression(exp.arguments!![0], 1)
        testInfixExpression(exp.arguments!![1], 2, "*", 3)
        testInfixExpression(exp.arguments!![2], 4, "+", 5)
    }

    @Test
    fun testStringLiteral() {
        val input = """
            "hello world";
        """.trimIndent()
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        checkParseErrors(parser)

        val stmt = program.statements[0] as ExpressionStatement
        val literal = stmt.expression as StringLiteral
        assertEquals("hello world", literal.value)
    }

    @Test
    fun testParsingArrayLiterals() {
        val input = """
            [1, 2 * 2, 3 + 3]
        """.trimIndent()
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        checkParseErrors(parser)

        val stmt = program.statements[0] as ExpressionStatement
        val array = stmt.expression as ArrayLiteral
        assertEquals(3, array.elements!!.size)
        testIntegerLiteral(array.elements?.get(0), 1)
        testInfixExpression(array.elements?.get(1), 2, "*", 2)
        testInfixExpression(array.elements?.get(2), 3, "+", 3)
    }
}

fun testLetStatement(stmt: Statement, expected: String) {
    assertEquals("let", stmt.tokenLiteral)
    val letStmt = stmt as LetStatement
    assertEquals(expected, letStmt.name.value)
    assertEquals(expected, letStmt.name.tokenLiteral)
}

fun checkParseErrors(parser: Parser) {
    if (parser.errors.isEmpty()) return
    println("parser has ${parser.errors.size} errors")
    for (error in parser.errors) {
        println("parser error: $error")
    }
    error("Parse error!")
}

fun <T> testLiteralExpression(exp: Expression?, expected: T) {
    when (expected) {
        is Long -> testIntegerLiteral(exp, expected)
        is Boolean -> testBooleanLiteral(exp, expected)
    }
}

fun testIntegerLiteral(exp: Expression?, expected: Long) {
    val integ = exp as IntegerLiteral
    assertEquals(expected, integ.value)
    assertEquals("$expected", integ.tokenLiteral)
}

fun testBooleanLiteral(exp: Expression?, expected: Boolean) {
    val bo = exp as Bool
    assertEquals(expected, bo.value)
    assertEquals(expected.toString(), bo.tokenLiteral)
}

fun testInfixExpression(exp: Expression?,
                        left: Any,
                        operator: String,
                        right: Any) {
    val opExp = exp as InfixExpression
    testLiteralExpression(opExp.left, left)
    assertEquals(operator, opExp.operator)
    testLiteralExpression(opExp.right, right)
}

fun testIdentifier(exp: Expression?, value: String) {
    val ident = exp as Identifier
    assertEquals(value, ident.value)
    assertEquals(value, ident.tokenLiteral)
}
