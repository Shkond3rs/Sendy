package shkonda.sendy.domain.usecases

import land.sendy.pfe_sdk.activies.MasterActivity
import land.sendy.pfe_sdk.api.API
import shkonda.sendy.data.AuthRepository
import shkonda.sendy.domain.model.ApiError
import shkonda.sendy.domain.model.ConfirmationResult
import shkonda.sendy.domain.model.Result

/**
 * Use Case для подтверждения активации кошелька с помощью SMS-кода
 * Реализует бизнес-логику подтверждения активации
 */
class ConfirmPhoneUseCase(private val authRepository: AuthRepository) {

    /**
     * Выполнить подтверждение активации кошелька
     * @param activity Активность для контекста
     * @param code SMS-код
     * @param phone Номер телефона пользователя
     * @param callback Колбэк с результатом операции
     */
    operator fun invoke(
        activity: MasterActivity,
        code: String,
        phone: String,
        callback: (Result<ConfirmationResult, ApiError>) -> Unit
    ) {
        // Валидация кода
        if (!isValidCode(code)) {
            callback(Result.Error(ApiError(-1, "Код должен содержать ровно 6 цифр")))
            return
        }

        // Делегирование вызова репозиторию
        authRepository.confirmPhone(activity, code, phone, callback)
    }

    /**
     * Проверка валидности кода
     * @param code Код для проверки
     * @return true если код валидный, иначе false
     */
    private fun isValidCode(code: String): Boolean {
        // Проверка, что код состоит из 6 цифр
        return code.matches(Regex("^\\d{6}$"))
    }
}