package com.cardfolio.data

import kotlinx.serialization.Serializable

@Serializable
enum class CardCategory(val displayName: String) {
    supplier("供应商"),
    competitor("竞争对手"),
    domestic("国内客户"),
    international("国外客户"),
    other("其他");
}

@Serializable
data class Card(
    val id: String,
    val company: String? = null,
    val person_name: String? = null,
    val title: String? = null,
    val phone: List<String>? = null,
    val email: List<String>? = null,
    val address: String? = null,
    val website: String? = null,
    val country: String? = null,
    val category: CardCategory = CardCategory.other,
    val tags: List<String>? = null,
    val notes: String? = null,
    val front_image_path: String? = null,
    val back_image_path: String? = null,
    val front_thumbnail_url: String? = null,
    val back_thumbnail_url: String? = null,
    val needs_review: Boolean = false,
    val created_at: String? = null,
    val updated_at: String? = null,
)

@Serializable
data class CardListResponse(
    val total: Int,
    val page: Int,
    val page_size: Int,
    val items: List<Card>,
)

@Serializable
data class UploadResponse(
    val card: Card,
    val ocr_status: String,
    val message: String = "",
)

@Serializable
data class CategoryStats(
    val category: String,
    val count: Int,
)

@Serializable
data class CardUpdate(
    val company: String? = null,
    val person_name: String? = null,
    val title: String? = null,
    val phone: List<String>? = null,
    val email: List<String>? = null,
    val address: String? = null,
    val website: String? = null,
    val country: String? = null,
    val category: String? = null,
    val tags: List<String>? = null,
    val notes: String? = null,
    val needs_review: Boolean? = null,
)
