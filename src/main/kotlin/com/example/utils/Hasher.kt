package com.example.utils

import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

fun sha256Hash(input: String): String {
    val bytes = MessageDigest
        .getInstance(HASH_TYPE)
        .digest(input.toByteArray())
    //println("Bytes: ${DatatypeConverter.printHexBinary(bytes).uppercase()}")
    return DatatypeConverter.printHexBinary(bytes).uppercase()
}