/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package generators.unicode.ranges.writers

import generators.unicode.ranges.RangesWritingStrategy
import generators.unicode.ranges.patterns.CategorizedRangePattern
import generators.unicode.ranges.patterns.UNASSIGNED_CATEGORY_VALUE_REPLACEMENT
import java.io.FileWriter

internal open class CategoryRangesWriter(protected val strategy: RangesWritingStrategy) {

    fun write(ranges: List<CategorizedRangePattern>, writer: FileWriter) {
        beforeWritingRanges(writer)

        writeRangeStart(ranges.map { it.rangeStart() }, writer)
        writer.appendLine()
        writeRangeCategory(ranges.map { it.category() }, writer)
        writer.appendLine()
        writeInit(ranges, writer)

        afterWritingRanges(writer)
    }

    protected open fun beforeWritingRanges(writer: FileWriter) {
        strategy.beforeWritingRanges(writer)
    }

    protected open fun afterWritingRanges(writer: FileWriter) {
        strategy.afterWritingRanges(writer)

        writer.appendLine()
        writer.appendLine(categoryValueFrom())
        writer.appendLine()
        writer.appendLine(getCategoryValue())
    }

    protected open fun writeRangeStart(elements: List<Int>, writer: FileWriter) {
        writer.writeIntArray("rangeStart", elements, strategy)
    }

    protected open fun writeRangeCategory(elements: List<Int>, writer: FileWriter) {
        writer.writeIntArray("rangeCategory", elements, strategy)
    }

    protected open fun writeInit(ranges: List<CategorizedRangePattern>, writer: FileWriter) {}

    private fun categoryValueFrom(): String = """
        private fun categoryValueFrom(code: Int, ch: Int): Int {
            return when {
                code < 0x20 -> code
                code < 0x400 -> if ((ch and 1) == 1) code shr 5 else code and 0x1f
                else ->
                    when (ch % 3) {
                        2 -> code shr 10
                        1 -> (code shr 5) and 0x1f
                        else -> code and 0x1f
                    }
            }
        }
        """.trimIndent()

    private fun getCategoryValue(): String = """
        /**
         * Returns the Unicode general category of this character as an Int.
         */
        internal fun Char.getCategoryValue(): Int {
            val ch = this.toInt()

            val index = ${indexOf("ch")}
            val code = ${categoryAt("index")}
            val value = categoryValueFrom(code, ch)

            return if (value == $UNASSIGNED_CATEGORY_VALUE_REPLACEMENT) CharCategory.UNASSIGNED.value else value
        }
        """.trimIndent()

    protected open fun indexOf(charCode: String): String {
        return "binarySearchRange(${strategy.rangeRef("rangeStart")}, $charCode)"
    }

    protected open fun categoryAt(index: String): String {
        return "${strategy.rangeRef("rangeCategory")}[$index]"
    }
}

internal class VarLenBase64CategoryRangesWriter(strategy: RangesWritingStrategy) : CategoryRangesWriter(strategy) {

    override fun afterWritingRanges(writer: FileWriter) {
        super.afterWritingRanges(writer)
        writer.appendLine()
        writer.appendLine(decodeVarLenBase64())
    }

    override fun writeInit(ranges: List<CategorizedRangePattern>, writer: FileWriter) {
        val length = ranges.map { it.rangeStart() }.zipWithNext { a, b -> b - a }
        val rangeLength = length.toVarLenBase64()

        val rangeCategory = ranges.map { it.category() }.toVarLenBase64()

        writer.appendLine(
            """
            internal val decodedRangeStart: IntArray
            internal val decodedRangeCategory: IntArray
            
            init {
                val toBase64 = "$TO_BASE64"
                val fromBase64 = IntArray(128)
                for (i in toBase64.indices) {
                    fromBase64[toBase64[i].toInt()] = i
                }
                
                // rangeLength.length = ${rangeLength.length}
                val rangeLength = "$rangeLength"
                val length = decodeVarLenBase64(rangeLength, fromBase64, ${ranges.size - 1})
                val start = IntArray(length.size + 1)
                for (i in length.indices) {
                    start[i + 1] = start[i] + length[i]
                }
                decodedRangeStart = start
                
                // rangeCategory.length = ${rangeCategory.length}
                val rangeCategory = "$rangeCategory"
                decodedRangeCategory = decodeVarLenBase64(rangeCategory, fromBase64, ${ranges.size})
            }
            """.replaceIndent(strategy.indentation)
        )
    }

    override fun writeRangeStart(elements: List<Int>, writer: FileWriter) {}

    override fun writeRangeCategory(elements: List<Int>, writer: FileWriter) {}

    private fun decodeVarLenBase64() = """
        internal fun decodeVarLenBase64(base64: String, fromBase64: IntArray, resultLength: Int): IntArray {
            val result = IntArray(resultLength)
            var index = 0
            var int = 0
            var shift = 0
            for (char in base64) {
                val sixBit = fromBase64[char.toInt()]
                int = int or ((sixBit and 0x1f) shl shift)
                if (sixBit < 0x20) {
                    result[index++] = int
                    int = 0
                    shift = 0
                } else {
                    shift += 5
                }
            }
            return result
        }
        """.trimIndent()

    override fun indexOf(charCode: String): String {
        return "binarySearchRange(${strategy.rangeRef("decodedRangeStart")}, $charCode)"
    }

    override fun categoryAt(index: String): String {
        return "${strategy.rangeRef("decodedRangeCategory")}[$index]"
    }
}
