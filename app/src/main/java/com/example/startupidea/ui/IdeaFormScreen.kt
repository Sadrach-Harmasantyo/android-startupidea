package com.example.startupidea.ui

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.startupidea.data.SupabaseService
import com.example.startupidea.model.StartupIdea
import com.example.startupidea.viewmodel.IdeaViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Definisi warna tema
//val OrangeMain = Color(0xFFFF9800)
//val OrangeDark = Color(0xFFF57C00)
//val BlackMain = Color(0xFF212121)
//val BlackLight = Color(0xFF424242)
//val GrayLight = Color(0xFFE0E0E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeaFormScreen(
    ideaViewModel: IdeaViewModel, 
    onSubmitted: () -> Unit,
    ideaToEdit: StartupIdea? = null // Parameter baru untuk ide yang akan diedit
) {
    var title by remember { mutableStateOf(ideaToEdit?.title ?: "") }
    var desc by remember { mutableStateOf(ideaToEdit?.description ?: "") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(ideaToEdit?.phone ?: "") }
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val isUploading by ideaViewModel.isUploading.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Mendapatkan email pengguna yang sedang login
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val session = SupabaseService.getCurrentSession()
            session?.let {
                email = it.user?.email ?: ""
            }
        }
    }
    
    // Buat file sementara untuk menyimpan foto dari kamera
    val tempImageFile = remember { createTempImageFile(context) }
    val tempImageUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempImageFile
        )
    }
    
    // Launcher untuk memilih gambar dari galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it }
    }
    
    // Launcher untuk mengambil foto dari kamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempImageUri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (ideaToEdit == null) "Tambahkan Ide Startup" else "Edit Ide Startup",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        // Tambahkan Box dengan verticalScroll modifier di sini
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // Tambahkan verticalScroll di sini
                    .padding(bottom = 88.dp, start = 16.dp, end = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        // Image upload section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .border(2.dp, Color(0xFFFF9800), RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray)
                                .clickable {
                                    // Tampilkan dialog pilihan: Kamera atau Galeri
                                    // Untuk sederhananya, kita langsung buka galeri
                                    galleryLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUri != null) {
                                // Tampilkan gambar yang dipilih
                                Image(
                                    painter = rememberAsyncImagePainter(imageUri),
                                    contentDescription = "Logo Startup",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Tampilkan placeholder
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Klik untuk upload logo",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("(Opsional)", color = Color.Black)
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF9800)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Pilih dari Galeri")
                            }
                        }
                    }
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            "Detail Ide Startup",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        OutlinedTextField(
                            value = title, 
                            onValueChange = { title = it }, 
                            label = { Text("Judul") }, 
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFFF9800),
                                unfocusedBorderColor = Color.Black,
                                cursorColor = Color(0xFFFF9800),
                                focusedLabelColor = Color(0xFFFF9800)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = desc, 
                            onValueChange = { desc = it }, 
                            label = { Text("Deskripsi") }, 
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFFF9800),
                                unfocusedBorderColor = Color.Black,
                                cursorColor = Color(0xFFFF9800),
                                focusedLabelColor = Color(0xFFFF9800)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = email, 
                            onValueChange = { /* email tidak bisa diubah */ }, 
                            label = { Text("Email") }, 
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                disabledBorderColor = Color.LightGray,
                                disabledTextColor = Color.Black
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = phone, 
                            onValueChange = { phone = it }, 
                            label = { Text("Nomor HP") }, 
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFFF9800),
                                unfocusedBorderColor = Color.Black,
                                cursorColor = Color(0xFFFF9800),
                                focusedLabelColor = Color(0xFFFF9800)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                // Konversi Uri ke File
                                val logoFile = imageUri?.let { uri ->
                                    getFileFromUri(context, uri)
                                }
                                
                                if (ideaToEdit == null) {
                                    // Tambah ide baru
                                    ideaViewModel.submitIdea(title, desc, email, phone, logoFile)
                                } else {
                                    // Update ide yang sudah ada
                                    ideaViewModel.updateIdea(ideaToEdit.id, title, desc, email, phone, logoFile)
                                }
                                onSubmitted()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !isUploading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800),
                                disabledContainerColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    if (ideaToEdit == null) "Kirim Ide" else "Simpan Perubahan",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Fungsi untuk membuat file sementara untuk menyimpan foto dari kamera
private fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    return File.createTempFile(
        imageFileName,
        ".jpg",
        context.cacheDir
    )
}

// Fungsi untuk mendapatkan File dari Uri
private fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        // Untuk Uri dari galeri
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    val displayName = if (displayNameIndex != -1) {
                        it.getString(displayNameIndex)
                    } else {
                        "image_${System.currentTimeMillis()}.jpg"
                    }
                    
                    val tempFile = File(context.cacheDir, displayName)
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempFile
                } else null
            }
        } 
        // Untuk Uri dari kamera (file:// atau content:// dari FileProvider)
        else {
            val path = uri.path
            if (path != null) File(path) else null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
