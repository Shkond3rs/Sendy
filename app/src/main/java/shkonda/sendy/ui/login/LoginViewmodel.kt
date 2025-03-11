package shkonda.sendy.ui.login

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
    val phoneInput: LiveData<String> = _phoneInput

    // Состояние согласия с условиями
    private val _isAgreed = MutableLiveData(false)
    val isAgreed: LiveData<Boolean> = _isAgreed

    /**
     * Обновляет номер телефона
     * Поддерживает различные форматы ввода и ограничивает длину в зависимости от формата
     *
     *Примечание: При вводе символов в любую позицию строки левее её конца
     * существующие цифры справа заменяются на новые.
     *
     * @param phone Введенный номер телефона
     */
    fun updatePhone(phone: String) {
        if (phone.isEmpty()) {
            _phoneInput.value = ""
            return
        }

        // Удаляем все нецифровые символы, кроме +
        val cleaned = phone.replace(PHONE_REGEX, "")

        // Определяем максимальную длину в зависимости от формата
        val maxLength = when {
            cleaned.startsWith("+") -> MAX_LENGTH_WITH_PLUS
            cleaned.startsWith("7") || cleaned.startsWith("8") -> MAX_LENGTH_WITH_CODE
            else -> MAX_LENGTH_WITHOUT_CODE
        }

        // Ограничиваем длину в соответствии с форматом
        val limited = if (cleaned.length > maxLength) cleaned.substring(0, maxLength) else cleaned

        _phoneInput.value = limited
    }

    /**
     * Обновление состояния согласия
     */
    fun updateAgreement(agreed: Boolean) {
        _isAgreed.value = agreed
    }

    /**
     * Форматирует номер телефона для отправки на сервер
     * Поддерживает различные форматы ввода:
     * 1. +7XXXXXXXXXX
     * 2. 7XXXXXXXXXX
     * 3. 8XXXXXXXXXX
     * 4. XXXXXXXXXX
     * @return Отформатированный номер телефона в формате +7XXXXXXXXXX или null, если номер некорректный
     */
    private fun formatPhoneNumber(phone: String): String? {
        // Удаляем все пробелы и другие ненужные символы
        val digitsOnly = phone.replace(Regex("[^0-9+]"), "")

        // Определяем формат и нормализуем номер
        return when {
            // Формат 1: +7XXXXXXXXXX
            digitsOnly.startsWith("+7") && digitsOnly.length == MAX_LENGTH_WITH_PLUS -> digitsOnly
            // Формат 2: 7XXXXXXXXXX
            digitsOnly.startsWith("7") && digitsOnly.length == MAX_LENGTH_WITH_CODE -> "+$digitsOnly"
            // Формат 3: 8XXXXXXXXXX
            digitsOnly.startsWith("8") && digitsOnly.length == MAX_LENGTH_WITH_CODE -> "+7${digitsOnly.substring(1)}"
            // Формат 4: XXXXXXXXXX
            digitsOnly.length == MAX_LENGTH_WITHOUT_CODE -> "+7$digitsOnly"
            // Некорректный формат
            else -> null
        }

        // DEPRECATED
        // Данная проверка уже не имеет смысла за счёт нормализации номера телефона выше
//        if (!normalizedPhone.startsWith("+7") || normalizedPhone.length != 12) {
//            return null
//        }
    }

    /**
     * Активировать кошелек
     * @param phone Номер телефона пользователя
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

    /* Прощайте магические числа!!! */
    companion object {
        private val PHONE_REGEX = Regex("[^0-9+]")
        private const val MAX_LENGTH_WITH_PLUS = 12 // Формат +7XXXXXXXXXX
        private const val MAX_LENGTH_WITH_CODE = 11 // Формат 7XXXXXXXXXX или 8XXXXXXXXXX
        private const val MAX_LENGTH_WITHOUT_CODE = 10 // Формат XXXXXXXXXX
        private const val ERROR_INVALID_PHONE_FORMAT =
            "Некорректный формат номера телефона. Введите номер в формате +7XXXXXXXXXX"
        private const val ERROR_UNKNOWN_VIEWMODEL_CLASS = "Неизвестный класс ViewModel"
    }
}


















