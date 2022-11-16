package edu.austral.ingsis.starships.ui.util

import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections.observableArrayList
import javafx.collections.ListChangeListener
import javafx.collections.MapChangeListener
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import javafx.scene.shape.Shape

fun <T, R> ReadOnlyProperty<T>.map(mapper: (t: T) -> R): ObjectProperty<R> {
    val result = SimpleObjectProperty<R>()

    addListener { _, _, newValue ->
        result.set(mapper(newValue))
    }
    result.set(mapper(value))

    return result
}

fun <T> ObservableList<T>.bindListTo(observed: ObservableList<T>) {
    observed.addListener(ListChangeListener { change ->
        if (change.next() && (change.wasAdded() || change.wasRemoved())) {
            addAll(change.addedSubList)
            removeAll(change.removed)
        }
    })

    addAll(observed)
}

fun <K, V, I> ObservableMap<K, V>.mapToList(renderer: (t: V) -> I): ObservableList<I> {
    val items = observableArrayList<I>()

    val nodeMap = mapValues { renderer(it.value) }.toMutableMap()
    nodeMap.values.forEach { items.add(it) }

    addListener(MapChangeListener { change ->
        if (change.valueRemoved != null) {
            nodeMap[change.key]?.let { items.remove(it) }
            nodeMap.remove(change.key)
        }

        if (change.valueAdded != null) {
            val newNode = renderer(change.valueAdded)
            nodeMap[change.key] = newNode
            items.add(newNode)
        }
    })

    return items
}
