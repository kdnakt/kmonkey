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
}
