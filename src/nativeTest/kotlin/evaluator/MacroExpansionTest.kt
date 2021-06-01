package evaluator

import ast.Program
import lexer.Lexer
import obj.Macro
import obj.get
import obj.newEnvironment
import parser.Parser
import parser.parseProgram
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MacroExpansionTest {
    @Test
    fun testDefineMacros() {
        val input = """
            let number = 1;
            let function = fn(x, y) { x + y };
            let mymacro = macro(x, y) { x + y; };
        """.trimIndent()

        val env = newEnvironment()
        val program = testParseProgram(input)

        defineMacros(program, env)

        assertEquals(2, program.statements.size)
        assertNull(env.get("number"))
        assertNull(env.get("function"))
        val obj = env.get("mymacro")!!
        val macro = obj as Macro
        assertEquals(2, macro.parameters.size)
        assertEquals("x", macro.parameters[0].string())
        assertEquals("y", macro.parameters[1].string())
        assertEquals("(x + y)", macro.body.string())
    }

    @Test
    fun testExpandMacros() {
        val tests = mapOf(
                """
                    let infixExpression = macro() { quote(1 + 2); };
                    infixExpression();
                """.trimIndent() to "(1 + 2)",
                """
                    let reverse = macro(a, b) { quote(unquote(b) - unquote(a)); };
                    reverse(2 + 2, 10 - 5);
                """.trimIndent() to "(10 - 5) - (2 + 2)",
                """
                    let unless = macro(cond, cons, alt) {
                        quote(if (!(unquote(cond))) {
                            unquote(cons);
                        } else {
                            unquote(alt);
                        });
                    };
                    
                    unless(10 > 5, puts("not greater"), puts("greater"));
                """.trimIndent() to """
                    if (!(10 > 5)) { puts("not greater") } else { puts("greater") }
                """.trimIndent()
        )

        for (test in tests) {
            val expected = testParseProgram(test.value)
            val program = testParseProgram(test.key)

            val env = newEnvironment()
            defineMacros(program, env)
            val expanded = expandMacros(program, env)

            assertEquals(expected.string(), expanded?.string())
        }
    }
}

private fun testParseProgram(input: String): Program {
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    return parser.parseProgram()
}
