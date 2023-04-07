package com.example.plugins

import com.example.models.Block
import com.example.models.BlockChain
import com.example.utils.sha256Hash
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.configureRouting() {
    routing {
        get("/") {
            println(BlockChain.blockChain.map { listOf(it.index, it.prevHash, it.hash).joinToString(separator = " ") })
            call.respond(BlockChain.blockChain.map { listOf(it.index, it.prevHash, it.hash).joinToString(separator = " ") })
        }
    }
}
