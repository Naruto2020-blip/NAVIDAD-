package com.example.model

import androidx.annotation.RawRes
import com.example.R

enum class FestiveTarget(
    val title: String,
    val month: Int,
    val dayOfMonth: Int,
    val celebrationTitle: String,
    val celebrationSubtitle: String,
) {
    NOCHEBUENA(
        title = "Nochebuena",
        month = 12,
        dayOfMonth = 24,
        celebrationTitle = "¡Feliz Nochebuena!",
        celebrationSubtitle = "Que la paz, los abrazos y la cena en familia iluminen esta noche.",
    ),
    NAVIDAD(
        title = "Navidad",
        month = 12,
        dayOfMonth = 25,
        celebrationTitle = "¡Feliz Navidad!",
        celebrationSubtitle = "Hoy brilla la magia navideña en cada hogar y corazón.",
    ),
    ANO_NUEVO(
        title = "Año Nuevo",
        month = 1,
        dayOfMonth = 1,
        celebrationTitle = "¡Feliz Año Nuevo!",
        celebrationSubtitle = "Comienza un año lleno de esperanza, música y nuevos sueños.",
    ),
    REYES(
        title = "Reyes Magos",
        month = 1,
        dayOfMonth = 6,
        celebrationTitle = "¡Feliz Día de Reyes!",
        celebrationSubtitle = "La ilusión y la alegría llegan con los regalos de Oriente.",
    ),
}

data class CountdownSnapshot(
    val target: FestiveTarget,
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val progressFraction: Float,
    val isCelebrationToday: Boolean,
    val targetDateLabel: String,
)

data class CarolTrack(
    val id: String,
    val title: String,
    val subtitle: String,
    val tempoLabel: String,
    val durationSeconds: Int,
    @RawRes val rawResId: Int,
)

data class FestiveMessage(
    val title: String,
    val body: String,
)

object FestiveCatalog {
    val carols: List<CarolTrack> = listOf(
        CarolTrack(
            id = "noche_de_paz",
            title = "Noche de Paz",
            subtitle = "Celesta cálida y armonía serena",
            tempoLabel = "Sereno · 3/4",
            durationSeconds = 46,
            rawResId = R.raw.carol_noche_de_paz,
        ),
        CarolTrack(
            id = "cascabeles",
            title = "Cascabeles (Jingle Bells)",
            subtitle = "Campanas festivas y trineo nevado",
            tempoLabel = "Alegre · 4/4",
            durationSeconds = 30,
            rawResId = R.raw.carol_cascabeles,
        ),
        CarolTrack(
            id = "adornemos",
            title = "Adornemos los Salones",
            subtitle = "Carillón navideño tradicional",
            tempoLabel = "Festivo · 4/4",
            durationSeconds = 31,
            rawResId = R.raw.carol_adornemos,
        ),
        CarolTrack(
            id = "feliz_navidad",
            title = "Os Deseamos Feliz Navidad",
            subtitle = "Brindis musical de fin de año",
            tempoLabel = "Jubiloso · 3/4",
            durationSeconds = 21,
            rawResId = R.raw.carol_feliz_navidad,
        ),
    )

    val dailyMessages: List<FestiveMessage> = listOf(
        FestiveMessage(
            title = "Espíritu de Adviento",
            body = "Enciende una vela cálida, prepara chocolate caliente y disfruta cada segundo de la espera.",
        ),
        FestiveMessage(
            title = "Melodías del Hogar",
            body = "Los villancicos transforman cualquier tarde tranquila en un recuerdo inolvidable de diciembre.",
        ),
        FestiveMessage(
            title = "Pequeños Gestos",
            body = "Una llamada, una tarjeta escrita a mano o un abrazo sincero son los mejores regalos.",
        ),
        FestiveMessage(
            title = "Luces en la Ventana",
            body = "Cada estrella y guirnalda encendida recuerda que siempre hay motivos para celebrar juntos.",
        ),
    )
}
