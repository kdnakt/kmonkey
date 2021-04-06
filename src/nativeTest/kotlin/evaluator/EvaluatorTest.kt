package evaluator

import lexer.Lexer
import obj.*
import parser.Parser
import parser.parseProgram
import kotlin.test.Test
import kotlin.test.assertEquals

class EvaluatorTest {
    @Test
    fun testBangOperator() {
        data class Test(val input: String, val expected: Boolean)
        val tests = listOf(
                Test("!true", false),
                Test("!false", true),
                Test("!5", false),
                Test("!!true", true),
                Test("!!false", false),
                Test("!!5", true),
        )

        for (test in tests) {
            val evaluated = testEval(test.input)
            testBooleanObject(evaluated!!, test.expected)
        }
    }

    @Test
    fun testEvalIntegerExpression() {
        data class Test(val input: String, val expected: Long)
        val tests = listOf(
                Test("5", 5),
                Test("10", 10),
                Test("-5", -5),
                Test("-10", -10),
                Test("5 + 5 + 5 + 5 - 10", 10),
                Test("2 * 2", 4),
                Test("50 / 2 * 2 + 10", 60),
        )

        for (test in tests) {
            val evaluated = testEval(test.input)
            testIntegerObject(evaluated!!, test.expected)
        }
    }

    @Test
    fun testEvalBooleanExpression() {
        data class Test(val input: String, val expected: Boolean)
        val tests = listOf(
                Test("true", true),
                Test("false", false),
                Test("1 < 2", true),
                Test("1 > 2", false),
                Test("1 == 1", true),
                Test("1 != 1", false),
                Test("true == true", true),
                Test("false == false", true),
                Test("true == false", false),
                Test("true != false", true),
        )

        for (test in tests) {
            val evaluated = testEval(test.input)
            testBooleanObject(evaluated!!, test.expected)
        }
    }

    @Test
    fun testIfElseExpression() {
        data class Test(val input: String, val expected: Long?)
        val tests = listOf(
                Test("if (true) { 10 }", 10),
                Test("if (false) { 10 }", null),
        )
        for (test in tests) {
            val evaluated = testEval(test.input)!!
            if (test.expected != null) {
                testIntegerObject(evaluated, test.expected)
            } else {
                testNullObj(evaluated)
            }
        }
    }

    @Test
    fun testReturnStatements() {
        data class Test(val input: String, val expected: Long)
        val tests = listOf(
                Test("return 10;", 10)
        )

        for (test in tests) {
            val evaluated = testEval(test.input)!!
            testIntegerObject(evaluated, test.expected)
        }
    }

    @Test
    fun testErrorHandling() {
        val tests = mapOf(
                "5 + true;" to "type mismatch: INTEGER + BOOLEAN",
                "5 + true; 5;" to "type mismatch: INTEGER + BOOLEAN",
                "-true;" to "unknown operator: -BOOLEAN",
                "false + true;" to "unknown operator: BOOLEAN + BOOLEAN",
                "foobar" to "identifier not found: foobar"
        )

        for (test in tests) {
            val evaluated = testEval(test.key)!!
            val errObj = evaluated as ErrorObj
            assertEquals(test.value, errObj.message)
        }
    }

    @Test
    fun testLetStatements() {
        val tests = mapOf<String, Long>(
                "let a = 5; a;" to 5,
                "let a = 5 * 5; a;" to 25,
                "let a = 5; let b = a; b;" to 5,
                "let a = 5; let b = a; let c = a + b + 5; c;" to 15,
        )

        for (test in tests) {
            testIntegerObject(testEval(test.key)!!, test.value)
        }
    }
}

fun testEval(input: String): Obj? {
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()
    val env = newEnvironment()
    return eval(program, env)
}

fun testBooleanObject(obj: Obj, expected: Boolean) {
    val result = obj as BooleanObj
    assertEquals(expected, result.value)
}

fun testIntegerObject(obj: Obj, expected: Long) {
    val result = obj as IntegerObj
    assertEquals(expected, result.value)
}

fun testNullObj(obj: Obj) {
    assertEquals(NULL, obj)
}
