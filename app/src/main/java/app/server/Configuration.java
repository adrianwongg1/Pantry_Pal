package app.server;

/** Centralises runtime configuration so credentials never live in source code. */
public final class Configuration {
    private Configuration() { }

    public static String openAiApiKey() {
        String key = value("OPENAI_API_KEY", "openai.api.key", null);
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                "OPENAI_API_KEY is not configured. Set it before using the image generation feature.");
        }
        return key;
    }

    /** Recipe generation and transcription run on Groq's free tier instead of OpenAI. */
    public static String groqApiKey() {
        String key = value("GROQ_API_KEY", "groq.api.key", null);
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                "GROQ_API_KEY is not configured. Set it before using recipe generation or transcription features.");
        }
        return key;
    }

    /** Runs the app with bundled sample data and no external services. */
    public static boolean demoMode() {
        return Boolean.parseBoolean(value("PANTRYPAL_DEMO_MODE", "pantrypal.demo", "false"));
    }

    /** Image generation is by far the priciest OpenAI call, so it stays off unless explicitly enabled. */
    public static boolean imageGenerationEnabled() {
        return Boolean.parseBoolean(value("PANTRYPAL_GENERATE_IMAGES", "pantrypal.generateImages", "false"));
    }

    public static String mongoUri() {
        return value("MONGODB_URI", "mongodb.uri", "mongodb://localhost:27017");
    }

    public static String textModel() {
        return value("GROQ_TEXT_MODEL", "groq.text.model", "llama-3.3-70b-versatile");
    }

    public static String imageModel() {
        return value("OPENAI_IMAGE_MODEL", "openai.image.model", "gpt-image-1");
    }

    public static String transcriptionModel() {
        return value("GROQ_TRANSCRIPTION_MODEL", "groq.transcription.model", "whisper-large-v3-turbo");
    }

    private static String value(String environmentName, String propertyName, String defaultValue) {
        String property = System.getProperty(propertyName);
        if (property != null && !property.isBlank()) return property;
        String environment = System.getenv(environmentName);
        return environment == null || environment.isBlank() ? defaultValue : environment;
    }
}
