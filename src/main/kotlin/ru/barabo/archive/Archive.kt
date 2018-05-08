package ru.barabo.archive

import org.slf4j.LoggerFactory
import ru.barabo.cmd.Cmd
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


object Archive {

    private val logger = LoggerFactory.getLogger(Archive::class.java)

    fun tempFolder() :File = Cmd.tempFolder("a")

    private fun tempArchive(ext :String = "cab") :File = File("${tempFolder().absolutePath}/temp.$ext")

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
