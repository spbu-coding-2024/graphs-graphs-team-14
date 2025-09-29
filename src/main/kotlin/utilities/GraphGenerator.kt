import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.cos
import kotlin.math.sin


@kotlinx.serialization.Serializable
data class Vertex2(
    val id: String,
    var x: Float = 0f,
    var y: Float = 0f,
    var radius: Float = 25f
)

@kotlinx.serialization.Serializable
data class Edge2(
    val source: String,
    val target: String,
    var weight: Float = 1f
)

@kotlinx.serialization.Serializable
data class Graph2(
    val vertices: Map<String, Vertex2> = mapOf(), 
    val Edge2s: List<Edge2> = listOf()             
)

/**
 * Структура: ID вершины -> степень
 */
typealias DegreeSequence = Map<String, Int>

/**
 * Генерирует граф по заданной последовательности степеней (Havel-Hakimi алгоритм)
 * @param degreeSequence Карта "ID вершины" -> "степень"
 * @return Graph2 с вершинами и рёбрами, удовлетворяющими степени
 */
fun generateGraph2FromDegreeSequence(degreeSequence: DegreeSequence): Graph2 {
    
    val degrees = degreeSequence.toMutableMap()
    val Graph2 = Graph2(
        vertices = mutableMapOf(),
        Edge2s = mutableListOf()
    )

    
    val Vertex2Ids = degreeSequence.keys.toList()
    val centerX = 400f
    val centerY = 300f
    val radius = 300f 

    Vertex2Ids.forEachIndexed { index, id ->
        val angle = 2f * kotlin.math.PI.toFloat() * index / Vertex2Ids.size
        val x = centerX + radius * cos(angle)
        val y = centerY + radius * sin(angle)
        (Graph2.vertices as MutableMap)[id] = Vertex2(id, x, y)
    }
    

    
    val Edge2sToAdd = mutableListOf<Pair<String, String>>()

    while (degrees.values.any { it > 0 }) {
        
        val sortedDegrees = degrees.filter { it.value > 0 }.toList().sortedByDescending { it.second }

        if (sortedDegrees.isEmpty()) break

        val (highestId, highestDegree) = sortedDegrees.first()

        
        degrees.remove(highestId)

        
        val targets = sortedDegrees.drop(1).take(highestDegree)

        for ((targetId, _) in targets) {
            
            Edge2sToAdd.add(highestId to targetId)

            
            degrees[targetId] = degrees[targetId]!! - 1
        }
    }

    
    Edge2sToAdd.forEach { (source, target) ->
        (Graph2.Edge2s as MutableList).add(Edge2(source, target, 1.0f))
    }

    return Graph2
}

/**
 * Сохраняет граф в JSON файл
 */
fun saveGraph2ToFile(Graph2: Graph2, filePath: String) {
    val json = Json { prettyPrint = true }
    val jsonString = json.encodeToString(Graph2)
    File(filePath).writeText(jsonString)
}

fun main() {
    val degreeSequence = mapOf(
        "V0" to 16,
        "V1" to 9,
        "V2" to 10,
        "V3" to 6,
        "V4" to 3,
        "V5" to 4,
        "V6" to 4,
        "V7" to 4,
        "V8" to 5,
        "V9" to 2,
        "V10" to 3,
        "V11" to 1,
        "V12" to 2,
        "V13" to 5,
        "V14" to 2,
        "V15" to 2,
        "V16" to 2,
        "V17" to 2,
        "V18" to 2,
        "V19" to 3,
        "V20" to 2,
        "V21" to 2,
        "V22" to 2,
        "V23" to 5,
        "V24" to 3,
        "V25" to 3,
        "V26" to 2,
        "V27" to 4,
        "V28" to 3,
        "V29" to 4,
        "V30" to 4,
        "V31" to 6,
        "V32" to 12,
        "V33" to 17
    )

    val Graph2 = generateGraph2FromDegreeSequence(degreeSequence)

    saveGraph2ToFile(Graph2, "generated_Graph2.json")

    println("Граф сгенерирован и сохранён в generated_Graph2.json")
    println("Вершин: ${Graph2.vertices.size}")
    println("Рёбер: ${Graph2.Edge2s.size}")
}