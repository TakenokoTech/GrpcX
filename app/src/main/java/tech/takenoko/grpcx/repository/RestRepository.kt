package tech.takenoko.grpcx.repository

import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

object RestRepository {

    const val host = "192.168.0.106"
    const val port = 8088
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.JAPANESE)

    private var retrofit = Retrofit.Builder().baseUrl("http://$host:$port").addConverterFactory(MoshiConverterFactory.create()).build()
    private var service = retrofit.create(HalloService::class.java)

    suspend fun getHallo(): String? {
        return service.hallo()?.result
    }

    interface HalloService {
        @GET("hallo")
        suspend fun hallo(): HalloResponse?
    }

    @JsonClass(generateAdapter = true)
    data class HalloResponse(var result: String)
}
