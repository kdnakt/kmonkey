package evaluator

import ast.FunctionLiteral
import lexer.Lexer
import obj.*
import parser.Parser
import parser.parseProgram
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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
                "foobar" to "identifier not found: foobar",
                """"Hello" - "World"""" to "unknown operator: STRING - STRING",
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

    @Test
    fun testFunctionObject() {
        val input = "fn(x) { x + 2; };"
        val evaluated = testEval(input)!!
        val fn = evaluated as FunctionObj
        assertEquals(1, fn.parameters!!.size)
        assertEquals("x", fn.parameters!![0].string())
        val expectedBody = "(x + 2)"
        assertEquals(expectedBody, fn.body.string())
    }

    @Test
    fun testFunctionApplication() {
        val tests = mapOf<String, Long>(
                "let identity = fn(x) { x; }; identity(5);" to 5,
                "let identity = fn(x) { return x; }; identity(5);" to 5,
                "let double = fn(x) { x * 2; }; double(5);" to 10,
                "let add = fn(x, y) { x + y; }; add(5, 5);" to 10,
                "fn(x) { x; }(5)" to 5,
        )

        for (test in tests) {
            testIntegerObject(testEval(test.key)!!, test.value)
        }
    }

    @Test
    fun testClosures() {
        val input = """
            let newAdder = fn(x) {
              fn (y) { x + y };
            };

            let addTwo = newAdder(2);
            addTwo(3);
        """.trimIndent()

        testIntegerObject(testEval(input)!!, 5)
    }

    @Test
    fun testStringLiteral() {
        val input = """
            "Hello World!"
        """.trimIndent()

        val evaluated = testEval(input)!!
        val str = evaluated as StringObj
        assertEquals("Hello World!", str.value)
    }

    @Test
    fun testStringConcatenation() {
        val input = """
            "Hello" + " " + "World!"
        """.trimIndent()

        val evaluated = testEval(input)!!
        val str = evaluated as StringObj
        assertEquals("Hello World!", str.value)
    }

    @Test
    fun testBuiltinFunctions() {
        data class Testcase<T>(val input: String,
                           val expected: T)
        val tests = listOf(
            Testcase<Long>("""len("")""", 0),
            Testcase<Long>("""len("four")""", 4),
            Testcase<Long>("""len("hello world")""", 11),
            Testcase<String>("len(1)", "argument to `len` not supported, got INTEGER"),
            Testcase<String>("""len("one", "two")""", "wrong number of arguments. got=2, want=1"),
        )

        for (test in tests) {
            val evaluated = testEval(test.input)!!
            when (test.expected) {
                is Long -> testIntegerObject(evaluated, test.expected as Long)
                is String -> {
                    val errObj = evaluated as ErrorObj
                    assertEquals(test.expected, errObj.message)
                }
            }
        }
    }

    @Test
    fun testArrayLiterals() {
        val input = "[1, 2 * 2, 3 + 3]"
        val evaluated = testEval(input)!!
        val result = evaluated as ArrayObj

        assertEquals(3, result.elements.size)
        testIntegerObject(result.elements[0]!!, 1)
        testIntegerObject(result.elements[1]!!, 4)
        testIntegerObject(result.elements[2]!!, 6)
    }

    @Test
    fun testArrayIndexExpressions() {
        val tests = mapOf<String, Long?>(
            "[1, 2, 3][0]" to 1,
            "[1, 2, 3][1]" to 2,
            "[1, 2, 3][2]" to 3,
            "let myArray = [1, 2, 3]; myArray[2]" to 3,
            "[1, 2, 3][3]" to null,
        )

        for (test in tests) {
            val evaluated = testEval(test.key)!!
            when (val value = test.value) {
                is Long -> testIntegerObject(evaluated, value)
                else -> testNullObj(evaluated)
            }
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

private fun testNullObj(obj: Obj) {
    assertEquals(NULL, obj)
}
