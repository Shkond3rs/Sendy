package shkonda.sendy.domain.usecases

import land.sendy.pfe_sdk.activies.MasterActivity
import land.sendy.pfe_sdk.api.API
import shkonda.sendy.data.AuthRepository
import shkonda.sendy.domain.model.ActivationResult
import shkonda.sendy.domain.model.ApiError
import shkonda.sendy.domain.model.Result

/**
 * Use Case для активации кошелька
 * Реализует бизнес-логику активации кошелька
 */
class ActivateWalletUseCase(private val authRepository: AuthRepository) {

    /**
     * Выполнить активацию кошелька
     * @param activity Активность для контекста
     * @param phone Номер телефона пользователя
     * @param callback Колбэк с результатом операции
     */
    operator fun invoke(
        activity: MasterActivity,
        phone: String,
        callback: (Result<ActivationResult, ApiError>) -> Unit
    ) {
        // Делегирование вызова репозиторию
        authRepository.activateWallet(activity, phone, callback)
    }

}