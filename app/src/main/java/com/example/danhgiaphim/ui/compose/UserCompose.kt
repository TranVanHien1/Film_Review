package com.example.danhgiaphim.ui.compose

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.danhgiaphim.data.Comments
import com.example.danhgiaphim.ui.user.ChangePasswordUiState
import com.example.danhgiaphim.ui.user.ChangePasswordViewModel
import com.example.danhgiaphim.ui.user.LikeCommentUiState
import com.example.danhgiaphim.ui.user.LikeCommentViewModel
import com.example.danhgiaphim.ui.user.ProfileUiState
import com.example.danhgiaphim.ui.user.ProfileViewModel
import com.example.danhgiaphim.ui.user.UserUiState
import com.example.danhgiaphim.ui.user.UserViewModel
import java.util.Calendar

@Composable
fun UserScreen(
    viewModel: UserViewModel,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    onPassword: () -> Unit,
    onComments: () -> Unit,
    onNotifications: () -> Unit,
    onDeleted: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
    LaunchedEffect(state.deleted) {
        if (state.deleted) {
            viewModel.clearDeleted()
            onDeleted()
        }
    }

    AppScreen("Tài khoản", state.user?.email, onBack) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlideImage(state.user?.avatarURL.orEmpty(), Modifier.size(72.dp), circle = true)
                    Spacer(Modifier.size(14.dp))
                    Column {
                        Text(state.user?.username?.ifBlank { "Người dùng" } ?: "Người dùng", fontWeight = FontWeight.Bold)
                        Text(state.user?.email.orEmpty(), color = AppTextMuted)
                    }
                }
            }
            MenuRow("Cập nhật hồ sơ", onProfile)
            MenuRow("Đổi mật khẩu", onPassword)
            MenuRow("Bình luận của tôi", onComments)
            MenuRow("Thông báo", onNotifications)
            MenuRow("Xóa tài khoản") { confirmDelete = true }
        }
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = "Xác nhận",
            message = "Bạn có chắc chắn muốn xóa tài khoản không?",
            onDismiss = { confirmDelete = false },
            onConfirm = {
                confirmDelete = false
                viewModel.deleteAccount()
            }
        )
    }
}

@Composable
private fun MenuRow(text: String, onClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Text(text, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))
    }
}

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var dob by remember { mutableStateOf(0L) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var genderMenu by remember { mutableStateOf(false) }

    LaunchedEffect(state.user) {
        state.user?.let {
            username = it.username
            email = it.email
            gender = if (it.gender.isBlank()) "Male" else it.gender
            dob = it.dateOfBirth
        }
    }
    LaunchedEffect(state.message) {
        state.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.clearSaved()
            onSaved()
        }
    }

    AppScreen("Cập nhật hồ sơ", onBack = onBack) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ImagePickerBox(imageUri, state.user?.avatarURL.orEmpty(), "Chọn ảnh đại diện", onImagePicked = { imageUri = it })
            AppTextField(username, { username = it }, "Username")
            AppTextField(email, {}, "Email", enabled = false)
            AppCard(modifier = Modifier.fillMaxWidth().clickable { genderMenu = true }) {
                Text("Giới tính", color = AppTextMuted)
                Text(gender, fontWeight = FontWeight.Bold)
                DropdownMenu(expanded = genderMenu, onDismissRequest = { genderMenu = false }) {
                    listOf("Male", "Female").forEach {
                        DropdownMenuItem(text = { Text(it) }, onClick = {
                            gender = it
                            genderMenu = false
                        })
                    }
                }
            }
            AppCard(modifier = Modifier.fillMaxWidth().clickable {
                val calendar = Calendar.getInstance()
                if (dob > 0L) calendar.timeInMillis = dob
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val selected = Calendar.getInstance()
                        selected.set(year, month, day)
                        dob = selected.timeInMillis
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) {
                Text("Ngày sinh", color = AppTextMuted)
                Text(formatDate(dob).ifBlank { "Chọn ngày sinh" }, fontWeight = FontWeight.Bold)
            }
            AppButton("Lưu", enabled = !state.isSaving) {
                viewModel.save(username, gender, dob, imageUri)
            }
        }
    }
    if (state.isSaving) LoadingOverlay()
}

@Composable
fun ChangePasswordScreen(viewModel: ChangePasswordViewModel, onBack: () -> Unit, onChanged: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    LaunchedEffect(state.message) {
        state.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
    LaunchedEffect(state.changed) {
        if (state.changed) onChanged()
    }

    AppScreen("Đổi mật khẩu", onBack = onBack) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(oldPass, { oldPass = it }, "Mật khẩu cũ", keyboardType = KeyboardType.Password)
            AppTextField(newPass, { newPass = it }, "Mật khẩu mới", keyboardType = KeyboardType.Password)
            AppTextField(confirmPass, { confirmPass = it }, "Nhập lại mật khẩu", keyboardType = KeyboardType.Password)
            AppButton("Đổi mật khẩu", enabled = !state.isLoading) {
                viewModel.changePassword(oldPass, newPass, confirmPass)
            }
        }
    }
    if (state.isLoading) LoadingOverlay()
}

@Composable
fun UserCommentsScreen(viewModel: LikeCommentViewModel, onBack: () -> Unit, onMovieClick: (Comments) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(state.message) {
        state.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
    AppScreen("Bình luận của tôi", onBack = onBack) {
        SimpleList(state.comments, "Bạn chưa đăng bình luận nào") { (comment, user, title) ->
            AppCard(modifier = Modifier.fillMaxWidth().clickable { onMovieClick(comment) }) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(user.username, color = AppTextMuted)
                Text(comment.comment)
                Text("❤ ${comment.like}", color = AppPrimary)
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
}
