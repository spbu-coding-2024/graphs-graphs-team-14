import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.serialization.Serializable
import java.awt.FileDialog
import java.awt.Frame
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


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

@Serializable
data class SerializableGraph(
    val vertices: Map<String, Vertex> = mapOf(),
    val edges: List<Edge> = listOf()
)


enum class InteractionMode {
    SELECT,      
    DRAG,        
    ADD_VERTEX,  
    ADD_EDGE,    
    DELETE       
}


class GraphState {

    var isDragging by mutableStateOf(false)
    var draggedVertexId by mutableStateOf<String?>(null)
    var canvasUpdateTrigger by mutableStateOf(0)
    var graph by mutableStateOf(createSampleGraph())
    var selectedAlgorithm by mutableStateOf<AlgorithmType?>(null)
    var selectedVertexId by mutableStateOf<String?>(null)
    var hoveredVertexId by mutableStateOf<String?>(null)
    var edgeEditMode by mutableStateOf<String?>(null) 
    var newEdgeWeight by mutableStateOf("1.0")
    
    var interactionMode by mutableStateOf(InteractionMode.SELECT)
    var edgeCreationStart by mutableStateOf<String?>(null) 

    
    var communities by mutableStateOf<Map<String, Int>>(emptyMap())
    var bridges by mutableStateOf<List<Edge>>(emptyList())
    var mstEdges by mutableStateOf<List<Edge>>(emptyList())

    
    var vertexColors by mutableStateOf<Map<String, Color>>(emptyMap())
    var highlightedEdges by mutableStateOf<Set<Edge>>(emptySet())

    
    var algorithmParams by mutableStateOf("")
    var statusMessage by mutableStateOf("Готов к работе")
    var newVertexId by mutableStateOf("V${createSampleGraph().vertices.size}")
    var edgeWeight by mutableStateOf("1.0")
    
    fun getImmutableGraph() = SerializableGraph(
        vertices = this.graph.vertices.toMap(),
        edges = this.graph.edges.toList()
    )

}

enum class AlgorithmType {
    COMMUNITY_DETECTION,
    BRIDGE_FINDING,
    MST
}

fun generateColorForCommunity(communityId: Int): Color {
    val hue = (communityId * 137.508f) % 360f 
    return Color.hsv(hue, 0.7f, 0.9f) 
}


fun createSampleGraph(): Graph {
    val graph = Graph()

    
    val vertexCount = 0
    val centerX = 400f
    val centerY = 300f
    val radius = 200f

    repeat(vertexCount) { i ->
        val angle = 2f * Math.PI.toFloat() * i / vertexCount
        val x = centerX + radius * cos(angle)
        val y = centerY + radius * sin(angle)
        val vertex = Vertex(id = "V$i", x = x, y = y)
        graph.vertices["V$i"] = vertex
    }

    









    return graph
}


fun findVertexAt(graph: Graph, position: Offset, radius: Float = 25f): String? {
    return graph.vertices.entries.find { (_, vertex) ->
        val distance = sqrt(
            (position.x - vertex.x) * (position.x - vertex.x) +
                    (position.y - vertex.y) * (position.y - vertex.y)
        )
        distance <= radius
    }?.key
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GraphApplication() {
    val state = remember { GraphState() }
    var isDragging by remember { mutableStateOf(false) }
    var selectedVertexToMove by remember { mutableStateOf<String?>(null) }
    var currentDraggedVertex by remember { mutableStateOf<String?>(null) }
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(8.dp)
        ) {
            
            Text(
                "Анализатор графов",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                
                ControlPanel(state, modifier = Modifier.width(300.dp))

                Spacer(modifier = Modifier.width(8.dp))

                
                GraphCanvas(state, modifier = Modifier.weight(1f))

                Spacer(modifier = Modifier.width(8.dp))

                
                InformationPanel(state, modifier = Modifier.width(280.dp))
            }

            
            StatusBar(state, modifier = Modifier.fillMaxWidth().height(30.dp))
        }
    }
}

@Composable
fun ControlPanel(state: GraphState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Управление",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )

        
        InteractionModesSection(state)

        Divider(color = Color(0xFFE0E0E0))

        
        FileOperationsSection(state)

        Divider(color = Color(0xFFE0E0E0))

        
        AlgorithmsSection(state)

        Divider(color = Color(0xFFE0E0E0))

        
        ParametersSection(state)

        Spacer(modifier = Modifier.weight(1f))

        
        Button(
            onClick = {
                state.communities = emptyMap()
                state.bridges = emptyList()
                state.mstEdges = emptyList()
                state.highlightedEdges = emptySet()
                state.vertexColors = emptyMap()
                state.selectedVertexId = null
                state.edgeCreationStart = null
                state.statusMessage = "Состояние сброшено"
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF757575))
        ) {
            Text("🔄 Сбросить всё")
        }
    }
}

@Composable
fun InteractionModesSection(state: GraphState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Режимы взаимодействия", fontWeight = FontWeight.Medium)

        
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            InteractionModeButton(
                mode = InteractionMode.SELECT,
                currentMode = state.interactionMode,
                onClick = {
                    state.vertexColors=emptyMap()
                    state.communities=emptyMap()
                    state.bridges=emptyList()
                    state.mstEdges = emptyList()
                    state.highlightedEdges=emptySet()
                    state.selectedAlgorithm = null
                    state.interactionMode = InteractionMode.SELECT
                    state.edgeEditMode = null
                },
                label = "↔ Выбор"
            )
            InteractionModeButton(
                mode = InteractionMode.DRAG,
                currentMode = state.interactionMode,
                onClick = {
                    state.vertexColors=emptyMap()
                    state.communities=emptyMap()
                    state.bridges=emptyList()
                    state.mstEdges = emptyList()
                    state.highlightedEdges=emptySet()
                    state.selectedAlgorithm = null
                    state.interactionMode = InteractionMode.DRAG
                    state.edgeEditMode = null
                },
                label = "↔ Перетащить")
            InteractionModeButton(
                mode = InteractionMode.ADD_VERTEX,
                currentMode = state.interactionMode,
                onClick = {
                    state.vertexColors=emptyMap()
                    state.communities=emptyMap()
                    state.bridges=emptyList()
                    state.mstEdges = emptyList()
                    state.highlightedEdges=emptySet()
                    state.selectedAlgorithm = null
                    state.interactionMode = InteractionMode.ADD_VERTEX
                    state.edgeEditMode = null
                },
                label = "+ Вершина"
            )

        }

        
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            InteractionModeButton(
                mode = InteractionMode.ADD_EDGE,
                currentMode = state.interactionMode,
                onClick = {
                    state.vertexColors=emptyMap()
                    state.communities=emptyMap()
                    state.bridges=emptyList()
                    state.highlightedEdges=emptySet()
                    state.mstEdges = emptyList()
                    state.selectedAlgorithm = null
                    state.interactionMode = InteractionMode.ADD_EDGE
                    state.edgeEditMode = null
                },
                label = "+ Ребро"
            )
            InteractionModeButton(
                mode = InteractionMode.DELETE,
                currentMode = state.interactionMode,
                onClick = {
                    state.vertexColors=emptyMap()
                    state.communities=emptyMap()
                    state.bridges=emptyList()
                    state.highlightedEdges=emptySet()
                    state.mstEdges = emptyList()
                    state.selectedAlgorithm = null
                    state.interactionMode = InteractionMode.DELETE
                    state.edgeEditMode = null
                },
                label = "🗑 Удалить"
            )
            Button(
                onClick = {
                    state.vertexColors=emptyMap()
                    state.communities=emptyMap()
                    state.bridges=emptyList()
                    state.highlightedEdges=emptySet()
                    state.mstEdges = emptyList()
                    state.selectedAlgorithm = null
                    state.interactionMode = InteractionMode.SELECT
                    state.edgeEditMode = if (state.edgeEditMode == null) "edit" else null
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (state.edgeEditMode != null) Color(0xFF2196F3) else Color(0xFFE0E0E0)
                )
            ) {
                Text("✏️ Вес ребра", fontSize = 10.sp)
            }
        }

        
        if (state.interactionMode == InteractionMode.ADD_VERTEX) {
            while (state.newVertexId in state.graph.vertices){
                state.newVertexId="V${state.newVertexId.drop(1).toInt() + 1}"
            }
            OutlinedTextField(
                value = state.newVertexId,
                onValueChange = { state.newVertexId = it },
                label = { Text("ID новой вершины") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        
        if (state.interactionMode == InteractionMode.ADD_EDGE || state.edgeEditMode != null) {
            OutlinedTextField(
                value = if (state.interactionMode == InteractionMode.ADD_EDGE) state.edgeWeight else state.newEdgeWeight,
                onValueChange = {
                    if (state.interactionMode == InteractionMode.ADD_EDGE) {
                        state.edgeWeight = it
                    } else {
                        state.newEdgeWeight = it
                    }
                },
                label = { Text("Вес ребра") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
fun InteractionModeButton(
    mode: InteractionMode,
    currentMode: InteractionMode?,
    onClick: () -> Unit,
    label: String
) {
    val isSelected = mode == currentMode
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isSelected) Color(0xFF2196F3) else Color(0xFFE0E0E0)
        )
    ) {
        Text(label, fontSize = 10.sp)
    }
}

@Composable
fun FileOperationsSection(state: GraphState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Работа с файлами", fontWeight = FontWeight.Medium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val fileDialog = FileDialog(
                        null as Frame?,
                        "Загрузить граф из...",
                        FileDialog.LOAD
                    )
                    fileDialog.file = "*.json"
                    fileDialog.directory = System.getProperty("user.home")
                    fileDialog.isVisible = true

                    val selectedFile = fileDialog.file
                    val selectedDir = fileDialog.directory

                    if (selectedFile != null && selectedDir != null) {
                        val filePath = "$selectedDir$selectedFile"
                        val loadedGraph = loadGraphFromFile(filePath)
                        if (loadedGraph != null) {
                            state.graph = loadedGraph.copy(
                                vertices = loadedGraph.vertices.toMutableMap(),
                                edges = loadedGraph.edges.toMutableList()
                            )
                            state.statusMessage = "Граф загружен из: $filePath"
                        } else {
                            state.statusMessage = "Не удалось загрузить файл: $filePath"
                        }
                    } else {
                        state.statusMessage = "Загрузка отменена"
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
            ) {
                Text("📁 Загрузить")
            }

            Button(
                onClick = {
                    
                    val fileDialog = FileDialog(
                        null as Frame?, 
                        "Сохранить граф как...",
                        FileDialog.SAVE
                    )
                    fileDialog.file = "*.json" 
                    fileDialog.directory = System.getProperty("user.home") 
                    fileDialog.isVisible = true

                    val selectedFile = fileDialog.file
                    val selectedDir = fileDialog.directory

                    if (selectedFile != null && selectedDir != null) {
                        val filePath = "$selectedDir$selectedFile"
                        if (!filePath.endsWith(".json", ignoreCase = true)) {
                            
                            val correctedPath = "$filePath.json"
                            saveGraphToFile(
                                graph = state.getImmutableGraph(),
                                filePath = correctedPath
                            )
                            state.statusMessage = "Граф сохранён в: $correctedPath"
                        } else {
                            saveGraphToFile(
                                graph = state.getImmutableGraph(),
                                filePath = filePath
                            )
                            state.statusMessage = "Граф сохранён в: $filePath"
                        }
                    } else {
                        state.statusMessage = "Сохранение отменено"
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
            ) {
                Text("💾 Сохранить")
            }
        }
    }
}

@Composable
fun AlgorithmsSection(state: GraphState) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Алгоритмы анализа", fontWeight = FontWeight.Medium)

        
        Box {
            Button(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF9800))
            ) {
                Text(state.selectedAlgorithm?.let { getAlgorithmName(it) } ?: "Выберите алгоритм")
                Text("▼")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                AlgorithmType.values().forEach { algorithm ->
                    DropdownMenuItem(onClick = {
                        state.selectedAlgorithm = algorithm
                        expanded = false
                        state.statusMessage = "Выбран алгоритм: ${getAlgorithmName(algorithm)}"
                    }) {
                        Text(getAlgorithmName(algorithm))
                    }
                }
            }
        }

        
        Button(
            onClick = {

                when (state.selectedAlgorithm) {
                    AlgorithmType.COMMUNITY_DETECTION -> {
                        val communitiesFound = findCommunitiesByCoreExpansion(state.graph)
                        state.communities = communitiesFound
                        state.vertexColors = communitiesFound.mapValues { (_, communityId) ->
                            generateColorForCommunity(communityId)
                        }
                        state.statusMessage = "Найдено ${communitiesFound.values.distinct().size} сообществ"
                    }
                    AlgorithmType.BRIDGE_FINDING -> {
                        
                        val bridgesFound = findBridges(state.graph)
                        state.bridges = bridgesFound
                        state.highlightedEdges = bridgesFound.toSet()
                        state.statusMessage = "Найдено ${bridgesFound.size} мостов"
                    }
                    AlgorithmType.MST -> {
                        val mstEdgesFound = findMst(state.graph)
                        state.mstEdges = mstEdgesFound
                        state.highlightedEdges = mstEdgesFound.toSet()
                        state.statusMessage = "Построено МОД с ${mstEdgesFound.size} рёбрами"
                    }
                    null -> state.statusMessage = "Сначала выберите алгоритм"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.selectedAlgorithm != null,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2E7D32))
        ) {
            Text("▶ Выполнить алгоритм")
        }
    }
}

@Composable
fun ParametersSection(state: GraphState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Параметры алгоритма", fontWeight = FontWeight.Medium)

        OutlinedTextField(
            value = state.algorithmParams,
            onValueChange = { state.algorithmParams = it },
            label = { Text("Параметры (опционально)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GraphCanvas(state: GraphState, modifier: Modifier = Modifier) {
    
    
    
    

    
    println("Dragged Vertex ID: ${state.draggedVertexId}")
    println("Is dragging (state): ${state.isDragging}")
    println("Selected Vertex ID: ${state.selectedVertexId}")
    println("=== GraphCanvas recomposition ===")
    println("Interaction mode: ${state.interactionMode}")

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
    ) {
        
        
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .pointerInput(state.interactionMode) {
                        if (state.interactionMode != InteractionMode.DRAG) {
                            detectTapGestures(onTap = { offset ->
                                println("TAP detected at: $offset")
                                handleCanvasClick(state, offset)
                            })
                        }
                    }
                    .pointerInput(state.interactionMode) { 
                        if (state.interactionMode == InteractionMode.DRAG) {
                            println("Drag gestures ENABLED")
                            detectDragGestures(
                                onDragStart = { offset ->
                                    println("=== DRAG START ===")
                                    println("Drag start offset: $offset")
                                    val selectedVertexId = findVertexAt(state.graph, offset)
                                    if (selectedVertexId != null) {
                                        
                                        state.isDragging = true
                                        state.draggedVertexId = selectedVertexId
                                        state.selectedVertexId = selectedVertexId 
                                        state.statusMessage = "Перемещение вершины: $selectedVertexId"
                                        println("Dragging vertex: $selectedVertexId")
                                    } else {
                                        println("No vertex found at drag start position.")
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    
                                    
                                    

                                    
                                    if (state.isDragging && state.draggedVertexId != null) {
                                        val vertexId = state.draggedVertexId!!
                                        val vertex = state.graph.vertices[vertexId]
                                        vertex?.let {
                                            
                                            it.x += dragAmount.x
                                            it.y += dragAmount.y
                                            

                                            
                                            
                                            state.canvasUpdateTrigger++
                                            
                                        }
                                    }
                                },
                                onDragEnd = {
                                    println("=== DRAG END ===")
                                    state.isDragging = false
                                    state.draggedVertexId = null
                                    state.statusMessage = "Перемещение завершено"
                                    println("Drag ended")
                                },
                                onDragCancel = {
                                    println("=== DRAG CANCEL ===")
                                    state.isDragging = false
                                    state.draggedVertexId = null
                                    state.statusMessage = "Перемещение отменено"
                                    println("Drag cancelled")
                                }
                            )
                        }
                    }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val position = event.changes.first().position
                                if (!state.isDragging) { 
                                    state.hoveredVertexId = findVertexAt(state.graph, position)
                                }
                            }
                        }
                    }
            ) {
                println("=== Canvas drawing ===")
                
                val q = state.canvasUpdateTrigger
                state.graph.edges.forEach { edge ->
                    val source = state.graph.vertices[edge.source]
                    val target = state.graph.vertices[edge.target]
                    if (source != null && target != null) {
                        val isHighlighted = edge in state.highlightedEdges
                        val isBridge = state.bridges.contains(edge)
                        val isMST = state.mstEdges.contains(edge)
                        val color = when {
                            isBridge -> Color.Red
                            isMST -> Color(0xFF2E7D32)
                            isHighlighted -> Color(0xFF2196F3)
                            else -> Color.Gray.copy(alpha = 0.6f)
                        }
                        val strokeWidth = when {
                            isBridge || isMST -> 4f
                            isHighlighted -> 3f
                            else -> 2f
                        }
                        drawLine(
                            color = color,
                            start = Offset(source.x, source.y),
                            end = Offset(target.x, target.y),
                            strokeWidth = strokeWidth
                        )
                    }
                }
                
                state.graph.vertices.values.forEach { vertex ->
                    val color = state.vertexColors[vertex.id] ?: Color(0xFF2196F3)
                    val isSelected = vertex.id == state.selectedVertexId
                    val isHovered = vertex.id == state.hoveredVertexId
                    
                    if (isSelected) {
                        drawCircle(
                            color = Color.Yellow.copy(alpha = 0.3f),
                            radius = vertex.radius + 8f,
                            center = Offset(vertex.x, vertex.y)
                        )
                    } else if (isHovered && !state.isDragging) { 
                        drawCircle(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            radius = vertex.radius + 5f,
                            center = Offset(vertex.x, vertex.y)
                        )
                    }
                    
                    drawCircle(
                        color = color,
                        radius = vertex.radius,
                        center = Offset(vertex.x, vertex.y)
                    )
                }
                
                
                if (state.isDragging && state.draggedVertexId != null) {
                    val vertex = state.graph.vertices[state.draggedVertexId!!]
                    vertex?.let {
                        drawCircle(
                            color = Color(0xFFFF9800).copy(alpha = 0.3f),
                            radius = it.radius + 15f,
                            center = Offset(it.x, it.y)
                        )
                    }
                }
            }
            
            key(state.canvasUpdateTrigger){
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                state.graph.edges.forEach { edge ->

                        val source = state.graph.vertices[edge.source]
                        val target = state.graph.vertices[edge.target]
                        if (source != null && target != null) {
                            val midX = (source.x + target.x) / 2
                            val midY = (source.y + target.y) / 2
                            Text(
                                text = "%.1f".format(edge.weight),
                                modifier = Modifier.offset(x = (midX - 15).dp, y = (midY - 10).dp),
                                fontSize = 10.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )

                    }
                }
                state.graph.vertices.values.forEach { vertex ->
                    Text(
                        text = vertex.id,
                        modifier = Modifier.offset(x = (vertex.x - 8).dp, y = (vertex.y - 8).dp),
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }}
            
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text("Легенда", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    LegendItem("Вершины", Color(0xFF2196F3))
                    LegendItem("Мосты", Color.Red)
                    LegendItem("МОД", Color(0xFF2E7D32))
                    LegendItem("Сообщества", Color(0xFFFF6B6B))
                    Text("Режим: ${getModeName(state.interactionMode)}", fontSize = 10.sp)
                    if (state.isDragging) { 
                        Text("Перетаскивание...", fontSize = 10.sp, color = Color(0xFFFF9800))
                    }
                }
            }
        }
        
    }
}
fun findEdgeAt(graph: Graph, position: Offset, tolerance: Float = 10f): Edge? {
    return graph.edges.find { edge ->
        val source = graph.vertices[edge.source]
        val target = graph.vertices[edge.target]
        if (source != null && target != null) {
            isPointOnLine(position, Offset(source.x, source.y), Offset(target.x, target.y), tolerance)
        } else false
    }
}


fun handleCanvasClick(state: GraphState, position: Offset) {
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    if (state.edgeEditMode != null) {
        
        val clickedEdge = findEdgeAt(state.graph, position)
        if (clickedEdge != null) {
            val newWeight = state.newEdgeWeight.toFloatOrNull() ?: 1.0f
            clickedEdge.weight = newWeight
            state.statusMessage = "Вес ребра ${clickedEdge.source}-${clickedEdge.target} изменен на $newWeight"
            state.edgeEditMode = null
        } else {
            state.statusMessage = "Кликните на ребро для изменения веса"
        }
        state.canvasUpdateTrigger++
        return
    }

    when (state.interactionMode) {
        InteractionMode.SELECT -> {
            val vertexId = findVertexAt(state.graph, position)
            state.selectedVertexId = vertexId
            state.statusMessage = vertexId?.let { "Выбрана вершина: $it" } ?: "Снято выделение"
        }
        InteractionMode.DRAG -> {
            
            
            
            
            
            
            
            
            
            
            val vertexId = findVertexAt(state.graph, position)
            state.selectedVertexId = vertexId
            state.statusMessage = vertexId?.let { "Выбрана вершина: $it (режим DRAG)" } ?: "Снято выделение (режим DRAG)"
        }
        InteractionMode.ADD_VERTEX -> {
            addVertex(state, position)
            state.canvasUpdateTrigger++
        }
        InteractionMode.ADD_EDGE -> {
            addEdge(state, position)
            state.canvasUpdateTrigger++
        }
        InteractionMode.DELETE -> {
            val vertexId = findVertexAt(state.graph, position)
            if (vertexId != null) {
                
                state.graph.vertices.remove(vertexId)
                state.graph.edges.removeAll { it.source == vertexId || it.target == vertexId }
                if (state.selectedVertexId == vertexId) {
                    state.selectedVertexId = null
                }
                state.statusMessage = "Удалена вершина: $vertexId"
            } else {
                
                val initialEdgesCount = state.graph.edges.size
                state.graph.edges.removeAll { edge ->
                    val source = state.graph.vertices[edge.source]
                    val target = state.graph.vertices[edge.target]
                    if (source != null && target != null) {
                        isPointOnLine(position, Offset(source.x, source.y), Offset(target.x, target.y), 5f)
                    } else false
                }
                if (state.graph.edges.size < initialEdgesCount) {
                    state.statusMessage = "Удалено ребро"
                } else {
                    state.statusMessage = "Ребро не найдено для удаления"
                }
            }
            state.canvasUpdateTrigger++
        }
    }
}
fun addVertex(state: GraphState, position: Offset) {
    val newId = state.newVertexId.ifEmpty { "V${state.graph.vertices.size}" }
    if (!state.graph.vertices.containsKey(newId)) {
        state.graph.vertices[newId] = Vertex(newId, position.x, position.y)
        state.statusMessage = "Добавлена вершина: $newId"
        state.newVertexId = "V${state.graph.vertices.size + 1}"
    } else {
        state.statusMessage = "Вершина с ID $newId уже существует"
    }
}

fun addEdge(state: GraphState, position: Offset) {
    val vertexId = findVertexAt(state.graph, position)
    if (vertexId != null) {
        if (state.edgeCreationStart == null) {
            state.edgeCreationStart = vertexId
            state.statusMessage = "Выберите вторую вершину для ребра"
        } else {
            val startId = state.edgeCreationStart!!
            if (startId != vertexId) {
                val weight = state.edgeWeight.toFloatOrNull() ?: 1f
                val newEdge = Edge(startId, vertexId, weight)
                if (!state.graph.edges.any { it.source == startId && it.target == vertexId || it.source == vertexId && it.target == startId }) {
                    state.graph.edges.add(newEdge)
                    state.statusMessage = "Добавлено ребро: $startId - $vertexId (вес: $weight)"
                } else {
                    state.statusMessage = "Ребро уже существует"
                }
            }
            state.edgeCreationStart = null
        }
    } else {
        state.edgeCreationStart = null
        state.statusMessage = "Выберите вершину для создания ребра"
    }
}

fun isPointOnLine(point: Offset, lineStart: Offset, lineEnd: Offset, tolerance: Float): Boolean {
    val lineLength = sqrt(
        (lineEnd.x - lineStart.x) * (lineEnd.x - lineStart.x) +
                (lineEnd.y - lineStart.y) * (lineEnd.y - lineStart.y)
    )

    val distance = abs(
        (lineEnd.x - lineStart.x) * (lineStart.y - point.y) -
                (lineStart.x - point.x) * (lineEnd.y - lineStart.y)
    ) / lineLength

    return distance <= tolerance
}

@Composable
fun LegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 10.sp)
    }
}

@Composable
fun InformationPanel(state: GraphState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Информация",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )

        
        GraphStatistics(state)

        Divider(color = Color(0xFFE0E0E0))

        
        AlgorithmResults(state)

        Divider(color = Color(0xFFE0E0E0))

        
        DetailedInfo(state)
    }
}

@Composable
fun GraphStatistics(state: GraphState) {
    val q = state.canvasUpdateTrigger
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Статистика графа", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Text("Вершин: ${state.graph.vertices.size}", fontSize = 12.sp)
        Text("Рёбер: ${state.graph.edges.size}", fontSize = 12.sp)
        Text("Взвешенный: Да", fontSize = 12.sp)
    }
}

@Composable
fun AlgorithmResults(state: GraphState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Результаты анализа", fontWeight = FontWeight.Medium, fontSize = 14.sp)

        when (state.selectedAlgorithm) {
            AlgorithmType.COMMUNITY_DETECTION -> {
                val communityCount = state.communities.values.distinct().size
                Text("Сообществ: $communityCount", fontSize = 12.sp, color = Color.Blue)
                LazyColumn(modifier = Modifier.height(100.dp)) {
                    items(state.communities.entries.chunked(3)) { chunk ->
                        Row {
                            chunk.forEach { (vertex, community) ->
                                Text("$vertex: $community", fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                            }
                        }
                    }
                }
            }
            AlgorithmType.BRIDGE_FINDING -> {
                Text("Мостов: ${state.bridges.size}", fontSize = 12.sp, color = Color.Red)
                state.bridges.forEach { bridge ->
                    Text("${bridge.source} - ${bridge.target}", fontSize = 10.sp)
                }
            }
            AlgorithmType.MST -> {
                val totalWeight = state.mstEdges.sumOf { it.weight.toDouble() }
                Text("Рёбер в МОД: ${state.mstEdges.size}", fontSize = 12.sp, color = Color(0xFF2E7D32))
                Text("Вес МОД: ${"%.2f".format(totalWeight)}", fontSize = 12.sp)
            }
            null -> Text("Алгоритм не выбран", fontSize = 12.sp, color = Color.Gray)

        }
    }
}

@Composable
fun DetailedInfo(state: GraphState) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Детальная информация", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Text("Выбранная вершина: ${state.selectedVertexId ?: "нет"}", fontSize = 10.sp)
        Text("Алгоритм: ${state.selectedAlgorithm?.let { getAlgorithmName(it) } ?: "не выбран"}", fontSize = 10.sp)
        Text("Режим: ${getModeName(state.interactionMode)}", fontSize = 10.sp)
        if (state.edgeCreationStart != null) {
            Text("Создание ребра из: ${state.edgeCreationStart}", fontSize = 10.sp, color = Color(0xFFFF9800))
        }
    }
}

@Composable
fun StatusBar(state: GraphState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color(0xFFE3F2FD), RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("💡 ${state.statusMessage}", fontSize = 12.sp)
    }
}

fun getAlgorithmName(algorithm: AlgorithmType): String {
    return when (algorithm) {
        AlgorithmType.COMMUNITY_DETECTION -> "Поиск сообществ"
        AlgorithmType.BRIDGE_FINDING -> "Поиск мостов"
        AlgorithmType.MST -> "Минимальное остовное дерево"
    }
}

fun getModeName(mode: InteractionMode): String {
    return when (mode) {
        InteractionMode.SELECT -> "Выбор/Перемещение"
        InteractionMode.DRAG -> "Перетаскивание вершин"
        InteractionMode.ADD_VERTEX -> "Добавление вершин"
        InteractionMode.ADD_EDGE -> "Добавление рёбер"
        InteractionMode.DELETE -> "Удаление"
    }
}
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Graph Analyzer - Анализатор графов") {
        GraphApplication()
    }
}