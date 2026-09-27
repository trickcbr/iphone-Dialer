package com.example.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["phoneNumber"], unique = false),
        Index(value = ["name"], unique = false)
    ]
)
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val email: String = "",
    val company: String = "",
    val photoUri: String? = null,
    val isFavorite: Boolean = false,
    val notes: String = "",
    val label: String = "Mobile"
)
