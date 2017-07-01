package de.rakhman.kotlin.httpserver

import fi.iki.elonen.NanoHTTPD


fun NanoHTTPD.IHTTPSession.getBody(): String? {
    val map = mutableMapOf<String, String>()
    parseBody(map)
    return map["postData"]
}