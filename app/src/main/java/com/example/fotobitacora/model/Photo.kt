package com.example.fotobitacora.model

data class Photo(
    val id: Long,
    val userId: Long,
    val filePath: String,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val comment: String,
    val createdAt: Long
)
