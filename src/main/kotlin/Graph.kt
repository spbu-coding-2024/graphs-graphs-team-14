import kotlinx.serialization.Serializable


@Serializable
data class Vertex(
    val id: String,
    var x: Float = 0f,
    var y: Float = 0f,
    var radius: Float = 25f
)

@Serializable
data class Edge(
    val source: String,
    val target: String,
    var weight: Float = 1f
)

@Serializable
data class Graph(
    val vertices: MutableMap<String, Vertex> = mutableMapOf(),
    val edges: MutableList<Edge> = mutableListOf()
)