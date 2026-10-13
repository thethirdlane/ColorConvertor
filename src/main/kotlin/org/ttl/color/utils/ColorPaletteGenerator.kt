package org.ttl.color.utils

import java.awt.Color

object ColorPaletteGenerator {

    // 1. Complementary Palette (High Contrast, Bold Contrast)
    // Shifts the hue by 180 degrees and inverts the lightness for readability
    fun getComplementarySuggestion(base: Color): Color {
        val hsl = base.toHSL()
        val newHue = (hsl.hue + 180f) % 360f
        // If foreground is dark, make background light (and vice versa)
        val newLightness = if (hsl.lightness < 0.5f) 0.90f else 0.15f
        val newSaturation = hsl.saturation.coerceAtMost(0.7f) // Mute slightly so it's not blinding

        return HSL(newHue, newSaturation, newLightness).toColor()
    }

    // 2. Monochromatic / Tint-Shade Theme (Cohesive & Clean)
    // Keeps the exact same hue, but guarantees a high contrast lightness shift
    fun getMonochromaticSuggestion(base: Color): Color {
        val hsl = base.toHSL()
        val newLightness = if (hsl.lightness < 0.5f) 0.92f else 0.12f
        // Reduce saturation slightly for backgrounds, keep rich for text
        val newSaturation = if (newLightness > 0.5f) hsl.saturation * 0.4f else hsl.saturation

        return HSL(hsl.hue, newSaturation, newLightness).toColor()
    }

    // 3. Analogous / Soft Pastel Theme (Warm and Artistic)
    // Shifts hue by 30-40 degrees for a rich, multi-colored pastel scheme
    fun getAnalogousSuggestion(base: Color): Color {
        val hsl = base.toHSL()
        val newHue = (hsl.hue + 35f) % 360f
        val newLightness = if (hsl.lightness < 0.5f) 0.88f else 0.18f

        return HSL(newHue, 0.5f, newLightness).toColor()
    }
}
