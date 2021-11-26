package com.fanjiaxing.criminalintent.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Crime(
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false,
    var suspect: String = ""
) {
    @PrimaryKey
    var id: UUID = UUID.randomUUID()

    val photoFileName get() = "IMG_$id.jpg"
}
