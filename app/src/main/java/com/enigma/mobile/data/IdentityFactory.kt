package com.enigma.mobile.data

import kotlin.random.Random

object IdentityFactory {

    private val userAgents = listOf(
        "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 13; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 13; SM-A546B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
    )

    private val languages = listOf("es-MX,es,en", "es-ES,es,en", "en-US,en,es", "pt-BR,pt,en")

    private val timezones = listOf(
        "America/Mexico_City", "America/Bogota", "America/Argentina/Buenos_Aires",
        "America/Santiago", "America/Lima", "Europe/Madrid"
    )

    private val models = listOf("Pixel 8 Pro", "SM-S918B", "Pixel 7", "SM-A546B", "Pixel 8a")
    private val gpuVendors = listOf("Qualcomm", "ARM", "Google")
    private val gpuRenderers = listOf("Adreno 750", "Mali-G715", "Mali-G78", "Adreno 640")
    private val regions = listOf("mx", "co", "ar", "cl", "pe", "es")

    fun randomUserAgent() = userAgents.random()
    fun randomLanguages() = languages.random()
    fun randomTimezone() = timezones.random()
    fun randomCores() = listOf(4, 6, 8).random()
    fun randomRam() = listOf(4, 6, 8, 12, 16).random()
    fun randomScreen() = listOf("1080x2400", "1440x3200", "1080x2340").random()
    fun randomFpId() = UUID.randomUUID().toString().take(12)
    fun randomModel() = models.random()
    fun randomGpuVendor() = gpuVendors.random()
    fun randomGpuRenderer() = gpuRenderers.random()
    fun randomChromeMajor() = Random.nextInt(128, 132)
    fun randomCanvasSeed() = Random.nextDouble()
    fun randomAudioSeed() = Random.nextDouble()
    fun randomRegion() = regions.random()

    fun coherentPack(region: String? = null): IdentityPack {
        val r = region ?: randomRegion()
        return IdentityPack(
            os = "Android 14",
            userAgent = randomUserAgent(),
            languages = randomLanguages(),
            timezone = when(r) {
                "mx" -> "America/Mexico_City"
                "co" -> "America/Bogota"
                "ar" -> "America/Argentina/Buenos_Aires"
                else -> "America/Mexico_City"
            },
            cores = randomCores(),
            ram = randomRam(),
            screen = randomScreen(),
            fpId = randomFpId(),
            model = randomModel(),
            gpuVendor = randomGpuVendor(),
            gpuRenderer = randomGpuRenderer(),
            chromeMajor = randomChromeMajor(),
            chromeFull = "",
            canvasSeed = randomCanvasSeed(),
            audioSeed = randomAudioSeed(),
            region = r,
        )
    }
}

data class IdentityPack(
    val os: String, val userAgent: String, val languages: String,
    val timezone: String, val cores: Int, val ram: Int, val screen: String,
    val fpId: String, val model: String, val gpuVendor: String,
    val gpuRenderer: String, val chromeMajor: Int, val chromeFull: String,
    val canvasSeed: Double, val audioSeed: Double, val region: String,
)