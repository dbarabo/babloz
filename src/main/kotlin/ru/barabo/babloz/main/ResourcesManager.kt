package ru.barabo.babloz.main

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import java.io.InputStreamReader

object ResourcesManager {
    //private val logger = LoggerFactory.getLogger(ResourcesManager::class.java)!!

    private const val ICO_PATH = "/ico/"

    fun icon(icoName :String) :ImageView = loadIcon(icoName)

    fun image(icoName :String) :Image = Image(pathResource(ICO_PATH + icoName))

    private fun loadIcon(icoName :String) :ImageView = ImageView(pathResource(ICO_PATH + icoName))

    private fun pathResource(fullPath :String)
            = ResourcesManager::class.java.getResource(fullPath).toURI().toString()

    private const val DB_STRUCTURE = "/db/db.sql"

    private const val DB_DATA = "/db/init.sql"

    private fun textFileInJar(fullPath :String) =
            InputStreamReader(javaClass.getResourceAsStream(fullPath), "UTF-8").buffered().readText()

    fun dbStructureText(): String = textFileInJar(DB_STRUCTURE)

    fun dbDataText(): String = textFileInJar(DB_DATA)

}