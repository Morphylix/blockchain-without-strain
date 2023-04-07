package com.example.routing

import com.example.models.BlockChain
import com.example.utils.VALIDATE_BLOCKCHAIN_ROUTING
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureValidationReceiverRouting() {
    routing {
        get(VALIDATE_BLOCKCHAIN_ROUTING) {
            call.respond(BlockChain.blockChain)
        }
    }
}