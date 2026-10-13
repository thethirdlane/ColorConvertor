package org.ttl.color.ui

import org.ttl.color.events.ColorEvents
import org.ttl.color.events.eventCentral
import org.ttl.color.utils.copy
import org.ttl.color.utils.hexStringToColorAndHexString
import org.ttl.color.utils.paste
import org.ttl.color.utils.rgbStringToColorAndHexString
import org.ttl.color.utils.setHEXTextFieldFromColor
import org.ttl.color.utils.setRGBTextFieldFromColor
import ttl.kotdsls.swing.Gbc
import ttl.kotdsls.swing.button
import ttl.kotdsls.swing.gbc
import ttl.kotdsls.swing.label
import ttl.kotdsls.swing.panel
import ttl.kotdsls.swing.textField
import java.awt.Color
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.Locale.getDefault
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.UIManager

class ColorPanel(val title: String) : JPanel() {
    val mainPanel: JPanel
    lateinit var rgbTextField: JTextField
    lateinit var rgbCopyButton: JButton
    lateinit var rgbPasteButton: JButton
    lateinit var hexTextField: JTextField
    lateinit var hexCopyButton: JButton
    lateinit var hexPasteButton: JButton
    lateinit var suggestButton: JButton
    lateinit var lAndFButton: JButton
//    val rgbPrevious: JComboBox<String>
//    val hexPrevious: JComboBox<String>

    companion object {
    }

    init {
        layout = GridBagLayout()
        var gbc = gbc(weightx = 1.0, weighty = 1.0, fill = Gbc.BOTH, insets = Insets(10, 10, 10, 10))
        mainPanel = panel(layoutConstraint = gbc) {
            layout = GridBagLayout()

            //Title Label
            gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, anchor = Gbc.CENTER,
                insets = Insets(0, 0, 10, 0))
            label(layoutConstraint = gbc, text = title)

            gbc = gbc(fill = Gbc.NONE, anchor = Gbc.EAST, insets = Insets(0, 0, 10, 10))
            label(layoutConstraint = gbc, text = "RGB[A]:")
            gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 10))
            rgbTextField = textField(layoutConstraint = gbc) {
                addActionListener { event ->
                    val source = event.source as JTextField
                    val p = rgbStringToColorAndHexString(source.text)
                    p?.let {
                        val color = p.first
                        val hexText = p.second
                        setHEXTextFieldFromColor(color, hexTextField)
                        eventCentral.shoutOut(ColorEvents.RGBInput, listOf(this@ColorPanel,
                            title, color, hexText))
                    }
                }
            }
            gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
            rgbCopyButton = button(layoutConstraint = gbc, text = "C") {
                toolTipText = "Copy"
                addActionListener { event ->
                    copy(rgbTextField)

//                //Let them know a copy happened
//                eventCentral.shoutOut(ColorEvents.Copy, listOf(this@ColorPanel,
//                    title, rgbTextField.text))
                }
            }
            gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
            rgbPasteButton = button(layoutConstraint = gbc, text = "P") {
                toolTipText = "Paste"
                addActionListener { event ->
                    paste(rgbTextField)

//                //Let them know a copy happened
//                eventCentral.shoutOut(ColorEvents.Paste, listOf(this@ColorPanel,
//                    title, rgbTextField.text))
                }
            }

            gbc = gbc(fill = Gbc.NONE, anchor = Gbc.EAST, insets = Insets(0, 0, 0, 10))
            label(layoutConstraint = gbc, text = "HEX[A]:")
            gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 0, 10))
            hexTextField = textField(layoutConstraint = gbc) {
                addActionListener { event ->
                    val source = event.source as JTextField
                    val p = hexStringToColorAndHexString(source.text)
                    p?.let {
                        val color = p.first
                        val hexText = p.second
                        //Set our other fields
                        setRGBTextFieldFromColor(color, rgbTextField)

                        //Let the world know we god hex input
                        eventCentral.shoutOut(ColorEvents.HEXInput, listOf(this@ColorPanel,
                            title, color, hexText))
                    }
                }
            }
            gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 0, 0))
            hexCopyButton = button(layoutConstraint = gbc, text = "C") {
                toolTipText = "Copy"
                addActionListener { event ->
                    copy(hexTextField)
                }

            }
            gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, insets = Insets(0, 0, 0, 0))
            hexPasteButton = button(layoutConstraint = gbc, text = "P") {
                toolTipText = "Paste"
                addActionListener { event ->
                    paste(hexTextField)
                }
            }

            gbc = gbc(w = Gbc.REMAINDER, weightx = 1.0, fill = Gbc.BOTH, insets = Insets(15, 0, 0, 10))
            panel(layoutConstraint = gbc) {
                layout = GridBagLayout()

                gbc = gbc(weightx = 1.0, fill = Gbc.NONE, insets = Insets(15, 0, 0, 10))
                suggestButton = button(layoutConstraint = gbc, text = "Suggest") {
                    toolTipText = "Suggest Colors for the opposite side"
                    addActionListener { event ->
                        rgbStringToColorAndHexString(rgbTextField.text)?.let {
                            val color = it.first
                            val hexText = it.second
                            eventCentral.shoutOut(ColorEvents.Suggest, listOf(this@ColorPanel,
                                title, color, hexText))
                        }
                    }
                }

                gbc = gbc(weightx = 1.0, fill = Gbc.NONE, insets = Insets(15, 0, 0, 0))
                lAndFButton = button(layoutConstraint = gbc, text = "L&F Default") {
                    toolTipText = "Set to Look And Feel Default"
                    addActionListener { event ->
                        val defaultColor = if (isForeground()) {
                            UIManager.getColor("Panel.foreground")
                        } else {
                            UIManager.getColor("Panel.background")
                        }
                        setRGBTextFieldFromColor(defaultColor, rgbTextField)
                        setHEXTextFieldFromColor(defaultColor, hexTextField)

                        eventCentral.shoutOut(ColorEvents.RGBInput, listOf(this@ColorPanel,
                            title, defaultColor, hexTextField.text))
                    }
                }
            }

        }

        eventCentral.addObserver(this, ColorEvents.InitialValues) { event, args ->
            val fg = args[1] as Color
            val bg = args[2] as Color

            if (isForeground()) {
                setRGBTextFieldFromColor(fg, rgbTextField)
                setHEXTextFieldFromColor(fg, hexTextField)
            } else {
                setRGBTextFieldFromColor(bg, rgbTextField)
                setHEXTextFieldFromColor(bg, hexTextField)
            }
        }
    }

    fun isForeground() = (title.lowercase(getDefault()).contains("foreground"))
}