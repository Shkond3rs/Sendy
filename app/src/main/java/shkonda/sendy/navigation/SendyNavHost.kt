package shkonda.sendy.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import shkonda.sendy.screens.LoginScreen
import shkonda.sendy.screens.SMSCodeScreen
import shkonda.sendy.screens.SplashScreen

@Composable
fun SendyNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onTimeout = {
                navController.navigate("login") {
                    popUpTo("splash") {inclusive = true}
                }
            })
        }

        composable("login") {
            LoginScreen(
                onRegSuccess = { phone ->
                    navController.navigate("sms?phone=$phone")
                }
            )
        }

        composable("sms?phone={phone}") { it ->
            val phone = it.arguments?.getString("phone") ?: ""
            SMSCodeScreen(phone, onSmsValidated = {} )
        }
    }
}