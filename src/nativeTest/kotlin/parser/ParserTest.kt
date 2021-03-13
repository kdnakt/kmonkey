package parser

import ast.LetStatement
import ast.Statement
import lexer.Lexer
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {

    @Test
    fun testLetStatements() {
        val input ="""
            let x = 5;
            let y = 10;
            let foobar = 838383;
        """.trimIndent()

        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        checkParseErrors(parser)

        assertEquals(3, program.statements.size,
                "wrong program.statements count")
        val tests = listOf("x", "y", "foobar")
        for ((i, expected) in tests.withIndex()) {
            val stmt = program.statements[i]
            testLetStatement(stmt, expected)
        }
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
