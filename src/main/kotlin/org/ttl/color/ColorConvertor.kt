/*
Copyright (c) 2026 The Third Lane

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package org.ttl.color

import org.slf4j.LoggerFactory
import org.ttl.color.events.ColorEvents
import org.ttl.color.events.eventCentral
import org.ttl.color.themes.ThemeManager
import org.ttl.color.themes.themeManager
import org.ttl.color.ui.ColorConvertorGui
import org.ttl.color.utils.ColorPaletteGenerator
import org.ttl.color.utils.setHEXTextFieldFromColor
import org.ttl.color.utils.setRGBTextFieldFromColor
import ttl.kotdsls.swing.ui.getMinFontSizeForScreen
import ttl.kotdsls.swing.ui.setUIMinFontSize
import java.awt.Color
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.util.*
import java.util.Locale.getDefault
import javax.swing.JFrame
import javax.swing.JTextField
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource

var RB: Properties = Properties()

class ColorConvertor {
    val gui = ColorConvertorGui("Color Convertor")

    companion object {
        val logger = LoggerFactory.getLogger(ColorConvertor::class.java)
    }

    init {

        eventCentral.addObserver(this, ColorEvents.ThemeChanged) { _, args ->
//            eventCentral.shoutOut(ColorEvents.InitialValues, listOf(this@ColorConvertor,
//                gui.displayPanel.foreground, gui.displayPanel.background))
        }

        eventCentral.addObserver(this, ColorEvents.RGBInput) { event, args ->
            val panelName = args[1] as String
            val color = args[2] as Color
            val hextText = args[3] as String

            val doingForeground = (panelName.lowercase(getDefault()).contains("foreground"))
            handleRGBClicked(color, hextText, doingForeground)
        }

        eventCentral.addObserver(this, ColorEvents.HEXInput) { event, args ->
            val panelName = args[1] as String
            val color = args[2] as Color
            val hexText = args[3] as String

            val doingForeground = (panelName.lowercase(getDefault()).contains("foreground"))
            handleHEXClicked(color, hexText, doingForeground)
        }

        gui.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    }

    fun show() {
//        gui.size = Dimension(1500, 400)
        gui.pack()
        gui.isVisible = true

        eventCentral.shoutOut(ColorEvents.InitialValues, listOf(this@ColorConvertor,
            gui.displayPanel.foreground, gui.displayPanel.background))
    }


    fun copy(textField: JTextField) {
        val text = textField.text
        val selection = StringSelection(text)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
    }

    fun paste(textField: JTextField) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val transferable = clipboard.getContents(null)
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            val pastedText = transferable.getTransferData(DataFlavor.stringFlavor) as String
            textField.text = pastedText
        }

        textField.postActionEvent()
    }

    fun handleHEXClicked(color: Color, hexText: String, doingForeground: Boolean) {
        try {
            if (doingForeground) {
                gui.displayfgText.foreground = color
            } else {
                gui.displayPanel.background = color
            }
//            handleSuggestions(color)
            gui.repaint()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handleRGBClicked(color: Color, hexText: String, doingForeground: Boolean) {
        try {
            if (doingForeground) {
                gui.displayfgText.foreground = color
            } else {
                gui.displayPanel.background = color
            }

//            handleSuggestions(color)
            gui.repaint()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Voodoo color magic from Gemini to suggest an alternate color.
     * If you keep flip flopping with these suggestions, you end up
     * with some variation of black and white.
     */
    fun getSuggestedBackground(inputColor: Color): Color {
        val yiq = (inputColor.red * 299 + inputColor.green * 587 + inputColor.blue * 114) / 1000

        return if (yiq >= 128) {
            // Foreground is light -> suggest a deep, muted dark shade
            Color(
                (inputColor.red * 0.15).toInt(),
                (inputColor.green * 0.15).toInt(),
                (inputColor.blue * 0.15).toInt()
            )
        } else {
            // Foreground is dark -> suggest a soft, tinted light background
            Color(
                Math.min(255, (inputColor.red + (255 - inputColor.red) * 0.85).toInt()),
                Math.min(255, (inputColor.green + (255 - inputColor.green) * 0.85).toInt()),
                Math.min(255, (inputColor.blue + (255 - inputColor.blue) * 0.85).toInt())
            )
        }
    }

    fun handleSuggestions(color: Color) {
        val otherColor = getSuggestedBackground(color)
        setRGBTextFieldFromColor(otherColor, gui.complementarySuggestion.suggestionRGB)
        setHEXTextFieldFromColor(otherColor, gui.complementarySuggestion.suggestionHEX)

        updateColorSuggestions(color, false)
        println("otherColor: $otherColor")
    }


    // Example inside your Swing Controller/Panel logic
    fun updateColorSuggestions(selectedColor: Color, isForegroundPick: Boolean) {
        val pastelBg = ColorPaletteGenerator.getAnalogousSuggestion(selectedColor)
        val cleanBg = ColorPaletteGenerator.getMonochromaticSuggestion(selectedColor)
        val boldBg = ColorPaletteGenerator.getComplementarySuggestion(selectedColor)

        // Update your UI suggestion swatches or buttons with these colors
        // suggestionButton1.background = pastelBg
        // suggestionButton2.background = cleanBg
        // suggestionButton3.background = boldBg
    }
}

/*
10, 25, 200
 */

object ColorConvertorApp {
    @JvmStatic
    fun main(args: Array<String>) {
        //Set this here so you don't need to set is as a property on the command line
        System.setProperty("sun.java2d.opengl", "true");

        themeManager = ThemeManager(RB)
        val lAndFName = (UIManager.getSystemLookAndFeelClassName())

        UIManager.setLookAndFeel(lAndFName)
//        FlatLaf.updateUI()

//        AP - "scale" fonts
        val minSize = getMinFontSizeForScreen()
        val newFonts = setUIMinFontSize(minSize)
        newFonts.forEach { (k: String?, v: FontUIResource?) -> UIManager.put(k, v) }
//        End AP - "scale" fonts

        val cc = ColorConvertor()

        cc.show()
    }
}