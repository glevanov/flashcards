package io.levanov.flashcards.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class CsvParserTest {

    @Test
    fun `parses basic three-column rows`() {
        val csv = "en fritid,free time,\"På min fritid spelar jag fotboll.\"\n" +
            "en sport,a sport,\"Tennis är en populär sport i Sverige.\""
        val cards = CsvParser.parse(csv)
        assertEquals(2, cards.size)
        assertEquals("en fritid", cards[0].swedish)
        assertEquals("free time", cards[0].english)
        assertEquals("På min fritid spelar jag fotboll.", cards[0].example)
        assertEquals("en sport", cards[1].swedish)
        assertEquals("a sport", cards[1].english)
        assertEquals("Tennis är en populär sport i Sverige.", cards[1].example)
    }

    @Test
    fun `skips header on first line`() {
        val csv = "swedish,english,example\n" +
            "träna,to exercise,ex1\n" +
            "en kurs,a course,ex2"
        val cards = CsvParser.parse(csv)
        assertEquals(2, cards.size)
        assertEquals("träna", cards[0].swedish)
        assertEquals("en kurs", cards[1].swedish)
    }

    @Test
    fun `keeps swedish-looking row when not first line`() {
        val csv = "en fritid,free time,ex\n" +
            "swedish,english,example"
        val cards = CsvParser.parse(csv)
        assertEquals(2, cards.size)
        assertEquals("swedish", cards[1].swedish)
        assertEquals("english", cards[1].english)
        assertEquals("example", cards[1].example)
    }

    @Test
    fun `skips comment lines`() {
        val csv = "# this is a comment\n" +
            "  # indented comment\n" +
            "stor,big,ex"
        val cards = CsvParser.parse(csv)
        assertEquals(1, cards.size)
        assertEquals("stor", cards[0].swedish)
    }

    @Test
    fun `skips blank and whitespace-only lines`() {
        val csv = "\n" +
            "   \n" +
            ",,\n" +
            "en bil,a car,ex\n" +
            "\n" +
            "  \t \n"
        val cards = CsvParser.parse(csv)
        assertEquals(1, cards.size)
        assertEquals("en bil", cards[0].swedish)
    }

    @Test
    fun `parses quoted field containing comma`() {
        val csv = "en match,\"a match / game, extra\",ex"
        val cards = CsvParser.parse(csv)
        assertEquals(1, cards.size)
        assertEquals("a match / game, extra", cards[0].english)
        assertEquals("en match", cards[0].swedish)
    }

    @Test
    fun `parses escaped double quotes`() {
        val csv = "\"Hon sa \"\"hej\"\"\",she said hi,ex"
        val cards = CsvParser.parse(csv)
        assertEquals(1, cards.size)
        assertEquals("Hon sa \"hej\"", cards[0].swedish)
        assertEquals("she said hi", cards[0].english)
    }

    @Test
    fun `handles missing example column`() {
        val csv = "träna,to exercise"
        val cards = CsvParser.parse(csv)
        assertEquals(1, cards.size)
        assertEquals("träna", cards[0].swedish)
        assertEquals("to exercise", cards[0].english)
        assertEquals("", cards[0].example)
    }

    @Test
    fun `handles missing english column`() {
        val csv = "hej"
        val cards = CsvParser.parse(csv)
        assertEquals(1, cards.size)
        assertEquals("hej", cards[0].swedish)
        assertEquals("", cards[0].english)
        assertEquals("", cards[0].example)
    }

    @Test
    fun `ignores columns beyond three`() {
        val csv = "sv,en,ex,extra1,extra2"
        val cards = CsvParser.parse(csv)
        assertEquals(1, cards.size)
        assertEquals("sv", cards[0].swedish)
        assertEquals("en", cards[0].english)
        assertEquals("ex", cards[0].example)
    }

    @Test
    fun `preserves swedish characters`() {
        val csv = "lång,long,ex\nlägenhet,apartment,ex"
        val cards = CsvParser.parse(csv)
        assertEquals(2, cards.size)
        assertEquals("lång", cards[0].swedish)
        assertEquals("lägenhet", cards[1].swedish)
    }

    @Test
    fun `handles CRLF line endings`() {
        val csv = "en fritid,free time,ex1\r\n" +
            "en sport,a sport,ex2\r\n"
        val cards = CsvParser.parse(csv)
        assertEquals(2, cards.size)
        assertEquals("en fritid", cards[0].swedish)
        assertEquals("en sport", cards[1].swedish)
        assertEquals("ex2", cards[1].example)
    }

    @Test
    fun `trims whitespace around cells`() {
        val csv = "  stor , big ,\" x \""
        val cards = CsvParser.parse(csv)
        assertEquals(1, cards.size)
        assertEquals("stor", cards[0].swedish)
        assertEquals("big", cards[0].english)
        assertEquals("x", cards[0].example)
    }

    @Test
    fun `input stream overload reads utf-8`() {
        val text = "lång,long ett åäö"
        val fromText = CsvParser.parse(text)
        val fromStream = CsvParser.parse(ByteArrayInputStream(text.toByteArray(Charsets.UTF_8)))
        assertEquals(fromText, fromStream)
        assertEquals("lång", fromStream[0].swedish)
    }

    @Test
    fun `empty input yields no cards`() {
        val cards = CsvParser.parse("")
        assertTrue(cards.isEmpty())
    }
}