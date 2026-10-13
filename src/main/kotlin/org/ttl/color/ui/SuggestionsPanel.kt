package org.ttl.color.ui

import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyles
import org.ttl.color.events.ColorEvents
import org.ttl.color.events.eventCentral
import org.ttl.color.utils.copy
import org.ttl.color.utils.setHEXTextFieldFromColor
import org.ttl.color.utils.setRGBTextFieldFromColor
import sun.tools.jconsole.LabeledComponent.layout
import ttl.kotdsls.swing.Gbc
import ttl.kotdsls.swing.button
import ttl.kotdsls.swing.gbc
import ttl.kotdsls.swing.label
import ttl.kotdsls.swing.textField
import java.awt.Color
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.Locale.getDefault
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField

class SuggestionsPanel(title: String, suggester: (Color) -> Color) : JPanel() {
    lateinit var suggestionRGB: JTextField
    lateinit var suggestionHEX: JTextField
    lateinit var suggestionCopyRGBButton: JButton
    lateinit var suggestionCopyHEXButton: JButton

    init {
        //Suggestions Panel
        layout = GridBagLayout()
        border = BorderFactory.createTitledBorder(title)

//            var gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, anchor = Gbc.CENTER,
//                insets = Insets(0, 0, 10, 0))
//            label(layoutConstraint = gbc, text = "$heading Suggestion")

        var gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 10))
        label(layoutConstraint = gbc, text = "Suggested RGB")
        gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
        suggestionRGB = textField(layoutConstraint = gbc)
        gbc = gbc(w = Gbc.REMAINDER, fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
        suggestionCopyRGBButton = button(layoutConstraint = gbc, text = "C") {
            toolTipText = "Copy"
            addActionListener { event ->
                copy(suggestionRGB)
            }
        }

        gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 10))
        label(layoutConstraint = gbc, text = "Suggested HEX")
        gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
        suggestionHEX = textField(layoutConstraint = gbc)
        gbc = gbc(fill = Gbc.NONE, insets = Insets(0, 0, 10, 0))
        suggestionCopyHEXButton = button(layoutConstraint = gbc, text = "C") {
            toolTipText = "Copy"
            addActionListener { event ->
                copy(suggestionHEX)
            }
        }

        eventCentral.addObserver(this, ColorEvents.Suggest) { event, args ->
            val panelName = args[1] as String
            val color = args[2] as Color
            val hextText = args[3] as String

            //Call the suggester function that is passed in to us in the Constructor.
            val suggestion = suggester(color)

            setRGBTextFieldFromColor(suggestion, suggestionRGB)
            setHEXTextFieldFromColor(suggestion, suggestionHEX)
        }
    }
}
