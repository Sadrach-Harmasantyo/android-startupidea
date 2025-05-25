package com.example.startupidea

import com.example.startupidea.ui.IdeaFormScreen
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.startupidea.model.StartupIdea
import com.example.startupidea.ui.IdeaDetailScreen
import com.example.startupidea.ui.IdeaListScreen
import com.example.startupidea.ui.LoginScreen
import com.example.startupidea.ui.ProfileScreen
//import com.example.startupidea.uii.SplashScreen
import com.example.startupidea.viewmodel.AuthViewModel
import com.example.startupidea.viewmodel.IdeaViewModel

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Pasang splash screen kustom sebelum memanggil super.onCreate()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Atur agar splash screen tetap ditampilkan sampai data dimuat
        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        setContent {
            val ideaViewModel = viewModel<IdeaViewModel>()
            val authViewModel = viewModel<AuthViewModel>()
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

            // State untuk mengontrol apakah splash screen ditampilkan
            var showSplashScreen by remember { mutableStateOf(true) }

            // Periksa status login saat aplikasi dimulai
            LaunchedEffect(Unit) {
                authViewModel.checkLoginStatus()
                // Selalu muat data ide, terlepas dari status login
                ideaViewModel.fetchIdeas()
                // Setelah data dimuat, izinkan splash screen menghilang
                keepSplashOnScreen = false
            }

            var showForm by remember { mutableStateOf(false) }
            var showLogin by remember { mutableStateOf(false) }
            var selectedTab by remember { mutableStateOf(0) }
            var selectedIdea by remember { mutableStateOf<StartupIdea?>(null) }
            var ideaToEdit by remember { mutableStateOf<StartupIdea?>(null) }


            Scaffold(
                bottomBar = {
                    // Tampilkan bottom navigation hanya jika tidak sedang melihat detail ide
                    if (selectedIdea == null) {
                        NavigationBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .height(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    color = Color.Blue,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            containerColor = Color.Black
                        ) {
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painterResource(id = R.drawable.ic_home),
                                        contentDescription = "Home",
                                        tint = if (selectedTab == 0) Color.Black else Color.White
                                    )
                                },
                                label = {
                                    Text(
                                        "Home",
                                        color = if (selectedTab == 0) Color(0xFFFF9800) else Color.White
                                    )
                                },
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    showForm = false
                                    showLogin = false
                                    ideaViewModel.fetchIdeas()
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    unselectedIconColor = Color.White,
                                    selectedTextColor = Color(0xFFFF9800),
                                    unselectedTextColor = Color.White,
                                    indicatorColor = Color(0xFFFF9800)
                                )
                            )

                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painterResource(id = R.drawable.ic_add),
                                        contentDescription = "Add Idea",
                                        tint = if (selectedTab == 1) Color.Black else Color.White
                                    )
                                },
                                label = {
                                    Text(
                                        "Add Idea",
                                        color = if (selectedTab == 1) Color(0xFFFF9800) else Color.White
                                    )
                                },
                                selected = selectedTab == 1,
                                onClick = {
                                    if (isLoggedIn) {
                                        selectedTab = 1
                                        showForm = true
                                        showLogin = false
                                    } else {
                                        // Jika belum login, tampilkan halaman login
                                        showLogin = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    unselectedIconColor = Color.White,
                                    selectedTextColor = Color(0xFFFF9800),
                                    unselectedTextColor = Color.White,
                                    indicatorColor = Color(0xFFFF9800)
                                )
                            )

                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painterResource(id = R.drawable.ic_profile),
                                        contentDescription = "Profile",
                                        tint = if (selectedTab == 2) Color.Black else Color.White
                                    )
                                },
                                label = {
                                    Text(
                                        "Profile",
                                        color = if (selectedTab == 2) Color(0xFFFF9800) else Color.White
                                    )
                                },
                                selected = selectedTab == 2,
                                onClick = {
                                    if (isLoggedIn) {
                                        selectedTab = 2
                                        showForm = false
                                        showLogin = false
                                        ideaViewModel.fetchIdeas()
                                    } else {
                                        // Jika belum login, tampilkan halaman login
                                        showLogin = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    unselectedIconColor = Color.White,
                                    selectedTextColor = Color(0xFFFF9800),
                                    unselectedTextColor = Color.White,
                                    indicatorColor = Color(0xFFFF9800)
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                when {
                    // Tampilkan halaman detail jika ada ide yang dipilih
                    selectedIdea != null -> {
                        IdeaDetailScreen(
                            idea = selectedIdea!!,
                            onBackPressed = { selectedIdea = null },
                            ideaViewModel = ideaViewModel,
                            onEditIdea = { idea ->
                                // Implementasi navigasi ke halaman edit ide
                                // Untuk sementara, kita bisa menggunakan halaman form yang sama
                                selectedIdea = null
                                selectedTab = 1
                                showForm = true
                                ideaToEdit = idea // Simpan ide yang akan diedit
                            }
                        )
                    }

                    showLogin -> {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onLoginSuccess = {
                                showLogin = false
                                ideaViewModel.fetchIdeas()
                                selectedTab = 0
                            }
                        )
                    }

                    showForm || (selectedTab == 1 && isLoggedIn) -> {
                        IdeaFormScreen(
                            ideaViewModel = ideaViewModel,
                            ideaToEdit = ideaToEdit,
                            onSubmitted = {
                                showForm = false
                                selectedTab = 0
                                ideaToEdit = null // Reset ide yang diedit setelah selesai
                            }
                        )
                    }

                    selectedTab == 2 && isLoggedIn -> {
                        ProfileScreen(
                            authViewModel = authViewModel,
                            ideaViewModel = ideaViewModel,
                            paddingValues = innerPadding,
                            onLogout = {
                                // Arahkan ke halaman home setelah logout
                                selectedTab = 0
                            },
                            onIdeaClick = { idea -> selectedIdea = idea }
                        )
                    }

                    else -> {
                        // Tampilkan IdeaListScreen untuk semua pengguna
                        IdeaListScreen(
                            viewModel = ideaViewModel,
                            paddingValues = innerPadding,
                            onIdeaClick = { idea -> selectedIdea = idea }
                        )
                    }
                }
            }

        }
    }
}
