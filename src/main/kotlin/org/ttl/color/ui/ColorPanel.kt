package org.ttl.color.ui

import org.ttl.color.events.ColorEvents
import org.ttl.color.events.eventCentral
import org.ttl.color.utils.colorToHEXString
import org.ttl.color.utils.colorToRGBString
import org.ttl.color.utils.copy
import org.ttl.color.utils.hexStringToColorAndHexString
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

    lateinit var hexCopyButton: JButton
    lateinit var hexPasteButton: JButton
    lateinit var suggestButton: JButton
    lateinit var lAndFButton: JButton

    lateinit var rgbPreviousBox: JComboBox<String>
    lateinit var hexPreviousBox: JComboBox<String>

    var internalSetting = false
//    val hexPrevious: JComboBox<String>

    companion object {
        var comboBoxLimit = 5
    }

    fun isForeground() = (title.lowercase(getDefault()).contains("foreground"))

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
            rgbPreviousBox = JComboBox<String>().apply {
                isEditable = true
                prototypeDisplayValue = ("200, 200, 200, 200 ")
                model = LimitedComboBoxModel<String>(comboBoxLimit)
                addActionListener { event ->
                    val source = event.source as JComboBox<*>
                    // Check if action was triggered by user input (e.g. typing and pressing Enter)
//                    if (event.actionCommand == "comboBoxEdited" || event.actionCommand == "comboBoxChanged") {
//                    if (event.actionCommand == "comboBoxEdited" ) {
                    //If the user entered new text, we need to add it to this
                    //ComboBox and to the other one.
                    //The actionCommand will be appended by ":paste_data" if
                    //the event was triggered by a Paste.
                    if (event.actionCommand.startsWith("comboBoxEdited")) {
                        val input = if (event.actionCommand.indexOf(":") != -1)
                            event.actionCommand.removePrefix("comboBoxEdited:")
                        else
                            editor.item?.toString()?.trim()
                        if (!input.isNullOrBlank()) {
                            val p = rgbStringToColorAndHexString(input)
                            p?.let {
                                val color = p.color
                                val hexText = p.hexText
                                //Add it to our Model
                                (this.model as LimitedComboBoxModel<String>).addElement(input)
                                hexPreviousBox.addItem(hexText)
                                //And let the world know that a new item has been added/selected
                                eventCentral.shoutOut(ColorEvents.RGBInput, listOf(this@ColorPanel,
                                    title, color, hexText))
                            }
                        }
                    } else if (event.actionCommand == "comboBoxChanged") {
                        //The user selected a different item, so we
                        //synchronous our list with the other ComboBox
                        //and let the world know that a new item has been selected.
                        val ourIndex = rgbPreviousBox.selectedIndex
                        if (ourIndex >= 0 && ourIndex < hexPreviousBox.itemCount) {
                            hexPreviousBox.selectedIndex = ourIndex
                            rgbPreviousBox.selectedItem?.let { item ->
                                rgbStringToColorAndHexString(item.toString())?.let { p ->
                                    eventCentral.shoutOut(ColorEvents.RGBInput, listOf(this@ColorPanel,
                                        title, p.color, p.hexText))
                                }
                            }
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
            hexPreviousBox = JComboBox<String>().apply {
                isEditable = true
                prototypeDisplayValue = ("200, 200, 200, 200 ")
                model = LimitedComboBoxModel<String>(comboBoxLimit)
                addActionListener { event ->
                    val source = event.source as JComboBox<*>
                    // Check if action was triggered by user input (e.g. typing and pressing Enter)
//                    if (event.actionCommand == "comboBoxEdited" || event.actionCommand == "comboBoxChanged") {
//                    if (event.actionCommand == "comboBoxEdited" ||
                    //If the user entered new text, we need to add it to this
                    //ComboBox and to the other one.
                    //The actionCommand will be appended by ":paste_data" if
                    //the event was triggered by a Paste.
                    if (event.actionCommand.startsWith("comboBoxEdited")) {
                        val input = if (event.actionCommand.indexOf(":") != -1)
                            event.actionCommand.removePrefix("comboBoxEdited:")
                        else
                            editor.item?.toString()?.trim()
                        if (!input.isNullOrBlank()) {

                            hexStringToColorAndHexString(input)?.let { p ->
                                val color = p.color
                                val hexText = p.hexText
                                val colorStr = colorToRGBString(color)
                                //Add the new item to our model
                                (this.model as LimitedComboBoxModel<String>).addElement(input)
                                //And to the other Box
                                rgbPreviousBox.addItem(colorStr)

                                //And let the world know that we have new input.
                                eventCentral.shoutOut(ColorEvents.HEXInput, listOf(this@ColorPanel,
                                    title, color, hexText))

                            }
                        }
                    } else if (event.actionCommand == "comboBoxChanged") {
                        //The user selected a different item, so we
                        //synchronous our list with the other ComboBox
                        //and let the world know that a new item has been selected.
                        val ourIndex = hexPreviousBox.selectedIndex
                        if (ourIndex >= 0 && ourIndex < rgbPreviousBox.itemCount) {
                            rgbPreviousBox.selectedIndex = ourIndex
                            hexPreviousBox.selectedItem?.let { item ->
                                hexStringToColorAndHexString(item.toString())?.let { p ->
                                    eventCentral.shoutOut(ColorEvents.HEXInput, listOf(this@ColorPanel,
                                        title, p.color, p.hexText))
                                }
                            }
                        }
                    }
                }
            }
            gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 10))
            add(hexPreviousBox, gbc)


            gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 0, 0))
            hexCopyButton = button(layoutConstraint = gbc, text = "C") {
                toolTipText = "Copy"
                addActionListener { event ->
                    copy(hexPreviousBox)
                }

            }
            gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, insets = Insets(0, 0, 0, 0))
            hexPasteButton = button(layoutConstraint = gbc, text = "P") {
                toolTipText = "Paste"
                addActionListener { event ->
                    paste(hexPreviousBox)
                }
            }

            gbc = gbc(w = Gbc.REMAINDER, weightx = 1.0, fill = Gbc.BOTH, insets = Insets(15, 0, 0, 10))
            panel(layoutConstraint = gbc) {
                layout = GridBagLayout()

                gbc = gbc(weightx = 1.0, fill = Gbc.NONE, insets = Insets(15, 0, 0, 10))
                suggestButton = button(layoutConstraint = gbc, text = "Suggest") {
                    toolTipText = "Suggest Colors for the opposite side"
                    addActionListener { event ->
                        rgbPreviousBox.selectedItem?.let {
                            rgbStringToColorAndHexString(it.toString())?.let {
                                val color = it.color
                                val hexText = it.hexText
                                eventCentral.shoutOut(ColorEvents.Suggest, listOf(this@ColorPanel,
                                    title, color, hexText))
                            }
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

                        val rgbStr = colorToRGBString(defaultColor)
                        rgbPreviousBox.addItem(rgbStr)
                        val hexStr = colorToHEXString(defaultColor)
                        hexPreviousBox.addItem(hexStr)

                        hexPreviousBox.selectedItem?.let {
                            eventCentral.shoutOut(ColorEvents.RGBInput, listOf(this@ColorPanel,
                                title, defaultColor, it.toString()))
                        }
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

            rgbPreviousBox.addItem(rgbStr)
            hexPreviousBox.addItem(hexStr)
        }
    }
}