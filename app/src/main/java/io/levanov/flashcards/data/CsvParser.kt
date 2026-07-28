package io.levanov.flashcards.data

import java.io.InputStream

/**
 * Pure-Kotlin CSV parser for deck files. No Android imports — JVM-unit-testable.
 *
 * Format (must stay compatible with swedish-study/cards/flashcards.py):
 * - Columns: swedish,english,example — exactly 3, in this order.
 * - First row may be a header (swedish,english,...) — skipped only if it is
 *   the very first line of the file.
 * - Blank lines and rows whose first cell starts with `#` are skipped.
 * - Fields containing commas are quoted (standard CSV); a doubled `""` inside
 *   a quoted field yields a literal `"`.
 * - Every cell is trimmed of surrounding whitespace.
 * - Missing columns default to the empty string. Columns beyond 3 are ignored.
 */
object CsvParser {

    fun parse(input: InputStream): List<Card> =
        input.bufferedReader(Charsets.UTF_8).use { parse(it.readText()) }

    fun parse(text: String): List<Card> {
        val cards = mutableListOf<Card>()
        var isFirstLine = true
        for (line in text.lines()) {
            if (line.isBlank()) {
                isFirstLine = false
                continue
            }
            val row = parseLine(line).map { it.trim() }
            if (row.all { it.isEmpty() }) {
                isFirstLine = false
                continue
            }
            if (row[0].startsWith("#")) {
                isFirstLine = false
                continue
            }
            if (isFirstLine && row[0].equals("swedish", ignoreCase = true)) {
                isFirstLine = false
                continue
            }
            isFirstLine = false
            cards += Card(
                swedish = row[0],
                english = row.getOrElse(1) { "" },
                example = row.getOrElse(2) { "" },
            )
        }
        return cards
    }

    /**
     * Minimal RFC-4180-ish line parser: quotes, escaped "" quotes, commas.
     * Lenient: an unterminated quote ends the field at EOL (no exception).
     * Returns the list of raw (untrimmed) fields.
     */
    private fun parseLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        var i = 0
        val n = line.length
        while (i <= n) {
            val field = StringBuilder()
            if (i < n && line[i] == '"') {
                i++
                while (i < n) {
                    val c = line[i]
                    if (c == '"') {
                        if (i + 1 < n && line[i + 1] == '"') {
                            field.append('"')
                            i += 2
                        } else {
                            i++
                            break
                        }
                    } else {
                        field.append(c)
                        i++
                    }
                }
                while (i < n && line[i] != ',') i++
            } else {
                while (i < n && line[i] != ',') {
                    field.append(line[i])
                    i++
                }
            }
            fields.add(field.toString())
            if (i < n && line[i] == ',') {
                i++
            } else {
                break
            }
        }
        return fields
    }
}