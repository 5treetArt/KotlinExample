package ru.skillbranch.kotlinexample.extensions

val notNumbersAndPlusRegex = "[^+\\d]".toRegex()

fun String.clearPhoneNumber(): String = replace(notNumbersAndPlusRegex, "")