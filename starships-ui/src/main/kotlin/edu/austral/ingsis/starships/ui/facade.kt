package edu.austral.ingsis.starships.ui

import javafx.beans.property.BooleanProperty
import javafx.collections.ObservableMap
import javafx.scene.Parent

class ElementsViewFacade(private val imageResolver: ImageResolver) {
    private val timeEmitter = ListenableEmitter<TimePassed>()
    private val collisionsEmitter = ListenableEmitter<Collision>()

    private val internalView = ElementsView(imageResolver)

    private val timePassedTimer = TimePassedTimer(timeEmitter)
    private val collisionTimer = CollisionTimer(collisionsEmitter, internalView)

    val elements: ObservableMap<String, ElementModel> = internalView.elements
    val showCollider: BooleanProperty = internalView.showCollider
    val showGrid: BooleanProperty = internalView.showGrid
    val view: Parent = internalView

    val timeListenable: Listenable<TimePassed> = timeEmitter
    val collisionsListenable: Listenable<Collision> = collisionsEmitter

    fun start() {
        timePassedTimer.start()
        collisionTimer.start()
    }

    fun stop() {
        timePassedTimer.stop()
        collisionTimer.stop()
    }
}
