package edu.austral.ingsis.starships.ui

import javafx.animation.AnimationTimer


class TimePassedTimer(private val emitter: EventEmitter<TimePassed>) : AnimationTimer() {
    private var lastTimeInSeconds = 0.0

    override fun handle(now: Long) {
        val nowSeconds = now.toDouble() / 1_000_000_000.0
        val secondsSinceLastTime: Double = if (lastTimeInSeconds == 0.0) 0.0 else nowSeconds - lastTimeInSeconds
        lastTimeInSeconds = nowSeconds

        emitter.emit(TimePassed(nowSeconds, secondsSinceLastTime))
    }
}

class ViewEventsTime<E>(
    private val rawEventGenerator: (ElementsView) -> Set<E>,
    private val emitter: EventEmitter<E>,
    private val view: ElementsView
) : AnimationTimer() {
    private val eventSet = mutableSetOf<E>()

    override fun handle(now: Long) {
        val currentEvents = rawEventGenerator(view)

        val newEvents = currentEvents.filterNot { eventSet.contains(it) }
        val eventsToRemove = eventSet.toSet().filterNot { currentEvents.contains(it) }

        eventSet.removeAll(eventsToRemove.toSet())
        eventSet.addAll(newEvents)

        newEvents.forEach { emitter.emit(it) }
    }

    companion object {
        fun createCollisionTimer(emitter: EventEmitter<Collision>, view: ElementsView): ViewEventsTime<Collision> =
            ViewEventsTime({ it.checkCollisions() }, emitter, view)

        fun createOutOfBoundsTimer(emitter: EventEmitter<OutOfBounds>, view: ElementsView): ViewEventsTime<OutOfBounds> =
            ViewEventsTime({ it.checkOutOfBounds() }, emitter, view)

        fun createReachBoundsTimer(emitter: EventEmitter<ReachBounds>, view: ElementsView): ViewEventsTime<ReachBounds> =
            ViewEventsTime({ it.checkReachBounds() }, emitter, view)
    }
}
