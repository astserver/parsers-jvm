package org.astserver

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import kotlin.test.assertEquals
import org.junit.Test

class TestRpcDispatcher {
  @Test fun testHandleRpcSuccess() {
  }

  @Test fun testHandleRpcInvalidJson() {
    verifyResponse(
        "invalid json!!",
        "{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":\"Invalid JSON\"}")
  }

  @Test fun testHandleRpcNonObjectJson() {
    verifyResponse(
        "[]",
        "{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":\"Command not an object\"}")
  }

  @Test fun testHandleRpcNoId() {
    verifyResponse(
        "{\"jsonrpc\":\"2.0\",\"method\":\"mockMethod\"," +
            "\"params\":{\"grammar\":\"A\"}}",
        "{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":\"Missing id\"}")
  }

  @Test fun testHandleRpcNoMethod() {
    verifyResponse(
        "{\"jsonrpc\":\"2.0\",\"id\":\"1\"}",
        "{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"error\":\"Missing method\"}")
  }

  @Test fun testHandleRpcMethodNotString() {
    verifyResponse(
        "{\"jsonrpc\":\"2.0\",\"id\":\"2\",\"method\":{\"a\":\"b\"}}",
        "{\"jsonrpc\":\"2.0\",\"id\":\"2\",\"error\":\"Method not a string\"}")
    verifyResponse(
        "{\"jsonrpc\":\"2.0\",\"id\":\"3\",\"method\":2}",
        "{\"jsonrpc\":\"2.0\",\"id\":\"3\",\"error\":\"Method not a string\"}")
  }

  @Test fun testHandleRpcInvalidMethod() {
    verifyResponse(
        "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"nonExistentMethod\"," +
            "\"params\":2}",
        "{\"jsonrpc\":\"2.0\",\"id\":1,\"error\":\"Invalid method\"}")
  }

  @Test fun testHandleRpcNoParams() {
    verifyResponse(
        "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"mockMethod\"}",
        "{\"jsonrpc\":\"2.0\",\"id\":1,\"error\":\"Missing params\"}")
  }

  @Test fun testHandleRpcParamsNotObject() {
    verifyResponse(
        "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"mockMethod\"," +
            "\"params\":2}",
        "{\"jsonrpc\":\"2.0\",\"id\":1,\"error\":\"Params not an object\"}")
    verifyResponse(
        "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"mockMethod\"," +
            "\"params\":[1,2]}",
        "{\"jsonrpc\":\"2.0\",\"id\":1,\"error\":\"Params not an object\"}")
  }

  @Test fun testExceptionInHandler() {
    val mockHandler = mock<RpcHandler>()
    doThrow(
        RuntimeException("Test exception")
    ).whenever(mockHandler).printResultOrError(any(), any())
    val command = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"mockMethod\"," +
        "\"params\":{\"a\":1}}"

    val response = getResponse(command, mockHandler)

    assertEquals(
        "{\"jsonrpc\":\"2.0\",\"id\":1,\"error\":" +
            "\"java.lang.RuntimeException: Test exception\"}",
        response)
  }

  private fun verifyResponse(command: String, expectedResponse: String) {
    assertEquals(expectedResponse, getResponse(command, mock<RpcHandler>()))
  }

  private fun getResponse(command: String, mockHandler: RpcHandler): String {
    val handlerMap = mapOf("mockMethod" to mockHandler)
    val printStream = StringPrintStream()
    RpcDispatcher(handlerMap, printStream).handleRpc(command)
    return printStream.toString()
  }
}
