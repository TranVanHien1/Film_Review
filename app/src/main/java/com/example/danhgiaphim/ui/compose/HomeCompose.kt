package com.example.danhgiaphim.ui.compose

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.danhgiaphim.R
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.ui.home.HomeSortMode
import com.example.danhgiaphim.ui.home.HomeUiState
import com.example.danhgiaphim.ui.home.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onFilmClick: (Films) -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var userMenu by remember { mutableStateOf(false) }
    var genreMenu by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    AppScreen(
        title = if (state.userName.isNotBlank()) "Xin chào, ${state.userName}" else "Khám phá phim",
        subtitle = "Khám phá và đánh giá phim",
        actions = {
            Box {
                GlideImage(
                    state.avatarUrl,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { userMenu = true },
                    circle = true
                )
                DropdownMenu(expanded = userMenu, onDismissRequest = { userMenu = false }) {
                    DropdownMenuItem(text = { Text("Hồ sơ") }, onClick = {
                        userMenu = false
                        onProfile()
                    })
                    DropdownMenuItem(text = { Text("Đăng xuất") }, onClick = {
                        userMenu = false
                        viewModel.signOut()
                        onLogout()
                    })
                }
            }
        }
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp)) {
            AppTextField(search, {
                search = it
                viewModel.setQuery(it)
            }, "Tìm phim")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = state.sortMode == HomeSortMode.BY_RATING,
                    onClick = { viewModel.setSortMode(HomeSortMode.BY_RATING) },
                    label = { Text("Đề xuất") }
                )
                FilterChip(
                    selected = state.sortMode == HomeSortMode.BY_AI_RATING,
                    onClick = { viewModel.setSortMode(HomeSortMode.BY_AI_RATING) },
                    label = { Text("Yêu thích") }
                )
                Box {
                    FilterChip(
                        selected = state.selectedGenre != null,
                        onClick = { genreMenu = true },
                        label = { Text(state.selectedGenre ?: "Thể loại") }
                    )
                    DropdownMenu(expanded = genreMenu, onDismissRequest = { genreMenu = false }) {
                        DropdownMenuItem(text = { Text("Tất cả") }, onClick = {
                            genreMenu = false
                            viewModel.setGenre(null)
                        })
                        state.genres.forEach { genre ->
                            DropdownMenuItem(text = { Text(genre) }, onClick = {
                                genreMenu = false
                                viewModel.setGenre(genre)
                            })
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            if (state.featuredFilms.isNotEmpty()) {
                Text("Phim nổi bật", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppText)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.featuredFilms) { film ->
                        FeaturedFilmCard(film, rating = state.ratings[film.movieID]?.rating ?: 0F) {
                            onFilmClick(film)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            } else if (state.isEmpty) {
                EmptyState("Không có phim phù hợp")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 10.dp)
                ) {
                    items(state.films) { film ->
                        FilmGridCard(film, state.ratings[film.movieID]?.rating ?: 0F) {
                            onFilmClick(film)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = viewModel::previousPage, enabled = state.canPrevious) { Text("Trước") }
                    Text("Trang ${state.currentPage}")
                    TextButton(onClick = viewModel::nextPage, enabled = state.canNext) { Text("Sau") }
                }
            }
        }
    }
}

@Composable
private fun FeaturedFilmCard(film: Films, rating: Float, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box {
            GlideImage(film.posterURL, Modifier.fillMaxWidth().height(190.dp), placeholder = R.drawable.ic_user)
            Text(
                text = if (rating > 0F) "★ %.1f".format(rating) else "★ --",
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FilmGridCard(film: Films, rating: Float, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        GlideImage(
            film.posterURL,
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.68f),
            placeholder = R.drawable.ic_user
        )
        Spacer(Modifier.height(6.dp))
        Text(
            film.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 13.sp,
            color = AppText,
            fontWeight = FontWeight.Bold
        )
        Text(if (rating > 0F) "★ %.1f".format(rating) else "★ --", color = AppPrimary, fontSize = 12.sp)
    }
}
