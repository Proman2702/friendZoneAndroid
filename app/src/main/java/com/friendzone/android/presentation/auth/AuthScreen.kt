package com.friendzone.android.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.friendzone.android.R

private val AuthBackground = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF17113B),
        Color(0xFF0D0727)
    )
)
private val AuthAccent = Color(0xFFE3874F)
private val AuthFieldText = Color(0xFF1D163D)
private val AuthFieldLabel = Color(0xFF3EABE3)
private val LogoWhite = Color.White
private val Righteous = FontFamily(Font(R.font.righteous))

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
        Spacer(modifier = Modifier.height(120.dp))
        LogoBlock()
        Spacer(modifier = Modifier.height(96.dp))
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Логин",
            keyboardType = KeyboardType.Email,
            width = 320.dp,
            height = 64.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Пароль",
            keyboardType = KeyboardType.Password,
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            width = 320.dp,
            height = 64.dp
        )
        Spacer(modifier = Modifier.height(12.dp))
        UnderlinedActionText(
            text = "Забыли пароль?",
            modifier = Modifier.align(Alignment.End),
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
        PrimaryAuthButton(
            text = "Вход",
            width = 140.dp,
            height = 40.dp
        ) {
            onLogin(email, password)
        }
        UnderlinedActionText(
            text = "Регистрация",
            modifier = Modifier.padding(top = 6.dp),
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
        Spacer(modifier = Modifier.height(120.dp))
        AuthTitleBlock("Регистрация")
        Spacer(modifier = Modifier.height(96.dp))
        AuthTextField(
            value = name,
            onValueChange = { name = it },
            label = "Имя",
            width = 320.dp,
            height = 64.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Почта",
            keyboardType = KeyboardType.Email,
            width = 320.dp,
            height = 64.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Пароль",
            keyboardType = KeyboardType.Password,
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            width = 320.dp,
            height = 64.dp
        )
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = Color(0xFFFFB4AB),
                modifier = Modifier.padding(top = 20.dp),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(80.dp))
        PrimaryAuthButton(
            text = "Сохранить",
            width = 140.dp,
            height = 40.dp
        ) {
            onRegister(name, email, password)
        }
        UnderlinedActionText(
            text = "Назад ко входу",
            modifier = Modifier.padding(top = 6.dp),
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
        Spacer(modifier = Modifier.height(120.dp))
        AuthTitleBlock("Восстановление пароля")
        Spacer(modifier = Modifier.height(96.dp))
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Почта",
            keyboardType = KeyboardType.Email,
            width = 320.dp,
            height = 64.dp
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
        PrimaryAuthButton(
            text = "Отправить",
            width = 140.dp,
            height = 40.dp
        ) {
            onRecover(email)
        }
        UnderlinedActionText(
            text = "Назад ко входу",
            modifier = Modifier.padding(top = 6.dp),
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
            style = TextStyle(
                fontFamily = Righteous,
                fontSize = 60.sp,
                color = LogoWhite,
                letterSpacing = 3.sp
            )
        )
        Row(
            modifier = Modifier
                .offset(y = (-16).dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "z",
                    style = TextStyle(
                        fontFamily = Righteous,
                        fontSize = 60.sp,
                        color = LogoWhite
                    ),
                    modifier = Modifier.padding(end = 60.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.location_pin),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFFDF7E4A)),
                    modifier = Modifier
                        .size(60.dp)
                        .offset(x = 10.dp, y = (-4).dp)
                )
            }
            Text(
                text = "ne",
                style = TextStyle(
                    fontFamily = Righteous,
                    fontSize = 60.sp,
                    color = LogoWhite,
                    letterSpacing = 3.sp
                ),
                modifier = Modifier.offset(x = (-8).dp)
            )
        }
    }
}

@Composable
private fun AuthTitleBlock(title: String) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 40.sp,
            lineHeight = 50.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    width: Dp = 320.dp,
    height: Dp = 64.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null
) {
    val fieldShape = RoundedCornerShape(16.dp)
    val labelShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp)
    val visualTransformation = if (isPassword && !passwordVisible) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(height - 10.dp)
                .clip(fieldShape)
                .background(Color.White)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = AuthFieldText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = visualTransformation,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp, end = if (isPassword) 52.dp else 20.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        innerTextField()
                    }
                }
            )

            if (isPassword && onTogglePasswordVisibility != null) {
                IconButton(
                    onClick = onTogglePasswordVisibility,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.VisibilityOff
                        },
                        contentDescription = if (passwordVisible) {
                            "Пароль виден"
                        } else {
                            "Пароль скрыт"
                        },
                        tint = AuthFieldText
                    )
                }
            }
        }

        Text(
            text = label,
            color = AuthFieldLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 20.dp)
                .clip(labelShape)
                .background(Color.White)
                .padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PrimaryAuthButton(
    text: String,
    width: Dp = 140.dp,
    height: Dp = 48.dp,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthAccent,
            contentColor = Color.White
        ),
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(10.dp))
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
private fun UnderlinedActionText(
    text: String,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Bold,
    textSize: TextUnit = 14.sp,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = fontWeight,
        fontSize = textSize,
        modifier = modifier
            .clickable(onClick = onClick)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            }
            .padding(bottom = 1.dp)
    )
}
