package com.example.model

enum class AppTab(val labelAr: String) {
    DISCOVER("اكتشف"),
    PLAY("العب"),
    COLLECTION("مجموعتي"),
    ACCOUNT("حسابي")
}

enum class DiscoveryCategory(val titleAr: String, val badgeAr: String) {
    ALL("الكل", "الكل"),
    STRANGE_ANIMALS("حيوانات غريبة", "حيوان غريب"),
    MYSTERIOUS_PLACES("أماكن غامضة", "مكان غامض"),
    OPTICAL_ILLUSIONS("خدع بصرية", "خدعة بصرية"),
    MIND_RIDDLES("ألغاز العقل", "لغز عقلي"),
    HISTORICAL_SECRETS("أسرار تاريخية", "سر تاريخي")
}

enum class OpticalIllusionType {
    NONE,
    MULLER_LYER,
    HERMANN_GRID,
    SIMULTANEOUS_CONTRAST
}

enum class MindPuzzleType {
    NONE,
    MONTY_HALL,
    BIRTHDAY_PARADOX,
    ZEIGARNIK
}

data class DiscoveryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: DiscoveryCategory,
    val locationOrOrigin: String,
    val badgeTag: String,
    val summary: String,
    val facts: List<String>,
    val referenceNote: String,
    val pointsReward: Int = 15,
    val illusionType: OpticalIllusionType = OpticalIllusionType.NONE,
    val mindPuzzleType: MindPuzzleType = MindPuzzleType.NONE
)

enum class ExplorerRank(
    val titleAr: String,
    val subtitleAr: String,
    val minPoints: Int,
    val nextPoints: Int?
) {
    JUNIOR(
        titleAr = "مستكشف صغير",
        subtitleAr = "بداية رحلة البحث عن أسرار العالم",
        minPoints = 0,
        nextPoints = 200
    ),
    SENIOR(
        titleAr = "مستكشف كبير",
        subtitleAr = "حامل مفتاح المعرفة والأسرار الموثقة",
        minPoints = 200,
        nextPoints = 500
    ),
    MASTER(
        titleAr = "سيد الخاتم",
        subtitleAr = "أعلى رتبة في موسوعة خاتم سليمان",
        minPoints = 500,
        nextPoints = null
    );

    companion object {
        fun fromPoints(points: Int): ExplorerRank = when {
            points >= MASTER.minPoints -> MASTER
            points >= SENIOR.minPoints -> SENIOR
            else -> JUNIOR
        }
    }
}

enum class GameType(
    val titleAr: String,
    val subtitleAr: String,
    val rewardText: String
) {
    QUIZ(
        titleAr = "تحدي أسئلة",
        subtitleAr = "اختبر معرفتك بالاكتشافات الموثقة والأسرار",
        rewardText = "+10 لكل إجابة"
    ),
    MEMORY(
        titleAr = "لعبة ذاكرة",
        subtitleAr = "طابق بطاقات العجائب الذهبية بأقل عدد حركات",
        rewardText = "حتى +45 نقطة"
    ),
    SOLOMON_PUZZLE(
        titleAr = "لغز سليمان",
        subtitleAr = "حرّك حلقات الخاتم الذهبية لفك الشيفرة الملكية",
        rewardText = "+35 نقطة للمرحلة"
    ),
    SECRET_NUMBER(
        titleAr = "الرقم السري",
        subtitleAr = "اكتشف الرمز المكون من أرقام باستخدام منطق التطابق",
        rewardText = "حتى +50 نقطة"
    )
}

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class MemorySymbol(
    val id: String,
    val title: String,
    val glyph: String
)

data class SolomonRingStage(
    val stageNumber: Int,
    val title: String,
    val clue: String,
    val outerRingOptions: List<String>,
    val middleRingOptions: List<String>,
    val innerRingOptions: List<String>,
    val targetOuterIndex: Int,
    val targetMiddleIndex: Int,
    val targetInnerIndex: Int,
    val explanation: String
)

data class UserProgressState(
    val explorerName: String = "المستكشف",
    val points: Int = 0,
    val discoveredIds: Set<String> = emptySet(),
    val favoriteIds: Set<String> = emptySet(),
    val gamesWon: Int = 0,
    val quizBestScore: Int = 0,
    val memoryBestMoves: Int = 0,
    val secretCodesCracked: Int = 0,
    val solomonStagesCleared: Int = 0,
    val isDarkTheme: Boolean = true
) {
    val rank: ExplorerRank
        get() = ExplorerRank.fromPoints(points)
}
