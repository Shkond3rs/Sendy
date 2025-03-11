package shkonda.sendy.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import land.sendy.pfe_sdk.activies.MasterActivity
import land.sendy.pfe_sdk.api.API
import shkonda.sendy.placeholder_screens.BankSelectionScreen
import shkonda.sendy.placeholder_screens.MainScreen
import shkonda.sendy.placeholder_screens.SplashScreen
import shkonda.sendy.ui.login.LoginScreen
import shkonda.sendy.ui.login.LoginViewModel
import shkonda.sendy.ui.sms.SmsCodeScreen
import shkonda.sendy.ui.sms.SmsViewModel

/**
 * Основной навигационный компонент приложения
 * Определяет все экраны и переходы между ними
 */
@Composable
fun SendyNavHost(
    navController: NavHostController,
    termsText: String,
    api: API,
    activity: MasterActivity
) {
    // Создаем общую ViewModel для экрана входа, чтобы сохранять состояние
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.Factory(api, activity)
    )

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onTimeout = {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("login") {

            // Отображение экрана ввода телефона
            LoginScreen(
                viewModel = loginViewModel,
                termsText = termsText,
                onNavigateToSms = { phone ->
                    navController.navigate("sms?phone=$phone")
                }
            )
        }

        composable("sms?phone={phone}") { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""

            // Создание ViewModel с использованием Factory
            val smsViewModel: SmsViewModel = viewModel(
                factory = SmsViewModel.Factory(api, activity)
            )

            // Отображение экрана ввода SMS-кода
            SmsCodeScreen(
                viewModel = smsViewModel,
                phone = phone,
                onWalletActivated = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNeedBankSelection = {
                    navController.navigate("bank_selection?phone=$phone")
                },
                onReturnToLogin = {
                    // Возвращаемся на экран входа без сброса состояния
                    navController.popBackStack()
                }
            )
        }

        composable("bank_selection?phone={phone}") { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""

            // Отображение экрана выбора банка
            BankSelectionScreen(
                phone = phone,
                onBankSelected = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            // Отображение главного экрана
            MainScreen()
        }
    }
}