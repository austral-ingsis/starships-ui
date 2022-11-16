package edu.austral.ingsis.starships.ui

import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

class KeyTracker() {
    var scene: Scene? = null
    private val keySet: MutableSet<KeyCode> = HashSet()


    private val keyPressedEmitter = ListenableEmitter<KeyPressed>()
    private val keyReleasedEmitter = ListenableEmitter<KeyReleased>()

    val keyPressedListenable: Listenable<KeyPressed> = keyPressedEmitter
    val keyReleasedListenable: Listenable<KeyReleased> = keyReleasedEmitter

    fun start() {
        scene?.onKeyPressed = EventHandler { event: KeyEvent -> handleKeyPressed(event) }
        scene?.onKeyReleased = EventHandler { event: KeyEvent -> handleKeyReleased(event) }
    }

    fun stop() {
        scene?.removeEventHandler(KeyEvent.KEY_PRESSED) { event: KeyEvent -> handleKeyPressed(event) }
        scene?.removeEventHandler(KeyEvent.KEY_RELEASED) { event: KeyEvent -> handleKeyReleased(event) }
    }

    private fun handleKeyPressed(event: KeyEvent) {
        keySet.add(event.code)
        keyPressedEmitter.emit(KeyPressed(event.code, keySet.toSet()))
    }

    private fun handleKeyReleased(event: KeyEvent) {
        keySet.remove(event.code)
        keyReleasedEmitter.emit(KeyReleased(event.code, keySet.toSet()))
    }
}