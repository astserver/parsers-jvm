package org.astserver

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonObject
import com.google.gson.JsonElement
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

private const val TEST_COMMAND_ID = "testCommandId"

class TestAstServer {
  private val gson = Gson()
  private val jsonParser = JsonParser()

  private val grammarsToExampleFiles = mapOf(
      "ANTLRv4" to "grammars-v4/antlr4/examples/three.g4",
      "Golang" to "grammars-v4/golang/examples/example.go",
      "Java9" to "grammars-v4/java9/examples/helloworld.java",
      "Php" to "grammars-v4/php/examples/db.php",
      "AltPython3" to "grammars-v4/python3alt/examples/shaldq.py",
      "Swift3" to "grammars-v4/swift3/examples/CodeSamples/AppDelegate.swift"
  )

  @Test fun testGetNodeKinds() {
    grammarsToExampleFiles.keys.forEach(::verifyHasNodeKinds)
  }

  @Test fun testParse() {
    grammarsToExampleFiles.forEach(::verifyParseSucceeds)
  }

  private fun verifyHasNodeKinds(grammar: String) {
    val params = JsonObject()
    params.addProperty("grammar", grammar)

    val result = getResult("getNodeKinds", params)

    assertTrue(result.isJsonArray, grammar)
    assertTrue(result.asJsonArray.size() > 0, grammar)
  }

  private fun verifyParseSucceeds(grammar: String, exampleFile: String) {
    val params = JsonObject()
    params.addProperty("grammar", grammar)
    params.addProperty("sourceText", readFile(exampleFile))

    val result = getResult("parse", params)

    assertTrue(result.isJsonArray, grammar)
    assertTrue(result.asJsonArray.size() > 0, grammar)
  }

  private fun readFile(file: String): String {
    return String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8)
  }

  private fun getResult(method: String, params: JsonObject): JsonElement {
    val response = getResponse(method, params)
    assertEquals(TEST_COMMAND_ID, response.getAsJsonPrimitive("id").asString)
    return response.get("result")
  }

  private fun getResponse(method: String, params: JsonObject): JsonObject {
    val command = JsonObject()
    command.addProperty("jsonrpc", "2.0")
    command.addProperty("id", TEST_COMMAND_ID)
    command.addProperty("method", method)
    command.add("params", params)
    val input = gson.toJson(command).byteInputStream(StandardCharsets.UTF_8)
    val output = StringPrintStream()

    runAstServer(input, output)

    val outputElement = jsonParser.parse(output.toString())
    assertTrue(outputElement.isJsonObject)
    return outputElement.asJsonObject
  }
}
