package tech.takenoko.server.rest

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import java.text.SimpleDateFormat
import java.util.*

fun Route.halloController() {
    route("/hallo") {
        get("/") {
            val (code, response) = HalloController.getHallo()
            call.respond(code, response)
        }
    }
}

object HalloController {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.JAPANESE)

    fun getHallo(): KtorResponse {
        println("HalloController.getHallo")
        return HttpStatusCode.OK to HalloResponse("Hello, Kotlin ${sdf.format(Date().time)}")
    }

    data class HalloResponse(var result: String)
}
