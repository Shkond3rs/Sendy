package shkonda.sendy.ui.sms

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SmsCodeScreen(
    viewModel: SmsViewModel,
    phone: String,
    onWalletActivated: () -> Unit,
    onNeedBankSelection: () -> Unit,
    onReturnToLogin: () -> Unit
) {

    // Наблюдение за состоянием из ViewModel
    val uiState by viewModel.uiState.observeAsState()
    val smsCode by viewModel.smsCode.observeAsState("")

    // Обработка нажатия кнопки "Назад"
    BackHandler {
        onReturnToLogin()
    }
    // Обработка состояний UI
    LaunchedEffect(uiState) {
        when (uiState) {
            is SmsUiState.WalletActivated -> {
                onWalletActivated()
            }
            is SmsUiState.NeedBankSelection -> {
                onNeedBankSelection()
            }
            is SmsUiState.AttemptsExhausted -> {
                onReturnToLogin()
            }
            else -> { /* Другие состояния обрабатываются в соответствии с решением команды разработчиков */ }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "На номер $phone отправлен SMS-код",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = smsCode,
            onValueChange = { input ->
                // Валидация без viewModel
                // Я думаю так лучше поступать, когда проверка совсем небольшая, как с кодом подтверждения
                // Обработка вставки из буфера обмена
                val digitsOnly = input.filter { it.isDigit() }.take(6)
                viewModel.updateSmsCode(digitsOnly)
            },
            label = { Text("Введите 6-значный код") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is SmsUiState.Loading
        )

        if (uiState is SmsUiState.Error) {
            val error = uiState as SmsUiState.Error
            Text(
                text = error.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )

            error.attemptsLeft?.let {
                Text(
                    text = "Осталось попыток: $it",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.confirmCode(phone)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is SmsUiState.Loading
        ) {
            if (uiState is SmsUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Подтвердить")
            }
        }
    }
}