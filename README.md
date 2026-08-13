# PantryPal

PantryPal is a JavaFX application that creates hands-free recipes from recorded audio. It uses Groq for recipe generation and transcription, OpenAI for optional recipe images, and MongoDB for accounts and saved recipes.

## Run it

### Offline demo mode (recommended for recruiters)

This needs only a JDK 17; it does not contact OpenAI or MongoDB. It uses a sample
account, recipe, generated recipe response, transcription response, and local image.

```bash
PANTRYPAL_DEMO_MODE=true ./gradlew run
```

At the login screen, enter any username and password, then select **Login**. You can
create, filter, view, edit, and delete recipes during the session. Demo data resets
every time the application restarts. The record buttons still work as UI controls, but
they return bundled sample transcription rather than using your microphone.

### Live mode

Prerequisites: JDK 17 and MongoDB. Start MongoDB locally, or use a MongoDB Atlas connection string.

Recipe generation and transcription run on [Groq](https://console.groq.com)'s free tier by default. Create a free Groq API key, then configure this shell:

```bash
export GROQ_API_KEY="your_groq_api_key"
export MONGODB_URI="mongodb://localhost:27017"
./gradlew run
```

`MONGODB_URI` defaults to `mongodb://localhost:27017` if omitted. The application now starts its local server automatically; `./gradlew runServer` remains available when you want to run the server separately.

Optional model overrides:

```bash
export GROQ_TEXT_MODEL="llama-3.3-70b-versatile"
export GROQ_TRANSCRIPTION_MODEL="whisper-large-v3-turbo"
```

Recipe images are disabled by default since image generation is by far the most expensive call. To turn them back on, set an OpenAI key and enable the feature:

```bash
export OPENAI_API_KEY="your_openai_api_key"
export PANTRYPAL_GENERATE_IMAGES=true
export OPENAI_IMAGE_MODEL="gpt-image-1"
```

Run the offline test suite with `./gradlew test`. The legacy MongoDB integration tests are skipped by default; run them only against a disposable database with `RUN_INTEGRATION_TESTS=1`.

## Security note

This project previously stored API keys and MongoDB passwords in source code. Those values have been removed from the working tree. Rotate or revoke the previously exposed OpenAI and MongoDB credentials before using any related account again. OpenAI recommends loading API keys from an environment variable or key-management service rather than embedding them in application code.
