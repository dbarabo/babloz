package ru.barabo.babloz.db.importer

import java.io.File
import java.nio.charset.Charset

interface Importer {

    fun import(file: File, charset: Charset = Charsets.UTF_8)
}