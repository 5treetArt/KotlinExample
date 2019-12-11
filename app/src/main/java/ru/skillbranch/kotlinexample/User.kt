package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import ru.skillbranch.kotlinexample.extentions.clearPhoneNumber
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
    ) {

    val userInfo: String

    private val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .capitalize()

    private val initials: String
        get() = listOfNotNull(firstName, lastName)
            .map { it.first().toUpperCase() }
            .joinToString(" ")

    private var phone: String? = null
        set(value) {
            field = value?.clearPhoneNumber()
        }

    private var _login: String? = null

    internal var login: String
        set(value) {
            _login = value?.toLowerCase()
        }
        get() = _login!!


    private var _salt: String? = null
    private val salt: String by lazy {
        _salt ?: ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
    }

    private lateinit var passwordHash: String

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null

    //for email
    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ): this(firstName, lastName, email = email, meta = mapOf("auth" to "password")){
        println("Secondary mail constructor")
        passwordHash = getSaltedHash(password)
    }

    //for email and hash
    constructor(
        firstName: String,
        lastName: String?,
        email: Email,
        hashAndSalt: Pair<String, String?>
    ): this(firstName, lastName, email = email.email, meta = mapOf("src" to "csv")){
        println("Secondary mail and hash constructor")
        passwordHash = hashAndSalt.first
        _salt = hashAndSalt.second ?: ""
    }

    //for phone and hash
    constructor(
        firstName: String,
        lastName: String?,
        phone: Phone,
        hashAndSalt: Pair<String, String?>
    ): this(firstName, lastName, rawPhone = phone.rawPhone, meta = mapOf("src" to "csv")){
        println("Secondary phone and hash constructor")
        passwordHash = hashAndSalt.first
        _salt = hashAndSalt.second ?: ""
    }

    //for phone
    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ): this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")){
        println("Secondary phone constructor")
        requestAccessCode(rawPhone)
    }

    init {
        println("First init block, primary constructor was called")

        check(!firstName.isBlank()) {"FirstName must be not blank"}
        check(!email.isNullOrBlank() || !rawPhone.isNullOrBlank()) {"Email or phone must be not blank"}

        phone = rawPhone
        login = email ?: phone!!

        userInfo = """
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    fun checkPassword(pass: String) = getSaltedHash(pass) == passwordHash

    fun changePassword(oldPass: String, newPass: String){
        if (checkPassword(oldPass)) passwordHash = getSaltedHash(newPass)
        else throw IllegalArgumentException("The entered password does not match the current password")
    }

    fun requestAccessCode() = phone?.let { requestAccessCode(it) }

    private fun requestAccessCode(rawPhone: String) {
        val code = generateAccessCode()
        passwordHash = getSaltedHash(code)
        accessCode = code
        sendAccessCodeToUser(rawPhone, code)
    }

    private fun getSaltedHash(password: String): String = salt.plus(password).md5()

    private fun generateAccessCode(): String {
        val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

        return StringBuilder().apply {
            repeat(6) {
                (possible.indices).random().also {index ->
                    append(possible[index])
                }
            }
        }.toString()
    }


    private fun sendAccessCodeToUser(phone: String, code: String) {
        println("..... sending access code: $code on $phone")
    }

    private fun String.md5(): String = MessageDigest.getInstance("MD5")
            .digest(toByteArray())
            .let { BigInteger(1, it) }
            .toString(16)
            .padStart(32, '0')

    companion object Factory{
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null,
            access: String? = null
        ):User{
            val (firstName, lastName) = fullName.fullNameToPair()

            return when{
                !phone.isNullOrBlank() && access.isNullOrBlank() ->
                    User(firstName, lastName, phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() && access.isNullOrBlank() ->
                    User(firstName, lastName, email, password)
                !email.isNullOrBlank() && !access.isNullOrBlank() ->
                    User(firstName, lastName, Email(email), access.accessToPair())
                !phone.isNullOrBlank() && !access.isNullOrBlank() ->
                    User(firstName, lastName, Phone(phone), access.accessToPair())
                else -> throw IllegalArgumentException("Email or Phone must be not null or blank")
            }
        }

        enum class authType{
            SMS,
            PASSWORD
        }

        private fun String.fullNameToPair(): Pair<String, String?> {
            return split(" ")
                .filter { it.isNotBlank() }
                .run { when(size){
                    1 -> first() to null
                    2 -> first() to last()
                    else -> throw IllegalArgumentException("Fullname must contain first name " +
                            "and last name, current split result ${this@fullNameToPair}")
                }
            }
        }

        /*
        * get hash and salt from string
        *
        * returns pair of hash and salt
        */
        private fun String.accessToPair(): Pair<String, String?> {
            return split(":")
                .filter { it.isNotBlank() }
                .run {
                    when (size) {
                        1 -> first() to null
                        2 -> last() to first()
                        else -> throw IllegalArgumentException(
                            "Access must contain first name " +
                                    "and last name, current split result ${this@accessToPair}"
                        )
                    }
                }
        }
    }

    class Phone (val rawPhone: String)

    class Email (val email: String)
}

