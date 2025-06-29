package pe.chalk.bukkit.babyeternal

data class Registration(val id: String, val owner: String) {
  companion object {
    fun fromMap(map: Map<*, *>): Registration {
      return Registration(
              id = map["id"] as? String ?: error("Missing 'id' in map"),
              owner = map["owner"] as? String ?: error("Missing 'owner' in map")
      )
    }
  }

  fun toMap(): Map<String, String> {
    return mapOf("id" to id, "owner" to owner)
  }
}
