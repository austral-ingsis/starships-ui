package edu.austral.ingsis.starships.ui

import javafx.beans.property.BooleanProperty
import javafx.collections.ObservableMap
import javafx.scene.Parent

class ElementsViewFacade(imageResolver: ImageResolver) {
    private val timeEmitter = ListenableEmitter<TimePassed>()
    private val collisionsEmitter = ListenableEmitter<Collision>()
    private val outOfBoundsEmitter = ListenableEmitter<OutOfBounds>()
    private val reachBoundsEmitter = ListenableEmitter<ReachBounds>()

    private val internalView = ElementsView(imageResolver)

    private val timePassedTimer = TimePassedTimer(timeEmitter)
    private val collisionTimer = ViewEventsTime.createCollisionTimer(collisionsEmitter, internalView)
    private val outOfBoundsTimer = ViewEventsTime.createOutOfBoundsTimer(outOfBoundsEmitter, internalView)
    private val reachBoundsTimer = ViewEventsTime.createReachBoundsTimer(reachBoundsEmitter, internalView)

    val elements: ObservableMap<String, ElementModel> = internalView.elements
    val showCollider: BooleanProperty = internalView.showCollider
    val showGrid: BooleanProperty = internalView.showGrid
    val view: Parent = internalView

    val timeListenable: Listenable<TimePassed> = timeEmitter
    val collisionsListenable: Listenable<Collision> = collisionsEmitter
    val outOfBoundsListenable: Listenable<OutOfBounds> = outOfBoundsEmitter
    val reachBoundsListenable: Listenable<ReachBounds> = reachBoundsEmitter

    fun start() {
        timePassedTimer.start()
        collisionTimer.start()
        outOfBoundsTimer.start()
        reachBoundsTimer.start()
    }

    fun stop() {
        timePassedTimer.stop()
        collisionTimer.stop()
        outOfBoundsTimer.stop()
        reachBoundsTimer.stop()
    }
}
