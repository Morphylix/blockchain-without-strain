package com.example.routing

import com.example.controllers.BlockChainController
import com.example.controllers.NotificationReceivedCallback
import com.example.models.Block
import com.example.utils.BLOCK_INSERTED_ROUTING
import com.example.utils.PORT
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureNotificationReceiverRouting(notificationReceivedCallback: NotificationReceivedCallback) {
    routing {
        post(BLOCK_INSERTED_ROUTING) {
            val block = call.receive(Block::class)
            val senderNodePort = call.request.headers[PORT]
            println("Notification from other node. It generated block $block")
            if (senderNodePort != null) {
                BlockChainController.handleReceivedBlock(block, senderNodePort, notificationReceivedCallback)
            }
            call.respond(1)
        }
    }
}