package shkonda.sendy.data

import land.sendy.pfe_sdk.activies.MasterActivity
import land.sendy.pfe_sdk.api.API
import land.sendy.pfe_sdk.model.pfe.response.AuthActivateRs
import land.sendy.pfe_sdk.model.pfe.response.AuthLoginRs
import land.sendy.pfe_sdk.model.pfe.response.BResponse
import land.sendy.pfe_sdk.model.types.ApiCallback
import land.sendy.pfe_sdk.model.types.LoaderError
import shkonda.sendy.domain.model.ActivationResult
import shkonda.sendy.domain.model.ApiError
import shkonda.sendy.domain.model.ConfirmationResult
import shkonda.sendy.domain.model.Result

/**
 * Репозиторий для работы с API аутентификации
 * Инкапсулирует логику взаимодействия с API SDK
 */
class AuthRepository(private val api: API) {

    /**
     * Активация кошелька по номеру телефона
     * @param activity Активность для контекста
     * @param phone Номер телефона пользователя
     * @param callback Колбэк с результатом операции
     */
    fun activateWallet(
        activity: MasterActivity,
        phone: String,
        callback: (Result<ActivationResult, ApiError>) -> Unit
    ) {
        API.outLog("Попытка старта активации кошелька: $phone")

        val runResult = api.loginAtAuth(activity, phone, object : ApiCallback() {
            override fun onCompleted(res: Boolean) {
                if (!res || errNo != 0) {
                    // Обработка ошибки
                    val errorMessage = this.toString()
                    API.outLog("Ошибка: $errorMessage")
                    callback(Result.Error(ApiError(errNo, errorMessage)))
                } else {
                    // Обработка успешного результата
                    val response = this.oResponse as AuthLoginRs
                    API.outLog("Успешная активация кошелька:\r\n" + response.toString())
                    callback(Result.Success(ActivationResult(phone)))
                }
            }
        })

        if (runResult != null && runResult.hasError()) {
            API.outLog("Запрос не был запущен:\r\n" + runResult.toString())
            callback(Result.Error(ApiError(-1, "Запрос не был запущен: $runResult")))
        }
    }

    /**
     * Подтверждение активации кошелька с помощью SMS-кода
     * @param activity Активность для контекста
     * @param token SMS-код
     * @param phone Номер телефона пользователя
     * @param callback Колбэк с результатом операции
     */
    fun confirmPhone(
        activity: MasterActivity,
        token: String,
        phone: String,
        callback: (Result<ConfirmationResult, ApiError>) -> Unit
    ) {
        API.outLog("Попытка активации кошелька $phone, sms: $token")

        val runResult = api.activateWllet(activity, token, "sms", object : ApiCallback() {
            override fun <T : BResponse?> onSuccess(data: T) {
                if (data != null) {
                    if (this.errNo == 0) {
                        val response = this.oResponse as AuthActivateRs

                        // Проверяем, активирован ли кошелек
                        if (response.Active != null && response.Active) {
                            // Устанавливаем флаг активации девайса
                            activity.activate()
                            API.outLog("Девайс активирован!")
                            callback(Result.Success(ConfirmationResult.WalletActivated))
                        }
                        // Если кошелек не активирован или нет PANs, нужно выбрать банк и валюту
                        else if (response.Active == false || response.PANs == null || response.PANs.isEmpty()) {
                            API.outLog("Необходимо выбрать банк и валюту")
                            callback(Result.Success(ConfirmationResult.NeedBankSelection))
                        }
                    } else {
                        // Проверяем код ошибки
                        val errorString = this.toString()
                        API.outLog("Сервер вернул ошибку: $errorString")

                        // Проверяем количество оставшихся попыток
                        val abuse = (this.oResponse as AuthActivateRs).Abuse

                        if (abuse != null && abuse > 0) {
                            API.outLog("Осталось попыток: $abuse")

                            // Проверяем, является ли это ошибкой №806 (неверный код)
                            if (errorString.contains("№806")) {
                                callback(Result.Error(ApiError(806, "Неверный код", attemptsLeft = abuse)))
                            } else {
                                callback(Result.Error(ApiError(errNo, "Ошибка подтверждения: $errorString", attemptsLeft = abuse)))
                            }
                        } else {
                            API.outLog("Исчерпаны все попытки ввода кода. Необходимо повторить регистрацию")

                            // Решил возвращать пользователя на экран ввода номера телефона
                            callback(Result.Error(ApiError(errNo, "Исчерпаны все попытки", attemptsExhausted = true)))
                        }
                    }
                } else {
                    API.outLog("onSuccess. Проблема: сервер не вернул данные!")
                    callback(Result.Error(ApiError(-1, "Сервер не вернул данные")))
                }
            }

            override fun onFail(error: LoaderError?) {
                API.outLog("Фатальная ошибка: ${error.toString()}")
                callback(Result.Error(ApiError(-1, "Фатальная ошибка: ${error.toString()}")))
            }
        })

        if (runResult != null && runResult.hasError()) {
            API.outLog("Запрос не был запущен:\r\n$runResult")
            callback(Result.Error(ApiError(-1, "Запрос не был запущен: $runResult")))
        }
    }
}