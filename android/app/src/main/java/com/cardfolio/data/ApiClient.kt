package com.cardfolio.data

import kotlinx.serialization.json.Json
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType

private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
}

interface CardFolioApi {

    @GET("api/cards")
    suspend fun listCards(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("category") category: String? = null,
        @Query("country") country: String? = null,
        @Query("needs_review") needsReview: Boolean? = null,
    ): CardListResponse

    @GET("api/cards/{id}")
    suspend fun getCard(@Path("id") id: String): Card

    @PUT("api/cards/{id}")
    suspend fun updateCard(
        @Path("id") id: String,
        @Body update: CardUpdate,
    ): Card

    @DELETE("api/cards/{id}")
    suspend fun deleteCard(@Path("id") id: String)

    @GET("api/cards/search/q")
    suspend fun searchCards(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
    ): CardListResponse

    @Multipart
    @POST("api/cards/upload")
    suspend fun uploadCard(
        @Part front: MultipartBody.Part? = null,
        @Part back: MultipartBody.Part? = null,
    ): UploadResponse

    @GET("api/cards/categories/stats")
    suspend fun getCategoryStats(): List<CategoryStats>
}

object ApiClient {
    // Change this to your VPS domain in production
    var baseUrl: String = "http://10.0.2.2:8000/"  // Android emulator → host localhost

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)  // OCR can take a while
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val api: CardFolioApi by lazy { retrofit.create(CardFolioApi::class.java) }
}

// Helper to create multipart from file
fun createImagePart(file: File, partName: String): MultipartBody.Part {
    val mediaType = "image/jpeg".toMediaType()
    val requestBody = file.asRequestBody(mediaType)
    return MultipartBody.Part.createFormData(partName, file.name, requestBody)
}


