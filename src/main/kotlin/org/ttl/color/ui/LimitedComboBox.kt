package org.ttl.color.ui

import java.awt.EventQueue
import java.awt.event.ActionEvent
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

fun <T> JComboBox<T>.postActionEvent(actionCommand: String = "comboBoxEdited") {
    // Construct an ActionEvent with the JComboBox as the source
    val event = ActionEvent(
        this,                           // source
        ActionEvent.ACTION_PERFORMED,       // id
        actionCommand                       // command (e.g. "comboBoxEdited" or "comboBoxChanged")
    )

    // Post the event to the system EventQueue
//    EventQueue.invokeLater {
//        this.dispatchEvent(event)
    this.actionListeners.forEach { it.actionPerformed(event) }
//    java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(event);
//    }
}

class LimitedComboBoxModel<T>(private val capacity: Int) : DefaultComboBoxModel<T>() {

    override fun addElement(item: T?) {
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

    private fun indexOf(element: T): Int {
        for (i in 0 until size) {
            if (getElementAt(i) == element) return i
        }
        return -1
    }
}