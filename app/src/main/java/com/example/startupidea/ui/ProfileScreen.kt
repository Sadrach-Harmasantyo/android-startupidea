package com.example.startupidea.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.startupidea.R
import com.example.startupidea.data.SupabaseService
import com.example.startupidea.model.StartupIdea
import com.example.startupidea.ui.components.IdeaCard
import com.example.startupidea.viewmodel.AuthViewModel
import com.example.startupidea.viewmodel.IdeaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    ideaViewModel: IdeaViewModel,
    paddingValues: PaddingValues,
    onLogout: () -> Unit = {}, // Parameter baru dengan default value
    onIdeaClick: (StartupIdea) -> Unit = {} // Parameter baru untuk navigasi ke detail
) {
    val context = LocalContext.current
    val ideas by ideaViewModel.ideas.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Mendapatkan email pengguna yang sedang login
    val userEmail = remember { mutableStateOf("") }
    
    // Memfilter ide yang diunggah oleh pengguna saat ini
    val userIdeas = ideas.filter { it.email == userEmail.value }
    
    // Mendapatkan email pengguna saat komponen dimuat
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val session = SupabaseService.getCurrentSession()
            session?.let {
                userEmail.value = it.user?.email ?: ""
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Header profil
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF9800)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar pengguna
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp),
                        shape = CircleShape,
                        color = Color.LightGray
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_profile),
                            contentDescription = "Profil",
                            modifier = Modifier
                                .padding(16.dp)
                                .size(64.dp),
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Email pengguna
                    Text(
                        text = userEmail.value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Tombol logout
                    Button(
                        onClick = { 
                            authViewModel.signOut()
                            onLogout() // Panggil callback onLogout
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        )
                    ) {
                        Text("Logout")
                    }
                }
            }
            
            // Judul daftar ide
            if (userIdeas.isNotEmpty()) {
                Text(
                    text = "Ide Startup Anda",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Anda belum mengunggah ide startup",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tekan tab 'Add Idea' untuk membuat ide baru",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }
        }

        items(userIdeas) { idea ->
            IdeaCard(
                idea = idea,
                onClick = { onIdeaClick(idea) },
//                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }


    }
}