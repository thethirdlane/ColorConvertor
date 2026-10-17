package org.ttl.color.ui

import org.ttl.color.events.ColorEvents
import org.ttl.color.events.eventCentral
import org.ttl.color.utils.ColorAndHex
import org.ttl.color.utils.colorToRGBString
import org.ttl.color.utils.hexStringToColorAndHex
import org.ttl.color.utils.hslStringToColorAndHex
import org.ttl.color.utils.rgbStringToColorAndHex
import ttl.kotdsls.swing.Gbc
import ttl.kotdsls.swing.frame
import ttl.kotdsls.swing.gbc
import ttl.kotdsls.swing.ui.getMinFontSizeForScreen
import ttl.kotdsls.swing.ui.setUIMinFontSize
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.DefaultListCellRenderer
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JTextField
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource
import javax.swing.plaf.basic.BasicComboBoxEditor
import kotlin.collections.component1
import kotlin.collections.component2


class CombinedComboBoxModel<ColorAndHex>(private val capacity: Int) : DefaultComboBoxModel<ColorAndHex>() {

    override fun addElement(item: ColorAndHex) {
        if (item == null) return

        // Prevent duplicate entries if the item already exists
        val existingIndex = indexOf(item)
        if (existingIndex != -1) {
            // Move existing item to the end (most recent position)
            removeElementAt(existingIndex)
        } else if (size >= capacity) {
            // Remove the oldest item (index 0) if capacity is reached
            removeElementAt(0)
        }

        super.addElement(item)
        selectedItem = item
    }

    private fun indexOf(element: ColorAndHex): Int {
        for (i in 0 until size) {
            if (getElementAt(i) == element) return i
        }
        return -1
    }
}

class HEXCellRenderer : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean,
                                              cellHasFocus: Boolean): Component {
        // Delegate base rendering logic (handles background selection, fonts, focus borders)
        val comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

        if (comp is JLabel && value is ColorAndHex) {
            // Customize text display
            comp.text = value.hexText
        }
        return comp
    }
}

class HEXCellEditor : BasicComboBoxEditor() {
    private var currentItem: ColorAndHex? = null

    override fun getItem(): Any? {
        val hexText = (editor as JTextField).text
        if (!hexText.isNullOrBlank()) {
            val ttu = if(hexText.contains("#")) hexText else "#$hexText"
            val candh = hexStringToColorAndHex(ttu)
            // Return existing user object, or create a new instance if typed text is new
            return candh
        }
        return null
    }

    override fun setItem(candh: Any?) {
        if (candh is ColorAndHex) {
            editor.text = candh.hexText
        } else {
            editor.text = ""
        }
    }
}

class RGBCellEditor : BasicComboBoxEditor() {
    private var currentItem: ColorAndHex? = null

    override fun getItem(): Any? {
        val rgbText = (editor as JTextField).text
        if (!rgbText.isNullOrBlank()) {
            val candh = rgbStringToColorAndHex(rgbText)
            // Return existing user object, or create a new instance if typed text is new
            return candh
        }
        return null
    }

    override fun setItem(candh: Any?) {
        if (candh is ColorAndHex) {
            editor.text = colorToRGBString(candh.color)
        } else {
            editor.text = ""
        }
    }
}

class HSLCellEditor : BasicComboBoxEditor() {
    private var currentItem: ColorAndHex? = null

    override fun getItem(): Any? {
        val hslText = (editor as JTextField).text
        if (!hslText.isNullOrBlank()) {
            val candh = hslStringToColorAndHex(hslText)
            // Return existing user object, or create a new instance if typed text is new
            return candh
        }
        return null
    }

    override fun setItem(candh: Any?) {
        if (candh is ColorAndHex) {
            editor.text = candh.hsl.toText
        } else {
            editor.text = ""
        }
    }
}

class HSLCellRenderer : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean,
                                              cellHasFocus: Boolean): Component {
        // Delegate base rendering logic (handles background selection, fonts, focus borders)
        val comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

        if (comp is JLabel && value is ColorAndHex) {
            // Customize text display
            comp.text = value.hsl.toText
        }
        return comp
    }
}

class RGBCellRenderer : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean,
                                              cellHasFocus: Boolean): Component {
        // Delegate base rendering logic (handles background selection, fonts, focus borders)
        val comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

        if (comp is JLabel && value is ColorAndHex) {
            // Customize text display
            comp.text = "${value.color.red}, ${value.color.green}, ${value.color.blue}, ${value.color.alpha}"
        }
        return comp
    }
}

object TestCombinedComboBoxModel {
    @JvmStatic
    fun main(args: Array<String>) {
        System.setProperty("sun.awt.disablegrab", "true")
        System.setProperty("sun.java2d.opengl", "true");
        val lAndFName = (UIManager.getSystemLookAndFeelClassName())

        UIManager.setLookAndFeel(lAndFName)
//        FlatLaf.updateUI()

//        AP - "scale" fonts
        val minSize = getMinFontSizeForScreen()
        val newFonts = setUIMinFontSize(minSize)
        newFonts.forEach { (k: String?, v: FontUIResource?) -> UIManager.put(k, v) }


        val model = CombinedComboBoxModel<ColorAndHex>(10)

        val comboBox1 = JComboBox(model).apply {
            isEditable = true
            prototypeDisplayValue = ColorAndHex(Color(88, 20, 255), "#5814FFFF")
            renderer = RGBCellRenderer()
            editor = RGBCellEditor()
            addActionListener { event ->
                val i = editor.item
                if (event.actionCommand.startsWith("comboBoxEdited")) {
                    val input = if (event.actionCommand.indexOf(":") != -1)
                        event.actionCommand.removePrefix("comboBoxEdited:")
                    else
                        colorToRGBString((editor.item as ColorAndHex).color)
                    if (input.isNotBlank()) {

                        rgbStringToColorAndHex(input)?.let { candh ->
                            val color = candh.color
                            val hexText = candh.hexText
                            //Add it to our Model
                            (this.model as CombinedComboBoxModel<ColorAndHex>).addElement(candh)
                            //And let the world know that a new item has been added/selected
                            eventCentral.shoutOut(ColorEvents.RGBInput, listOf(this@TestCombinedComboBoxModel,
                                "blah", color, hexText))
                        }
                    }
                }
            }
        }

        val comboBox2 = JComboBox(model).apply {
            isEditable = true
            prototypeDisplayValue = ColorAndHex(Color(88, 20, 255), "#5814FFFF")
            renderer = HEXCellRenderer()
            editor = HEXCellEditor()
            addActionListener { event ->
                if (event.actionCommand.startsWith("comboBoxEdited")) {
                    val input = if (event.actionCommand.indexOf(":") != -1)
                        event.actionCommand.removePrefix("comboBoxEdited:")
                    else
                        (editor.item as ColorAndHex).hexText
                    if (input.isNotBlank()) {

                        hexStringToColorAndHex(input)?.let { candh ->
                            val color = candh.color
                            val hexText = candh.hexText
                            //Add it to our Model
                            (this.model as CombinedComboBoxModel<ColorAndHex>).addElement(candh)
                            //And let the world know that a new item has been added/selected
                            eventCentral.shoutOut(ColorEvents.RGBInput, listOf(this@TestCombinedComboBoxModel,
                                "blah", color, hexText))
                        }
                    }
                }
            }
        }

        var candh = ColorAndHex(Color(88, 20, 255), "#5814FF")
        model.addElement(candh)

        candh = ColorAndHex(Color(38, 38, 38, 255), "#262626FF")
        model.addElement(candh)

        val frame = frame("TestCombinedComboBoxModel") {
            layout = GridBagLayout()

            var gbc = gbc(weightx = 1.0, anchor = Gbc.WEST, fill = Gbc.NONE)
            add(comboBox1, gbc)

            gbc = gbc(weightx = 1.0, anchor = Gbc.EAST, fill = Gbc.NONE)
            add(comboBox2, gbc)
        }

        frame.size = Dimension(600, 500)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
}