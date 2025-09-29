
data class CommunityResult(
    val communities: Map<String, Int> 
)

/**
 * Находит сообщества с помощью алгоритма Core Expansion.
 * @param graph - граф с вершинами и рёбрами
 * @return карта vertexId -> communityId
 */
fun findCommunitiesByCoreExpansion(graph: Graph): Map<String, Int> {
    if (graph.vertices.isEmpty()) return emptyMap()

    val vertexDegrees = calculateDegrees(graph)
    val communities = mutableMapOf<String, Int>()
    var communityId = 0

    
    val uncoveredVertices = graph.vertices.keys.toMutableSet()

    while (uncoveredVertices.isNotEmpty()) {
        
        val core = findMaximalCore(uncoveredVertices, graph, vertexDegrees)

        if (core.isEmpty()) break 

        
        core.forEach { vertexId ->
            communities[vertexId] = communityId
            uncoveredVertices.remove(vertexId)
        }

        
        val expanded = expandCore(core, uncoveredVertices, graph)
        expanded.forEach { vertexId ->
            communities[vertexId] = communityId
            uncoveredVertices.remove(vertexId)
        }

        communityId++
    }

    return communities
}


private fun calculateDegrees(graph: Graph, vertices: Set<String>? = null): Map<String, Int> {
    val degrees = mutableMapOf<String, Int>()
    val vertexSet = vertices ?: graph.vertices.keys

    vertexSet.forEach { vertexId ->
        val count = graph.edges.count { edge ->
            (edge.source == vertexId && edge.target in vertexSet) ||
                    (edge.target == vertexId && edge.source in vertexSet)
        }
        degrees[vertexId] = count
    }

    return degrees
}


private fun findKCore(vertices: Set<String>, graph: Graph, k: Int): Set<String> {
    var currentVertices = vertices.toMutableSet()
    var degrees = calculateDegrees(graph, currentVertices)

    while (true) {
        val toRemove = degrees.filter { it.value < k }.keys
        if (toRemove.isEmpty()) break

        currentVertices.removeAll(toRemove)
        degrees = calculateDegrees(graph, currentVertices)
    }

    return currentVertices.toSet()
}


private fun findMaximalCore(vertices: Set<String>, graph: Graph, initialDegrees: Map<String, Int>): Set<String> {
    var k = 2
    var bestCore = emptySet<String>()

    while (true) {
        val core = findKCore(vertices, graph, k)
        if (core.isEmpty()) break
        bestCore = core
        k++
    }

    return bestCore
}


private fun expandCore(core: Set<String>, uncoveredVertices: Set<String>, graph: Graph, threshold: Int = 2): Set<String> {
    val expansion = mutableSetOf<String>()

    uncoveredVertices.forEach { vertexId ->
        val connectionCount = graph.edges.count { edge ->
            (edge.source == vertexId && edge.target in core) ||
                    (edge.target == vertexId && edge.source in core)
        }

        if (connectionCount >= threshold) {
            expansion.add(vertexId)
        }
    }

    return expansion
}