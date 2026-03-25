package com.ileader.app.ui.components

private val BASE = "https://ileader.kz/img"
private val UNSPLASH = "https://images.unsplash.com"

private val SPORT_IMAGES_LIST = mapOf(
    "картинг" to listOf(
        "$BASE/karting/karting-01-1280x719.jpeg",
        "$BASE/karting/karting-04-1280x853.jpeg",
        "$BASE/karting/karting-05-1280x719.jpeg",
        "$BASE/karting/karting-07-1280x853.jpeg",
        "$BASE/karting/karting-12-1280x853.jpeg",
        "$BASE/karting/karting-13-1280x853.jpeg",
        "$BASE/karting/karting-15-1280x853.jpeg",
        "$BASE/karting/karting-16-1280x853.jpeg",
        "$BASE/karting/karting-18-1280x852.jpeg",
        "$BASE/karting/karting-20-1280x853.jpeg",
    ),
    "стрельба" to listOf(
        "$BASE/shooting/shooting-01-1280x853.jpeg",
        "$BASE/shooting/shooting-02-1280x853.jpeg",
        "$BASE/shooting/shooting-04-1280x853.jpeg",
        "$BASE/shooting/shooting-05-1280x853.jpeg",
        "$BASE/shooting/shooting-06-1280x853.jpeg",
        "$BASE/shooting/shooting-07-1280x853.jpeg",
        "$BASE/shooting/shooting-08-1280x853.jpeg",
        "$BASE/shooting/shooting-09-1280x853.jpeg",
        "$BASE/shooting/shooting-11-1280x853.jpeg",
    ),
    "лёгкая атлетика" to listOf(
        "$UNSPLASH/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1552674605-db6ffd4facb5?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1571008887538-b36bb32f4571?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1530549387789-4c1017266635?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1541534741688-6078c6bfb5c5?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1476480862126-209bfaa8edc8?w=1280&q=80&fit=crop",
    ),
    "легкая атлетика" to listOf(
        "$UNSPLASH/photo-1461896836934-ffe607ba8211?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1552674605-db6ffd4facb5?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1571008887538-b36bb32f4571?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1530549387789-4c1017266635?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1541534741688-6078c6bfb5c5?w=1280&q=80&fit=crop",
        "$UNSPLASH/photo-1476480862126-209bfaa8edc8?w=1280&q=80&fit=crop",
    ),
)

fun sportImageUrl(sportName: String, seed: Int = 0): String? {
    val key = sportName.lowercase().trim()
    val list = SPORT_IMAGES_LIST[key] ?: return null
    return list[seed.mod(list.size)]
}

fun tournamentImageUrl(sportName: String, imageUrl: String?, seed: Int = 0): String? =
    sportImageUrl(sportName, seed) ?: imageUrl.takeIf { !it.isNullOrEmpty() }
