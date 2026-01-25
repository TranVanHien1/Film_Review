package com.example.danhgiaphim

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.danhgiaphim.User.UserActivity
import com.example.danhgiaphim.adapter.FilmGridAdapter
import com.example.danhgiaphim.data.Films
import com.example.danhgiaphim.data.Rating
import com.example.danhgiaphim.data.UserSession
import com.example.danhgiaphim.databinding.ActivityHomeBinding
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var homeBinding: ActivityHomeBinding
    private lateinit var database: DatabaseReference
    private lateinit var userRef: DatabaseReference

    private lateinit var filmAdapter: FilmGridAdapter
    private val filmList = mutableListOf<Films>()
    private var filteredFilms = listOf<Films>()
    private val filmRatingPairs = mutableListOf<Pair<Films, Rating?>>()

    private var currentPage = 1
    private val filmsPerPage = 15
    private var totalPages = 1

    private var selectedGenre: String? = null
    private val genreList = mutableListOf<String>()

    private enum class SortMode {
        BY_RATING, // Ưu tiên ratePoint, sau đó rating
        BY_AI_RATING // Ưu tiên ratePoint, sau đó rateAI
    }
    private var currentSortMode = SortMode.BY_RATING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(homeBinding.root)

        val uid = UserSession.uid
        if (uid != null) {
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)
            loadUserData()
        }

        setupRecyclerView()
        setupSearchListener()
        setupGenreFilter()
        setupPaginationButtons()
        setupUserMenu()
        setupSortButton()
        updateSortButtonText()

        fetchGenres()
        fetchFilmsAndRatings()
    }

    private fun loadUserData() {
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val avatarUrl = snapshot.child("avatarURL").getValue(String::class.java) ?: ""
                if (avatarUrl.isNotEmpty()) {
                    Glide.with(this@HomeActivity)
                        .load(avatarUrl)
                        .circleCrop()
                        .into(homeBinding.icUser)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Không tải được dữ liệu người dùng", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView() {
        val spanCount = 3
        val spacing = 16
        val screenWidth = resources.displayMetrics.widthPixels
        val totalSpacing = spacing * (spanCount + 1)
        val itemWidth = (screenWidth - totalSpacing) / spanCount

        homeBinding.recyclerViewFilms.layoutManager = GridLayoutManager(this, spanCount)
        filmAdapter = FilmGridAdapter(this, filmList, itemWidth)
        homeBinding.recyclerViewFilms.adapter = filmAdapter
    }

    private fun setupSearchListener() {
        homeBinding.txtSearchFilmInHome.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFilms(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupGenreFilter() {
        homeBinding.btnFilterGenre.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            genreList.forEach { genre ->
                popup.menu.add(genre)
            }
            popup.menu.add("Hiển thị toàn bộ phim")

            popup.setOnMenuItemClickListener { item ->
                selectedGenre = if (item.title == "Hiển thị toàn bộ phim") null else item.title.toString()
                filterFilms(homeBinding.txtSearchFilmInHome.text.toString())
                true
            }
            popup.show()
        }
    }

    private fun setupPaginationButtons() {
        homeBinding.btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                updatePaginationUI()
            }
        }

        homeBinding.btnNext.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                updatePaginationUI()
            }
        }
    }

    private fun setupUserMenu() {
        homeBinding.icUser.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_profile -> {
                        startActivity(Intent(this, UserActivity::class.java))
                        true
                    }
                    R.id.menu_logout -> {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun fetchGenres() {
        val genreRef = FirebaseDatabase.getInstance().getReference("Genre")
        genreRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                genreList.clear()
                for (child in snapshot.children) {
                    val name = child.child("genreName").getValue(String::class.java)
                    if (!name.isNullOrEmpty()) {
                        genreList.add(name)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Không tải được thể loại", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateSortButtonText() {
        homeBinding.btnToggleSorts.text = when (currentSortMode) {
            SortMode.BY_RATING -> "Đề xuất"
            SortMode.BY_AI_RATING -> "Yêu thích"
        }
    }

    private fun setupSortButton() {
        homeBinding.btnToggleSorts.setOnClickListener {
            // Chuyển đổi chế độ sắp xếp
            currentSortMode = when (currentSortMode) {
                SortMode.BY_RATING -> SortMode.BY_AI_RATING
                SortMode.BY_AI_RATING -> SortMode.BY_RATING
            }

            // Cập nhật text nút
            homeBinding.btnToggleSorts.text = when (currentSortMode) {
                SortMode.BY_RATING -> "Đề xuất"
                SortMode.BY_AI_RATING -> "Yêu thích"
            }

            // Áp dụng sắp xếp mới
            updateSortButtonText()

            // Áp dụng sắp xếp mới
            applyCurrentSortMode()
        }
    }

    // Hàm áp dụng chế độ sắp xếp hiện tại
    private fun applyCurrentSortMode() {
        val sortedPairs = when (currentSortMode) {
            SortMode.BY_RATING -> {
                filmRatingPairs.sortedWith(compareByDescending<Pair<Films, Rating?>> {
                    it.second?.ratePoint ?: 0F
                }.thenByDescending {
                    it.second?.rating ?: 0F
                })
            }
            SortMode.BY_AI_RATING -> {
                filmRatingPairs.sortedWith(compareByDescending<Pair<Films, Rating?>> {
                    it.second?.ratePoint ?: 0F
                }.thenByDescending {
                    it.second?.rateAI ?: 0F
                })
            }
        }

        filmList.clear()
        filmList.addAll(sortedPairs.map { it.first })
        filteredFilms = filmList.toList()
        currentPage = 1

        updatePaginationUI()
    }

    private fun fetchFilmsAndRatings() {
        try {
            val filmRef = FirebaseDatabase.getInstance().getReference("Films")
            val ratingRef = FirebaseDatabase.getInstance().getReference("Rating")

            ratingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(ratingSnapshot: DataSnapshot) {
                    try {
                        val ratingMap = mutableMapOf<String, Rating>()

                        // Loop trực tiếp qua các child của Rating (không cần loop 2 cấp)
                        for (ratingEntry in ratingSnapshot.children) {
                            try {
                                // Parse thủ công từng field thay vì dùng getValue(Rating::class.java)
                                val movieID = ratingEntry.child("movieID").getValue(String::class.java)
                                val ratePoint = ratingEntry.child("ratePoint").getValue(Float::class.java)
                                val castRating = ratingEntry.child("castRating").getValue(Float::class.java)
                                val contentRating = ratingEntry.child("contentRating").getValue(Float::class.java)
                                val effectRating = ratingEntry.child("effectRating").getValue(Float::class.java)
                                val rating = ratingEntry.child("rating").getValue(Float::class.java)
                                val rateAI = ratingEntry.child("rateAI").getValue(Float::class.java)
                                val reviewCount = ratingEntry.child("reviewCount").getValue(Long::class.java)

                                if (!movieID.isNullOrEmpty() && ratePoint != null) {
                                    // Tạo Rating object với constructor
                                    val ratingObject = Rating(
                                        movieID = movieID,
                                        ratePoint = ratePoint,
                                        castRating = castRating ?: 0F,
                                        contentRating = contentRating ?: 0F,
                                        effectRating = effectRating ?: 0F,
                                        rating = rating ?: 0F,
                                        reviewCount = reviewCount ?: 0L,
                                        rateAI = rateAI ?: 0F
                                    )
                                    ratingMap[movieID] = ratingObject
                                }
                            } catch (e: Exception) {
                                println("Error parsing rating entry ${ratingEntry.key}: ${e.message}")
                                continue
                            }
                        }

                        println("Successfully parsed ${ratingMap.size} ratings")
                        fetchFilmsWithRatings(filmRef, ratingMap)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@HomeActivity, "Lỗi xử lý dữ liệu rating: ${e.message}", Toast.LENGTH_SHORT).show()
                        fetchFilmsWithRatings(filmRef, emptyMap())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@HomeActivity, "Lỗi tải rating: ${error.message}", Toast.LENGTH_SHORT).show()
                    fetchFilmsWithRatings(filmRef, emptyMap())
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi kết nối database: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchFilmsWithRatings(filmRef: DatabaseReference, ratingMap: Map<String, Rating>) {
        filmRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(filmSnapshot: DataSnapshot) {
                try {
                    filmRatingPairs.clear()

                    for (child in filmSnapshot.children) {
                        try {
                            val film = child.getValue(Films::class.java)
                            if (film != null && !film.movieID.isNullOrEmpty()) {
                                val rating = ratingMap[film.movieID]
                                filmRatingPairs.add(Pair(film, rating))
                            }
                        } catch (e: Exception) {
                            println("Error converting film ${child.key}: ${e.message}")
                            continue
                        }
                    }

                    // Thay thế sắp xếp cũ bằng hàm applyCurrentSortMode
                    applyCurrentSortMode()

                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@HomeActivity, "Lỗi xử lý dữ liệu phim: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                runOnUiThread {
                    Toast.makeText(this@HomeActivity, "Lỗi tải phim: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun filterFilms(query: String) {
        filteredFilms = filmList.filter { film ->
            val matchTitle = film.title.contains(query, ignoreCase = true)

            val matchGenre = selectedGenre?.let { selected ->
                film.genre?.values?.any { genre ->
                    genre.genreName == selected
                } ?: false
            } ?: true

            matchTitle && matchGenre
        }

        currentPage = 1
        updatePaginationUI()
    }

    private fun updatePaginationUI() {
        totalPages = (filteredFilms.size + filmsPerPage - 1) / filmsPerPage
        homeBinding.txtPageIndicator.text = "Trang $currentPage"
        homeBinding.btnPrev.isEnabled = currentPage > 1
        homeBinding.btnNext.isEnabled = currentPage < totalPages

        val startIndex = (currentPage - 1) * filmsPerPage
        val endIndex = minOf(startIndex + filmsPerPage, filteredFilms.size)
        val pagedList = filteredFilms.subList(startIndex, endIndex)
        filmAdapter.updateList(pagedList)
    }
}
