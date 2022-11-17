package edu.austral.ingsis.starships.ui

import javafx.scene.input.KeyCode

interface EventListener<E> {
    fun handle(event: E)
}

data class KeyPressed(val key: KeyCode, val currentPressedKeys: Set<KeyCode>)

data class KeyReleased(val key: KeyCode, val currentPressedKeys: Set<KeyCode>)

data class TimePassed(val currentTimeInSeconds: Double, val secondsSinceLastTime: Double)

data class OutOfBounds(val id: String)

data class ReachBounds(val id: String)

interface EventEmitter<E> {
    fun emit(event: E)
}

interface Listenable<E> {
    fun addEventListener(listener: EventListener<E>)

    fun removeEventListener(listener: EventListener<E>)
}

class ListenableEmitter<E> : EventEmitter<E>, Listenable<E> {
    private val listeners = mutableListOf<EventListener<E>>()

    override fun addEventListener(listener: EventListener<E>) {
        listeners.add(listener)
    }

    override fun removeEventListener(listener: EventListener<E>) {
        listeners.remove(listener)
    }

    override fun emit(event: E) = listeners.forEach { it.handle(event) }
}
