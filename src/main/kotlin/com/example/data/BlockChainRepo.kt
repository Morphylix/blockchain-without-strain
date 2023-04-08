package com.example.data

import com.example.models.Block
import com.example.models.BlockChain
import com.example.utils.PORT
import com.example.utils.currentNodePort
import com.example.utils.node1Port
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*

class BlockChainRepo(private val client: HttpClient) {

    suspend fun notifyBlockInserted(block: Block, url: String): Boolean {
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            header(PORT, currentNodePort)
            setBody(block)
        }
        return response.bodyAsText() == "1"
    }

    suspend fun askThirdNode(url: String): Block {
        val response = client.get(url) {
            contentType(ContentType.Application.Json)
        }
        return response.body()
    }

    fun sendLastBlock(): Block {
        return BlockChain.blockChain.last()
    }

    suspend fun validateBlockChain(url: String): MutableList<Block> {
        val response = client.get(url) {
            contentType(ContentType.Application.Json)
        }
        return response.body()
    }

    fun insertBlock(block: Block) {
        BlockChain.blockChain.add(block)
        println("Block was inserted. Current BlockChain size is ${BlockChain.blockChain.size}, last block hash is ${BlockChain.blockChain.last().hash}")
    }

    companion object {
        private var instance: BlockChainRepo? = null

        fun get(): BlockChainRepo {
            if (instance == null) {
                instance = BlockChainRepo(HttpClient(CIO) {
                    install(ContentNegotiation) {
                        gson()
                    }
                })
            }
            return instance!!
        }
    }
}