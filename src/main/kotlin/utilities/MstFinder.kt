


data class MstResult(
    val mstEdges: List<Edge>
)

/**
 * Находит минимальное остовное дерево (MST) с помощью алгоритма Крускала.
 * @param graph - граф с вершинами и рёбрами
 * @return список рёбер, входящих в MST
 */
fun findMst(graph: Graph): List<Edge> {
    if (graph.vertices.isEmpty()) return emptyList()
    if (graph.edges.isEmpty()) return emptyList()

    
    val sortedEdges = graph.edges.sortedBy { it.weight }

    
    val dsu = DisjointSetUnion(graph.vertices.keys.toList())

    val mstEdges = mutableListOf<Edge>()
    var edgesAdded = 0

    
    for (edge in sortedEdges) {
        val rootA = dsu.find(edge.source)
        val rootB = dsu.find(edge.target)

        
        if (rootA != rootB) {
            mstEdges.add(edge)
            dsu.union(rootA, rootB)
            edgesAdded++

            
            if (edgesAdded == graph.vertices.size - 1) break
        }
    }

    return mstEdges
}


class DisjointSetUnion(elements: List<String>) {
    private val parent = mutableMapOf<String, String>()
    private val rank = mutableMapOf<String, Int>()

    init {
        elements.forEach { element ->
            parent[element] = element
            rank[element] = 0
        }
    }


    fun find(x: String): String {
        if (parent[x] != x) {
            parent[x] = find(parent[x]!!) 
        }
        return parent[x]!!
    }


    fun union(x: String, y: String) {
        val rootX = find(x)
        val rootY = find(y)

        if (rootX != rootY) {
            if (rank[rootX]!! < rank[rootY]!!) {
                parent[rootX] = rootY
            } else if (rank[rootX]!! > rank[rootY]!!) {
                parent[rootY] = rootX
            } else {
                parent[rootY] = rootX
                rank[rootX] = rank[rootX]!! + 1
            }
        }
    }
}