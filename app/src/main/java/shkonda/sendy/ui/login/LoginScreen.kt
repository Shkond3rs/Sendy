package shkonda.sendy.ui.login

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import shkonda.sendy.ui.theme.SendyTheme

@Preview
@Composable
private fun TextPrev() {
    SendyTheme {
        Surface {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Checkbox(
                        checked = false,
                        onCheckedChange = { }
                    )
                    Column(
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Я согласен с ",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable(onClick = { })
                            )
                            Text(
                                text = "правилами пользования",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { }
                            )
                        }
                        Text(
                            text = "Необходимо для продолжения регистрации",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    termsText: String,
    onNavigateToSms: (String) -> Unit
) {
    // Наблюдение за состоянием из ViewModel
    val uiState by viewModel.uiState.observeAsState()
    val textFieldValue by viewModel.textFieldValue.observeAsState(TextFieldValue(""))
    val isAgreed by viewModel.isAgreed.observeAsState(false)

    var showTermsDialog by remember { mutableStateOf(false) }

    // При входе на экран сбрасываем состояние Success, если оно было
    LaunchedEffect(Unit) {
        if (uiState is LoginUiState.Success) {
            viewModel.resetSuccessState()
        }
    }

    // Обработка состояний UI
    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> {
                onNavigateToSms((uiState as LoginUiState.Success).phone)
            }

            else -> { /* Другие состояния обрабатываются в соответствии с решением команды разработчиков */
            }
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
            value = textFieldValue,
            onValueChange = { viewModel.updatePhoneWithCursor(it) },
            label = { Text("Номер телефона") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = uiState !is LoginUiState.Loading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            singleLine = true,
            maxLines = 1,
            visualTransformation = PrefixTransformation("+7")
        )

        if (uiState is LoginUiState.Error) {
            Text(
                text = (uiState as LoginUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Заменяем строку с чекбоксом на карточку
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable(
                    enabled = uiState !is LoginUiState.Loading,
                    onClick = { viewModel.updateAgreement(!isAgreed) }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAgreed)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Checkbox(
                    checked = isAgreed,
                    onCheckedChange = { viewModel.updateAgreement(it) },
                    enabled = uiState !is LoginUiState.Loading,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
                Column(
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Я согласен с ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "правилами пользования",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable(
                                    enabled = uiState !is LoginUiState.Loading,
                                    onClick = {
                                        showTermsDialog = true
                                    }
                                )
                                .padding(vertical = 4.dp, horizontal = 2.dp) // Увеличиваем область нажатия
                        )
                    }
                    Text(
                        text = "Необходимо для продолжения регистрации",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
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

                                    loadDataWithBaseURL(
                                        null,
                                        styledHtml,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
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