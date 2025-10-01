package com.ora.wellbeing.data.remote.dto

data class ContentListResponse(
    val content: List<ContentDto>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalElements: Long,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)