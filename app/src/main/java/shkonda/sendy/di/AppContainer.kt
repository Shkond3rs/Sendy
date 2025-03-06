package shkonda.sendy.di

import land.sendy.pfe_sdk.api.API
import shkonda.sendy.data.AuthRepository
import shkonda.sendy.domain.usecases.ActivateWalletUseCase
import shkonda.sendy.domain.usecases.ConfirmPhoneUseCase

/**
 * Простой контейнер зависимостей для приложения без использования Dagger/Hilt
 */
object AppContainer {

    private var authRepository: AuthRepository? = null

    /**
     * Получить репозиторий аутентификации
     */
    fun getAuthRepository(api: API): AuthRepository {
        return authRepository ?: AuthRepository(api).also {
            authRepository = it
        }
    }

    /**
     * Получить Use Case для активации кошелька
     */
    fun getActivateWalletUseCase(api: API): ActivateWalletUseCase {
        return ActivateWalletUseCase(getAuthRepository(api))
    }

    /**
     * Получить Use Case для подтверждения кода из SMS
     */
    fun getConfirmPhoneUseCase(api: API): ConfirmPhoneUseCase {
        return ConfirmPhoneUseCase(getAuthRepository(api))
    }
}