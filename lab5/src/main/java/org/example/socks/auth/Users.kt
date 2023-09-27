package org.example.socks.auth

import java.io.File
import java.io.FileNotFoundException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException


class Users {
    var userMap: HashMap<String, String> = HashMap()
    private var rootMap: HashMap<String, String> = HashMap()
    private var adminMap: HashMap<String, String> = HashMap()
    private var userFile: File = File(this.javaClass.classLoader.getResource("users.txt")!!.path)
    private var rootFile: File = File(this.javaClass.classLoader.getResource("root.txt")!!.path)
    private var adminFile: File = File(this.javaClass.classLoader.getResource("admins.txt")!!.path)
    private var cipher: PasswordCipher = PasswordCipher("Wearemenmanlymen")

    init {
        getDataFromFile(userMap, userFile.canonicalPath)
        getDataFromFile(adminMap, adminFile.canonicalPath)
        getDataFromFile(rootMap, rootFile.canonicalPath)
    }

    @Throws(
        FileNotFoundException::class,
        IllegalBlockSizeException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class
    )
    fun getDataFromFile(map: HashMap<String, String>, pathname: String?) {
        val file = File(pathname!!)
        val magicReader = Scanner(file)
        val delimiter = "///"
        var userdata: Array<String>
        while (magicReader.hasNext()) {
            val aux = magicReader.next()
            userdata = aux.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            userdata[1] = cipher.decrypt(userdata[1])
            map[userdata[0]] = userdata[1]
        }
        magicReader.close()
    }
}

