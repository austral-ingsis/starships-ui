package edu.austral.ingsis.starships.ui

import edu.austral.ingsis.starships.ui.ColliderView.bindFillAndStroke
import edu.austral.ingsis.starships.ui.ElementColliderType.*
import edu.austral.ingsis.starships.ui.util.bindListTo
import edu.austral.ingsis.starships.ui.util.concatenate
import edu.austral.ingsis.starships.ui.util.map
import edu.austral.ingsis.starships.ui.util.mapToList
import javafx.beans.binding.Bindings.createObjectBinding
import javafx.beans.property.*
import javafx.collections.FXCollections.observableArrayList
import javafx.collections.FXCollections.observableMap
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import javafx.scene.Node
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Color.*
import javafx.scene.shape.*
import javafx.scene.text.Text

data class ImageRef(val id: String, val height: Double, val width: Double)

class ElementModel(
    val id: String,

    initialX: Double,
    initialY: Double,

    initialHeight: Double,
    initialWidth: Double,

    initialRotationInDegrees: Double,

    initialColliderType: ElementColliderType,

    initialImage: ImageRef?
) {
    val x: DoubleProperty = SimpleDoubleProperty(initialX)
    val y: DoubleProperty = SimpleDoubleProperty(initialY)

    val height: DoubleProperty = SimpleDoubleProperty(initialHeight)
    val width: DoubleProperty = SimpleDoubleProperty(initialWidth)

    val rotationInDegrees: DoubleProperty = SimpleDoubleProperty(initialRotationInDegrees)

    val colliderShapeType: Property<ElementColliderType> = SimpleObjectProperty(initialColliderType)

    val image: Property<ImageRef> = SimpleObjectProperty(initialImage)
}

enum class ElementColliderType { Elliptical, Rectangular, Triangular }

class ElementCollider(private val model: ElementModel, private val shape: Shape) : Collider {
    override fun getId(): String = model.id

    override fun getShape(): Shape = shape
}

object ColliderView {
    private const val COLLIDER_STROKE_WIDTH = 1.0

    fun bindFillAndStroke(shape: Shape, show: BooleanProperty) {
        shape.fill = TRANSPARENT
        shape.strokeWidth = COLLIDER_STROKE_WIDTH
        shape.strokeProperty().bind(createObjectBinding({ if (show.value) RED else TRANSPARENT }, show))
    }
}

class EllipticalColliderView(height: DoubleProperty, width: DoubleProperty, show: BooleanProperty) : Ellipse() {
    init {
        bindFillAndStroke(this, show)

        radiusXProperty().bind(width.map { it.toDouble() / 2 })
        radiusYProperty().bind(height.map { it.toDouble() / 2 })
    }
}

class RectangularColliderView(height: DoubleProperty, width: DoubleProperty, show: BooleanProperty) : Rectangle() {
    init {
        bindFillAndStroke(this, show)

        widthProperty().bind(width.map { it.toDouble() })
        heightProperty().bind(height.map { it.toDouble() })
    }
}

class GridView(
    private val containerHeight: ReadOnlyDoubleProperty,
    private val containerWidth: ReadOnlyDoubleProperty
) : Pane() {
    companion object {
        private const val LINE_SPACE = 50.0
        private val GRID_STROKE_TYPE = StrokeType.CENTERED
        private val GRID_STROKE_COLOR: Color = GREY
        private val GRID_STROKE_DASH = listOf(5.0, 5.0)
        private val NUMBER_OFFSET = 20.0

    }

    init {
        containerHeight.addListener { _, _, _ -> updateLines() }
        containerWidth.addListener { _, _, _ -> updateLines() }
    }

    private fun updateLines() {
        val horizontalLines = createLines((containerHeight.value / LINE_SPACE).toInt(), ::createHorizontalLine)
        val verticalLines = createLines((containerWidth.value / LINE_SPACE).toInt(), ::createVerticalLine)
        children.setAll(concatenate(horizontalLines, verticalLines))
    }

    private fun createLines(numberOfLines: Int, fn: (index: Int) -> List<Node>): List<Node> {
        return IntRange(1, numberOfLines).flatMap { fn(it) }
    }

    private fun createVerticalLine(index: Int): List<Node> {
        val x = index * LINE_SPACE
        val number = Text(x, NUMBER_OFFSET, x.toString())
        val line = withDashedStroke(Line(x, NUMBER_OFFSET, x, containerHeight.value))

        return listOf(number, line)
    }

    private fun createHorizontalLine(index: Int): List<Node> {
        val y = index * LINE_SPACE
        val number = Text(NUMBER_OFFSET / 2, y - 2.0, y.toString())
        val line = withDashedStroke(Line(NUMBER_OFFSET, y, containerWidth.value, y))

        return listOf(number, line)
    }

    private fun withDashedStroke(line: Line): Line {
        line.strokeType = GRID_STROKE_TYPE
        line.strokeDashArray.setAll(GRID_STROKE_DASH)
        line.stroke = GRID_STROKE_COLOR

        return line
    }

}

class TriangularColliderView(
    private val height: DoubleProperty,
    private val width: DoubleProperty,
    show: BooleanProperty
) : Polygon() {
    init {
        bindFillAndStroke(this, show)

        updatePoints()
        height.addListener { _, _, _ -> updatePoints() }
        width.addListener { _, _, _ -> updatePoints() }
    }

    private fun updatePoints() {
        points.setAll(observableArrayList(createPoints(height.value, width.value)))
    }

    companion object {
        private fun createPoints(height: Double, width: Double): List<Double> {
            val topX = 0.0
            val topY = height / 2.0
            val leftX = -1 * (width / 2.0)
            val leftY = -1 * (height / 2.0)
            val rightX = (width / 2.0)
            val rightY = -1 * (height / 2.0)

            return listOf(topX, topY, leftX, leftY, rightX, rightY)
        }
    }
}

class ElementView(
    val element: ElementModel,
    private val showCollider: BooleanProperty,
    private val imageResolver: ImageResolver
) : StackPane() {
    companion object {
        const val ACTUAL_VIEWS_INDEX = 0
        const val COLLIDERS_INDEX = 1
    }

    val collider = SimpleObjectProperty<Collider>()

    init {
        // Actual view
        val actualView = SimpleObjectProperty<Node>()
        actualView.bind(createObjectBinding({ createActualView(element) }, element.image))

        children.add(ACTUAL_VIEWS_INDEX, actualView.value)
        actualView.addListener { _, oldValue, newValue ->
            if (oldValue != null) children.remove(oldValue)
            if (newValue != null) children.add(ACTUAL_VIEWS_INDEX, newValue)
        }

        // Collider
        val colliderShape = SimpleObjectProperty<Shape>()
        colliderShape.bind(createObjectBinding({ createColliderShape(element) }, element.colliderShapeType))
        collider.bind(createObjectBinding({ ElementCollider(element, colliderShape.value) }, colliderShape))

        children.add(COLLIDERS_INDEX, colliderShape.value)
        colliderShape.addListener { _, oldValue, newValue ->
            if (oldValue != null) children.remove(oldValue)
            if (newValue != null) children.add(COLLIDERS_INDEX, newValue)
        }

        // Positioning
        rotateProperty().bind(element.rotationInDegrees)
        translateXProperty().bind(element.x)
        translateYProperty().bind(element.y)
    }

    private fun createActualView(element: ElementModel): Node {
        val imageRef = element.image.value
        return if (imageRef != null) {
            val imageView = ImageView(imageResolver.resolve(imageRef.id, imageRef.width, imageRef.height))
            imageView.fitHeightProperty().bind(element.height)
            imageView.fitWidthProperty().bind(element.width)
            imageView
        } else {
            val shapeView = createColliderShape(element)
            shapeView.fill = BLACK
            shapeView
        }
    }

    private fun createColliderShape(element: ElementModel): Shape {
        return when (element.colliderShapeType.value) {
            Elliptical -> EllipticalColliderView(element.height, element.width, showCollider)
            Rectangular -> RectangularColliderView(element.height, element.width, showCollider)
            Triangular -> TriangularColliderView(element.height, element.width, showCollider)
        }
    }
}

class ElementsView(
    private val imageResolver: ImageResolver
) : Pane() {
    companion object {
        const val GRID_INDEX = 0
        const val ELEMENTS_INDEX = 1

    }

    val elements: ObservableMap<String, ElementModel> = observableMap(HashMap())
    val showCollider: BooleanProperty = SimpleBooleanProperty(true)
    val showGrid: BooleanProperty = SimpleBooleanProperty(true)

    private val collisionEngine = CollisionEngine()
    private val elementViews: ObservableList<ElementView>

    init {
        // Grid view
        val gridView = GridView(heightProperty(), widthProperty())
        if (showGrid.value) children.add(GRID_INDEX, gridView)
        showGrid.addListener { _, _, show ->
            if (show) children.add(
                GRID_INDEX,
                gridView
            ) else children.remove(gridView)
        }


        // Element views
        elementViews = elements.mapToList(::renderElement)
        val elementsPane = Pane()
        elementsPane.children.bindListTo(elementViews as ObservableList<Node>)
        children.add(ELEMENTS_INDEX, elementsPane)
    }

    private fun renderElement(element: ElementModel): ElementView = ElementView(element, showCollider, imageResolver)

    fun checkCollisions(): Set<Collision> =
        collisionEngine.checkCollisions(elementViews.map { it.collider.value }).toSet()

    fun checkOutOfBounds(): Set<OutOfBounds> = elementViews.toList()
        .filter(::isNotInBounds)
        .map { OutOfBounds(it.element.id) }
        .toSet()

    private fun isNotInBounds(node: Node) =
        !layoutBounds.contains(node.boundsInParent) and !layoutBounds.intersects(node.boundsInParent)

    fun checkReachBounds(): Set<ReachBounds> = elementViews.toList()
        .filter(::hasReachBounds)
        .map { ReachBounds(it.element.id) }
        .toSet()

    private fun hasReachBounds(node: Node) =
        !layoutBounds.contains(node.boundsInParent) and layoutBounds.intersects(node.boundsInParent)
}

