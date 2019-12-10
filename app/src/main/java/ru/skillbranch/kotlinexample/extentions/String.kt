package ru.skillbranch.kotlinexample.extentions

val notNumbersAndPlusRegex = "[^+\\d]".toRegex()

fun String.clearPhoneNumber(): String = replace(notNumbersAndPlusRegex, "")