package com.ora.wellbeing.presentation.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.ora.wellbeing.data.mapper.LessonMapper
import com.ora.wellbeing.data.model.firestore.LessonDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * Debug screen to diagnose Firestore lesson loading issues
 */
@Composable
fun FirestoreDebugScreen(
    viewModel: FirestoreDebugViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDebugInfo()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "🔍 Firestore Debug Info",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            state.isLoading -> {
                CircularProgressIndicator()
            }
            state.error != null -> {
                Text(
                    text = "❌ Error: ${state.error}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("📊 Summary", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Total documents: ${state.totalDocs}")
                                Text("Documents with status='ready': ${state.readyDocs}")
                                Text("Documents parsed successfully: ${state.parsedDocs}")
                                Text("Mapping errors: ${state.mappingErrors}")
                            }
                        }
                    }

                    item {
                        Text("📝 Lesson Details", style = MaterialTheme.typography.titleLarge)
                    }

                    items(state.lessons) { lesson ->
                        Card {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = lesson.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text("ID: ${lesson.id}", style = MaterialTheme.typography.bodySmall)
                                Text("Status: ${lesson.status}", style = MaterialTheme.typography.bodySmall)
                                Text("Category: ${lesson.category}", style = MaterialTheme.typography.bodySmall)
                                Text("Duration: ${lesson.durationMinutes} min", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    if (state.errors.isNotEmpty()) {
                        item {
                            Text("⚠️ Errors", style = MaterialTheme.typography.titleLarge)
                        }
                        items(state.errors) { error ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = error,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class DebugState(
    val isLoading: Boolean = true,
    val totalDocs: Int = 0,
    val readyDocs: Int = 0,
    val parsedDocs: Int = 0,
    val mappingErrors: Int = 0,
    val lessons: List<DebugLesson> = emptyList(),
    val errors: List<String> = emptyList(),
    val error: String? = null
)

data class DebugLesson(
    val id: String,
    val title: String,
    val status: String,
    val category: String,
    val durationMinutes: Int
)

@HiltViewModel
class FirestoreDebugViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : androidx.lifecycle.ViewModel() {

    private val _state = MutableStateFlow(DebugState())
    val state: StateFlow<DebugState> = _state

    fun loadDebugInfo() {
        viewModelScope.launch {
            try {
                Timber.d("FirestoreDebug: Starting diagnostic")

                // Query ALL lessons (no filter)
                val allSnapshot = firestore
                    .collection("lessons")
                    .get()
                    .await()

                val totalDocs = allSnapshot.size()
                Timber.d("FirestoreDebug: Found $totalDocs total documents")

                // Query lessons with status=ready
                val readySnapshot = firestore
                    .collection("lessons")
                    .whereEqualTo("status", "ready")
                    .get()
                    .await()

                val readyDocs = readySnapshot.size()
                Timber.d("FirestoreDebug: Found $readyDocs documents with status=ready")

                // Try to parse each lesson
                val lessons = mutableListOf<DebugLesson>()
                val errors = mutableListOf<String>()
                var parsedCount = 0
                var mappingErrorCount = 0

                readySnapshot.documents.forEach { doc ->
                    try {
                        val lessonDoc = doc.toObject(LessonDocument::class.java)
                        if (lessonDoc != null) {
                            try {
                                val contentItem = LessonMapper.fromFirestore(doc.id, lessonDoc)
                                lessons.add(
                                    DebugLesson(
                                        id = doc.id,
                                        title = contentItem.title,
                                        status = lessonDoc.status,
                                        category = contentItem.category,
                                        durationMinutes = contentItem.durationMinutes
                                    )
                                )
                                parsedCount++
                            } catch (e: Exception) {
                                mappingErrorCount++
                                errors.add("Mapping error for ${doc.id}: ${e.message}")
                                Timber.e(e, "FirestoreDebug: Mapping error for ${doc.id}")
                            }
                        } else {
                            errors.add("Failed to parse document ${doc.id}")
                            Timber.w("FirestoreDebug: Failed to parse ${doc.id}")
                        }
                    } catch (e: Exception) {
                        errors.add("Parse error for ${doc.id}: ${e.message}")
                        Timber.e(e, "FirestoreDebug: Parse error for ${doc.id}")
                    }
                }

                _state.value = DebugState(
                    isLoading = false,
                    totalDocs = totalDocs,
                    readyDocs = readyDocs,
                    parsedDocs = parsedCount,
                    mappingErrors = mappingErrorCount,
                    lessons = lessons,
                    errors = errors
                )

                Timber.d("FirestoreDebug: Diagnostic complete - parsed $parsedCount lessons, ${errors.size} errors")

            } catch (e: Exception) {
                Timber.e(e, "FirestoreDebug: Failed to load diagnostic info")
                _state.value = DebugState(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}
