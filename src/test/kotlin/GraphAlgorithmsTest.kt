// GraphAlgorithmsTest.kt

import kotlin.test.*

class GraphAlgorithmsTest {

    @Test
    fun `test findBridges - simple bridge graph`() {
        // Граф: A -- B -- C
        //       |      |
        //       D ---- E
        // Ребро B-C — мост
        val graph = Graph().apply {
            vertices.putAll(mapOf(
                "A" to Vertex("A", 0f, 0f),
                "B" to Vertex("B", 0f, 0f),
                "C" to Vertex("C", 0f, 0f),
                "D" to Vertex("D", 0f, 0f),
                "E" to Vertex("E", 0f, 0f)
            ))
            edges.addAll(listOf(
                Edge("A", "B", 1f),
                Edge("A", "D", 1f),
                Edge("B", "C", 1f),
                Edge("B", "E", 1f),
                Edge("D", "E", 1f)
            ))
        }

        val bridges = findBridges(graph)

        assertEquals(1, bridges.size)
        assertTrue(bridges.contains(Edge("B", "C", 1f)))
    }

    @Test
    fun `test findBridges - no bridges`() {
        // Граф: A -- B
        //       |    |
        //       D -- C
        val graph = Graph().apply {
            vertices.putAll(mapOf(
                "A" to Vertex("A", 0f, 0f),
                "B" to Vertex("B", 0f, 0f),
                "C" to Vertex("C", 0f, 0f),
                "D" to Vertex("D", 0f, 0f)
            ))
            edges.addAll(listOf(
                Edge("A", "B", 1f),
                Edge("B", "C", 1f),
                Edge("C", "D", 1f),
                Edge("D", "A", 1f)
            ))
        }

        val bridges = findBridges(graph)

        assertTrue(bridges.isEmpty())
    }

    @Test
    fun `test findMst - simple tree`() {
        // Граф: A --2-- B --3-- C
        val graph = Graph().apply {
            vertices.putAll(mapOf(
                "A" to Vertex("A", 0f, 0f),
                "B" to Vertex("B", 0f, 0f),
                "C" to Vertex("C", 0f, 0f)
            ))
            edges.addAll(listOf(
                Edge("A", "B", 2f),
                Edge("B", "C", 3f)
            ))
        }

        val mst = findMst(graph)

        assertEquals(2, mst.size)
        assertTrue(mst.contains(Edge("A", "B", 2f)))
        assertTrue(mst.contains(Edge("B", "C", 3f)))
    }

    @Test
    fun `test findMst - cycle graph with weights`() {
        // Граф: A --2-- B
        //       |      |
        //       1      3
        //       |      |
        //       D --4-- C
        // MST: A-B (2), A-D (1), B-C (3) => сумма = 6
        val graph = Graph().apply {
            vertices.putAll(mapOf(
                "A" to Vertex("A", 0f, 0f),
                "B" to Vertex("B", 0f, 0f),
                "C" to Vertex("C", 0f, 0f),
                "D" to Vertex("D", 0f, 0f)
            ))
            edges.addAll(listOf(
                Edge("A", "B", 2f),
                Edge("B", "C", 3f),
                Edge("C", "D", 4f),
                Edge("A", "D", 1f)
            ))
        }

        val mst = findMst(graph)

        assertEquals(3, mst.size)
        val totalWeight = mst.sumOf { it.weight.toDouble() }
        assertEquals(6.0, totalWeight)
    }

    @Test
    fun `test findMst - empty graph`() {
        val graph = Graph()

        val mst = findMst(graph)

        assertTrue(mst.isEmpty())
    }

    @Test
    fun `test findCommunitiesByCoreExpansion - simple core`() {
        // Граф: A -- B -- C -- D
        //       |    |    |    |
        //       E -- F -- G -- H
        //       Это сетка, где B-F, C-G — центральные узлы, можно выделить ядра
        val graph = Graph().apply {
            vertices.putAll(mapOf(
                "A" to Vertex("A", 0f, 0f),
                "B" to Vertex("B", 0f, 0f),
                "C" to Vertex("C", 0f, 0f),
                "D" to Vertex("D", 0f, 0f),
                "E" to Vertex("E", 0f, 0f),
                "F" to Vertex("F", 0f, 0f),
                "G" to Vertex("G", 0f, 0f),
                "H" to Vertex("H", 0f, 0f)
            ))
            edges.addAll(listOf(
                Edge("A", "B", 1f), Edge("B", "C", 1f), Edge("C", "D", 1f),
                Edge("E", "F", 1f), Edge("F", "G", 1f), Edge("G", "H", 1f),
                Edge("A", "E", 1f), Edge("B", "F", 1f), Edge("C", "G", 1f), Edge("D", "H", 1f)
            ))
        }

        val communities = findCommunitiesByCoreExpansion(graph)


        assertEquals(graph.vertices.keys.toSet(), communities.keys.toSet())
    }

    @Test
    fun `test findCommunitiesByCoreExpansion - single node`() {
        val graph = Graph().apply {
            vertices["A"] = Vertex("A", 0f, 0f)
        }

        val communities = findCommunitiesByCoreExpansion(graph)

        assertEquals(0, communities.size)  // Это нормально, так как алгоритм допускает существование вершин без сообществ
    }

    @Test
    fun `test findCommunitiesByCoreExpansion - disconnected components`() {
        val graph = Graph().apply {
            vertices.putAll(mapOf(
                "A" to Vertex("A", 0f, 0f),
                "B" to Vertex("B", 0f, 0f),
                "C" to Vertex("C", 0f, 0f),
                "D" to Vertex("D", 0f, 0f)
            ))
            edges.addAll(listOf(
                Edge("A", "B", 1f),
                Edge("C", "D", 1f)
            ))
        }

        val communities = findCommunitiesByCoreExpansion(graph)

        assertEquals(0, communities.size)
    }
}