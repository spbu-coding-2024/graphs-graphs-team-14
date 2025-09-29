

import kotlinx.serialization.Serializable


@Serializable
data class BridgeResult(
    val bridges: List<Edge>
)

/**
 * Находит мосты в графе с помощью модифицированного DFS (алгоритм Тарьяна).
 * @param graph - граф с вершинами и рёбрами
 * @return список рёбер, являющихся мостами
 */
fun findBridges(graph: Graph): List<Edge> {
    val bridges = mutableListOf<Edge>()
    val visited = mutableMapOf<String, Boolean>()
    val disc = mutableMapOf<String, Int>()
    val low = mutableMapOf<String, Int>()
    val time = intArrayOf(0) 

    
    graph.vertices.keys.forEach { v ->
        visited[v] = false
    }

    
    graph.vertices.keys.forEach { vertexId ->
        visited[vertexId]?.let {
            if (!it) {
                dfsBridge(
                    vertexId = vertexId,
                    parent = null,
                    visited = visited,
                    disc = disc,
                    low = low,
                    time = time,
                    bridges = bridges,
                    graph = graph
                )
            }
        }
    }

    return bridges
}

private fun dfsBridge(
    vertexId: String,
    parent: String?,
    visited: MutableMap<String, Boolean>,
    disc: MutableMap<String, Int>,
    low: MutableMap<String, Int>,
    time: IntArray,
    bridges: MutableList<Edge>,
    graph: Graph
) {
    visited[vertexId] = true
    disc[vertexId] = time[0]
    low[vertexId] = time[0]
    time[0]++

    val vertex = graph.vertices[vertexId] ?: return

    graph.edges.forEach { edge ->
        val neighborId = when {
            edge.source == vertexId -> edge.target
            edge.target == vertexId -> edge.source
            else -> null
        }

        if (neighborId != null) {
            visited[neighborId]?.let {
                if (!it) {
                    dfsBridge(
                        vertexId = neighborId,
                        parent = vertexId,
                        visited = visited,
                        disc = disc,
                        low = low,
                        time = time,
                        bridges = bridges,
                        graph = graph
                    )

                    
                    low[vertexId] = minOf(low[vertexId]!!, low[neighborId]!!)

                    
                    if (low[neighborId]!! > disc[vertexId]!!) {
                        
                        bridges += edge
                    }
                } else if (neighborId != parent) {
                    
                    low[vertexId] = minOf(low[vertexId]!!, disc[neighborId]!!)
                }
            }
        }
    }
}