package com.example.routing

import com.example.controllers.BlockChainController
import com.example.data.BlockChainRepo
import com.example.utils.ASK_THIRD_NODE_ROUTING
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureAskThirdNodeReceiverRouting() {
    routing {
        get(ASK_THIRD_NODE_ROUTING) {
            call.respond(BlockChainController.sendLastBlock())
        }
    }
}