package com.polimorph.walletquickadd

import java.math.BigDecimal

object AmountParser {
    private val candidate = Regex("""[-+]?\d[\d\s\u00A0.,]*""")

    fun parse(text: String): BigDecimal? {
        val raw = candidate.find(text)?.value
            ?.replace(" ", "")
            ?.replace("\u00A0", "")
            ?.trim()
            ?: return null
        if (raw.isBlank()) return null

        val comma = raw.lastIndexOf(',')
        val dot = raw.lastIndexOf('.')
        val decimalSeparator = when {
            comma >= 0 && dot >= 0 -> if (comma > dot) ',' else '.'
            comma >= 0 -> chooseSingleSeparator(raw, ',')
            dot >= 0 -> chooseSingleSeparator(raw, '.')
            else -> null
        }

        val normalized = buildString {
            raw.forEach { char ->
                when {
                    char == decimalSeparator -> append('.')
                    char == ',' || char == '.' -> Unit
                    else -> append(char)
                }
            }
        }
        return normalized.toBigDecimalOrNull()
    }

    private fun chooseSingleSeparator(value: String, separator: Char): Char? {
        val digitsAfter = value.length - value.lastIndexOf(separator) - 1
        return if (digitsAfter in 1..2) separator else null
    }
}
