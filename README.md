# Wahoo Cloud Api Kotlin HTTP4K Backend

This project is a simple Kotlin HTTP4K backend designed to manage the Wahoo Cloud API OAuth flow and enable users to obtain an API token.
It utilizes environment variables for self-configuration of API credentials and includes functionality to create a Dockerfile for easy deployment with fly.io.
Additionally, an example config file for fly.io has been included.

## Prerequisites

Before running this project, ensure that you have the following prerequisites installed:

- Kotlin
- HTTP4K
- Docker (optional)
- fly.io CLI (optional)

## Installation

To install and run the project locally, follow these steps:

1. Clone the repository:

   ```shell
   git clone https://github.com/james-millner/kotlin-wahoo-cloud-api.git
   ```

2. Navigate to the project directory:

   ```shell
   cd kotlin-wahoo-cloud-api
   ```

3. Set up the required environment variables. You can do this by creating a `.env` file in the root directory of the project and populating it with the necessary values. The required environment variables are:

    - `CLIENT_ID`: The Wahoo Cloud API client ID.
    - `CLIENT_SECRET`: The Wahoo Cloud API client secret.
    - `REDIRECT_URI`: The redirect URI for the OAuth flow.

4. Build and run the project:

   ```shell
   ./gradlew run
   ```

   This command will compile and run the project, making it accessible at `http://localhost:8080`.

## Usage

Once the project is running, you can use an HTTP client (e.g., cURL or Postman) to interact with the API endpoints. Here are some example requests:

- **GET /authorize**: Retrieves the API token using the OAuth flow.

## Docker Deployment

To deploy the project using Docker, follow these steps:

1. Ensure that Docker is installed and running on your machine.

2. Build the Docker image:

   ```shell
   docker build -t your-image-name .
   ```

3. Run the Docker container:

   ```shell
   docker run -p 8080:8080 your-image-name
   ```

   The project will be accessible at `http://localhost:8080`.

4. Hit the `/authorize` endpoint to retrieve the API token.

## Deployment with fly.io

To deploy the project using fly.io, follow these steps:

1. Install the fly.io CLI by following the instructions provided in the [official documentation](https://fly.io/docs/getting-started/installing-flyctl/).

2. Log in to fly.io using the CLI:

   ```shell
   flyctl auth login
   ```

3. Initialize a new fly.io app:

   ```shell
   flyctl init
   ```

4. Update the generated `fly.toml` file with your desired configuration. You can use the included `fly.toml` file as a reference.

5. Deploy the app to fly.io:

   ```shell
   flyctl deploy
   ```

   Follow the prompts to deploy your application.

For more information on deploying with fly.io, refer to their [official documentation](https://fly.io/docs/).

## Contributing

Contributions are welcome! If you find any issues or have suggestions for improvements, please submit an issue or a pull request to the repository.

## License

This project is licensed under the [MIT License](LICENSE).