package org.astserver

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.RuleContext
import org.antlr.v4.runtime.TokenStream
import org.junit.Test

abstract class MockGrammarParser(val tokens: TokenStream) : Parser(tokens) {
  abstract fun sourceFileRootRule(): RuleContext
}

class TestParserStore {
  @Test fun testGetLexerAndParser() {
    val mockParserClass = (mock<MockGrammarParser>() as Parser).javaClass
    val mockLexerClass = mock<Lexer>().javaClass
    val mockClassLoader = mock<ClassLoader>()
    doAnswer {
      val className = it.getArgument<String>(0)
      if (className.equals("test.fakeparser.MockGrammarLexer")) {
        mockLexerClass
      } else if (className.equals("test.fakeparser.MockGrammarParser")) {
        mockParserClass
      } else {
        throw ClassNotFoundException()
      }
    }.whenever(mockClassLoader).loadClass(any())

    val parserStore = ParserStore(mockClassLoader, "test.fakeparser.",
        mapOf("MockGrammar" to "sourceFileRootRule",
            "UnimplementedGrammar" to "rootRule"))

    val lexerAndParser = parserStore.getLexerAndParser("MockGrammar")
    assertEquals(0, lexerAndParser.parser.errorListeners.size)
    assertEquals(mockParserClass, lexerAndParser.parser.javaClass)
    assertEquals(mockLexerClass, lexerAndParser.lexer.javaClass)
    assertEquals("sourceFileRootRule", lexerAndParser.rootRuleMethod.name)

    assertFailsWith(IllegalArgumentException::class) {
      parserStore.getLexerAndParser("NonExistentGrammar")
    }

    assertFailsWith(ClassNotFoundException::class) {
      parserStore.getLexerAndParser("UnimplementedGrammar")
    }
  }
}
