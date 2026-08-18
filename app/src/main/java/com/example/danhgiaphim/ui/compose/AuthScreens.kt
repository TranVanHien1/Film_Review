package com.example.danhgiaphim.ui.compose

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.danhgiaphim.R
import com.example.danhgiaphim.ui.auth.LoginDestination
import com.example.danhgiaphim.ui.auth.LoginUiState
import com.example.danhgiaphim.ui.auth.LoginViewModel
import com.example.danhgiaphim.ui.auth.SignUiState
import com.example.danhgiaphim.ui.auth.SignViewModel
import com.example.danhgiaphim.ui.main.MainUiState
import com.example.danhgiaphim.ui.main.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onAdmin: () -> Unit,
    onHome: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
    LaunchedEffect(state.destination) {
        when (state.destination) {
            LoginDestination.Admin -> {
                viewModel.clearNavigation()
                onAdmin()
            }
            LoginDestination.Home -> {
                viewModel.clearNavigation()
                onHome()
            }
            null -> Unit
        }
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.image),
                contentDescription = null,
                modifier = Modifier.size(112.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(28.dp))
            Text("Đánh Giá Phim", color = AppText, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Theo dõi, bình luận và quản lý phim yêu thích",
                color = AppTextMuted,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(28.dp))
            PrimaryButton(text = "Đăng nhập", onClick = onLogin)
            Spacer(Modifier.height(12.dp))
            SecondaryButton(text = "Đăng ký", onClick = onRegister)
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onRegister: () -> Unit,
    onAdmin: () -> Unit,
    onHome: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(state.destination) {
        when (state.destination) {
            LoginDestination.Admin -> {
                viewModel.clearNavigation()
                onAdmin()
            }
            LoginDestination.Home -> {
                viewModel.clearNavigation()
                onHome()
            }
            null -> Unit
        }
    }

    AuthBackground {
        AuthCard {
            Text("Đăng nhập", color = AppText, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Chào mừng bạn quay lại", color = AppTextMuted, fontSize = 14.sp)
            Spacer(Modifier.height(22.dp))
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Mật khẩu",
                keyboardType = KeyboardType.Password,
                isPassword = true
            )
            Text(
                text = "Quên mật khẩu?",
                color = AppPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(vertical = 12.dp)
                    .clickable { showForgotDialog = true }
            )
            PrimaryButton(
                text = "Đăng nhập",
                enabled = !state.isLoading,
                onClick = { viewModel.login(email, password) }
            )
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Text("Bạn chưa có tài khoản? ", color = AppTextMuted)
                Text(
                    "Đăng ký ngay",
                    color = AppPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onRegister)
                )
            }
        }
    }

    if (state.isLoading) LoadingDialog()
    if (showForgotDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotDialog = false },
            onSubmit = {
                viewModel.resetPassword(it)
                showForgotDialog = false
            }
        )
    }
}

@Composable
fun SignScreen(
    viewModel: SignViewModel,
    onLogin: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) {
            viewModel.clearRegistered()
            onLogin()
        }
    }

    AuthBackground {
        AuthCard {
            Text("Đăng ký", color = AppText, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Tạo tài khoản để bắt đầu đánh giá phim", color = AppTextMuted, fontSize = 14.sp)
            Spacer(Modifier.height(22.dp))
            AuthTextField(email, { email = it }, "Email", KeyboardType.Email)
            Spacer(Modifier.height(12.dp))
            AuthTextField(password, { password = it }, "Mật khẩu", KeyboardType.Password, true)
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                repeatPassword,
                { repeatPassword = it },
                "Nhập lại mật khẩu",
                KeyboardType.Password,
                true
            )
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = "Đăng ký",
                enabled = !state.isLoading,
                onClick = { viewModel.register(email, password, repeatPassword) }
            )
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Text("Bạn đã có tài khoản? ", color = AppTextMuted)
                Text(
                    "Đăng nhập ngay",
                    color = AppPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onLogin)
                )
            }
        }
    }

    if (state.isLoading) LoadingDialog()
}

@Composable
private fun AuthBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun AuthCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp), content = content)
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppSurface,
            contentColor = AppPrimary
        )
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LoadingDialog() {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        title = { Text("Đang xử lý") },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                Spacer(Modifier.size(16.dp))
                Text("Vui lòng chờ...")
            }
        }
    )
}

@Composable
private fun ForgotPasswordDialog(onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quên mật khẩu") },
        text = {
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email
            )
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(email) }) {
                Text("Gửi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
