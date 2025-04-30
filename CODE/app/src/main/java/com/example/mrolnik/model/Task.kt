import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val taskId: Int? = null,
    val taskName: String,
    val realizeDate: String,
    val description: String = "",
    val plannerId: Int
)
