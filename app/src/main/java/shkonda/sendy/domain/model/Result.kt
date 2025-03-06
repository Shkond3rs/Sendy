package shkonda.sendy.domain.model

/**
 * Обобщенный класс для представления результата операции
 * @param T Тип данных при успешном выполнении
 * @param E Тип данных при ошибке
 */
sealed class Result<out T, out E> {
    data class Success<T>(val data: T) : Result<T, Nothing>()
    data class Error<E>(val error: E) : Result<Nothing, E>()
}

/**
 * Результат активации кошелька
 * @param phone Номер телефона пользователя
 */
data class ActivationResult(val phone: String)

/**
 * Результат подтверждения активации кошелька
 */
sealed class ConfirmationResult {
    object WalletActivated : ConfirmationResult()
    object NeedBankSelection : ConfirmationResult()
}

/**
 * Модель ошибки API
 * @param code Код ошибки
 * @param message Сообщение об ошибке
 * @param attemptsLeft Количество оставшихся попыток (если применимо)
 * @param attemptsExhausted Флаг, указывающий, что все попытки исчерпаны
 */
data class ApiError(
    val code: Int,
    val message: String,
    val attemptsLeft: Int? = null,
    val attemptsExhausted: Boolean = false
)