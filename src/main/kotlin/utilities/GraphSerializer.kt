 import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


fun saveGraphToFile(graph: SerializableGraph, filePath: String) {
    val json = Json { prettyPrint = true }
    val jsonString = json.encodeToString(graph)
    File(filePath).writeText(jsonString)
}


fun loadGraphFromFile(filePath: String): Graph? {
    val file = File(filePath)
    if (!file.exists()) return null
    val json = Json { prettyPrint = true }
    return json.decodeFromString<Graph>(file.readText())
}