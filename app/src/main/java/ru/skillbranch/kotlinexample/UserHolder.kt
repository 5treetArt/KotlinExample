package ru.skillbranch.kotlinexample

import java.lang.IllegalArgumentException

object UserHolder {

    private val map = mutableMapOf<String, User>()

    private val telephoneNumberRegex = Regex("\\+\\d{11}")

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User{
        return User.makeUser(fullName, email = email, password = password)
            .also { require(isLoginUnique(it.login)) { "A user with this email already exists"} }
            .also { user -> map[user.login] = user}
    }

    fun registerUserByPhone(
        fullName: String,
        rawPhone: String
    ): User{
        return User.makeUser(fullName, phone = rawPhone)
            .also { require(isPhoneCorrect(it.login)) { "Enter a valid phone number starting with a + and containing 11 digits" } }
            .also { require(isLoginUnique(rawPhone)) { "A user with this phone already exists"} }
            .also { user -> map[rawPhone] = user}
    }

    fun loginUser(login: String, password: String) : String?{
        return map[login.trim().toLowerCase()]?.run {
            if (checkPassword(password)) userInfo
            else null
        }
    }

    fun importUsers(list: List<String>): List<User> =
        list.mapNotNull { row ->
            val (fullName: String, email: String, access: String, phone: String) = row.split(";")
            registerImportedUser(fullName, email, phone, access)
        }

    fun requestAccessCode(login: String) {
        check(map.keys.contains(login)) {"User must be registered"}
        map[login]!!.requestAccessCode()
    }

    fun clearData() {
        map.clear()
    }

    private fun registerImportedUser(
        fullName: String,
        email: String,
        phone: String,
        access: String
    ): User?{
        return when {
            !email.isBlank() -> registerImportedUserByEmail(fullName, email, access)
            !phone.isBlank() -> registerImportedUserByPhone(fullName, phone, access)
            else -> null//throw IllegalArgumentException("Email or Phone must be not null or blank")
        }
    }

    private fun registerImportedUserByEmail(
        fullName: String,
        email: String,
        access: String
    ): User{
        return User.makeUser(fullName, email = email, access = access)
            .also { require(isLoginUnique(it.login)) { "A user with this email already exists"} }
            .also { user -> map[user.login] = user}
    }

    private fun registerImportedUserByPhone(
        fullName: String,
        rawPhone: String,
        access: String
    ): User{
        return User.makeUser(fullName, phone = rawPhone, access = access)
            .also { require(isPhoneCorrect(it.login)) { "Enter a valid phone number starting with a + and containing 11 digits" } }
            .also { require(isLoginUnique(rawPhone)) { "A user with this phone already exists"} }
            .also { user -> map[rawPhone] = user}
    }

    private fun isLoginUnique(login: String): Boolean = !map.keys.contains(login)

    private fun isPhoneCorrect(login: String): Boolean = telephoneNumberRegex.matches(login)
}