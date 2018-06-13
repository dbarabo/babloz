package ru.barabo.archive

import java.io.File
import java.util.*

object Cmd {

    //private val logger = LoggerFactory.getLogger(Cmd::class.java)

    private fun cTemp(prefix :String) =  "$TEMP_FOLDER/$prefix${Date().time}"

    private fun String.cyrReplace() =  this.replace("[^A-Za-z0-9] ".toRegex(), "F")

    fun tempFolder(prefix :String) : File {
        val temp = File(cTemp(prefix.cyrReplace()) )

        temp.mkdirs()

        return temp
    }

    val JAR_FOLDER = File(Cmd::class.java.protectionDomain.codeSource.location.path).parentFile.path!!

    private val TEMP_FOLDER = "$JAR_FOLDER/temp"
}

