package ru.barabo.babloz.db

import ru.barabo.babloz.main.ResourcesManager
import ru.barabo.db.DbConnection
import ru.barabo.db.DbSetting
import ru.barabo.db.Session

object BablozConnection :DbConnection(DbSetting(driver = "org.sqlite.JDBC",
        url = "jdbc:sqlite:babloz.db", user = "", password = "",
        selectCheck = "select 1 from currency where id = 0")) {

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

    private fun createStructure(session :Session) {

        val textStruct = ResourcesManager.dbStructureText()

        textStruct.split(";").forEach { session.execute("$it;") }

        val textData = ResourcesManager.dbDataText()

        textData.split(";").forEach { session.execute(it) }
    }
}