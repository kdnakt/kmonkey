package obj

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ObjTest {
    @Test
    fun testStringHashKey() {
        val hello1 = StringObj("Hello World")
        val hello2 = StringObj("Hello World")
        val diff1 = StringObj("My name is johnny")
        val diff2 = StringObj("My name is johnny")

        assertEquals(hello1.hashKey(), hello2.hashKey())
        assertEquals(diff1.hashKey(), diff2.hashKey())
        assertNotEquals(hello1.hashKey(), diff1.hashKey())
    }

    @Test
    fun testIntegerHashKey() {
        val asc1 = IntegerObj(123456)
        val asc2 = IntegerObj(123456)
        val desc1 = IntegerObj(654321)
        val desc2 = IntegerObj(654321)

        assertEquals(asc1.hashKey(), asc2.hashKey())
        assertEquals(desc1.hashKey(), desc2.hashKey())
        assertNotEquals(asc1.hashKey(), desc1.hashKey())
    }
}