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
}

private fun testParseProgram(input: String): Program {
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    return parser.parseProgram()
}
