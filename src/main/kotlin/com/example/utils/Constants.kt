package com.example.utils

const val HASH_TYPE = "SHA-256"
val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
const val STRING_LENGTH = 256
var node1Port = ""
var node2Port = ""
var currentNodePort = ""
const val BASE_URL = "http://127.0.0.1:"
const val BLOCK_INSERTED_ROUTING = "/notify_block_inserted"
const val VALIDATE_BLOCKCHAIN_ROUTING = "/validate_blockchain"
const val ASK_THIRD_NODE_ROUTING = "/ask_third_node"
var mainNode = false
const val PORT = "PORT"