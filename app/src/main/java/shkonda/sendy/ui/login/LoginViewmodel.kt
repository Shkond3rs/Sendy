package shkonda.sendy.ui.login

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import land.sendy.pfe_sdk.activies.MasterActivity
import land.sendy.pfe_sdk.api.API
import shkonda.sendy.di.AppContainer
import shkonda.sendy.domain.model.Result.Error
import shkonda.sendy.domain.model.Result.Success
import shkonda.sendy.domain.usecases.ActivateWalletUseCase

/**
 * Состояния UI для экрана входа
 */
sealed class LoginUiState {
    object Initial : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val phone: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

/**
 * Визуальная трансформация для отображения префикса перед вводимым текстом
 *
 * @param prefix Префикс, который будет отображаться перед текстом
 */
class PrefixTransformation(private val prefix: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Создаем новую строку с префиксом
        val prefixedText = AnnotatedString(prefix + text.text)

        // Создаем маппинг для корректной работы курсора
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Смещаем позицию курсора на длину префикса
                return offset + prefix.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                // Если курсор находится в пределах префикса, возвращаем 0
                return if (offset <= prefix.length) 0 else offset - prefix.length
            }
        }

        return TransformedText(prefixedText, offsetMapping)
    }
}

/**
 * ViewModel для экрана входа
 * Управляет состоянием UI и взаимодействует с Use Case
 */
class LoginViewModel(
    private val activateWalletUseCase: ActivateWalletUseCase,
    private val activity: MasterActivity
) : ViewModel() {

    // Состояние UI
    private val _uiState = MutableLiveData<LoginUiState>(LoginUiState.Initial)
    val uiState: LiveData<LoginUiState> = _uiState

    // Состояние ввода номера телефона
    private val _phoneInput = MutableLiveData("")

    // Состояние текстового поля с учетом позиции курсора
    private val _textFieldValue = MutableLiveData(TextFieldValue(""))
    val textFieldValue: LiveData<TextFieldValue> = _textFieldValue

    // Состояние согласия с условиями
    private val _isAgreed = MutableLiveData(false)
    val isAgreed: LiveData<Boolean> = _isAgreed

    /**
     * Сбрасывает состояние Success, сохраняя введенные данные
     */
    fun resetSuccessState() {
        if (_uiState.value is LoginUiState.Success) {
            _uiState.value = LoginUiState.Initial
        }
    }

    /**
     * Обновляет номер телефона с учетом позиции курсора
     * Решает проблему с заменой символов при вводе в середину строки
     * Запрещает ввод любых символов, кроме цифр
     *
     * @param value Текущее значение текстового поля с позицией курсора
     */
    fun updatePhoneWithCursor(value: TextFieldValue) {
        val currentPhone = _phoneInput.value ?: ""
        val newText = value.text
        val selection = value.selection

        if (newText.isEmpty()) {
            _phoneInput.value = ""
            _textFieldValue.value = TextFieldValue(text = "", selection = androidx.compose.ui.text.TextRange(0))
            return
        }

        // Проверяем, содержит ли новый текст недопустимые символы
        if (!isDigitsOnly(newText)) {
            // Если содержит недопустимые символы, сохраняем предыдущее состояние
            _textFieldValue.value = TextFieldValue(text = currentPhone, selection = selection)
            return
        }

        // Если номер уже достиг максимальной длины и пытаются добавить еще символы
        if (currentPhone.length >= MAX_LENGTH_WITHOUT_CODE && newText.length > currentPhone.length) {
            // Не обновляем номер, оставляем текущее значение
            _textFieldValue.value = TextFieldValue(text = currentPhone, selection = selection)
            return
        }

        // Обрабатываем различные форматы ввода
        val withoutPrefix = when {
            // Если номер начинается с 8, заменяем 8 на пустую строку
            newText.startsWith("8") && newText.length > 1 -> {
                newText.substring(1)
            }
            // Если номер начинается с 7 и достаточно длинный, это может быть "7..."
            newText.startsWith("7") && newText.length > MAX_LENGTH_WITHOUT_CODE -> {
                newText.substring(1)
            }
            // В остальных случаях оставляем как есть
            else -> {
                newText
            }
        }

        // Ограничиваем длину (10 цифр после кода страны)
        val maxDigits = MAX_LENGTH_WITHOUT_CODE
        val limited = if (withoutPrefix.length > maxDigits) withoutPrefix.substring(0, maxDigits) else withoutPrefix

        // Если пытаются ввести символ в середину (не в конец)
        if (selection.start < currentPhone.length && newText.length != currentPhone.length) {
            // Определяем, сколько символов было до курсора в исходном тексте
            val originalTextBeforeCursor = newText.substring(0, selection.start)
            // Определяем, сколько цифр было до курсора в исходном тексте
            val digitsBeforeCursor = originalTextBeforeCursor.length

            // Определяем, сколько символов было удалено из-за префикса
            val prefixOffset = if (newText.length != withoutPrefix.length) {
                if (digitsBeforeCursor > 0) 1 else 0 // Если курсор после первой цифры, учитываем префикс
            } else 0

            // Вычисляем новую позицию курсора
            val newCursorPos = digitsBeforeCursor - prefixOffset

            // Проверяем, что позиция не выходит за границы
            val finalPos = newCursorPos.coerceIn(0, limited.length)

            _phoneInput.value = limited
            _textFieldValue.value = TextFieldValue(text = limited, selection = androidx.compose.ui.text.TextRange(finalPos))
            return
        }

        // Для других случаев используем стандартный расчет позиции курсора
        val newCursorPos = calculateNewCursorPosition(newText, newText, withoutPrefix, limited, selection.start)

        _phoneInput.value = limited
        _textFieldValue.value = TextFieldValue(text = limited, selection = androidx.compose.ui.text.TextRange(newCursorPos))
    }

    /**
     * Проверяет, содержит ли строка только цифры
     */
    private fun isDigitsOnly(text: String): Boolean {
        return text.all { it.isDigit() }
    }

    /**
     * Вычисляет новую позицию курсора после обработки ввода
     */
    private fun calculateNewCursorPosition(
        originalText: String,
        cleanedText: String,
        withoutPrefix: String,
        limitedText: String,
        originalCursor: Int
    ): Int {
        // Количество символов, удаленных до позиции курсора
        val nonDigitsBeforeCursor = originalText.substring(0, minOf(originalCursor, originalText.length))
            .count { !it.isDigit() && it != '+' }

        // Если был удален префикс (7 или 8), корректируем позицию
        val prefixOffset = if (cleanedText != withoutPrefix) 1 else 0

        // Вычисляем новую позицию с учетом удаленных символов и префикса
        var newPos = originalCursor - nonDigitsBeforeCursor - prefixOffset

        // Проверяем, что позиция не выходит за границы
        newPos = newPos.coerceIn(0, limitedText.length)

        return newPos
    }

    /**
     * Обновление состояния согласия
     */
    fun updateAgreement(agreed: Boolean) {
        _isAgreed.value = agreed
    }

    /**
     * Форматирует номер телефона для отправки на сервер
     * Теперь номер всегда хранится без префикса +7, поэтому просто добавляем его
     *
     * @return Отформатированный номер телефона в формате +7XXXXXXXXXX или null, если номер некорректный
     */
    private fun formatPhoneNumber(phone: String): String? {
        // Проверяем, что номер содержит ровно 10 цифр
        if (phone.length != MAX_LENGTH_WITHOUT_CODE) {
            return null
        }

        // Добавляем префикс +7 к номеру
        return "+7$phone"
    }

    /**
     * Активировать кошелек
     */
    fun activateWallet() {
        val phone = _phoneInput.value ?: ""

        val formattedPhone = formatPhoneNumber(phone)

        if (formattedPhone == null) {
            _uiState.value = LoginUiState.Error(ERROR_INVALID_PHONE_FORMAT)
            return
        }

        // Обновление состояния UI на загрузку
        _uiState.value = LoginUiState.Loading

        // Вызов Use Case
        activateWalletUseCase(activity, formattedPhone) { result ->
            // Можем сразу в значение _uiState передать результат работы when сократив код
            _uiState.value = when (result) {
                is Success -> LoginUiState.Success(result.data.phone)
                is Error -> LoginUiState.Error(result.error.message)
            }
        }
    }

    /**
     * Factory для создания ViewModel с зависимостями
     */
    class Factory(
        private val api: API,
        private val activity: MasterActivity
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                val useCase = AppContainer.getActivateWalletUseCase(api)
                return LoginViewModel(useCase, activity) as T
            }
            throw IllegalArgumentException(ERROR_UNKNOWN_VIEWMODEL_CLASS)
        }
    }

    companion object {
        private val PHONE_REGEX = Regex("[^0-9+]")
        private const val MAX_LENGTH_WITHOUT_CODE = 10 // Формат XXXXXXXXXX
        private const val ERROR_INVALID_PHONE_FORMAT =
            "Некорректный формат номера телефона. Введите номер в формате +7XXXXXXXXXX"
        private const val ERROR_UNKNOWN_VIEWMODEL_CLASS = "Неизвестный класс ViewModel"
    }
}