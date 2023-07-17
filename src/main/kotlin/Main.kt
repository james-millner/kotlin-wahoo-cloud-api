import mu.KotlinLogging
import org.http4k.client.ApacheClient
import org.http4k.core.*
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import java.util.concurrent.Semaphore

data class AuthorizationResponse(val code: String, val state: String)

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}

fun main() {
    val wahooClientId = "***REMOVED***"
    val wahooClientSecret = "***REMOVED***"
    val redirectUri = "https://localhost:8000"

    val client = ApacheClient()
    val stateMap = mutableMapOf<String, String>()

    val app: HttpHandler = routes(
        "/authorize" bind Method.GET to { request: Request ->
            val state = request.query("state")
            val authorizationUrl = "https://api.wahooligan.com/oauth/authorize?" +
                    "client_id=$wahooClientId" +
                    "&redirect_uri=$redirectUri" +
                    "&scope=user_read%20workouts_read%20offline_data" +
                    "&response_type=code"
            Response(Status.TEMPORARY_REDIRECT).header("Location", authorizationUrl)
        },

        "/" bind Method.GET to { request: Request ->
            val code = request.query("code") ?: error("Authorization code not received")
            Response(Status.OK).body("Authorization code received: $code")
        }
    )

    val filteredApp = DebuggingFilters.PrintRequestAndResponse().then(app)
    val securedApp = ServerFilters.CatchLensFailure.then(filteredApp)

    val server = securedApp.asServer(SunHttp(8000)).start()

    println("Authorization Server started on port 8000")

    // Perform authorization flow
    val authorizationRequestUrl = "http://localhost:8000/authorize?state=random_state"
    println("Open the following URL in your browser to authorize the application:")
    println(authorizationRequestUrl)

    server.shutdownOnExit()

    waitForExitSignal()
}

private fun Http4kServer.shutdownOnExit() {
    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutting down...")

        stop()

        println("Shutdown complete.")
    })
}

private fun waitForExitSignal() {
    val semaphore = Semaphore(0)
    Runtime.getRuntime().addShutdownHook(Thread { semaphore.release() })
    semaphore.acquire()
}