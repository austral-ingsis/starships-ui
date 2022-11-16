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

class CollisionTimer(private val emitter: EventEmitter<Collision>, private val view: ElementsView) : AnimationTimer() {
    private val collisionSet = mutableSetOf<Collision>()

    override fun handle(now: Long) {
        val currentCollisions = view.checkCollisions().toSet()

        val newCollisions = currentCollisions.filterNot { collisionSet.contains(it) }
        val collisionsToRemove = collisionSet.toSet().filterNot { currentCollisions.contains(it) }

        collisionSet.removeAll(collisionsToRemove.toSet())
        collisionSet.addAll(newCollisions)

        newCollisions.forEach { emitter.emit(it) }

    }
}
