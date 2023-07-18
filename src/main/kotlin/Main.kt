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

private val logger = KotlinLogging.logger {}

fun main() {
    val wahooClientId = System.getenv("WAHOO_CLIENT_ID") ?: error("WAHOO_CLIENT_ID environment variable not set")
    val wahooClientSecret = System.getenv("WAHOO_CLIENT_SECRET") ?: error("WAHOO_CLIENT_SECRET environment variable not set")
    val redirectUri = System.getenv("REDIRECT_URI") ?: error("REDIRECT_URI environment variable not set")

    val client = ApacheClient()

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
            logger.info { "Authorization code received: $code" }
            // Need to do a POST here
            val url = Uri.of("https://api.wahooligan.com/oauth/token?" +
                    "client_id=$wahooClientId" +
                    "&client_secret=$wahooClientSecret" +
                    "&code=$code" +
                    "&grant_type=authorization_code" +
                    "&redirect_uri=$redirectUri")
            val request = Request(Method.POST, url)
            val response = client(request)

            if (response.status == Status.OK) {
                Response(Status.OK).body("Authorization code received: ${response.bodyString()}")
            } else {
                logger.error { response.bodyString() }
                Response(Status.INTERNAL_SERVER_ERROR).body("Authorization code not received")
            }
        },

        "/webhook" bind Method.POST to { req ->
            // handle webhook payload here
            logger.info { "Received Webhook Invocation" }
            logger.info { req.bodyString() }
            Response(Status.OK)
        }
    )

    val filteredApp = DebuggingFilters.PrintRequestAndResponse().then(app)
    val securedApp = ServerFilters.CatchLensFailure.then(filteredApp)

    val server = securedApp.asServer(SunHttp(8080)).start()

    logger.info("Authorization Server started on port 8000")
    
    server.shutdownOnExit()
    waitForExitSignal()
}

private fun Http4kServer.shutdownOnExit() {
    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info("Shutting down...")

        stop()

        logger.info("Shutdown complete.")
    })
}

private fun waitForExitSignal() {
    val semaphore = Semaphore(0)
    Runtime.getRuntime().addShutdownHook(Thread { semaphore.release() })
    semaphore.acquire()
}