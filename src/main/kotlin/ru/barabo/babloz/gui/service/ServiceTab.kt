package ru.barabo.babloz.gui.service

import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Tab
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.importer.CssImporter
import ru.barabo.babloz.gui.dialog.LoginDb
import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.babloz.sync.Sync
import ru.barabo.babloz.sync.SyncLoader
import ru.barabo.babloz.sync.SyncSaver
import tornadofx.*
import java.io.File

object ServiceTab : Tab("Настройки", VBox()) {

    private val logger = LoggerFactory.getLogger(CssImporter::class.java)!!

    private var importButton: Button? = null

    init {
        form {
            fieldset {
                field("Импорт из CSS") {
                    button ("Нажмите для выбора...", ResourcesManager.icon("importcss.png")).apply {
                        importButton = this

                        setOnAction { importCss() }
                    }
                }

                field("Синхронизация") {
                    button ("Запуск Синхронизации...", ResourcesManager.icon("sync.png")).apply {

                        setOnAction { syncStart() }
                    }
                }

                field("Бэкап") {
                    button ("Сделать бэкап").apply {

                        setOnAction { SyncSaver.toZipBackup() }
                    }
                }

                field("Из Бэкапа") {
                    button ("Восстановить из Бэкапа").apply {

                        setOnAction { SyncLoader.fromZipBackup() }
                    }
                }
            }
        }
    }

    private fun importCss() {
        val files = chooseFile("Выбор импортируемого файла данных",
                arrayOf(FileChooser.ExtensionFilter("CSV files (*.csv) with UTF-8", "*.csv")))

        if(files.isEmpty()) return

        val (file) = files

        importButton?.text = file.absolutePath

        importCss(file)
    }

    private fun importCss(file: File) {
        try {
            CssImporter.import(file)
        } catch (e: Exception) {
            logger.error("importCss", e)

            alert(Alert.AlertType.ERROR, e.message ?: "Error")
        }
    }

    private fun syncStart() {

        if(Sync.isSuccessMailPropertySmtp()) {
            Sync.saveDbToEMail()
        } else {
            LoginDb.showAndWait().ifPresent {
                Sync.saveDbToEMail(it.first!!, it.second!!)
            }
        }
    }
}