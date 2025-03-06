package shkonda.sendy.ui.login

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    termsText: String,
    onNavigateToSms: (String) -> Unit
) {
    // Наблюдение за состоянием из ViewModel
    val uiState by viewModel.uiState.observeAsState()
    val phone by viewModel.phoneInput.observeAsState("")
    val isAgreed by viewModel.isAgreed.observeAsState(false)

    var showTermsDialog by remember { mutableStateOf(false) }

    // Инициализация номера телефона с префиксом +7
    LaunchedEffect(Unit) {
        if (phone.isEmpty()) {
            viewModel.updatePhone("+7 ")
        }
    }

    // Обработка состояний UI
    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> {
                onNavigateToSms((uiState as LoginUiState.Success).phone)
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
            text = "Введите номер телефона",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { viewModel.updatePhone(it) },
            label = { Text("Телефон") },
            placeholder = { Text("+7 XXX XXX XX XX") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = uiState !is LoginUiState.Loading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        if (uiState is LoginUiState.Error) {
            Text(
                text = (uiState as LoginUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Checkbox(
                checked = isAgreed,
                onCheckedChange = { viewModel.updateAgreement(it) },
                enabled = uiState !is LoginUiState.Loading
            )
            Text(
                text = "Я согласен с ",
                modifier = Modifier.padding(start = 8.dp)
            )
            TextButton(onClick = { showTermsDialog = true }) {
                Text("правилами пользования")
            }
        }

        Button(
            onClick = {
                viewModel.activateWallet()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isAgreed && uiState !is LoginUiState.Loading
        ) {
            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Продолжить")
            }
        }
    }

    // Диалоговое окно для отображения текста соглашения
    if (showTermsDialog) {
        Dialog(
            onDismissRequest = { showTermsDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false // Делаем диалоговое окно шире
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Права пользователя",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    // Настройка WebView вместе со стилями
                                    settings.apply {
                                        javaScriptEnabled = false
                                        loadWithOverviewMode = true
                                        useWideViewPort = true
                                        setSupportZoom(true)
                                        builtInZoomControls = true
                                        displayZoomControls = false
                                    }

                                    val styledHtml = """
                                        <!DOCTYPE html>
                                        <html>
                                        <head>
                                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                            <style>
                                                body {
                                                    font-family: 'Roboto', sans-serif;
                                                    line-height: 1.6;
                                                    color: #333;
                                                    padding: 8px;
                                                    font-size: 16px;
                                                }
                                                h1, h2, h3 {
                                                    color: #1976D2;
                                                }
                                                p {
                                                    margin-bottom: 16px;
                                                }
                                                ul, ol {
                                                    padding-left: 20px;
                                                }
                                                li {
                                                    margin-bottom: 8px;
                                                }
                                            </style>
                                        </head>
                                        <body>
                                            $termsText
                                        </body>
                                        </html>
                                    """

                                    loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.updateAgreement(true)
                                showTermsDialog = false
                            }
                        ) {
                            Text("Принять")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(
                            onClick = { showTermsDialog = false }
                        ) {
                            Text("Закрыть")
                        }
                    }
                }
            }
        }
    }
}