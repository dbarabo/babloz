package ru.barabo.babloz.db

import ru.barabo.db.DbConnection
import ru.barabo.db.DbSetting
import ru.barabo.db.Session
import java.io.File

object BablozConnection :DbConnection(DbSetting(driver = "org.sqlite.JDBC",
        url = "jdbc:sqlite:babloz.db", user = "", password = "", selectCheck = "select 1 from currency where id = 0")) {

    init {
        checkCreateStructure()
    }

    private fun checkCreateStructure() {

        val session = addSession(false)

        if(!checkBase(session)) {
            createStructure(session)
        }
        session.isFree = true
    }

    private val DB_STRUCTURE = "/db/db.sql"

    private val DB_DATA = "/db/init.sql"

    private fun pathResource(fullPath :String) :File {
        val path = DbConnection::class.java.getResource(fullPath).file//   toURI().toString()
 //       logger.info("pathResource=$path")
        return File(path)
    }

    private fun createStructure(session :Session) {
        val struct = pathResource(DB_STRUCTURE)

        val textStruct = struct.readText()

        textStruct.split(";").forEach {
            session.execute(it + ";") }

        val data = pathResource(DB_DATA)

        val textData = data.readText()

        textData.split(";").forEach { session.execute(it) }
    }
}