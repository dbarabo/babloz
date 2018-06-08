package ru.barabo.babloz.gui.binding

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import org.slf4j.LoggerFactory
import ru.barabo.babloz.gui.custom.ChangeSelectEdit
import tornadofx.alert

abstract class AbstractSaver<T, out B : BindProperties<T>>(clazzBind: Class<B>,
                     changeSelectEdit: ChangeSelectEdit = ChangeSelectEdit.SAVE) : Saver<T> {

    val editBind: B = clazzBind.newInstance()

    private val oldBind: B = clazzBind.newInstance()

    private val funChangeSelectEdit  =
            when (changeSelectEdit) {
                ChangeSelectEdit.SAVE -> ::save
                ChangeSelectEdit.CANCEL -> ::cancel
                ChangeSelectEdit.CONFIRM -> ::confirmSave
                else -> throw Exception("changeSelectEdit is not valid = $changeSelectEdit")
    }

    override fun isDisableEdit() = editBind.isDisableEdit(oldBind)

    companion object {
        private const val ALERT_ERROR_SAVE = "Ошибка при сохранении"

        private const val QUEST_SAVE_THIS_ROW = "Сохранить данные предыдущей строки?"
    }

    protected abstract fun serviceSave(value: T)

    override fun save() {
        try {
            val accountUpdated = editBind.saveToEditValue()

            serviceSave(accountUpdated)

            editBind.copyToProperties(oldBind)

        } catch (e :Exception) {
            LoggerFactory.getLogger(Saver::class.java).error("save", e)

            alert(Alert.AlertType.ERROR, e.message ?: ALERT_ERROR_SAVE )
        }
    }

    private fun confirmSave() {
        val okType = Alert(Alert.AlertType.CONFIRMATION, QUEST_SAVE_THIS_ROW,
                ButtonType.OK, ButtonType.NO).showAndWait()

        if(okType.isPresent && okType.get() == ButtonType.OK) {
            save()
        } else {
            cancel()
        }
    }

    override fun cancel() {

        oldBind.copyToProperties(editBind)
    }

    override fun changeSelectEditValue(value: T?) {

        saveOrCancelEdit()

        startEdit(value)
    }

    private fun saveOrCancelEdit() {

        if(!editBind.isInit() || editBind.isEqualsProperties(oldBind).value ) return

        funChangeSelectEdit.invoke()
    }

    private fun startEdit(value: T?) {

        editBind.initValue(value)

        oldBind.initValue(value)
    }
}