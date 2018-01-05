package ru.barabo.babloz.gui

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.control.Tab
import javafx.scene.layout.VBox
import org.slf4j.LoggerFactory
import ru.barabo.babloz.db.entity.Category
import ru.barabo.babloz.db.entity.CategoryType
import ru.barabo.babloz.db.service.CategoryService
import ru.barabo.babloz.main.ResourcesManager
import tornadofx.*

object CategoryEdit : Tab("Правка категории", VBox()) {

    private val logger = LoggerFactory.getLogger(CategoryEdit::class.java)

    private val nameProperty = SimpleStringProperty()

    private val categoryProperty = SimpleObjectProperty<Category>()

    private val categoryTypeProperty = SimpleObjectProperty<CategoryType>()

    private lateinit var editCategory : Category

    init {
        this.graphic = ResourcesManager.icon("edit.png")

        form {
            toolbar {
                button ("Сохранить", ResourcesManager.icon("save.png")).setOnAction { save() }

                button ("Отменить", ResourcesManager.icon("cancel.png")).setOnAction { cancel() }
            }

            fieldset {
                field("Наименование") {
                    textfield(nameProperty)
                }
                field("Тип категории") {
                    combobox<CategoryType>(property = categoryTypeProperty, values = CategoryType.values().toList())
                }
                field("Родительская категория") {
                    combobox<Category>(property = categoryProperty, values = CategoryService.parentList())
                }
            }
        }
    }

    private fun cancel() {
        tabPane.tabs.remove(CategoryEdit)
    }

    private val ALERT_ERROR_SAVE = "Ошибка при сохранении"

    private fun save() {

        editCategory = setCategoryFromProperty(editCategory)

        try {
            editCategory = CategoryService.save(editCategory)

            tabPane.tabs.remove(CategoryEdit)
        } catch (e :Exception) {
            logger.error("save", e)

            alert(Alert.AlertType.ERROR, e.message ?: ALERT_ERROR_SAVE)
        }
    }

    private fun setCategoryFromProperty(category : Category) : Category {

        category.name = nameProperty.value

        category.parent = categoryProperty.value?.id

        category.type = categoryTypeProperty.value

        return category
    }

    fun editCategory(category : Category) {

        editCategory = category

        tabPane.selectionModel.select(CategoryEdit)

        this.text = category.id ?. let { "Правка категории" } ?: "Новая категория"

        nameProperty.value = category.name ?. let { category.name } ?: ""

        categoryProperty.value = CategoryService.findCategoryById(category.parent)

        categoryTypeProperty.value = category.type
    }
}