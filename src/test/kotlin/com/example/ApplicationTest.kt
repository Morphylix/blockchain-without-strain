package com.example

import com.example.controllers.BlockChainController
import com.example.data.BlockChainRepo
import com.example.models.Block
import com.example.models.BlockChain
import com.example.utils.*
import com.google.gson.GsonBuilder
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test


class ApplicationTest {

    @Test
    fun testValidateGenesisManually() {
        initMainNode()
        mainNode = false
        assertEquals(false, BlockChain.generateGenesis()) // only main node can generate genesis
        mainNode = true
        assertEquals(true, BlockChain.generateGenesis())
        val genesis = BlockChain.blockChain[0]
        assertEquals("000000", genesis.hash.takeLast(6))
        assertEquals("Genesis", genesis.data)
    }

    @Test
    fun testValidateBlockChainManually() {
        initMainNode()
        BlockChain.generateGenesis()
        val genesis = BlockChain.blockChain[0]
        val secondBlock: Block?
        val thirdBlock: Block?
        val fourthBlock: Block?
        runBlocking {
            secondBlock = BlockChainController.generateNewBlock()
            thirdBlock = BlockChainController.generateNewBlock()
            fourthBlock = BlockChainController.generateNewBlock()
        }
        assertEquals(genesis.hash, secondBlock?.prevHash)
        assertEquals(secondBlock?.hash, thirdBlock?.prevHash)
        assertEquals(thirdBlock?.hash, fourthBlock?.prevHash)
        assertEquals(4, BlockChain.blockChain.size)
        assertEquals("000000", genesis.hash.takeLast(6))
        assertEquals("000000", secondBlock?.hash?.takeLast(6))
        assertEquals("000000", thirdBlock?.hash?.takeLast(6))
        assertEquals("000000", fourthBlock?.hash?.takeLast(6))

        assertEquals(0, genesis.index)
        assertEquals(1, secondBlock?.index)
        assertEquals(2, thirdBlock?.index)
        assertEquals(3, fourthBlock?.index)
    }

    @Test
    fun testBlockInsertedNotification() {
        initMainNode()
        BlockChain.generateGenesis()
        runBlocking {
            BlockChainController.generateNewBlock()
        }
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("""1"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = BlockChainRepo(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                gson()
            }
        })

        runBlocking {
            assertEquals(
                true,
                client.notifyBlockInserted(BlockChain.blockChain.last(), BLOCK_INSERTED_ROUTING + node1Port)
            )
        }
    }

    @Test
    fun testBlockInsertedNotificationFailed() {
        initMainNode()
        BlockChain.generateGenesis()
        runBlocking {
            BlockChainController.generateNewBlock()
        }
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("""0"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = BlockChainRepo(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                gson()
            }
        })

        runBlocking {
            assertEquals(
                false,
                client.notifyBlockInserted(BlockChain.blockChain.last(), BLOCK_INSERTED_ROUTING + node1Port)
            )
        }
    }

    @Test
    fun testAskThirdNode() {
        initMainNode()
        BlockChain.generateGenesis()
        runBlocking {
            BlockChainController.generateNewBlock()
        }
        val mockEngine = MockEngine { request ->
            GsonBuilder().create().toJson(BlockChain.blockChain.last())
            respond(
                content = ByteReadChannel(GsonBuilder().create().toJson(BlockChain.blockChain.last())),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = BlockChainRepo(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                gson()
            }
        })

        runBlocking {
            assertEquals(
                BlockChain.blockChain.last(),
                client.askThirdNode(BLOCK_INSERTED_ROUTING + node2Port)
            )
        }
    }

    @Test
    fun testSendLastBlock() {
        initMainNode()
        BlockChain.generateGenesis()
        runBlocking {
            BlockChainController.generateNewBlock()
        }
        assertEquals(BlockChain.blockChain.last(), BlockChainRepo.get().sendLastBlock())
    }

    @Test
    fun testValidateBlockChain() {
        initMainNode()
        BlockChain.generateGenesis()
        runBlocking {
            BlockChainController.generateNewBlock()
        }
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(GsonBuilder().create().toJson(BlockChain.blockChain)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = BlockChainRepo(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                gson()
            }
        })

        runBlocking {
            assertEquals(
                BlockChain.blockChain,
                client.validateBlockChain(BLOCK_INSERTED_ROUTING + node1Port)
            )
        }
    }

    @Test
    fun testValidateBlockChainFailed() {
        initMainNode()
        BlockChain.generateGenesis()
        runBlocking {
            BlockChainController.generateNewBlock()
        }
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(GsonBuilder().create().toJson(listOf(BlockChain.blockChain.first()))),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = BlockChainRepo(HttpClient(mockEngine) {
            install(ContentNegotiation) {
                gson()
            }
        })

        runBlocking {
            assertNotEquals(
                BlockChain.blockChain,
                client.validateBlockChain(BLOCK_INSERTED_ROUTING + node1Port)
            )
        }
    }

    @Test
    fun testInsertBlock() {
        initMainNode()
        BlockChain.generateGenesis()
        val blockToInsert: Block?
        runBlocking {
            blockToInsert = BlockChainController.generateNewBlock()
        }
        BlockChain.blockChain.dropLast(1) // drop generated block to check if it is inserted correctly
        if (blockToInsert != null) {
            BlockChainRepo.get().insertBlock(blockToInsert)
        }
        assertEquals(blockToInsert, BlockChain.blockChain.last())
    }

    @Test
    fun testHandleReceivedBlock() {
        initMainNode()
        BlockChain.generateGenesis()
        runBlocking {
            BlockChainController.generateNewBlock()
        }
        val receivedBlock = Block(
            BlockChain.blockChain.size,
            BlockChain.blockChain.last().hash,
            sha256Hash("Received block").dropLast(6).plus("000000"),
            "receivedBlock", 0)

        runBlocking {
            BlockChainController.handleReceivedBlock(receivedBlock, node1Port)
        }
        assertEquals(receivedBlock, BlockChain.blockChain.last())
    }

    private fun initMainNode() {
        BlockChain.blockChain.clear()
        currentNodePort = "8080"
        node1Port = "8081"
        node2Port = "8082"
        mainNode = true
    }
}
