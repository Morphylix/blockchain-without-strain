package com.example.models

import com.example.utils.mainNode
import com.example.utils.sha256Hash

object BlockChain {
    var blockChain: MutableList<Block> = mutableListOf()

    fun generateGenesis(): Boolean {
        if (mainNode) {
            blockChain.add(
                Block(0, "", sha256Hash("Genesis").dropLast(6).plus("000000"), "Genesis", 0)
            )
        }
        return mainNode
    }
}
