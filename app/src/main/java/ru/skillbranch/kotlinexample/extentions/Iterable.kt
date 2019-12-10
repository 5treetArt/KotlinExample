package ru.skillbranch.kotlinexample.extentions

//listOf(1, 2, 3).dropLastUntil{ it==2 } // [1]
//"House Nymeros Martell of Sunspear".split(" ")
//.dropLastUntil{ it == "of" } // [House, Nymeros, Martell])
fun <T> Iterable<T>.dropLastUntil(predicate: (T) -> Boolean): Iterable<T> =
    reversed().dropWhile { !predicate(it) }.drop(1).reversed()
