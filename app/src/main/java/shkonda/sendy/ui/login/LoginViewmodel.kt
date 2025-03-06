package shkonda.sendy.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import land.sendy.pfe_sdk.activies.MasterActivity
import land.sendy.pfe_sdk.api.API
import shkonda.sendy.data.AuthRepository
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

    // Обновление номера телефона
    fun updatePhone(phone: String) {
        _phoneInput.value = phone
    }

    // Обновление состояния согласия
    fun updateAgreement(agreed: Boolean) {
        _isAgreed.value = agreed
    }

    /**
     * Форматирует номер телефона, удаляя пробелы и другие ненужные символы
     * @return Отформатированный номер телефона или null, если номер некорректный
     */
    private fun formatPhoneNumber(phone: String): String? {
        // Удаляем все пробелы и другие ненужные символы
        val formattedPhone = phone.replace("\\s".toRegex(), "")

        // Проверяем, что номер начинается с +7 и имеет правильную длину
        if (!formattedPhone.startsWith("+7") || formattedPhone.length != 12) {
            return null
        }

        return formattedPhone
    }

    /**
     * Активировать кошелек
     * @param phone Номер телефона пользователя
     */
    fun activateWallet() {
        val phone = _phoneInput.value ?: ""

        val formattedPhone = formatPhoneNumber(phone)

        if (formattedPhone == null) {
            _uiState.value = LoginUiState.Error("Некорректный формат номера телефона. Введите номер в формате +7XXXXXXXXXX")
            return
        }

        // Обновление состояния UI на загрузку
        _uiState.value = LoginUiState.Loading

        // Вызов Use Case
        activateWalletUseCase(activity, formattedPhone) { result ->
            when (result) {
                is Success -> {
                    _uiState.value = LoginUiState.Success(result.data.phone)
                }
                is Error -> {
                    _uiState.value = LoginUiState.Error(result.error.message)
                }

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
            throw IllegalArgumentException("Неизвестный класс ViewModel")
        }
    }
}


















