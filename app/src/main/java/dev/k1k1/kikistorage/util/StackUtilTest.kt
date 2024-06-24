package dev.k1k1.kikistorage.util


import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Stack

class StackUtilTest {
    @Test
    fun testRemoveRepeatingTail() {
        val stack1 = Stack<String>()
        stack1.addAll(listOf("a", "b", "c", "b", "d", "b", "d"))
        val stack2 = Stack<String>()
        stack2.addAll(listOf("a", "b", "c", "b", "d", "b", "a", "d", "b", "a"))

        StackUtil.removeRepeatingTail(stack1)
        StackUtil.removeRepeatingTail(stack2)

        assertEquals(listOf("a", "b", "c", "b", "d"), stack1)
        assertEquals(listOf("a", "b", "c", "b", "d", "b", "a"), stack2)
    }
}
