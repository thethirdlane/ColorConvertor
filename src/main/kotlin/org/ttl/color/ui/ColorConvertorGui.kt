package org.ttl.color.ui

import com.formdev.flatlaf.FlatLaf
import org.ttl.color.RB
import org.ttl.color.themes.ThemeManager
import org.ttl.color.themes.themeManager
import org.ttl.color.utils.ColorPaletteGenerator
import ttl.kotdsls.swing.Gbc
import ttl.kotdsls.swing.button
import ttl.kotdsls.swing.gbc
import ttl.kotdsls.swing.label
import ttl.kotdsls.swing.panel
import ttl.kotdsls.swing.textArea
import ttl.kotdsls.swing.textField
import ttl.kotdsls.swing.ui.getMinFontSizeForScreen
import ttl.kotdsls.swing.ui.setUIMinFontSize
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagLayout
import java.awt.Insets
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Properties
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource
import kotlin.collections.component1
import kotlin.collections.component2

class ColorConvertorGui(title: String) : JFrame(title) {

    val iconFile = "rgb.png"

    var foregroundPanel: JPanel

    lateinit var displayPanel: JPanel
    lateinit var displayfgText: JTextArea

    lateinit var complementarySuggestion: SuggestionsPanel
    lateinit var analagousSuggestion: SuggestionsPanel
    lateinit var monoChromaticSuggestion: SuggestionsPanel

    var backgroundPanel: JPanel
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
//            gbc = gbc(fill = Gbc.NONE)
        //Foreground Panel
        foregroundPanel = ColorPanel("Choose Foreground")
        add(foregroundPanel, gbc)

        //Display Panel
        gbc = gbc(weightx = 1.0, weighty = 1.0, fill = Gbc.BOTH)
        panel(layoutConstraint = gbc) {
            layout = GridBagLayout()
            val mb = BorderFactory.createMatteBorder(2, 1, 1, 1, foreground)
            border = BorderFactory.createTitledBorder(mb, "Display Panel")
            minimumSize = Dimension(500, 400)
            gbc = gbc(weightx = 1.0, weighty = 1.0, fill = Gbc.BOTH)
            displayPanel = panel(layoutConstraint = gbc) {
                layout = GridBagLayout()

                gbc = gbc(fill = Gbc.NONE)
//                displayfgText = textField(layoutConstraint = gbc, text = "A lazy cow sidestepped the moon.").apply {
                displayfgText = textArea(layoutConstraint = gbc, text = "A lazy cow sidestepped the moon.").apply {
                    columns = 20
                    rows = 5
                    border = BorderFactory.createEmptyBorder()
                    isOpaque = false
                    font = Font("Helvitica", Font.PLAIN, 28)
                }
            }
        }

        //Background Panel
        gbc = gbc(w = Gbc.REMAINDER, weightx = 1.0, weighty = 1.0, fill = Gbc.BOTH, insets = Insets(0, 10, 0, 10))
        backgroundPanel = ColorPanel("Choose Background")
        add(backgroundPanel, gbc)

        gbc = gbc(w = Gbc.REMAINDER, weightx = 1.0, weighty = 1.0, fill = Gbc.BOTH,
            insets = Insets(10, 0, 0, 0))
        panel(layoutConstraint = gbc) {
            layout = GridBagLayout()
            border = BorderFactory.createTitledBorder("Suggestions")
            complementarySuggestion =
                SuggestionsPanel("Complementary", ColorPaletteGenerator::getComplementarySuggestion)
            gbc = gbc(weightx = 1.0, weighty = 1.0, fill = Gbc.BOTH)
            add(complementarySuggestion, gbc)

            analagousSuggestion = SuggestionsPanel("Analogous", ColorPaletteGenerator::getAnalogousSuggestion)
            add(analagousSuggestion, gbc)

            monoChromaticSuggestion =
                SuggestionsPanel("MonoChromatic", ColorPaletteGenerator::getMonochromaticSuggestion)
            add(monoChromaticSuggestion, gbc)
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

object TestColorConvertorGui {
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

//        val cc = frame("Suggestions Check") {
//            layout = GridBagLayout()
//            val complementarySuggestions = SuggestionsPanel("Complementary")
//            var gbc = gbc(x = 1, weighty = 1.0, fill = Gbc.BOTH)
//            add(complementarySuggestions, gbc)
//        }
        val cc = ColorConvertorGui("Test Color Convertor Gui")
//
        cc.size = Dimension(1500, 800)
        cc.setLocationRelativeTo(null)
        cc.isVisible = true
    }

}
