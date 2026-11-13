package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.CountdownSnapshot
import com.example.model.FestiveTarget
import java.util.Locale

@Composable
fun TargetSelectorRow(
    selectedTarget: FestiveTarget,
    onSelectTarget: (FestiveTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FestiveTarget.entries.forEach { target ->
            val selected = target == selectedTarget
            val icon = when (target) {
                FestiveTarget.NAVIDAD -> Icons.Rounded.Park
                FestiveTarget.NOCHEBUENA -> Icons.Rounded.NightsStay
                FestiveTarget.ANO_NUEVO -> Icons.Rounded.Celebration
                FestiveTarget.REYES -> Icons.Rounded.AutoAwesome
            }
            FilterChip(
                selected = selected,
                onClick = { onSelectTarget(target) },
                label = {
                    Text(
                        text = target.title,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (selected) Icons.Rounded.Check else icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconColor = MaterialTheme.colorScheme.primary,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.testTag("target_chip_${target.name.lowercase()}"),
            )
        }
    }
}

@Composable
fun CountdownDeck(
    snapshot: CountdownSnapshot,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = snapshot.progressFraction,
        animationSpec = spring(),
        label = "countdown_progress",
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (snapshot.isCelebrationToday) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("celebration_banner"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Celebration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp),
                    )
                    Text(
                        text = snapshot.target.celebrationTitle,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = snapshot.target.celebrationSubtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CountdownUnitTile(
                    value = snapshot.days,
                    label = if (snapshot.days == 1L) "Día" else "Días",
                    highlighted = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("countdown_days"),
                )
                CountdownUnitTile(
                    value = snapshot.hours,
                    label = "Horas",
                    highlighted = false,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("countdown_hours"),
                )
                CountdownUnitTile(
                    value = snapshot.minutes,
                    label = "Min",
                    highlighted = false,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("countdown_minutes"),
                )
                CountdownUnitTile(
                    value = snapshot.seconds,
                    label = "Seg",
                    highlighted = false,
                    isSecondsAccent = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("countdown_seconds"),
                )
            }
        }

        // Progress toward the celebration
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Camino a ${snapshot.target.title}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val pct = (snapshot.progressFraction * 100f).toInt().coerceIn(0, 100)
                    Text(
                        text = "$pct% completado",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface,
                    strokeCap = StrokeCap.Round,
                )
                Text(
                    text = "Fecha objetivo: ${snapshot.targetDateLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CountdownUnitTile(
    value: Long,
    label: String,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
    isSecondsAccent: Boolean = false,
) {
    val containerColor = when {
        highlighted -> MaterialTheme.colorScheme.primaryContainer
        isSecondsAccent -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f)
    }
    val borderColor = when {
        highlighted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        isSecondsAccent -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    }
    val numberColor = when {
        highlighted -> MaterialTheme.colorScheme.primary
        isSecondsAccent -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlighted) 6.dp else 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val formatted = String.format(Locale.getDefault(), "%02d", value)
            AnimatedContent(
                targetState = formatted,
                transitionSpec = {
                    (slideInVertically { height -> height / 2 } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height / 2 } + fadeOut()) using
                        SizeTransform(clip = false)
                },
                label = "digit_anim_$label",
            ) { targetText ->
                Text(
                    text = targetText,
                    style = MaterialTheme.typography.displayMedium,
                    color = numberColor,
                    maxLines = 1,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}
