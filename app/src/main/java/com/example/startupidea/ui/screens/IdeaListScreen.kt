package com.example.startupidea.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.startupidea.data.model.StartupIdea
import com.example.startupidea.ui.components.IdeaCard
import com.example.startupidea.ui.components.SearchBar
import com.example.startupidea.ui.viewmodel.IdeaViewModel

@Composable
fun IdeaListScreen(
    viewModel: IdeaViewModel,
    paddingValues: PaddingValues,
    onIdeaClick: (StartupIdea) -> Unit
) {
    val ideas by viewModel.ideas.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    // Filter ideas based on search query
    val filteredIdeas = if (searchQuery.isBlank()) {
        ideas
    } else {
        ideas.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }
    
    // Sort ideas for trending (oldest first) and recently added (newest first)
    val trendingIdeas = filteredIdeas.sortedBy { it.created_at }
    val recentlyAddedIdeas = filteredIdeas.sortedByDescending { it.created_at }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // SearchBar yang tetap di atas dengan zIndex lebih tinggi
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f) // Memastikan SearchBar selalu di atas
        )
        
        // Konten yang dapat di-scroll
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                // Trending Section
                Text(
                    text = "Trending",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trendingIdeas) { idea ->
                        IdeaCard(
                            idea = idea,
                            onClick = { onIdeaClick(idea) },
                            modifier = Modifier.width(240.dp),
                            isHorizontal = true
                        )
                    }
                }
                
                // Recently Added Section
                Text(
                    text = "Recently Added",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
            }


            // Recently Added Items (vertical list)
            items(recentlyAddedIdeas) { idea ->

                IdeaCard(
                    idea = idea,
                    onClick = { onIdeaClick(idea) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

            }

             // Tambahkan item kosong di bagian bawah untuk memberikan ruang tambahan
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
