package com.example.model

data class DialKey(
    val digit: String,
    val letters: String,
    val secondaryChar: String? = null
)

val DIAL_PAD_KEYS = listOf(
    DialKey("1", ""),
    DialKey("2", "A B C"),
    DialKey("3", "D E F"),
    DialKey("4", "G H I"),
    DialKey("5", "J K L"),
    DialKey("6", "M N O"),
    DialKey("7", "P Q R S"),
    DialKey("8", "T U V"),
    DialKey("9", "W X Y Z"),
    DialKey("*", ""),
    DialKey("0", "+", secondaryChar = "+"),
    DialKey("#", "")
)
