package com.friendzone.android.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AuthBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF17113B),
        Color(0xFF0D0727)
    )
)
private val AuthAccent = Color(0xFFE3874F)
private val AuthFieldText = Color(0xFF1D163D)

@Composable
fun LoginScreen(
    errorMessage: String?,
    infoMessage: String?,
    onLogin: (String, String) -> Unit,
    onOpenRegistration: () -> Unit,
    onOpenForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AuthScaffold {
        LogoBlock()
        Spacer(modifier = Modifier.height(72.dp))
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Логин",
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Пароль",
            keyboardType = KeyboardType.Password,
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
        )
        UnderlinedActionText(
            text = "Забыли пароль?",
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp),
            onClick = onOpenForgotPassword
        )
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = Color(0xFFFFB4AB),
                modifier = Modifier.padding(top = 20.dp),
                textAlign = TextAlign.Center
            )
        }
        if (!infoMessage.isNullOrBlank()) {
            Text(
                text = infoMessage,
                color = Color(0xFFD9D5F1),
                modifier = Modifier.padding(top = 20.dp),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(80.dp))
        PrimaryAuthButton(text = "Вход") {
            onLogin(email, password)
        }
        UnderlinedActionText(
            text = "Регистрация",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp),
            onClick = onOpenRegistration
        )
    }
}

@Composable
fun RegisterScreen(
    errorMessage: String?,
    onRegister: (String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AuthScaffold {
        AuthHeader("Регистрация")
        Spacer(modifier = Modifier.height(40.dp))
        AuthTextField(value = name, onValueChange = { name = it }, label = "Имя")
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Почта",
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Пароль",
            keyboardType = KeyboardType.Password,
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
        )
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = Color(0xFFFFB4AB),
                modifier = Modifier.padding(top = 20.dp),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(48.dp))
        PrimaryAuthButton(text = "Сохранить") {
            onRegister(name, email, password)
        }
        UnderlinedActionText(
            text = "Назад ко входу",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp),
            onClick = onBack
        )
    }
}

@Composable
fun ForgotPasswordScreen(
    errorMessage: String?,
    infoMessage: String?,
    onRecover: (String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    AuthScaffold {
        AuthHeader("Восстановление")
        Spacer(modifier = Modifier.height(40.dp))
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Почта",
            keyboardType = KeyboardType.Email
        )
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = Color(0xFFFFB4AB),
                modifier = Modifier.padding(top = 20.dp),
                textAlign = TextAlign.Center
            )
        }
        if (!infoMessage.isNullOrBlank()) {
            Text(
                text = infoMessage,
                color = Color(0xFFD9D5F1),
                modifier = Modifier.padding(top = 20.dp),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(48.dp))
        PrimaryAuthButton(text = "Отправить") {
            onRecover(email)
        }
        UnderlinedActionText(
            text = "Назад ко входу",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp),
            onClick = onBack
        )
    }
}

@Composable
private fun AuthScaffold(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(horizontal = 30.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
private fun LogoBlock() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Friend",
            color = Color.White,
            fontSize = 46.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "zOne",
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun AuthHeader(title: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Friend zOne",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = title,
            color = AuthAccent,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        trailingIcon = if (isPassword && onTogglePasswordVisibility != null) {
            {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                        tint = AuthFieldText
                    )
                }
            }
        } else {
            null
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = AuthFieldText,
            unfocusedTextColor = AuthFieldText,
            focusedLabelColor = Color(0xFF2E77B8),
            unfocusedLabelColor = Color(0xFF2E77B8),
            cursorColor = AuthAccent
        )
    )
}

@Composable
private fun PrimaryAuthButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthAccent,
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth(0.38f)
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun UnderlinedActionText(
    text: String,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal,
    underlineOffset: Dp = 2.dp,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = fontWeight,
        modifier = modifier
            .clickable(onClick = onClick)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height + underlineOffset.toPx()
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            }
            .padding(bottom = 3.dp)
    )
}


