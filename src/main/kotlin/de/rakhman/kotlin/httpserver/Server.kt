package de.rakhman.kotlin.httpserver

import fi.iki.elonen.NanoHTTPD

abstract class AbstractServer: NanoHTTPD(8080) {
    override fun serve(session: IHTTPSession): Response {
        return newFixedLengthResponse(Response.Status.NOT_FOUND,"application/json", "Not found")
    }
}