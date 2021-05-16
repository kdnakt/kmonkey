package evaluator

import obj.Quote
import kotlin.test.Test
import kotlin.test.assertEquals

class QuoteUnquoteTest {
    @Test
    fun testQuote() {
        val tests = mapOf(
            "quote(5)" to "5",
        )

        for (test in tests) {
            val evaluated = testEval(test.key)!!
            val quote = evaluated as Quote
            assertEquals(test.value, quote.node.string())
        }
    }

    @Test
    fun testQuoteUnquote() {
        val tests = mapOf(
            "quote(unquote(4))" to "4",
            "quote(unquote(4 + 4))" to "8",
            "quote(8 + unquote(4 + 4))" to "(8 + 8)",
            "quote(unquote(4 + 4) + 8)" to "(8 + 8)",
            """
                let foobar = 8;
                quote(unquote(foobar))
            """.trimIndent() to "8",
            """
                let foobar = 8;
                quote(foobar)
            """.trimIndent() to "foobar",
            "quote(unquote(true))" to "true",
            "quote(unquote(true == false))" to "false",
            "quote(unquote(quote(4 + 4)))" to "(4 + 4)",
            """
                let quotedInfixExpression = quote(4 + 4);
                quote(unquote(4 + 4) + unquote(quotedInfixExpression))
            """.trimIndent() to "(8 + (4 + 4))"
        )

        for (test in tests) {
            val evaluated = testEval(test.key)!!
            val quote = evaluated as Quote
            assertEquals(test.value, quote.node.string())
        }
    }
}
