package com.knowledgepearls.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val name: String? = null,
    val bio: String? = null,
    val deanery: String? = null,
    val specialty: String? = null,
    val grade: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("allow_messages") val allowMessages: Boolean = true,
    @SerialName("show_email") val showEmail: Boolean = false,
    @SerialName("public_email") val publicEmail: String? = null,
    @SerialName("allow_pearl_shares") val allowPearlShares: Boolean = true,
    @SerialName("notify_pearl_shares_email") val notifyPearlSharesEmail: Boolean = true,
)

object ProfileConstants {
    val deaneries = listOf(
        "East Midlands", "East of England", "Kent, Surrey and Sussex", "London",
        "North East and Yorkshire", "North West", "Northern Ireland (NIMDTA)",
        "Scotland (NES)", "South East", "South West", "Thames Valley",
        "Wales (HEIW)", "Wessex", "West Midlands",
    )
    val specialties = listOf(
        "Medicine", "Surgery", "Trauma & Orthopaedics", "General Practice",
        "Paediatrics", "Psychiatry", "Anaesthetics", "Emergency Medicine",
        "Obstetrics & Gynaecology", "Radiology", "Pathology", "Neurology",
        "Cardiology", "Student", "Other",
    )
    val grades = listOf(
        "Consultant", "SpR", "SHO", "IMT", "F2", "F1", "Nurse", "Midwife", "ACP",
    )
}
