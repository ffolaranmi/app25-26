package com.example.smartvoice.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "children")
data class ChildTable(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val birthMonth: Int,
    val birthYear: Int,
    val hospitalId: String = ""
)

object HospitalData {
    data class Hospital(
        val id: String,
        val name: String,
        val phone: String
    )

    val hospitals = mapOf(
        "1001" to Hospital("1001", "Queen Elizabeth University Hospital, ENT", "0141 201 1100"),
        "1002" to Hospital("1002", "Aberdeen Royal Infirmary, ENT", "0345 456 6000"),
        "1003" to Hospital("1003", "St Johns Hospital Livingstone, ENT", "01506 523000"),
        "1004" to Hospital("1004", "Ninewells Hospital, ENT", "01382 660111"),
        "1005" to Hospital("1005", "University Hospital Monklands, ENT", "01236 748748"),
        "1006" to Hospital("1006", "Forth Valley Royal Hospital, ENT", "01324 566000")
    )

    fun getHospitalName(id: String): String {
        return hospitals[id]?.name ?: "—"
    }

    fun getHospitalPhone(id: String): String {
        return hospitals[id]?.phone ?: ""
    }
}