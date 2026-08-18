package com.example.danhgiaphim.ui.compose

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.danhgiaphim.data.Actors
import com.example.danhgiaphim.data.Comments
import com.example.danhgiaphim.data.Genre
import com.example.danhgiaphim.data.Rating
import com.example.danhgiaphim.data.repository.FilmInput
import com.example.danhgiaphim.ui.film.AddActorViewModel
import com.example.danhgiaphim.ui.film.AddFilmViewModel
import com.example.danhgiaphim.ui.film.EditFilmUiState
import com.example.danhgiaphim.ui.film.EditFilmViewModel
import com.example.danhgiaphim.ui.film.FilmProfileUiState
import com.example.danhgiaphim.ui.film.FilmProfileViewModel
import com.example.danhgiaphim.ui.film.FilmRatingUiState
import com.example.danhgiaphim.ui.film.FilmRatingViewModel
import com.example.danhgiaphim.ui.film.ManageSubmitState

@Composable
fun FilmProfileScreen(
    viewModel: FilmProfileViewModel,
    onBack: () -> Unit,
    onTrailer: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var ratingDialog by remember { mutableStateOf(false) }
    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(state.shouldDismissRatingDialog) {
        if (state.shouldDismissRatingDialog) {
            ratingDialog = false
            viewModel.clearDismissRatingDialog()
        }
    }

    AppScreen("Chi tiết phim", onBack = onBack) {
        state.film?.let { film ->
            Column(Modifier.verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlideImage(film.posterURL, Modifier.fillMaxWidth().height(300.dp))
                Text(film.title, fontWeight = FontWeight.Bold, color = AppText)
                Text("Năm phát hành: ${film.releaseYear}", color = AppTextMuted)
                Text("Đạo diễn: ${film.director}", color = AppTextMuted)
                Text("Thể loại: ${film.genre?.values?.joinToString { it.genreName } ?: "Không rõ"}")
                Text(film.synopsis)
                RatingSummary(state.rating)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(film.actor?.values?.toList() ?: emptyList()) { actor ->
                        AppCard(Modifier.size(width = 110.dp, height = 145.dp)) {
                            GlideImage(actor.actorAvatarURL, Modifier.fillMaxWidth().height(92.dp), circle = true)
                            Text(actor.actorName, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                AppButton("Xem trailer") { if (film.trailerURL.isNotBlank()) onTrailer(film.trailerURL) }
                if (state.canRate) AppButton("Đánh giá phim") { ratingDialog = true }
                Text("Bình luận", fontWeight = FontWeight.Bold)
                state.comments.forEach { (comment, user) ->
                    AppCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GlideImage(user.avatarURL, Modifier.size(42.dp), circle = true)
                            Spacer(Modifier.size(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(user.username, fontWeight = FontWeight.Bold)
                                Text(comment.comment)
                            }
                            Text(
                                "❤ ${comment.like}",
                                modifier = Modifier.clickable { viewModel.toggleCommentLike(comment) },
                                color = if (state.likedCommentIds.contains(comment.reviewID)) AppPrimary else AppTextMuted
                            )
                        }
                    }
                }
                TextButton(onClick = viewModel::loadMoreComments, enabled = state.hasMoreComments && !state.isLoadingComments) {
                    Text(if (state.hasMoreComments) "Tải thêm bình luận" else "Không còn bình luận")
                }
            }
        } ?: EmptyState("Không có dữ liệu phim")
    }
    if (state.isSubmittingReview) LoadingOverlay("Đang kiểm tra nội dung...")
    if (ratingDialog) ReviewDialog(
        onDismiss = { ratingDialog = false },
        onSubmit = { content, effect, cast, comment ->
            viewModel.submitReview(content, effect, cast, comment)
        }
    )
}

@Composable
private fun RatingSummary(rating: Rating) {
    AppCard(Modifier.fillMaxWidth()) {
        Text("Điểm tổng: %.1f".format(rating.rating), fontWeight = FontWeight.Bold)
        Text("Nội dung: %.1f | Hiệu ứng: %.1f | Diễn viên: %.1f".format(rating.contentRating, rating.effectRating, rating.castRating))
        Text("Số lượt đánh giá: ${rating.reviewCount}", color = AppTextMuted)
    }
}

@Composable
private fun ReviewDialog(onDismiss: () -> Unit, onSubmit: (Float, Float, Float, String) -> Unit) {
    var content by remember { mutableStateOf("5") }
    var effect by remember { mutableStateOf("5") }
    var cast by remember { mutableStateOf("5") }
    var comment by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đánh giá phim") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(content, { content = it }, "Nội dung")
                NumberField(effect, { effect = it }, "Hiệu ứng")
                NumberField(cast, { cast = it }, "Diễn viên")
                AppTextField(comment, { comment = it }, "Bình luận", minLines = 3)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSubmit(
                    content.toFloatOrNull() ?: 0F,
                    effect.toFloatOrNull() ?: 0F,
                    cast.toFloatOrNull() ?: 0F,
                    comment
                )
            }) { Text("Gửi") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun NumberField(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

@Composable
fun AddFilmScreen(viewModel: AddFilmViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    FilmFormScreen(
        title = "Thêm phim",
        state = state,
        imageUri = imageUri,
        imageUrl = "",
        onImage = { imageUri = it },
        onBack = onBack,
        onDone = {
            viewModel.addFilm(it, imageUri)
        },
        onSuccess = {
            viewModel.clearSuccess()
            onDone()
        }
    )
}

@Composable
private fun FilmFormScreen(
    title: String,
    state: ManageSubmitState,
    imageUri: Uri?,
    imageUrl: String,
    onImage: (Uri) -> Unit,
    onBack: () -> Unit,
    onDone: (FilmInput) -> Unit,
    onSuccess: () -> Unit,
    initial: FilmInput = FilmInput("", "", "", "", ""),
    extraContent: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    var name by remember(initial.title) { mutableStateOf(initial.title) }
    var year by remember(initial.releaseYear) { mutableStateOf(initial.releaseYear) }
    var director by remember(initial.director) { mutableStateOf(initial.director) }
    var synopsis by remember(initial.synopsis) { mutableStateOf(initial.synopsis) }
    var trailer by remember(initial.trailerUrl) { mutableStateOf(initial.trailerUrl) }
    LaunchedEffect(state.message) { state.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
    LaunchedEffect(state.isSuccess) { if (state.isSuccess) onSuccess() }
    AppScreen(title, onBack = onBack) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ImagePickerBox(imageUri, imageUrl, "Chọn poster", onImagePicked = onImage)
            AppTextField(name, { name = it }, "Tên phim")
            AppTextField(year, { year = it }, "Năm phát hành")
            AppTextField(director, { director = it }, "Đạo diễn")
            AppTextField(synopsis, { synopsis = it }, "Nội dung", minLines = 4)
            AppTextField(trailer, { trailer = it }, "Trailer URL")
            extraContent()
            AppButton("Lưu", enabled = !state.isLoading) {
                onDone(FilmInput(name, year, director, synopsis, trailer))
            }
        }
    }
    if (state.isLoading) LoadingOverlay()
}

@Composable
fun AddActorScreen(viewModel: AddActorViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    LaunchedEffect(state.message) { state.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.clearSuccess()
            onDone()
        }
    }
    AppScreen("Thêm diễn viên", onBack = onBack) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ImagePickerBox(imageUri, "", "Chọn ảnh diễn viên", onImagePicked = { imageUri = it })
            AppTextField(name, { name = it }, "Tên diễn viên")
            AppButton("Thêm", enabled = !state.isLoading) { viewModel.addActor(name, imageUri) }
        }
    }
    if (state.isLoading) LoadingOverlay()
}

@Composable
fun EditFilmScreen(viewModel: EditFilmViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var genreDialog by remember { mutableStateOf(false) }
    var actorDialog by remember { mutableStateOf(false) }
    LaunchedEffect(state.message) { state.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.clearSaved()
            onDone()
        }
    }
    val film = state.film
    FilmFormScreen(
        title = "Sửa phim",
        state = ManageSubmitState(isLoading = state.isSaving, message = state.message, isSuccess = state.isSaved),
        imageUri = imageUri,
        imageUrl = film?.posterURL.orEmpty(),
        onImage = { imageUri = it },
        onBack = onBack,
        onDone = { input -> film?.movieID?.let { viewModel.save(it, input, imageUri) } },
        onSuccess = onDone,
        initial = FilmInput(film?.title.orEmpty(), film?.releaseYear.orEmpty(), film?.director.orEmpty(), film?.synopsis.orEmpty(), film?.trailerURL.orEmpty()),
        extraContent = {
            AppCard(Modifier.fillMaxWidth().clickable { genreDialog = true }) {
                Text("Thể loại", color = AppTextMuted)
                Text(state.selectedGenres.values.joinToString { it.genreName }.ifBlank { "Chọn thể loại" })
            }
            AppCard(Modifier.fillMaxWidth().clickable { actorDialog = true }) {
                Text("Diễn viên", color = AppTextMuted)
                Text(state.selectedActors.values.joinToString { it.actorName }.ifBlank { "Chọn diễn viên" })
            }
        }
    )
    if (genreDialog) MultiGenreDialog(state, { genreDialog = false }, viewModel::setSelectedGenres)
    if (actorDialog) MultiActorDialog(state, { actorDialog = false }, viewModel::setSelectedActors)
}

@Composable
private fun MultiGenreDialog(state: EditFilmUiState, onDismiss: () -> Unit, onSave: (Map<String, Genre>) -> Unit) {
    val selected = remember { mutableStateMapOf<String, Genre>().apply { putAll(state.selectedGenres) } }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Chọn thể loại") }, text = {
        Column { state.allGenres.forEach { genre ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(selected.containsKey(genre.genreID), { checked -> if (checked) selected[genre.genreID] = genre else selected.remove(genre.genreID) })
                Text(genre.genreName)
            }
        } }
    }, confirmButton = { TextButton(onClick = { onSave(selected); onDismiss() }) { Text("OK") } })
}

@Composable
private fun MultiActorDialog(state: EditFilmUiState, onDismiss: () -> Unit, onSave: (Map<String, Actors>) -> Unit) {
    val selected = remember { mutableStateMapOf<String, Actors>().apply { putAll(state.selectedActors) } }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Chọn diễn viên") }, text = {
        Column { state.allActors.forEach { actor ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(selected.containsKey(actor.actorID), { checked -> if (checked) selected[actor.actorID] = actor else selected.remove(actor.actorID) })
                Text(actor.actorName)
            }
        } }
    }, confirmButton = { TextButton(onClick = { onSave(selected); onDismiss() }) { Text("OK") } })
}

@Composable
fun FilmRatingScreen(viewModel: FilmRatingViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var ratingDialog by remember { mutableStateOf(false) }
    var deleteComment by remember { mutableStateOf<Comments?>(null) }
    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
    AppScreen("Đánh giá phim", onBack = onBack, actions = {
        TextButton(onClick = { ratingDialog = true }) { Text("Điểm", color = androidx.compose.ui.graphics.Color.White) }
    }) {
        Column {
            AppTextField(search, {
                search = it
                viewModel.filter(it)
            }, "Tìm theo người dùng", modifier = Modifier.padding(14.dp))
            SimpleList(state.comments, "Chưa có bình luận") { (comment, user) ->
                AppCard(Modifier.fillMaxWidth().clickable { deleteComment = comment }) {
                    Text(user.username, fontWeight = FontWeight.Bold)
                    Text(comment.comment)
                    Text("❤ ${comment.like}", color = AppPrimary)
                }
            }
        }
    }
    if (ratingDialog) RatePointDialog(state.rating, onDismiss = { ratingDialog = false }) {
        ratingDialog = false
        viewModel.updateRatePoint(state.rating.movieID, it)
    }
    deleteComment?.let { comment ->
        ConfirmDialog("Xóa bình luận", "Bạn có chắc muốn xóa bình luận này không?", { deleteComment = null }) {
            deleteComment = null
            viewModel.deleteComment(comment.reviewID, comment.filmID)
        }
    }
}

@Composable
private fun RatePointDialog(rating: Rating, onDismiss: () -> Unit, onSave: (Float) -> Unit) {
    var ratePoint by remember { mutableStateOf(rating.ratePoint.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thông tin đánh giá") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Average Rating: ${rating.rating}")
                Text("Số lượt đánh giá: ${rating.reviewCount}")
                NumberField(ratePoint, { ratePoint = it }, "Rate point")
            }
        },
        confirmButton = { TextButton(onClick = { onSave(ratePoint.toFloatOrNull() ?: 0F) }) { Text("Lưu") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
