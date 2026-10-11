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

import com.formdev.flatlaf.FlatLaf
import org.slf4j.LoggerFactory
import org.ttl.events.ColorEvents
import org.ttl.events.eventCentral
import ttl.kotdsls.swing.Gbc
import ttl.kotdsls.swing.button
import ttl.kotdsls.swing.gbc
import ttl.kotdsls.swing.label
import ttl.kotdsls.swing.panel
import ttl.kotdsls.swing.textField
import ttl.kotdsls.swing.ui.getMinFontSizeForScreen
import ttl.kotdsls.swing.ui.setUIMinFontSize
import org.ttl.themes.ThemeManager
import org.ttl.themes.themeManager
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.Locale.getDefault
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource

var RB: Properties = Properties()

class ColorConvertorGui(title: String) : JFrame(title) {

    val mainPanel: JPanel

    val iconFile = "rgb.png"

    lateinit var foregroundPanel: JPanel
    lateinit var foregroundRGB: JTextField
    lateinit var foregroundHEX: JTextField
    lateinit var fgRGBCopyButton: JButton
    lateinit var fgRGBPasteButton: JButton
    lateinit var fgHEXCopyButton: JButton
    lateinit var fgHEXPasteButton: JButton

    lateinit var displayPanel: JPanel
    lateinit var displayfgPanel: JPanel
    lateinit var displayfgText: JTextField

    lateinit var backgroundPanel: JPanel
    lateinit var backgroundRGB: JTextField
    lateinit var backgroundHEX: JTextField
    lateinit var bgRGBCopyButton: JButton
    lateinit var bgRGBPasteButton: JButton
    lateinit var bgHEXCopyButton: JButton
    lateinit var bgHEXPasteButton: JButton
    val themeBox: JComboBox<ThemeManager.ThemeOption>

    init {
        //Make the icon for the app
        val iconURL = this.javaClass.classLoader.getResource(iconFile)
        if (iconURL != null) {
            val appIcon = ImageIcon(iconURL).image
            this.iconImage = appIcon
        }
        initTheme()
        themeBox = themeManager.createThemeComboBox()
        val menuBar = JMenuBar()
        menuBar.add(themeBox)
        this.jMenuBar = menuBar

        layout = GridBagLayout()

        var gbc = gbc(weightx = 1.0, weighty = 1.0, fill = Gbc.BOTH, insets = Insets(10, 10, 10, 10))
        mainPanel = panel(layoutConstraint = gbc) {
            layout = GridBagLayout()

            //Foreground Panel
            gbc = gbc(weightx = 1.0, weighty = 1.0, fill = Gbc.BOTH, insets = Insets(0, 10, 0, 10))
//            gbc = gbc(fill = Gbc.NONE)
            foregroundPanel = panel(layoutConstraint = gbc) {
                layout = GridBagLayout()

                //Title Label
                gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, anchor = Gbc.CENTER,
                    insets = Insets(0, 0, 10, 0))
                label(layoutConstraint = gbc, text = "Choose Foreground")


                gbc = gbc(fill = Gbc.NONE, anchor = Gbc.EAST, insets = Insets(0, 0, 10, 10))
                label(layoutConstraint = gbc, text = "RGB[A]:")
                gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 10))
                foregroundRGB = textField(layoutConstraint = gbc)
                gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
                fgRGBCopyButton = button(layoutConstraint = gbc, text = "C") {
                    toolTipText = "Copy"
                }
                gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
                fgRGBPasteButton = button(layoutConstraint = gbc, text = "P") {
                    toolTipText = "Paste"
                }

                gbc = gbc(fill = Gbc.NONE, anchor = Gbc.EAST, insets = Insets(0, 0, 0, 10))
                label(layoutConstraint = gbc, text = "HEX:")
                gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 0, 10))
                foregroundHEX = textField(layoutConstraint = gbc)
                gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 0, 0))
                fgHEXCopyButton = button(layoutConstraint = gbc, text = "C") {
                    toolTipText = "Copy"
                }
                gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, insets = Insets(0, 0, 0, 0))
                fgHEXPasteButton = button(layoutConstraint = gbc, text = "P") {
                    toolTipText = "Paste"
                }

            }
        }
        //Display Panel
        gbc = gbc(weightx = 1.0, weighty = 1.0, fill = Gbc.BOTH)
        panel(layoutConstraint = gbc) {
            layout = GridBagLayout()
            val mb = BorderFactory.createMatteBorder(2, 1, 1, 1, foreground)
            border = BorderFactory.createTitledBorder(mb, "Display Panel")
            minimumSize = Dimension(500, 400)
//        gbc = gbc(weightx = 1.0, weighty = 1.0, fill = Gbc.BOTH)
            displayPanel = panel(layoutConstraint = gbc) {
                layout = GridBagLayout()

                gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE)
                displayfgText = textField(layoutConstraint = gbc, text = "A lazy cow sidestepped the moon.").apply {
                    border = BorderFactory.createEmptyBorder()
                    background = Color(0, 0, 0, 0)
                    columns = 20
                    font = Font("Helvitica", Font.PLAIN, 28)
                }
            }
        }

        //Background Panel
        gbc = gbc(weightx = 1.0, weighty = 1.0, fill = Gbc.BOTH, insets = Insets(0, 10, 0, 10))
        backgroundPanel = panel(layoutConstraint = gbc) {
            layout = GridBagLayout()

            //Title Label
            gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, anchor = Gbc.CENTER,
                insets = Insets(0, 0, 10, 0))
            label(layoutConstraint = gbc, text = "Choose Background")


            gbc = gbc(fill = Gbc.NONE, anchor = Gbc.EAST, insets = Insets(0, 0, 10, 10))
            label(layoutConstraint = gbc, text = "RGB[A]:")
            gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 10))
            backgroundRGB = textField(layoutConstraint = gbc)
            gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
            bgRGBCopyButton = button(layoutConstraint = gbc, text = "C") {
                toolTipText = "Copy"
            }

            gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
            bgRGBPasteButton = button(layoutConstraint = gbc, text = "P") {
                toolTipText = "Paste"
            }


            gbc = gbc(fill = Gbc.NONE, anchor = Gbc.EAST, insets = Insets(0, 0, 0, 10))
            label(layoutConstraint = gbc, text = "HEX:")
            gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 0, 10))
            backgroundHEX = textField(layoutConstraint = gbc)
            gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 0, 0))
            bgHEXCopyButton = button(layoutConstraint = gbc, text = "C") {
                toolTipText = "Copy"
            }

            gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, insets = Insets(0, 0, 0, 0))
            bgHEXPasteButton = button(layoutConstraint = gbc, text = "P") {
                toolTipText = "Paste"
            }
        }
    }

    fun initTheme() {
        //So that graalvm is happy
        val userHome = System.getProperty("user.home")
        val userDir = System.getProperty("user.dir")
        if (System.getProperty("java.home") == null) {
            System.setProperty("java.home", userDir)
        }
        //Now we are using FlatLaf.  A great discovery
        System.setProperty("flatlaf.uiScale", "2.0")

        //Look for properties files
        var propFileDir = "$userHome/.anaclock"

        val propFileName = System.getProperty("colorconvertor.properties") ?: "$propFileDir/colorconvertor.properties"

        RB = loadProps(userHome, propFileName)

        //The LookAndFeel will be set up in the
        //ThemManager constructor
        themeManager = ThemeManager(RB)
        val lAndFName = themeManager.currentTheme.className

        FlatLaf.registerCustomDefaultsSource("themes")
        FlatLaf.registerCustomDefaultsSource(File(propFileDir))
        UIManager.setLookAndFeel(lAndFName)
        FlatLaf.updateUI()

    }


    fun loadProps(directory: String,
                  propFileName: String = "${directory}/.colorconvertor/colorconvertor.properties"): Properties {
        val props = Properties()
        //Look for properties files
        //Can override normal location with -D otherlocation at runtime.
        val fileToUse = System.getProperty("whirltime.properties") ?: propFileName

        if (Files.exists(Paths.get(fileToUse))) {
//            val p = Properties(baseProps)

            props.load(FileReader(fileToUse))
        }

        return props
    }
}

class ColorConvertor {
    val gui = ColorConvertorGui("Color Convertor")

    companion object {
        val logger = LoggerFactory.getLogger(ColorConvertor::class.java)
    }

    init {
        gui.foregroundRGB.addActionListener { event ->
            handleRGBClicked(true)
        }

        gui.fgRGBCopyButton.addActionListener { event ->
//            handleRGBClicked(true)
            copy(gui.foregroundRGB)
        }
        gui.fgRGBPasteButton.addActionListener { event ->
//            handleRGBClicked(true)
            paste(gui.foregroundRGB)
        }

        gui.foregroundHEX.addActionListener { event ->
            handleHEXClicked(true)
        }

        gui.fgHEXCopyButton.addActionListener { event ->
//            handleHEXClicked(true)
            copy(gui.foregroundHEX)
        }

        gui.fgHEXPasteButton.addActionListener { event ->
//            handleRGBClicked(true)
            paste(gui.foregroundHEX)
        }

        gui.backgroundRGB.addActionListener { event ->
            handleRGBClicked(false)
        }

        gui.bgRGBCopyButton.addActionListener { event ->
            copy(gui.backgroundRGB)
        }

        gui.bgRGBPasteButton.addActionListener { event ->
            paste(gui.backgroundRGB)
        }

        gui.backgroundHEX.addActionListener { event ->
            handleHEXClicked(false)
        }

        gui.bgHEXCopyButton.addActionListener { event ->
//            handleHEXClicked(false)
            copy(gui.backgroundHEX)
        }

        gui.bgHEXPasteButton.addActionListener { event ->
//            handleHEXClicked(false)
            paste(gui.backgroundHEX)
        }

        setInitialValues()
        gui.defaultCloseOperation = JFrame.EXIT_ON_CLOSE


        eventCentral.addObserver(this, ColorEvents.ThemeChanged) { _, args ->
            setInitialValues()
        }
    }

    fun show() {
//        gui.size = Dimension(1500, 400)
        gui.pack()
        gui.isVisible = true
    }

    fun setInitialValues() {
        val bg = gui.displayPanel.background
        gui.backgroundRGB.text = "${bg.red}, ${bg.green}, ${bg.blue}, ${bg.alpha}"
        gui.backgroundHEX.text = String.format("#%02X%02X%02X%02X",
            bg.red, bg.green, bg.blue, bg.alpha)

        val fg = gui.displayfgText.foreground
        gui.foregroundRGB.text = "${fg.red}, ${fg.green}, ${fg.blue}, ${fg.alpha}"
        gui.foregroundHEX.text = String.format("#%02X%02X%02X%02X", fg.red, fg.green, fg.blue, fg.alpha)

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
    }

    fun handleHEXClicked(doingForeground: Boolean) {
        try {
            var input = if (doingForeground) gui.foregroundHEX else gui.backgroundHEX

            var hexText = input.text.uppercase(getDefault())
            if (hexText.startsWith("#")) {
                hexText = hexText.substring(1)
            }
            val regex = "(..)".toRegex()

            val matches = regex.findAll(hexText).toList()
//            println("matches: $matches")
//            matches.forEach { println(it.value) }

            if (matches.size < 3 || matches.size > 4) {
                println("WTF")
            } else {
                val color = if (matches.size == 3) {
                    Color(matches.get(0).value.hexToInt(),
                        matches.get(1).value.hexToInt(),
                        matches.get(2).value.hexToInt())
                } else {
                    Color(matches.get(0).value.hexToInt(), matches.get(1).value.hexToInt(),
                        matches.get(2).value.hexToInt(), matches.get(3).value.hexToInt())
                }


                val rgbOutput = if (doingForeground) gui.foregroundRGB else gui.backgroundRGB
                rgbOutput.text = "${color.red}, ${color.green}, ${color.blue}, ${color.alpha}"
                val hexOutput = if (doingForeground) gui.foregroundHEX else gui.backgroundHEX
                hexOutput.text = "#$hexText"

                if (doingForeground) {
                    gui.displayfgText.foreground = color
                } else {
                    gui.displayPanel.background = color
                }
            }
            gui.repaint()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handleRGBClicked(doingForeground: Boolean) {
        try {
            val input = if (doingForeground) gui.foregroundRGB.text else gui.backgroundRGB.text
            val arr = input.split(",\\s*".toRegex()).toTypedArray()
            if (arr.size < 3 || arr.size > 4) {
                logger.error("Input should be 3 or 4 comma seperated RGB[A] between 0 and 255")
                return
            }

            val color = if (arr.size == 3) {
                Color(arr[0].trim().toInt(), arr[1].trim().toInt(), arr[2].trim().toInt())
            } else {
                Color(arr[0].trim().toInt(), arr[1].trim().toInt(), arr[2].trim().toInt(), arr[3].trim().toInt())
            }

            val hex = if (arr.size == 3) {
                String.format("#%02X%02X%02X", arr[0].trim().toInt(), arr[1].trim().toInt(), arr[2].trim().toInt())
            } else {
                String.format("#%02X%02X%02X%02X", arr[0].trim().toInt(), arr[1].trim().toInt(), arr[2].trim().toInt(), arr[3].trim().toInt())
            }

            val output = if (doingForeground) gui.foregroundHEX else gui.backgroundHEX
            output.text = hex

            if (doingForeground) {
                gui.displayfgText.foreground = color
            } else {
                gui.displayPanel.background = color
            }
            gui.repaint()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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