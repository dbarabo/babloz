package ru.barabo.babloz.sync

import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.BablozConnection
import ru.barabo.babloz.db.entity.Account
import ru.barabo.babloz.db.entity.Currency
import ru.barabo.babloz.db.entity.Pay
import ru.barabo.babloz.db.entity.budget.BudgetMain
import ru.barabo.babloz.db.entity.group.GroupAccount
import ru.barabo.babloz.db.entity.group.GroupCategory
import ru.barabo.babloz.db.entity.group.GroupPerson
import ru.barabo.babloz.db.entity.group.GroupProject
import ru.barabo.babloz.db.service.*
import ru.barabo.babloz.db.service.budget.BudgetCategoryService
import ru.barabo.babloz.db.service.budget.BudgetMainService
import ru.barabo.babloz.db.service.budget.BudgetRowService
import ru.barabo.babloz.sync.imap.GetMailDb
import ru.barabo.babloz.sync.imap.MailProperties
import ru.barabo.babloz.sync.smtp.SendMailDb
import ru.barabo.cmd.Cmd
import ru.barabo.db.EditType
import ru.barabo.db.service.StoreListener
import tornadofx.alert
import java.io.File
import java.nio.file.FileSystems

class DelegateChangeData<T> : StoreListener<T> {
    override fun refreshAll(elemRoot: T, refreshType: EditType) {
        Sync.changeData(refreshType)
    }
}

object Sync : GetMailDb, SendMailDb {

    private val logger = LoggerFactory.getLogger(Sync::class.java)!!

    private var mailProp: MailProperties? = null

    private var syncType: SyncTypes = SyncTypes.NO_SYNC_LOCAL_ONLY

    private var isChangeData: Boolean = false

    fun changeData(editType: EditType) {
        if(!isChangeData && editType.isEditable()) {
            isChangeData = true
        }
    }

    override fun subjectCriteria() = "Babloz db saved:"

    override fun bablozAttachName() = "babloz.db"

    override fun bablozFilePath(): String = currentDirectory()

    private fun currentDirectory(): String {
        val currentDb = File("$CURRENT_PATH/$BABLOZ_JAR")

        return if(currentDb.exists()) Cmd.JAR_FOLDER else CURRENT_PATH
    }

    private val CURRENT_PATH = "${FileSystems.getDefault().getPath("").toAbsolutePath()}"

    private const val BABLOZ_JAR = "babloz.jar"

    fun isSuccessMailPropertySmtp(): Boolean {

        val valid = mailProp?.let { it.user.isNotEmpty() && it.password.isNotEmpty()  } ?: return false

        return if(valid) isCheckSmtpConnect() else false
    }

    private fun isCheckSmtpConnect(): Boolean =
            try {
                mailProp?.smtpSession()

                true
            } catch (e: Exception) {
                false
            }

    fun startSync(login: String, password: String, syncType: SyncTypes) {

        logger.error("${FileSystems.getDefault().getPath("").toAbsolutePath()}")

        mailProp = MailProperties(user = login, password = password)

        this.syncType = syncType

        START_SYNC_PROCESS[syncType]!!.invoke(mailProp!!)

        initChangeData()
    }

    private fun initChangeData() {
        AccountService.addListener( DelegateChangeData<GroupAccount>() )
        CategoryService.addListener(DelegateChangeData<GroupCategory>())
        PayService.addListener(DelegateChangeData<List<Pay>>())
        PersonService.addListener(DelegateChangeData<GroupPerson>())
        CurrencyService.addListener(DelegateChangeData<List<Currency>>())
        ProjectService.addListener(DelegateChangeData<GroupProject>())
        BudgetMainService.addListener(DelegateChangeData())
        BudgetRowService.addListener(DelegateChangeData())
        BudgetCategoryService.addListener(DelegateChangeData())
    }

    private val START_SYNC_PROCESS = mapOf<SyncTypes, (MailProperties)->Unit>(
            SyncTypes.SYNC_START_SAVE_LOCAL to ::downloadSyncFile,
            SyncTypes.SYNC_START_DEL_LOCAL to ::downloadSyncFile,
            SyncTypes.NO_SYNC_LOCAL_ONLY to ::startLocalOnly
    )

    private fun startLocalOnly(mailProp: MailProperties) { }

    private fun downloadSyncFile(mailProp: MailProperties) {

        val file = try {
           this.getDbInMail(mailProp)
        } catch (e: Exception) {
            alert(Alert.AlertType.ERROR, e.message!!)

            return
        }

        file?.replaceToMainDb()?: alert(Alert.AlertType.ERROR, DB_IN_EMAIL_NOTFOUND)
    }

    private const val DB_IN_EMAIL_NOTFOUND = "База данных в эл. почте не найдена"

    private fun File.replaceToMainDb() {

        BablozConnection.closeAllSessions()

        val bablozDb = File("${currentDirectory()}/$name")

        if(bablozDb.exists()) {
            bablozDb.delete()
        }

        copyTo(bablozDb, true)

        if(delete()) {
            parentFile.delete()
        }
    }

    fun endSync() {

        BablozConnection.closeAllSessions()

        END_SYNC_PROCESS[syncType]!!.invoke(mailProp)
    }

    private val END_SYNC_PROCESS = mapOf<SyncTypes, (MailProperties?)->STATUS>(
            SyncTypes.SYNC_START_SAVE_LOCAL to ::saveDbToMail,
            SyncTypes.SYNC_START_DEL_LOCAL to ::saveDbDelete,
            SyncTypes.NO_SYNC_LOCAL_ONLY to ::endLocalOnly
    )

    fun saveDbToEMail(userName: String,  pswd: String) {
        mailProp = mailProp?.apply {
            this.user = userName
            this.password = pswd
        } ?: MailProperties(user = userName, password = pswd)

        saveDbToEMail()
    }

    fun saveDbToEMail() {
        isChangeData = true

        saveDbToMail(mailProp)
    }

    private fun saveDbToMail(mailProp: MailProperties?): STATUS {

        if(!isChangeData) return STATUS.OK

        return try {
            sendDb(mailProp!!)

            STATUS.OK
        } catch (e: Exception) {
            logger.error("endSync", e)

            alert(Alert.AlertType.ERROR, e.message!!)

            STATUS.FAIL
        }
    }

    private fun saveDbDelete(mailProp: MailProperties?): STATUS =
            if(saveDbToMail(mailProp) == STATUS.OK) deleteLocalDb() else questionDelete(mailProp)

    private fun questionDelete(mailProp: MailProperties?): STATUS {

        val alertResult = alert(Alert.AlertType.INFORMATION, "Не удалось синхронизировать БД",
                "Не удалось синхронизировать БД\n Всё-равно удалить локальную БД (данные будут потеряны)",
                ButtonType.YES, ButtonType.NO).showAndWait()

        if(alertResult.isPresent && alertResult.get().buttonData == ButtonBar.ButtonData.YES) {
            deleteLocalDb()
        }

        return STATUS.OK
    }

    private fun deleteLocalDb(): STATUS {
        val bablozDb = File("${currentDirectory()}/${bablozAttachName()}")

        return if(bablozDb.delete()) STATUS.OK else STATUS.FAIL
    }

    private fun endLocalOnly(mailProp: MailProperties?):STATUS = STATUS.OK
}

private enum class STATUS {
    OK,
    FAIL
}