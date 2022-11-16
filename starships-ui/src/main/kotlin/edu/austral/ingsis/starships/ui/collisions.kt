package edu.austral.ingsis.starships.ui

import edu.austral.ingsis.starships.ui.util.concatenate
import edu.austral.ingsis.starships.ui.util.tail
import javafx.scene.shape.Shape

interface Collider {
    fun getId(): String
    fun getShape(): Shape
}

data class Collision(val element1Id: String, val element2Id: String)

class CollisionEngine {
    fun checkCollisions(colliders: List<Collider>): List<Collision> {
        return if (colliders.isEmpty()) emptyList() else checkCollisions(colliders.first(), colliders.tail())
    }

    private fun checkCollisions(current: Collider, colliders: List<Collider>): List<Collision> {
        if (colliders.isEmpty()) return emptyList()

        val thisElementCollisions = colliders
            .filter { testIntersection(current.getShape(), it.getShape()) }
            .map { Collision(current.getId(), it.getId()) }

        return concatenate(thisElementCollisions, checkCollisions(colliders.first(), colliders.tail()))
    }

    private fun testIntersection(shapeA: Shape, shapeB: Shape): Boolean {
        val layoutIntersects = shapeA.boundsInParent.intersects(shapeB.boundsInParent)
        if (!layoutIntersects) return false
        val shapeIntersection = Shape.intersect(shapeA, shapeB)
        return !shapeIntersection.layoutBounds.isEmpty
    }
}