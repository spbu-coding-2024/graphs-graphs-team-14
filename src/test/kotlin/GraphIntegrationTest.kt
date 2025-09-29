
import androidx.compose.ui.geometry.Offset

import java.io.File
import kotlin.test.*

class GraphIntegrationTest {

    @Test
    fun `test user flow- create graph -- run MST -- save to file`() {
        
        val state = GraphState()

        
        addVertex(state, Offset(100f, 100f))
        state.newVertexId = "V1"
        addVertex(state, Offset(200f, 200f))
        state.newVertexId = "V2"
        addVertex(state, Offset(300f, 100f))

        assertEquals(3, state.graph.vertices.size, "Должно быть 3 вершины")

        
        state.edgeWeight = "1.0"
        state.edgeCreationStart = "V0"
        addEdge(state, Offset(200f, 200f)) 
        state.edgeCreationStart = "V1"
        addEdge(state, Offset(300f, 100f)) 
        state.edgeCreationStart = "V0"
        addEdge(state, Offset(300f, 100f)) 

        assertEquals(3, state.graph.edges.size, "Должно быть 3 ребра")

        
        val mstEdgesFound = findMst(state.graph)
        state.mstEdges = mstEdgesFound
        state.highlightedEdges = mstEdgesFound.toSet()

        assertTrue(state.mstEdges.isNotEmpty(), "MST должен содержать рёбра")
        assertTrue(state.highlightedEdges.isNotEmpty(), "Должны быть выделенные рёбра")

        
        val tempFile = File.createTempFile("test_graph", ".json")
        saveGraphToFile(state.getImmutableGraph(),
            filePath = tempFile.absolutePath
        )

        assertTrue(tempFile.exists(), "Файл должен быть создан")

        
        val loadedGraph = loadGraphFromFile(tempFile.absolutePath)

        assertNotNull(loadedGraph, "Граф должен быть загружен")
        assertEquals(3, loadedGraph.vertices.size, "Загруженный граф должен содержать 3 вершины")
        assertEquals(3, loadedGraph.edges.size, "Загруженный граф должен содержать 3 ребра")

        tempFile.delete() 
    }
}