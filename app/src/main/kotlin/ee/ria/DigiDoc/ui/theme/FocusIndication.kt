// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import ee.ria.DigiDoc.ui.theme.Dimensions.MSCornerRadius
import kotlinx.coroutines.launch

data class FocusIndication(
    private val color: Color,
    private val alpha: Float = 1.0f,
) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        FocusIndicationNode(interactionSource, color.copy(alpha = alpha))
}

private class FocusIndicationNode(
    private val interactionSource: InteractionSource,
    private val color: Color,
) : Modifier.Node(),
    DrawModifierNode {
    private var isFocused = false

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is FocusInteraction.Focus -> isFocused = true
                    is FocusInteraction.Unfocus -> isFocused = false
                }
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        if (isFocused) {
            drawRoundRect(
                color = color,
                size = size,
                cornerRadius = CornerRadius(MSCornerRadius.toPx()),
            )
        }
        drawContent()
    }
}
