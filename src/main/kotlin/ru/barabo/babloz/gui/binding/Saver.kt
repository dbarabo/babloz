package ru.barabo.babloz.gui.binding

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import org.slf4j.LoggerFactory
import ru.barabo.babloz.gui.custom.ChangeSelectEdit
import tornadofx.alert

interface Saver<T, out B : BindProperties<T>> {

    val editBind: B

    val oldBind: B

    var changeSelectEdit: ChangeSelectEdit

    fun isDisableEdit() = editBind.isEqualsProperties(oldBind)

    fun serviceSave(value: T)

    fun changeSelectEditValue(value: T?) {

        saveOrCancelEdit()

        startEdit(value)
    }

    fun saveOrCancelEdit(selectEvent: ChangeSelectEdit? = null) {

        if(!editBind.isInit() || editBind.isEqualsProperties(oldBind).value ) return

        val changeSelect = selectEvent?.let { it } ?: changeSelectEdit

        mapSelect()[changeSelect]?.invoke()
    }

    private fun mapSelect() = mapOf<ChangeSelectEdit, ()->Unit>(
        ChangeSelectEdit.SAVE to ::save,
        ChangeSelectEdit.CANCEL to ::cancelSave,
        ChangeSelectEdit.CONFIRM to ::confirmSave)

    private fun confirmSave() {
        val okType = Alert(Alert.AlertType.CONFIRMATION, saveThisDataRow(),
                ButtonType.OK, ButtonType.NO).showAndWait()

        if(okType.isPresent && okType.get() == ButtonType.OK) {
            save()
        } else {
            cancelSave()
        }
    }

    private fun saveThisDataRow() = "Сохранить данные предыдущей строки?"

    private fun cancelSave() {

        oldBind.copyToProperties(editBind)
    }

    private fun save() {
        try {
            val accountUpdated = editBind.saveToEditValue()

            serviceSave(accountUpdated)

            editBind.copyToProperties(oldBind)

        } catch (e :Exception) {
            LoggerFactory.getLogger(Saver::class.java).error("save", e)

            alert(Alert.AlertType.ERROR, e.message ?: alertErrorSave() )
        }
    }

    private fun alertErrorSave() = "Ошибка при сохранении"

    private fun startEdit(value: T?) {

        editBind.initValue(value)

        oldBind.initValue(value)
    }
}