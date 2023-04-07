package com.example

import com.example.controllers.BlockChainController
import com.example.controllers.NotificationReceivedCallback
import com.example.models.BlockChain
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.routing.configureAskThirdNodeReceiverRouting
import com.example.routing.configureNotificationReceiverRouting
import com.example.routing.configureValidationReceiverRouting
import com.example.utils.currentNodePort
import com.example.utils.mainNode
import com.example.utils.node1Port
import com.example.utils.node2Port
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.*
import java.net.ConnectException

/*
 args:
 [0] - current node port
 [1] - first node port
 [2] - second node port
 [3] - is current node main or not ("1" - main, "0" - secondary)
 */

fun main(args: Array<String>) {
    currentNodePort = args[0]
    node1Port = args[1]
    node2Port = args[2]
    mainNode = args[3] == "1"
    embeddedServer(CIO, port = currentNodePort.toInt(), host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()

    if (mainNode) {
        BlockChain.generateGenesis()
    }

    configureAskThirdNodeReceiverRouting()
    configureRouting()
    configureValidationReceiverRouting()
    var job = if (mainNode) {
        startMining(this)
    } else { // if current node isn't main it just asks for actual blockChain every 0.5 second until gets it
        waitForMainNode(this)
    }
    configureNotificationReceiverRouting(object : NotificationReceivedCallback {
        override fun onNotificationReceived() {
            println("Notification received => restarting coroutine and mining")
            job.cancel()
            job = startMining(this@module)
        }

    })
}

private fun startMining(coroutineScope: CoroutineScope): Job {
    return coroutineScope.launch(Dispatchers.Default) {
        while (true) {
//            if (BlockChain.blockChain.size % 10 == 0 && !mainNode) { // validate blockchain every 10 blocks
//                BlockChainController.validateBlockChain()
//            }
            BlockChainController.generateNewBlock()
        }
    }
}

private fun waitForMainNode(coroutineScope: CoroutineScope): Job {
    return coroutineScope.launch(Dispatchers.IO) {
        while (BlockChain.blockChain.isEmpty()) {
            try {
                BlockChainController.validateBlockChain()
            } catch (_: ConnectException) {
            }
            delay(500)
        }
        cancel()
        startMining(coroutineScope)
    }
}