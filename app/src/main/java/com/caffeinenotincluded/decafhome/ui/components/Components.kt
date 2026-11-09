package com.caffeinenotincluded.decafhome.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caffeinenotincluded.decafhome.data.AppEntry
import com.caffeinenotincluded.decafhome.ui.theme.Brand
import com.caffeinenotincluded.decafhome.ui.theme.Fredoka
import com.caffeinenotincluded.decafhome.ui.theme.LocalDecafColors
import com.caffeinenotincluded.decafhome.ui.theme.Nunito

/** The brand's dotted-paper background. */
@Composable
fun DotGrid(modifier: Modifier = Modifier) {
    val colors = LocalDecafColors.current
    Canvas(modifier) {
        drawRect(colors.background)
        val step = 22.dp.toPx()
        val radius = 1.4.dp.toPx()
        var y = step / 2
        while (y < size.height) {
            var x = step / 2
            while (x < size.width) {
                drawCircle(colors.dots, radius, Offset(x, y))
                x += step
            }
            y += step
        }
    }
}

/**
 * A widget card: paper colour, ink outline and a hard offset shadow, like the
 * brand guide. In dark mode the shadow takes the card's accent colour.
 */
@Composable
fun BrandCard(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    action: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalDecafColors.current
    val shape = RoundedCornerShape(18.dp)
    val shadow = if (colors.dark) accent else colors.outline
    Box(modifier.padding(end = 5.dp, bottom = 5.dp)) {
        Box(
            Modifier
                .matchParentSize()
                .offset(5.dp, 5.dp)
                .background(shadow, shape),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.card)
                .border(2.dp, colors.outline, shape)
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(accent, CircleShape)
                        .border(1.5.dp, colors.outline, CircleShape),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    label.uppercase(),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp,
                    color = colors.mutedOnCard,
                    modifier = Modifier.weight(1f),
                )
                if (action != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, content = action)
                }
            }
            Spacer(Modifier.size(10.dp))
            content()
        }
    }
}

/** A chunky pill button with an ink outline and a small hard shadow. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Brand.Sunshine,
    textColor: Color = Brand.Ink,
    enabled: Boolean = true,
    small: Boolean = false,
) {
    val shape = RoundedCornerShape(50)
    val alpha = if (enabled) 1f else 0.4f
    Box(modifier.padding(end = 3.dp, bottom = 3.dp)) {
        Box(
            Modifier
                .matchParentSize()
                .offset(3.dp, 3.dp)
                .background(Brand.Ink.copy(alpha = alpha), shape),
        )
        Box(
            Modifier
                .clip(shape)
                .background(color.copy(alpha = if (enabled) 1f else 0.5f))
                .border(2.dp, Brand.Ink.copy(alpha = alpha), shape)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = if (small) 12.dp else 16.dp, vertical = if (small) 6.dp else 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text,
                fontFamily = Fredoka,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (small) 13.sp else 15.sp,
                color = textColor.copy(alpha = alpha),
                maxLines = 1,
            )
        }
    }
}

/** A small selectable chip (timer lengths, idea kinds, theme...). */
@Composable
fun ChoiceChip(text: String, selected: Boolean, onClick: () -> Unit, accent: Color = Brand.Sunshine) {
    val colors = LocalDecafColors.current
    val shape = RoundedCornerShape(50)
    Box(
        Modifier
            .clip(shape)
            .background(if (selected) accent else Color.Transparent)
            .border(BorderStroke(1.5.dp, colors.outline), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text,
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (selected) Brand.Ink else colors.onCard,
        )
    }
}

/** A ring that empties as time runs out. */
@Composable
fun ProgressRing(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    stroke: Dp = 12.dp,
    track: Color = LocalDecafColors.current.onCard.copy(alpha = 0.12f),
    content: @Composable () -> Unit = {},
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val width = stroke.toPx()
            val inset = width / 2
            val arcSize = Size(size.width - width, size.height - width)
            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * fraction.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

/** A thin rounded progress bar. */
@Composable
fun ProgressBar(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    val colors = LocalDecafColors.current
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .clip(shape)
            .background(colors.onCard.copy(alpha = 0.10f))
            .border(1.5.dp, colors.outline, shape),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(10.dp)
                .background(color, shape),
        )
    }
}

/** An app icon with its name underneath. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTile(
    app: AppEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    labelColor: Color = LocalDecafColors.current.onBackground,
    iconSize: Dp = 52.dp,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(app.icon, contentDescription = app.label, modifier = Modifier.size(iconSize))
        if (showLabel) {
            Spacer(Modifier.size(4.dp))
            Text(
                app.label,
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** A dialog with one text field. */
@Composable
fun TextEntryDialog(
    title: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    initial: String = "",
    placeholder: String = "",
    multiLine: Boolean = false,
    supporting: String? = null,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (supporting != null) Text(supporting, fontFamily = Nunito)
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(placeholder) },
                    singleLine = !multiLine,
                    minLines = if (multiLine) 4 else 1,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
