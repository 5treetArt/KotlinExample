package ru.skillbranch.kotlinexample

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

    fun requestAccessCode(login: String) {
        check(map.keys.contains(login)) {"User must be registered"}
        map[login]!!.requestAccessCode()
    }

    fun clearData() {
        map.clear()
    }

    private fun isLoginUnique(login: String): Boolean = !map.keys.contains(login)

    private fun isPhoneCorrect(login: String): Boolean = telephoneNumberRegex.matches(login)
}