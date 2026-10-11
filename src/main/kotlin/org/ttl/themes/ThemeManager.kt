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
//@file:JvmName("ThemeManager")

package org.ttl.themes

import com.formdev.flatlaf.FlatDarculaLaf
import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatIntelliJLaf
import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.FlatLightLaf
import org.ttl.events.ColorEvents
import org.ttl.events.eventCentral
import ttl.kotdsls.swing.Gbc
import ttl.kotdsls.swing.button
import ttl.kotdsls.swing.dialog
import ttl.kotdsls.swing.frame
import ttl.kotdsls.swing.gbc
import ttl.kotdsls.swing.label
import java.awt.Component
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.*
import javax.swing.DefaultListCellRenderer
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.UIManager
import kotlin.collections.find

lateinit var themeManager: ThemeManager

class ThemeManager(val properties: Properties? = null) {
    // Data class to represent a theme choice
    data class ThemeOption(val name: String, val className: String) {
        override fun toString(): String = name
    }

    val themes: Array<ThemeOption>

    val defaultTheme: ThemeOption

    var currentTheme: ThemeOption

    init {
        val availableThemes = mutableListOf<ThemeOption>(
            ThemeOption("Flat Light", FlatLightLaf::class.java.name),
            ThemeOption("Flat Dark", FlatDarkLaf::class.java.name),
            ThemeOption("Flat IntelliJ", FlatIntelliJLaf::class.java.name),
            ThemeOption("Flat Darcula", FlatDarculaLaf::class.java.name),
            ThemeOption("SystemDefault", FlatDarculaLaf::class.java.name)
        )
        UIManager.getInstalledLookAndFeels().forEach {
           availableThemes.add(ThemeOption(it.name, it.className))
        }

        themes = availableThemes.toTypedArray()
        defaultTheme = themes[0]

        currentTheme = getStartingTheme()
    }

    //   @JvmStatic
    fun isDark(): Boolean {
        return currentTheme.className.contains("Dark") or
                currentTheme.className.contains("Darc")
    }

    private fun getStartingTheme(): ThemeOption {
        val result = properties?.let { props ->
            val lAndFName = props["theme"] as String?
            lAndFName?.let {
                val lc = lAndFName.lowercase()
                val result = themes.find { theme ->
                    theme.className.lowercase().contains(lc)
                }
                result
            }
        } ?: defaultTheme

        return result
    }


    /**
     * Creates and returns a JComboBox pre-populated with core FlatLaf themes,
     * complete with a selection listener that updates the UI globally.
     */
//   @JvmStatic
    fun createThemeComboBox(): JComboBox<ThemeOption> {

        val comboBox = JComboBox(themes)

        comboBox.renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean
            ): Component {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                if (value is ThemeOption) {
                    text = value.name
                }
                return this
            }
        }


        comboBox.addActionListener {
            val selectedTheme = comboBox.selectedItem as? ThemeOption
            if (selectedTheme != null) {
                try {
                    UIManager.setLookAndFeel(selectedTheme.className)
                    FlatLaf.updateUI() // Refreshes all open frames instantly

                    eventCentral.shoutOut(ColorEvents.ThemeChanged, listOf(this, currentTheme, selectedTheme))

                    currentTheme = selectedTheme
                } catch (ex: Exception) {
                    JOptionPane.showMessageDialog(
                        comboBox,
                        "Failed to change theme: ${ex.message}",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            }
        }

        //Set current item based on currentTheme
        comboBox.selectedItem = currentTheme

        return comboBox
    }

    //   @JvmStatic
    fun createThemeDialog(): JDialog {
        val dialog = dialog(label = "Theme Chooser", parent = null) {
            layout = GridBagLayout()

            var gbc = gbc(insets = Insets(0, 0, 0, 10))
            label(layoutConstraint = gbc, text = "Choose theme:")

            gbc = gbc(w = Gbc.REMAINDER, weightx = 1.0)
            val themeBox = createThemeComboBox()
            add(themeBox, gbc)

            gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.HORIZONTAL, insets = Insets(20, 0, 0, 0))
            button(layoutConstraint = gbc, text = "OK") {
                addActionListener { event ->
                    this@dialog.isVisible = false
                }
            }
        }

        dialog.pack()

        return dialog

    }
}

object TestThemeManager {
    @JvmStatic
    lateinit var themeBox: JComboBox<ThemeManager.ThemeOption>

    @JvmStatic
    fun main(args: Array<String>) {

        FlatLaf.registerCustomDefaultsSource("themes");

        val themeManager = ThemeManager()

        var dialog: JDialog? = null
        val frame = frame(name = "ThemeChecker") {
            layout = GridBagLayout()
            themeBox = themeManager.createThemeComboBox()

            var gbc = gbc(w = Gbc.REMAINDER)
            add(themeBox, gbc)

            gbc = gbc(w = Gbc.REMAINDER)
            button(layoutConstraint = gbc, text = "Theme Dialog") {
                addActionListener { event ->
                    if (dialog == null) {
                        dialog = themeManager.createThemeDialog()
                    }
                    dialog.isVisible = true
                }
            }
        }

        val currentTheme = (themeBox.selectedItem as ThemeManager.ThemeOption).className
        UIManager.setLookAndFeel(currentTheme)
        FlatLaf.updateUI() // Refreshes all open frames instantly

        frame.setSize(800, 600)
        frame.isVisible = true

        printAvailableFlatLafThemes()
    }

    fun printAvailableFlatLafThemes() {
        println("Available FlatLaf Themes:")
        for (info in UIManager.getInstalledLookAndFeels()) {
            // Filter for FlatLaf classes
            print("- Name: " + info.getName() + " | Class: " + info.getClassName())
            if (info.getClassName().startsWith("com.formdev.flatlaf.")) {
                println(": Is Flat")
            } else {
                println()
            }
        }
    }
}