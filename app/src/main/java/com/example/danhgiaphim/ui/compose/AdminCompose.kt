package com.example.danhgiaphim.ui.compose

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.example.danhgiaphim.data.Actors
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Genre
import com.example.danhgiaphim.data.Users
import com.example.danhgiaphim.ui.admin.ActorListUiState
import com.example.danhgiaphim.ui.admin.ActorListViewModel
import com.example.danhgiaphim.ui.admin.FilmDetailUiState
import com.example.danhgiaphim.ui.admin.FilmDetailViewModel
import com.example.danhgiaphim.ui.admin.FilmListUiState
import com.example.danhgiaphim.ui.admin.FilmListViewModel
import com.example.danhgiaphim.ui.admin.GenreListUiState
import com.example.danhgiaphim.ui.admin.GenreListViewModel
import com.example.danhgiaphim.ui.admin.UserDetailUiState
import com.example.danhgiaphim.ui.admin.UserDetailViewModel
import com.example.danhgiaphim.ui.admin.UserListUiState
import com.example.danhgiaphim.ui.admin.UserListViewModel
import java.util.Calendar

@Composable
fun AdminHomeScreen(
    onUsers: () -> Unit,
    onFilms: () -> Unit,
    onActors: () -> Unit,
    onGenres: () -> Unit,
    onNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    AppScreen("Quản trị", "Danh mục quản lý") {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MenuTile("Danh sách người dùng", onUsers)
            MenuTile("Danh sách phim", onFilms)
            MenuTile("Danh sách diễn viên", onActors)
            MenuTile("Danh sách thể loại", onGenres)
            MenuTile("Thông báo", onNotifications)
            MenuTile("Đăng xuất", onLogout)
        }
    }
}

@Composable
private fun MenuTile(text: String, onClick: () -> Unit) {
    AppCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Text(text, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun AdminUserListScreen(viewModel: UserListViewModel, onBack: () -> Unit, onUserClick: (Users) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    AppScreen("Người dùng", onBack = onBack) {
        Column {
            AppTextField(search, {
                search = it
                viewModel.search(it)
            }, "Tìm người dùng", modifier = Modifier.padding(14.dp))
            SimpleList(state.users, "Không có người dùng") { user ->
                AppCard(Modifier.fillMaxWidth().clickable { onUserClick(user) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlideImage(user.avatarURL, Modifier.size(54.dp), circle = true)
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text(user.username.ifBlank { "Chưa cập nhật" }, fontWeight = FontWeight.Bold)
                            Text(user.email, color = AppTextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFilmListScreen(
    viewModel: FilmListViewModel,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onFilmClick: (Films) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    AppScreen("Kho phim", onBack = onBack, actions = {
        TextButton(onClick = onAdd) { Text("Thêm", color = androidx.compose.ui.graphics.Color.White) }
    }) {
        Column {
            AppTextField(search, {
                search = it
                viewModel.search(it)
            }, "Tìm phim", modifier = Modifier.padding(14.dp))
            SimpleList(state.films, "Không có phim") { film ->
                FilmRow(film, onClick = { onFilmClick(film) })
            }
        }
    }
}

@Composable
private fun FilmRow(film: Films, onClick: () -> Unit) {
    AppCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlideImage(film.posterURL, Modifier.size(64.dp))
            Spacer(Modifier.size(12.dp))
            Column {
                Text(film.title, fontWeight = FontWeight.Bold)
                Text(film.director, color = AppTextMuted)
            }
        }
    }
}

@Composable
fun AdminGenreListScreen(viewModel: GenreListViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var addDialog by remember { mutableStateOf(false) }
    var editGenre by remember { mutableStateOf<Genre?>(null) }
    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
    AppScreen("Thể loại", onBack = onBack, actions = {
        TextButton(onClick = { viewModel.sort() }) { Text("Sắp xếp", color = androidx.compose.ui.graphics.Color.White) }
        TextButton(onClick = { addDialog = true }) { Text("Thêm", color = androidx.compose.ui.graphics.Color.White) }
    }) {
        Column {
            AppTextField(search, {
                search = it
                viewModel.search(it)
            }, "Tìm thể loại", modifier = Modifier.padding(14.dp))
            SimpleList(state.genres, "Không có thể loại") { genre ->
                AppCard(Modifier.fillMaxWidth().clickable { editGenre = genre }) {
                    Text(genre.genreName, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    if (addDialog) GenreDialog(
        title = "Thêm thể loại",
        currentName = "",
        onDismiss = { addDialog = false },
        onSave = {
            addDialog = false
            viewModel.addGenre(it)
        }
    )
    editGenre?.let { genre ->
        GenreDialog("Sửa thể loại", genre.genreName, { editGenre = null }, {
            editGenre = null
            viewModel.updateGenre(genre.genreID, it)
        }, onDelete = {
            editGenre = null
            viewModel.deleteGenre(genre.genreID)
        })
    }
}

@Composable
private fun GenreDialog(
    title: String,
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { AppTextField(name, { name = it }, "Tên thể loại") },
        confirmButton = { TextButton(onClick = { onSave(name) }) { Text("Lưu") } },
        dismissButton = {
            if (onDelete != null) TextButton(onClick = onDelete) { Text("Xóa") }
            else TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
fun AdminActorListScreen(
    viewModel: ActorListViewModel,
    onBack: () -> Unit,
    onAdd: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var editActor by remember { mutableStateOf<Actors?>(null) }
    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
    AppScreen("Diễn viên", onBack = onBack, actions = {
        TextButton(onClick = onAdd) { Text("Thêm", color = androidx.compose.ui.graphics.Color.White) }
    }) {
        Column {
            AppTextField(search, {
                search = it
                viewModel.search(it)
            }, "Tìm diễn viên", modifier = Modifier.padding(14.dp))
            SimpleList(state.actors, "Không có diễn viên") { actor ->
                AppCard(Modifier.fillMaxWidth().clickable { editActor = actor }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlideImage(actor.actorAvatarURL, Modifier.size(54.dp), circle = true)
                        Spacer(Modifier.size(12.dp))
                        Text(actor.actorName, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    editActor?.let { actor ->
        ActorEditDialog(actor, onDismiss = { editActor = null }, onSave = { name, uri ->
            editActor = null
            viewModel.updateActor(actor, name, uri)
        }, onDelete = {
            editActor = null
            viewModel.deleteActor(actor.actorID)
        })
    }
}

@Composable
private fun ActorEditDialog(actor: Actors, onDismiss: () -> Unit, onSave: (String, android.net.Uri?) -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(actor.actorName) }
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa diễn viên") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ImagePickerBox(imageUri, actor.actorAvatarURL, "Chọn ảnh", onImagePicked = { imageUri = it })
                AppTextField(name, { name = it }, "Tên diễn viên")
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, imageUri) }) { Text("Lưu") } },
        dismissButton = { TextButton(onClick = onDelete) { Text("Xóa") } }
    )
}

@Composable
fun AdminUserDetailScreen(viewModel: UserDetailViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AppScreen("Chi tiết người dùng", onBack = onBack) {
        state.user?.let { user ->
            Column(Modifier.verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GlideImage(user.avatarURL, Modifier.size(96.dp), circle = true)
                DetailLine("Username", user.username)
                DetailLine("Email", user.email)
                DetailLine("Giới tính", user.gender)
                DetailLine("Ngày sinh", formatDate(user.dateOfBirth))
                DetailLine("User ID", user.userID)
            }
        } ?: EmptyState("Không có dữ liệu")
    }
}

@Composable
fun AdminFilmDetailScreen(
    viewModel: FilmDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRating: () -> Unit,
    onDeleted: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var menu by remember { mutableStateOf(false) }
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
    AppScreen("Chi tiết phim", onBack = onBack, actions = {
        TextButton(onClick = { menu = true }) { Text("Tùy chọn", color = androidx.compose.ui.graphics.Color.White) }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(text = { Text("Chỉnh sửa") }, onClick = { menu = false; onEdit() })
            DropdownMenuItem(text = { Text("Đánh giá") }, onClick = { menu = false; onRating() })
            DropdownMenuItem(text = { Text("Xóa") }, onClick = { menu = false; confirmDelete = true })
        }
    }) {
        state.film?.let { film ->
            Column(Modifier.verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GlideImage(film.posterURL, Modifier.fillMaxWidth().height(260.dp))
                DetailLine("Tên phim", film.title)
                DetailLine("Đạo diễn", film.director)
                DetailLine("Năm", film.releaseYear)
                DetailLine("Trailer", film.trailerURL)
                DetailLine("Thể loại", film.genre?.values?.joinToString { it.genreName }.orEmpty())
                DetailLine("Diễn viên", film.actor?.values?.joinToString { it.actorName }.orEmpty())
                DetailLine("Nội dung", film.synopsis)
            }
        } ?: EmptyState("Không có dữ liệu")
    }
    if (confirmDelete) {
        ConfirmDialog("Xóa phim", "Bạn có chắc chắn muốn xóa phim này?", { confirmDelete = false }) {
            confirmDelete = false
            state.film?.movieID?.let { viewModel.deleteFilm(it) }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    AppCard(Modifier.fillMaxWidth()) {
        Text(label, color = AppTextMuted)
        Text(value.ifBlank { "Chưa cập nhật" }, fontWeight = FontWeight.Bold)
    }
}

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
}
