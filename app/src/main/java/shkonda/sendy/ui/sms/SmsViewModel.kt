package shkonda.sendy.ui.sms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import land.sendy.pfe_sdk.activies.MasterActivity
import land.sendy.pfe_sdk.api.API
import shkonda.sendy.di.AppContainer
import shkonda.sendy.domain.model.ConfirmationResult
import shkonda.sendy.domain.model.Result.Error
import shkonda.sendy.domain.model.Result.Success
import shkonda.sendy.domain.usecases.ConfirmPhoneUseCase

/**
 * Состояния UI для экрана ввода SMS-кода
 */
sealed class SmsUiState {
    object Initial : SmsUiState()
    object Loading : SmsUiState()
    object WalletActivated : SmsUiState()
    object NeedBankSelection : SmsUiState()
    object AttemptsExhausted : SmsUiState()
    data class Error(val message: String, val attemptsLeft: Int? = null) : SmsUiState()
}

/**
 * ViewModel для экрана ввода SMS-кода
 * Управляет состоянием UI и взаимодействует с Use Case
 */
class SmsViewModel(
    private val confirmPhoneUseCase: ConfirmPhoneUseCase,
    private val activity: MasterActivity
) : ViewModel() {

    // Состояние UI
    private val _uiState = MutableLiveData<SmsUiState>(SmsUiState.Initial)
    val uiState: LiveData<SmsUiState> = _uiState

    // Состояние ввода кода подтверждения
    private val _smsCode = MutableLiveData("")
    val smsCode: LiveData<String> = _smsCode

    /**
     * Обновить код подтверждения
     * @param code SMS-код
     */
    fun updateSmsCode(code: String) {
        _smsCode.value = code
    }


    /**
     * Подтвердить код из SMS
     * @param phone Номер телефона пользователя
     */
    fun confirmCode(phone: String) {
        val code = _smsCode.value ?: ""

        // Обновление состояния UI на загрузку
        _uiState.value = SmsUiState.Loading

        // Вызов Use Case с сокращенной обработкой результата
        confirmPhoneUseCase(activity, code, phone) { result ->
            // Сразу передаем результат работы when в _uiState.value
            _uiState.value = when (result) {
                is Success -> when (result.data) {
                    is ConfirmationResult.WalletActivated -> SmsUiState.WalletActivated
                    is ConfirmationResult.NeedBankSelection -> SmsUiState.NeedBankSelection
                }
                is Error -> if (result.error.attemptsExhausted) {
                    SmsUiState.AttemptsExhausted
                } else {
                    SmsUiState.Error(
                        message = result.error.message,
                        attemptsLeft = result.error.attemptsLeft
                    )
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
            if (modelClass.isAssignableFrom(SmsViewModel::class.java)) {
                val useCase = AppContainer.getConfirmPhoneUseCase(api)
                return SmsViewModel(useCase, activity) as T
            }
            throw IllegalArgumentException(ERROR_UNKNOWN_VIEWMODEL_CLASS)
        }
    }

    /* Прощайте магические числа!!! */
    companion object {
        private const val ERROR_UNKNOWN_VIEWMODEL_CLASS = "Неизвестный класс ViewModel"
    }
}
