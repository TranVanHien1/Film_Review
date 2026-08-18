package com.example.danhgiaphim.ui.compose

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.danhgiaphim.data.Notification
import com.example.danhgiaphim.ui.notification.NotificationListViewModel
import com.example.danhgiaphim.ui.notification.NotificationManageViewModel
import com.example.danhgiaphim.ui.notification.NotificationUiState

@Composable
fun NotificationListScreen(viewModel: NotificationListViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(state.message) {
        state.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
    AppScreen("Thông báo", "Tin mới dành cho bạn", onBack) {
        SimpleList(state.notifications, "Chưa có thông báo") { notification ->
            NotificationCard(notification)
        }
    }
}

@Composable
fun NotificationManageScreen(viewModel: NotificationManageViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var addDialog by remember { mutableStateOf(false) }
    var editNotification by remember { mutableStateOf<Notification?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    AppScreen(
        title = "Quản lý thông báo",
        onBack = onBack,
        actions = {
            TextButton(onClick = { addDialog = true }) { Text("Thêm", color = androidx.compose.ui.graphics.Color.White) }
        }
    ) {
        SimpleList(state.notifications, "Chưa có thông báo") { notification ->
            NotificationCard(notification, modifier = Modifier.clickable { editNotification = notification })
        }
    }

    if (addDialog) {
        NotificationEditDialog(
            title = "Gửi thông báo mới",
            notification = Notification(),
            onDismiss = { addDialog = false },
            onSave = { title, content ->
                addDialog = false
                viewModel.add(title, content)
            },
            onDelete = null
        )
    }
    editNotification?.let { item ->
        NotificationEditDialog(
            title = "Sửa hoặc xoá thông báo",
            notification = item,
            onDismiss = { editNotification = null },
            onSave = { title, content ->
                editNotification = null
                viewModel.update(item.copy(title = title, content = content))
            },
            onDelete = {
                editNotification = null
                viewModel.delete(item.id)
            }
        )
    }
}

@Composable
private fun NotificationCard(notification: Notification, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(notification.title, fontWeight = FontWeight.Bold)
        Text(notification.content, color = AppTextMuted)
    }
}

@Composable
private fun NotificationEditDialog(
    title: String,
    notification: Notification,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onDelete: (() -> Unit)?
) {
    var itemTitle by remember { mutableStateOf(notification.title) }
    var content by remember { mutableStateOf(notification.content) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(itemTitle, { itemTitle = it }, "Tiêu đề")
                AppTextField(content, { content = it }, "Nội dung", minLines = 3)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(itemTitle, content) }) { Text("Lưu") }
        },
        dismissButton = {
            if (onDelete != null) {
                TextButton(onClick = onDelete) { Text("Xoá") }
            } else {
                TextButton(onClick = onDismiss) { Text("Hủy") }
            }
        }
    )
}
