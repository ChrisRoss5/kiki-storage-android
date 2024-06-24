package dev.k1k1.kikistorage.util

import java.util.Stack

object StackUtil {
    fun removeRepeatingTail(stack: Stack<String>) {
        if (stack.size < 4) return  // No room for a sequence to repeat twice
        var possiblePatternLength = 1

        outer@while (possiblePatternLength * 2 <= stack.size) {
            val tailStart = stack.size - possiblePatternLength * 2
            for (i in 0 until possiblePatternLength) {
                if (stack[tailStart + i] != stack[tailStart + possiblePatternLength + i]) {
                    possiblePatternLength++
                    continue@outer
                }
            }
            // If we reach here, we found a repeating sequence
            repeat(possiblePatternLength) { stack.pop() }
            break
        }
    }
}