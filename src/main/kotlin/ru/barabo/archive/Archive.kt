package ru.barabo.archive

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


object Archive {

    //private val logger = LoggerFactory.getLogger(Archive::class.java)

    fun tempFolder() :File = Cmd.tempFolder("a")

    fun packToZip(zipFilePath: String, vararg files: File) :File {
        File(zipFilePath).let { if (it.exists()) it.delete() }

        val zipFile = Files.createFile(Paths.get(zipFilePath))

        ZipOutputStream(Files.newOutputStream(zipFile)).use { out ->
            for (file in files) {
                FileInputStream(file).use { fi ->
                    BufferedInputStream(fi).use { origin ->
                        val entry = ZipEntry(file.name)
                        out.putNextEntry(entry)
                        origin.copyTo(out)
                    }
                }
            }
        }

        return File(zipFilePath)
    }

    fun packToZipStream(zipFilePath: String = "babloz.zip", dataFileName: String = "babloz.bak", inputStream: InputStream): File {
        File(zipFilePath).let { if (it.exists()) it.delete() }

        val zipFile = Files.createFile(Paths.get(zipFilePath))

        val zipEntry = ZipEntry(dataFileName)

        ZipOutputStream(Files.newOutputStream(zipFile)).use { out ->
            inputStream.use { origin ->
                out.putNextEntry(zipEntry)
                origin.copyTo(out)
            }
        }

        return File(zipFilePath)
    }

    fun unpackFromZipToString(zipFilePath: String = "babloz.zip"): String {

        var text: String = ""

        ZipFile(zipFilePath).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->

                    text += input.bufferedReader().use { it.readText() }
                }
            }
        }

        return text
    }

    fun upPackFromZip(zipFilePath: String) {

        val zipFile = File(zipFilePath).parentFile

        ZipFile(zipFilePath).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    File("${zipFile.absolutePath}/${entry.name}").outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}
