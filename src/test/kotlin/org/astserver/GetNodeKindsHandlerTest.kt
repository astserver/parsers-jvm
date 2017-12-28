package org.astserver

import com.google.gson.JsonObject
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import java.lang.reflect.Method
import kotlin.test.assertEquals
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Parser
import org.junit.Test

class TestGetNodeKindsHandler {
  @Test fun testSuccessfulCall() {
    val mockParser = mock<Parser>()
    val mockLexerAndParser = LexerAndParser(mock<Lexer>(), mockParser,
        mock<Method>())
    whenever(mockParser.getRuleNames()).thenReturn(arrayOf("r1", "r2"))
    val mockParserStore = mock<ParserStore>()
    whenever(mockParserStore.getLexerAndParser("TestGrammar"))
        .thenReturn(mockLexerAndParser)
    val output = StringPrintStream()
    val params = JsonObject()
    params.addProperty("grammar", "TestGrammar")

    GetNodeKindsHandler(mockParserStore).printResultOrError(params, output)

    assertEquals("\"result\":[\"r1\",\"r2\"]", output.toString())
  }

  @Test fun testNoGrammar() {
    val output = StringPrintStream()
    val params = JsonObject()

    GetNodeKindsHandler(mock<ParserStore>()).printResultOrError(params, output)

    assertEquals("\"error\":\"No grammar specified\"", output.toString())
  }

  @Test fun testGrammarNotString() {
    val output = StringPrintStream()
    val params = JsonObject()
    params.addProperty("grammar", 3)

    GetNodeKindsHandler(mock<ParserStore>()).printResultOrError(params, output)

    assertEquals("\"error\":\"grammar not a string\"", output.toString())
  }

  @Test fun testParserNotFound() {
    val mockParserStore = mock<ParserStore>()
    doThrow(
        RuntimeException("TestGrammarParser not found")
    ).whenever(mockParserStore).getLexerAndParser(any())
    val params = JsonObject()
    params.addProperty("grammar", "TestGrammar")
    val output = StringPrintStream()

    GetNodeKindsHandler(mockParserStore).printResultOrError(params, output)

    assertEquals("\"error\":\"Parser not found for grammar\"", output.toString())
  }
}
