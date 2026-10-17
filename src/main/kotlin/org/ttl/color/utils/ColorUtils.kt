package org.ttl.color.utils

import org.ttl.color.ColorConvertor.Companion.logger
import org.ttl.color.ui.postActionEvent
import java.awt.Color
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.util.Locale.getDefault
import javax.swing.JComboBox
import javax.swing.JTextField

data class HSL(val hue: Float, val saturation: Float, val lightness: Float) {
    val toText = String.format("%.02f, %.02f, %.02f", hue, saturation, lightness)
}

data class ColorAndHex(val color: Color, val hexText: String, val hsl: HSL = color.toHSL())

fun rgbStringToColorAndHex(rgbString: String): ColorAndHex? {
    val arr = rgbString.split(",\\s*".toRegex()).toTypedArray()
    if (arr.size < 3 || arr.size > 4) {
        logger.error("Input should be 3 or 4 comma seperated RGB[A] between 0 and 255")
        return null
    }

    val color = if (arr.size == 3) {
        Color(arr[0].trim().toInt(), arr[1].trim().toInt(), arr[2].trim().toInt())
    } else {
        Color(arr[0].trim().toInt(), arr[1].trim().toInt(), arr[2].trim().toInt(), arr[3].trim().toInt())
    }

    val hsl = color.toHSL()

    val hex = if (arr.size == 3) {
        String.format("#%02X%02X%02X", arr[0].trim().toInt(), arr[1].trim().toInt(), arr[2].trim().toInt())
    } else {
        String.format("#%02X%02X%02X%02X",
            arr[0].trim().toInt(),
            arr[1].trim().toInt(),
            arr[2].trim().toInt(),
            arr[3].trim().toInt())
    }

    return ColorAndHex(color, hex, hsl)
}

fun hexStringToColorAndHex(input: String): ColorAndHex? {
    var hexText = input.uppercase(getDefault())
    val ttu = if (hexText.startsWith("#")) {
        hexText.substring(1)
    } else
        hexText

    val regex = "(..)".toRegex()

    val matches = regex.findAll(ttu).toList()
//            println("matches: $matches")
//            matches.forEach { println(it.value) }

    if (matches.size in 3..4) {
        val color = if (matches.size == 3) {
            Color(matches.get(0).value.hexToInt(),
                matches.get(1).value.hexToInt(),
                matches.get(2).value.hexToInt())
        } else {
            Color(matches.get(0).value.hexToInt(), matches.get(1).value.hexToInt(),
                matches.get(2).value.hexToInt(), matches.get(3).value.hexToInt())
        }

        return ColorAndHex(color, hexText, color.toHSL())
    }

    return null
}

fun hslStringToColorAndHex(hslString: String): ColorAndHex? {
    val arr = hslString.split(",\\s*".toRegex()).toTypedArray()
    if (arr.size < 3 || arr.size > 4) {
        logger.error("Input should be 3 or 4 comma seperated RGB[A] between 0 and 255")
        return null
    }

    val hsl = HSL(arr[0].trim().toFloat(), arr[1].trim().toFloat(), arr[2].trim().toFloat())

    val color = hsl.toColor()

    val hex = if (arr.size == 3) {
        String.format("#%02X%02X%02X", color.red, color.green, color.blue)
    } else {
        String.format("#%02X%02X%02X", color.red, color.green, color.blue, color.alpha)
    }

    return ColorAndHex(color, hex, hsl)
}

fun colorToRGBString(color: Color): String {
    val str = String.format("%d, %d, %d, %d", color.red,
        color.green, color.blue, color.alpha)
    return str
}

fun colorToHEXString(color: Color): String {
    val str = String.format("#%02X%02X%02X%02X",
        color.red, color.green, color.blue, color.alpha)
    return str
}

fun setRGBTextFieldFromColor(color: Color, textField: JTextField) {
    val str = String.format("%d, %d, %d, %d", color.red,
        color.green, color.blue, color.alpha)

    textField.text = str
}

fun setHEXTextFieldFromColor(color: Color, textField: JTextField) {
    val str = String.format("#%02X%02X%02X%02X",
        color.red, color.green, color.blue, color.alpha)

    textField.text = str
}

fun setRGBTextFieldFromHEX(hexStr: String, textField: JTextField) {
    val color = hexToColor(hexStr)
    color?.let {
        setRGBTextFieldFromColor(it, textField)
    }
}

fun hexToColor(hexStr: String): Color? {
    var hexText = hexStr.uppercase(getDefault())
    if (hexText.startsWith("#")) {
        hexText = hexText.substring(1)
    }
    val regex = "(..)".toRegex()

    val matches = regex.findAll(hexText).toList()
//            println("matches: $matches")
//            matches.forEach { println(it.value) }

    val color = if (matches.size in 3..4) {
        if (matches.size == 3) {
            Color(matches.get(0).value.hexToInt(),
                matches.get(1).value.hexToInt(),
                matches.get(2).value.hexToInt())
        } else {
            Color(matches.get(0).value.hexToInt(), matches.get(1).value.hexToInt(),
                matches.get(2).value.hexToInt(), matches.get(3).value.hexToInt())
        }
    } else {
        null
    }
    return color
}

fun Color.toHSL(): HSL {
    val r = red / 255.0f
    val g = green / 255.0f
    val b = blue / 255.0f

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    var h = 0f
    val l = (max + min) / 2f
    val s: Float

    if (delta == 0f) {
        h = 0f
        s = 0f
    } else {
        s = if (l < 0.5f) delta / (max + min) else delta / (2.0f - max - min)
        h = when (max) {
            r -> (g - b) / delta + (if (g < b) 6f else 0f)
            g -> (b - r) / delta + 2f
            else -> (r - g) / delta + 4f
        }
        h /= 6f
    }
    return HSL(h * 360f, s, l)
}

fun HSL.toColor(): Color {
    val c = (1f - kotlin.math.abs(2f * lightness - 1f)) * saturation
    val x = c * (1f - kotlin.math.abs((hue / 60f) % 2f - 1f))
    val m = lightness - c / 2f

    val (rPrime, gPrime, bPrime) = when {
        hue < 60f -> Triple(c, x, 0f)
        hue < 120f -> Triple(x, c, 0f)
        hue < 180f -> Triple(0f, c, x)
        hue < 240f -> Triple(0f, x, c)
        hue < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        ((rPrime + m) * 255).toInt().coerceIn(0, 255),
        ((gPrime + m) * 255).toInt().coerceIn(0, 255),
        ((bPrime + m) * 255).toInt().coerceIn(0, 255)
    )
}

fun copy(comboBox: JComboBox<*>) {
    val editorText = (comboBox.editor.editorComponent as JTextField).text
    val item = comboBox.selectedItem as ColorAndHex
    val textToCopy = if(editorText.contains(","))
        colorToRGBString(item.color)
    else
        item.hexText
    copy(textToCopy)
}

//242, 242, 242, 255

fun copy(textField: JTextField) {
    val text = textField.text
    copy(text)
}

fun copy(text: String) {
    val selection = StringSelection(text)
    Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
}

fun paste(comboBox: JComboBox<*>) {
    getStringFromClipboard()?.let { pastedText ->
        comboBox.postActionEvent("comboBoxEdited:$pastedText")
    }
}

fun paste(textField: JTextField) {
    getStringFromClipboard()?.let { pastedText ->
//    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
//    val transferable = clipboard.getContents(null)
//    if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
//        val pastedText = transferable.getTransferData(DataFlavor.stringFlavor) as String
//        textField.text = pastedText
//    }

        textField.text = pastedText
        textField.postActionEvent()
    }
}

fun getStringFromClipboard() : String? {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val transferable = clipboard.getContents(null)
    if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        val pastedText = transferable.getTransferData(DataFlavor.stringFlavor) as String
        return pastedText
    }
   return null
}
