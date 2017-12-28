package org.astserver

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets

class StringPrintStream(
    private val byteStream: ByteArrayOutputStream = ByteArrayOutputStream()) :
        PrintStream(byteStream, true, "utf-8") {
  override fun toString(): String {
    return String(byteStream.toByteArray(), StandardCharsets.UTF_8)
  }
}
