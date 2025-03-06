package shkonda.sendy

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import land.sendy.pfe_sdk.activies.MasterActivity
import land.sendy.pfe_sdk.api.API
import land.sendy.pfe_sdk.model.pfe.response.TermsOfUseRs
import land.sendy.pfe_sdk.model.types.ApiCallback
import shkonda.sendy.navigation.SendyNavHost
import shkonda.sendy.ui.theme.SendyTheme

class MainActivity : MasterActivity() {

    private val SERVER_URL = "https://testwallet.sendy.land/"
    private val LOG_TAG = "sendy"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        api = API.getInsatce(SERVER_URL, LOG_TAG)

        setContent {
            SendyTheme(darkTheme = true) {
                Surface {
                    val navController = rememberNavController()
                    var termsText by remember { mutableStateOf("Загрузка...") }
                    LaunchedEffect(Unit) {

                        API.outLog("Тест. Получение текста пользовательского соглашения мобильного приложения")

                        val runResult =
                            api.getTermsOfUse(this@MainActivity, object : ApiCallback() {
                                override fun onCompleted(res: Boolean) {
                                    if (!res || getErrNo() != 0) {
                                        // Вывод ошибки в лог и диалоговое окно
                                        API.outLog("Выполнение запроса завершилось с ошибкой:" + this.toString())
                                        termsText = "Ошибка загрузки соглашения"
                                    } else {
                                        val terms = (this.oResponse as TermsOfUseRs).TextTermsOfUse

                                        API.outLog("Текст соглашения:\r\n" + terms)
                                        termsText = terms
                                    }
                                }
                            })
                        if (runResult != null && runResult.hasError()) {
                            API.outLog("runResult ERROR: \r\n" + runResult.toString())
                        } else {
                            API.outLog("getTermsOfUse: запущено асинхронно!")
                        }
                    }
                    SendyNavHost(
                        navController,
                        termsText,
                        api = api,
                        activity = this@MainActivity
                    )
                }
            }
        }
    }
}