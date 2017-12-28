package org.astserver

import com.google.gson.JsonObject
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.whenever
import kotlin.test.assertEquals
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.Interval
import org.junit.Test

class MockRuleContext(private val ruleIndex: Int,
      private val interval: Interval) : ParserRuleContext() {
  public override fun getRuleIndex(): Int {
    return ruleIndex
  }

  public override fun getSourceInterval(): Interval {
    return interval
  }
}

class TestParseHandler {
  @Test fun testSuccess() {
    val rootNode = MockRuleContext(0, Interval(0, 6))
    rootNode.addChild(MockRuleContext(1, Interval(1, 1)))
    rootNode.addChild(MockRuleContext(2, Interval(3, 3)))
    rootNode.addChild(MockRuleContext(2, Interval(5, 5)))
    val mockLexerAndParser = mock<LexerAndParser>()
    whenever(mockLexerAndParser.parseRootRule("(* n n)"))
        .thenReturn(rootNode)
    val mockParserStore = mock<ParserStore>()
    whenever(mockParserStore.getLexerAndParser("TestGrammar"))
        .thenReturn(mockLexerAndParser)
    val printStream = StringPrintStream()
    val params = JsonObject()
    params.addProperty("grammar", "TestGrammar")
    params.addProperty("sourceText", "(* n n)")

    ParseHandler(mockParserStore).printResultOrError(params, printStream)

    assertEquals("\"result\":[0,0,6,[1,1,1],[2,3,3],[2,5,5]]",
        printStream.toString())
  }

  @Test fun testNoGrammar() {
    val output = StringPrintStream()
    val params = JsonObject()

    ParseHandler(mock<ParserStore>()).printResultOrError(params, output)

    assertEquals("\"error\":\"No grammar specified\"", output.toString())
  }

  @Test fun testGrammarNotString() {
    val output = StringPrintStream()
    val params = JsonObject()
    params.addProperty("grammar", 3)

    ParseHandler(mock<ParserStore>()).printResultOrError(params, output)

    assertEquals("\"error\":\"grammar not a string\"", output.toString())
  }

  @Test fun testParserNotFound() {
    val mockParserStore = mock<ParserStore>()
    doThrow(
        RuntimeException("TestGrammarParser not found")
    ).whenever(mockParserStore).getLexerAndParser(any())
    val params = JsonObject()
    params.addProperty("grammar", "TestGrammar")
    params.addProperty("sourceText", "Test source")
    val output = StringPrintStream()

    ParseHandler(mockParserStore).printResultOrError(params, output)

    assertEquals("\"error\":\"Parser not found for grammar\"", output.toString())
  }

  @Test fun testNoSourceText() {
    val output = StringPrintStream()
    val params = JsonObject()
    params.addProperty("grammar", "TestGrammar")
    val mockParserStore = mock<ParserStore>()
    whenever(mockParserStore.getLexerAndParser("TestGrammar"))
        .thenReturn(mock<LexerAndParser>())

    ParseHandler(mockParserStore).printResultOrError(params, output)

    assertEquals("\"error\":\"No sourceText specified\"", output.toString())
  }

  @Test fun testSourceTextNotString() {
    val output = StringPrintStream()
    val params = JsonObject()
    params.addProperty("grammar", "TestGrammar")
    params.add("sourceText", JsonObject())
    val mockParserStore = mock<ParserStore>()
    whenever(mockParserStore.getLexerAndParser("TestGrammar"))
        .thenReturn(mock<LexerAndParser>())

    ParseHandler(mockParserStore).printResultOrError(params, output)

    assertEquals("\"error\":\"sourceText not a string\"", output.toString())
  }
}
