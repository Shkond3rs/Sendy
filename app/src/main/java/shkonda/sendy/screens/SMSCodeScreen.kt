package shkonda.sendy.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SMSCodeScreen(phone: String, onSmsValidated: () -> Unit) {
    var smsCode by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    // Валидация кода: ровно 6 цифр
    fun isValidCode(code: String) = code.matches(Regex("^\\d{6}\$"))

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("На номер $phone отправлен SMS-код")

        OutlinedTextField(
            value = smsCode,
            onValueChange = { smsCode = it },
            label = { Text("Введите 6-значный код") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (error.isNotEmpty()) {
            Text(text = error, color = androidx.compose.ui.graphics.Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (!isValidCode(smsCode)) {
                error = "Код должен содержать ровно 6 цифр"
            } else {
                error = ""
                // TODO: API?
                onSmsValidated()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Продолжить")
        }
    }
}
