package shkonda.sendy

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import land.sendy.pfe_sdk.activies.MasterActivity
import land.sendy.pfe_sdk.api.API
import shkonda.sendy.navigation.SendyNavHost

class MainActivity : MasterActivity() {
    private val SERVER_URL = "https://testwallet.sendy.land/"
    private val LOG_TAG = "sendy"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        api = API.getInsatce(SERVER_URL, LOG_TAG)

        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    SendyNavHost(navController)
                }
            }
        }

    }
}