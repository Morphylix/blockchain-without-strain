package com.example.controllers

import com.example.data.BlockChainRepo
import com.example.models.Block
import com.example.models.BlockChain
import com.example.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BlockChainController {

    private val url1 = BASE_URL + node1Port
    private val url2 = BASE_URL + node2Port

    suspend fun validateBlockChain(): Boolean { // add more complex logic to cover more situations (at least add url2 check)
        val requestedBlockChain = BlockChainRepo.get().validateBlockChain(url1 + VALIDATE_BLOCKCHAIN_ROUTING)
        return if (requestedBlockChain == BlockChain.blockChain) {
            println("Current node is ACTUAL. Blockchain wasn't updated")
            true
        } else {
            BlockChain.blockChain = requestedBlockChain
            println("Current node is in MINORITY. Updated current blockchain. Cool!")
            false
        }
    }

    suspend fun generateNewBlock(): Block? {
        var gotBlock = false
        val prevBlock = BlockChain.blockChain.lastOrNull()
        if (prevBlock == null) { // It means that this node is not main one, and it's blockchain is empty so we
            // have to ask other nodes for an actual blockchain version
            // TODO: ask other nodes (Why does it work lol)
            BlockChainRepo.get().validateBlockChain(url1 + VALIDATE_BLOCKCHAIN_ROUTING)
            //BlockChainRepo.get().validateBlockChain(url2)
            return null
        }
        val currentBlock = Block(
            BlockChain.blockChain.size,
            prevBlock.hash,
            "",
            generateBlockData(),
            0
        )
        while (!gotBlock) {
            val hashInput = currentBlock.index.toString() +
                    currentBlock.prevHash +
                    currentBlock.data +
                    currentBlock.nonce.toString()
            val hashOutput = sha256Hash(hashInput)
            if (hashOutput.takeLast(6) == "000000") {
                gotBlock = true
                currentBlock.hash = hashOutput
                println("Successfully generated block with $hashOutput. Inserting it")
                handleReceivedBlock(currentBlock, currentNodePort)

                withContext(Dispatchers.IO) {
                    try {
                        BlockChainRepo.get().notifyBlockInserted(currentBlock, url1 + BLOCK_INSERTED_ROUTING)
                        BlockChainRepo.get().notifyBlockInserted(currentBlock, url2 + BLOCK_INSERTED_ROUTING)
                    } catch (_: Exception) {}
                }
            } else {
                currentBlock.nonce++
            }
        }
        return currentBlock
    }

    suspend fun handleReceivedBlock(
        block: Block,
        senderNodePort: String,
        notificationReceivedCallback: NotificationReceivedCallback? = null
    ) {
        val lastBlock = BlockChain.blockChain.last()
        if (block.index == lastBlock.index + 1 && block.prevHash == lastBlock.hash) { // got a valid block
            println("handling received block. Everything is OK. Inserting it")
            BlockChainRepo.get().insertBlock(block)
            notificationReceivedCallback?.onNotificationReceived()
        } else if (senderNodePort != currentNodePort) { // if node is in minority or sender is in minority (IF SENDER IS CURRENT NODE JUST SKIP)
            val askUrl = if (senderNodePort == node1Port) url2 + ASK_THIRD_NODE_ROUTING else url1 + ASK_THIRD_NODE_ROUTING
            val thirdNodeBlock = BlockChainRepo.get().askThirdNode(askUrl)
            if (thirdNodeBlock.hash == block.hash) { // => our node is in minority
                println("Node is in minority. Validating full blockchain")
                val validateUrl = if (senderNodePort == node1Port) url2 + VALIDATE_BLOCKCHAIN_ROUTING else url1 + VALIDATE_BLOCKCHAIN_ROUTING
                BlockChainRepo.get().validateBlockChain(validateUrl)
            } else if (thirdNodeBlock.hash == lastBlock.hash) { // => our node is NOT in minority
                println("Node is actual. Nothing to do")
            }
        }
    }

    fun sendLastBlock(): Block {
        return BlockChainRepo.get().sendLastBlock()
    }

    private fun generateBlockData(): String {
        return generateRandomData()
    }
}

interface NotificationReceivedCallback {
    fun onNotificationReceived()
}