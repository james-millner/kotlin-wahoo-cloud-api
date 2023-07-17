import mu.KotlinLogging
import org.http4k.client.ApacheClient
import org.http4k.core.*
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer

data class AuthorizationResponse(val code: String, val state: String)

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}

fun main() {


    val wahooClientId = "<Your Wahoo Client ID>"
    val wahooClientSecret = "<Your Wahoo Client Secret>"
    val redirectUri = "<Your Redirect URI>"
    val webhookUrl = "<Your Webhook URL>"

    val client = ApacheClient()
    val stateMap = mutableMapOf<String, String>()

    val app: HttpHandler = routes(
        "/authorize" bind Method.GET to { request: Request ->
            val state = request.query("state")
            val authorizationUrl = "https://api.wahoofitness.com/v1/oauth/authorize?" +
                    "client_id=$wahooClientId" +
                    "&response_type=code" +
                    "&redirect_uri=$redirectUri" +
                    "&state=$state"
            Response(Status.TEMPORARY_REDIRECT).header("Location", authorizationUrl)
        },

        "/callback" bind Method.GET to { request: Request ->
            val code = request.query("code") ?: error("Authorization code not received")
            val state = request.query("state") ?: error("State not received")
            stateMap[code] = state
            Response(Status.OK).body("Authorization code received: $code")
        },

        "/webhook" bind Method.POST to { request: Request ->
            val body = request.bodyString()
            // Process the received webhook payload
            // Implement your logic here

            logger.info { body }

            Response(Status.OK)
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

    // Wait for authorization code
    val authorizationCode = waitForAuthorizationCode()
    val state = stateMap.remove(authorizationCode)
        ?: error("Invalid or expired authorization code received")

    // Exchange authorization code for an access token
    val tokenRequest = Request(Method.POST, "https://api.wahoofitness.com/v1/oauth/token")
        .query("client_id", wahooClientId)
        .query("client_secret", wahooClientSecret)
        .query("grant_type", "authorization_code")
        .query("code", authorizationCode)
        .query("redirect_uri", redirectUri)

    val tokenResponse = client(tokenRequest)

    if (tokenResponse.status.successful) {
        // Access token retrieval successful
        println("Access token retrieved successfully")

        // Register webhook URL
        val registerWebhookRequest = Request(Method.POST, "https://api.wahoofitness.com/v1/webhooks")
            .header("Authorization", "Bearer ${tokenResponse.bodyString()}")
            .body("""{"url": "$webhookUrl"}""")

        val registerWebhookResponse = client(registerWebhookRequest)

        if (registerWebhookResponse.status.successful) {
            // Webhook URL registered successfully
            println("Webhook URL registered successfully: $webhookUrl")
        } else {
            // Handle error during webhook URL registration
            println("Failed to register webhook URL: ${registerWebhookResponse.status}")
        }
    } else {
        // Handle error during access token retrieval
        println("Failed to retrieve access token: ${tokenResponse.status}")
    }

    server.stop()
}

private fun waitForAuthorizationCode(): String {
    // Implement your code to wait for the authorization code
    // For example, you can use a blocking read from the console
    return readLine().orEmpty()
}