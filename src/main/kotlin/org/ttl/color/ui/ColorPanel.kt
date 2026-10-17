package org.ttl.color.ui

import org.ttl.color.events.ColorEvents
import org.ttl.color.events.eventCentral
import org.ttl.color.utils.ColorAndHex
import org.ttl.color.utils.colorToHEXString
import org.ttl.color.utils.colorToRGBString
import org.ttl.color.utils.copy
import org.ttl.color.utils.hexStringToColorAndHexString
import org.ttl.color.utils.hslStringToColorAndHex
import org.ttl.color.utils.paste
import org.ttl.color.utils.rgbStringToColorAndHexString
import ttl.kotdsls.swing.Gbc
import ttl.kotdsls.swing.button
import ttl.kotdsls.swing.gbc
import ttl.kotdsls.swing.label
import ttl.kotdsls.swing.panel
import java.awt.Color
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.Locale.getDefault
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.UIManager

class ColorPanel(val title: String) : JPanel() {
    val mainPanel: JPanel

    lateinit var rgbCopyButton: JButton
    lateinit var rgbPasteButton: JButton

    lateinit var commonModel: CombinedComboBoxModel<ColorAndHex>

    lateinit var hexCopyButton: JButton
    lateinit var hexPasteButton: JButton

    lateinit var hslCopyButton: JButton
    lateinit var hslPasteButton: JButton

    lateinit var suggestButton: JButton
    lateinit var lAndFButton: JButton

    lateinit var rgbPreviousBox: JComboBox<ColorAndHex>
    lateinit var hexPreviousBox: JComboBox<ColorAndHex>
    lateinit var hslPreviousBox: JComboBox<ColorAndHex>

    var internalSetting = false
//    val hexPrevious: JComboBox<String>

    companion object {
        var comboBoxLimit = 5
    }

    fun isForeground() = (title.lowercase(getDefault()).contains("foreground"))

    init {
        layout = GridBagLayout()
        commonModel = CombinedComboBoxModel<ColorAndHex>(comboBoxLimit)
        var gbc = gbc(weightx = 1.0, weighty = 1.0, fill = Gbc.BOTH, insets = Insets(10, 10, 10, 10))
        mainPanel = panel(layoutConstraint = gbc) {
            layout = GridBagLayout()

            //Title Label
            gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, anchor = Gbc.CENTER,
                insets = Insets(0, 0, 10, 0))
            label(layoutConstraint = gbc, text = title)

            gbc = gbc(fill = Gbc.NONE, anchor = Gbc.EAST, insets = Insets(0, 0, 10, 10))
            label(layoutConstraint = gbc, text = "RGB[A]:")
            rgbPreviousBox = JComboBox<ColorAndHex>().apply {
                isEditable = true
                prototypeDisplayValue = ColorAndHex(Color(200, 200, 255, 255), "#5814FFFF             ")
                renderer = RGBCellRenderer()
                editor = RGBCellEditor()
//                model = LimitedComboBoxModel<String>(comboBoxLimit)
                model = commonModel
                addActionListener { event ->
                    //If the user entered new text, we need to add it to this
                    //ComboBox and to the other one.
                    //The actionCommand will be appended by ":paste_data" if
                    //the event was triggered by a Paste.
                    if (event.actionCommand.startsWith("comboBoxEdited")) {
                        val input = if (event.actionCommand.indexOf(":") != -1)
                            event.actionCommand.removePrefix("comboBoxEdited:")
                        else
                            colorToRGBString((editor.item as ColorAndHex).color)
                        if (input.isNotBlank()) {
                            rgbStringToColorAndHexString(input)?.let { candh ->
                                val color = candh.color
                                val hexText = candh.hexText
                                //Add it to our Model
                                (this.model as CombinedComboBoxModel<ColorAndHex>).addElement(candh)
                                //And let the world know that a new item has been added/selected
                                eventCentral.shoutOut(ColorEvents.RGBInput, listOf(this@ColorPanel,
                                    title, color, hexText))
                            }
                        }
                    } else if (event.actionCommand == "comboBoxChanged") {
                        //The user selected a different item, so we
                        //synchronous our list with the other ComboBox
                        //and let the world know that a new item has been selected.
                        val si = rgbPreviousBox.selectedItem as ColorAndHex
                        si.let { item ->
                            eventCentral.shoutOut(ColorEvents.RGBInput, listOf(this@ColorPanel,
                                title, si.color, si.hexText))
                        }
                    }
                }
            }
            gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 10))
            add(rgbPreviousBox, gbc)

            gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
            rgbCopyButton = button(layoutConstraint = gbc, text = "C") {
                toolTipText = "Copy"
                addActionListener { event ->
                    copy(rgbPreviousBox)
                }
            }
            gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
            rgbPasteButton = button(layoutConstraint = gbc, text = "P") {
                toolTipText = "Paste"
                addActionListener { event ->
                    paste(rgbPreviousBox)
                }
            }

            gbc = gbc(fill = Gbc.NONE, anchor = Gbc.EAST, insets = Insets(0, 0, 0, 10))
            label(layoutConstraint = gbc, text = "HEX[A]:")
            hexPreviousBox = JComboBox<ColorAndHex>().apply {
                isEditable = true
                prototypeDisplayValue = ColorAndHex(Color(200, 200, 200, 200), "#5814FFFF        ")
                renderer = HEXCellRenderer()
                editor = HEXCellEditor()
//                model = LimitedComboBoxModel<String>(comboBoxLimit)
                model = commonModel
                addActionListener { event ->
                    val source = event.source as JComboBox<*>
                    //If the user entered new text, we need to add it to this
                    //ComboBox and to the other one.
                    //The actionCommand will be appended by ":paste_data" if
                    //the event was triggered by a Paste.
                    if (event.actionCommand.startsWith("comboBoxEdited")) {
                        val input = if (event.actionCommand.indexOf(":") != -1)
                            event.actionCommand.removePrefix("comboBoxEdited:")
                        else
                            (editor.item as ColorAndHex).hexText
                        if (input.isNotBlank()) {
                            val ttu = if(input.contains("#")) input else "#$input"

                            hexStringToColorAndHexString(ttu)?.let { candh ->
                                val color = candh.color
                                val hexText = candh.hexText
                                val colorStr = colorToRGBString(color)
                                //Add the new item to our model
                                (this.model as CombinedComboBoxModel<ColorAndHex>).addElement(candh)
                                //And to the other Box

                                //And let the world know that we have new input.
                                eventCentral.shoutOut(ColorEvents.HEXInput, listOf(this@ColorPanel,
                                    title, color, hexText))

                            }
                        }
                    } else if (event.actionCommand == "comboBoxChanged") {
                        val si = hexPreviousBox.selectedItem as ColorAndHex
                        si.let { item ->
                            eventCentral.shoutOut(ColorEvents.HEXInput, listOf(this@ColorPanel,
                                title, si.color, si.hexText))
                        }
                    }
                }
            }
            gbc = gbc(fill = Gbc.HORIZONTAL, insets = Insets(0, 0, 10, 10))
            add(hexPreviousBox, gbc)


            gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
            hexCopyButton = button(layoutConstraint = gbc, text = "C") {
                toolTipText = "Copy"
                addActionListener { event ->
                    copy(hexPreviousBox)
                }

            }
            gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
            hexPasteButton = button(layoutConstraint = gbc, text = "P") {
                toolTipText = "Paste"
                addActionListener { event ->
                    paste(hexPreviousBox)
                }
            }

            gbc = gbc(fill = Gbc.NONE, anchor = Gbc.EAST, insets = Insets(0, 0, 10, 10))
            label(layoutConstraint = gbc, text = "HSL:")
            hslPreviousBox = JComboBox<ColorAndHex>().apply {
                isEditable = true
                prototypeDisplayValue = ColorAndHex(Color(200, 200, 200, 200), "#5814FFFF        ")
                renderer = HSLCellRenderer()
                editor = HSLCellEditor()
//                model = LimitedComboBoxModel<String>(comboBoxLimit)
                model = commonModel
                addActionListener { event ->
                    val source = event.source as JComboBox<*>
                    //If the user entered new text, we need to add it to this
                    //ComboBox and to the other one.
                    //The actionCommand will be appended by ":paste_data" if
                    //the event was triggered by a Paste.
                    if (event.actionCommand.startsWith("comboBoxEdited")) {
                        val input = if (event.actionCommand.indexOf(":") != -1)
                            event.actionCommand.removePrefix("comboBoxEdited:")
                        else
                            (editor.item as ColorAndHex).hsl.toText
                        if (input.isNotBlank()) {
                            hslStringToColorAndHex(input)?.let { candh ->
                                //Add the new item to our model
                                (this.model as CombinedComboBoxModel<ColorAndHex>).addElement(candh)
                                //And to the other Box

                                //And let the world know that we have new input.
                                eventCentral.shoutOut(ColorEvents.HEXInput, listOf(this@ColorPanel,
                                    title, candh.color, candh.hexText, candh.hsl))
                            }
                        }
                    } else if (event.actionCommand == "comboBoxChanged") {
                        val si = hexPreviousBox.selectedItem as ColorAndHex
                        si.let { item ->
                            eventCentral.shoutOut(ColorEvents.HEXInput, listOf(this@ColorPanel,
                                title, si.color, si.hexText, si.hsl))
                        }
                    }
                }
            }
            gbc = gbc(fill = Gbc.HORIZONTAL, insets = Insets(0, 0, 10, 10))
            add(hslPreviousBox, gbc)

            gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
            hslCopyButton = button(layoutConstraint = gbc, text = "C") {
                toolTipText = "Copy"
                addActionListener { event ->
                    copy(hslPreviousBox)
                }

            }
            gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
            hslPasteButton = button(layoutConstraint = gbc, text = "P") {
                toolTipText = "Paste"
                addActionListener { event ->
                    paste(hslPreviousBox)
                }
            }

            gbc = gbc(w = Gbc.REMAINDER, weightx = 1.0, fill = Gbc.BOTH, insets = Insets(15, 0, 0, 10))
            panel(layoutConstraint = gbc) {
                layout = GridBagLayout()

                gbc = gbc(weightx = 1.0, fill = Gbc.NONE, insets = Insets(15, 0, 0, 10))
                suggestButton = button(layoutConstraint = gbc, text = "Suggest") {
                    toolTipText = "Suggest Colors for the opposite side"
                    addActionListener { event ->
                        val candh = rgbPreviousBox.selectedItem as ColorAndHex
                            eventCentral.shoutOut(ColorEvents.Suggest, listOf(this@ColorPanel,
                                title, candh.color, candh.hexText))
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

                        val hexText = colorToHEXString(defaultColor)
                        val candh = ColorAndHex(defaultColor, hexText)
                        commonModel.addElement(candh)

                        hexPreviousBox.selectedItem?.let {
                            eventCentral.shoutOut(ColorEvents.RGBInput, listOf(this@ColorPanel,
                                title, defaultColor, hexText))
                        }
                    }
                }
            }


            eventCentral.addObserver(this, ColorEvents.InitialValues) { event, args ->
                val fg = args[1] as Color
                val bg = args[2] as Color

                val rgbStr = if (isForeground()) colorToRGBString(fg)
                else colorToRGBString(bg)
                val hexStr = if (isForeground()) colorToHEXString(fg)
                else colorToHEXString(bg)

                rgbStringToColorAndHexString(rgbStr)?.let { candh ->
                    commonModel.addElement(candh)
                }

            }
        }
    }
}