package com.example

import com.example.data.DiscoveryRepository
import com.example.model.DiscoveryCategory
import com.example.model.ExplorerRank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun repositoryContainsAll15DocumentedDiscoveriesAcross5Categories() {
        val items = DiscoveryRepository.discoveries
        assertEquals(15, items.size)

        val categories = DiscoveryCategory.entries.filter { it != DiscoveryCategory.ALL }
        assertEquals(5, categories.size)
        categories.forEach { cat ->
            assertEquals(3, items.count { it.category == cat })
        }

        val ids = items.map { it.id }.toSet()
        assertTrue("octopus_three_hearts" in ids)
        assertTrue("socotra_island" in ids)
        assertTrue("derinkuyu_underground_city" in ids)
        assertTrue("pink_lake_hillier" in ids)
        assertTrue("door_to_hell_darvaza" in ids)
        assertTrue("great_pyramid_giza" in ids)
    }

    @Test
    fun explorerRankThresholdsMatchSpec() {
        assertEquals(ExplorerRank.JUNIOR, ExplorerRank.fromPoints(0))
        assertEquals(ExplorerRank.JUNIOR, ExplorerRank.fromPoints(199))
        assertEquals(ExplorerRank.SENIOR, ExplorerRank.fromPoints(200))
        assertEquals(ExplorerRank.SENIOR, ExplorerRank.fromPoints(499))
        assertEquals(ExplorerRank.MASTER, ExplorerRank.fromPoints(500))
    }

    @Test
    fun quizAndSolomonStagesAreValid() {
        assertTrue(DiscoveryRepository.quizQuestions.size >= 10)
        DiscoveryRepository.quizQuestions.forEach { q ->
            assertTrue(q.correctIndex in q.options.indices)
        }
        assertEquals(5, DiscoveryRepository.solomonStages.size)
        DiscoveryRepository.solomonStages.forEach { stage ->
            assertTrue(stage.targetOuterIndex in stage.outerRingOptions.indices)
            assertTrue(stage.targetMiddleIndex in stage.middleRingOptions.indices)
            assertTrue(stage.targetInnerIndex in stage.innerRingOptions.indices)
        }
    }

    @Test
    fun speechReaderSpeedsVoicesAndHighlightingWork() {
        assertEquals(3, com.example.audio.SpeechSpeed.entries.size)
        assertEquals(3, com.example.audio.VoicePersona.entries.size)
        assertEquals("خلصت! أحسنت يا مستكشف", com.example.audio.OfflineSpeechController.OUTRO_PHRASE)

        val sample = "الأخطبوط والنار والذهب"
        val annotated = com.example.audio.buildHighlightedText(sample, 0..7)
        assertEquals(sample, annotated.text)
        assertEquals(1, annotated.spanStyles.size)
        assertEquals(0, annotated.spanStyles[0].start)
        assertEquals(8, annotated.spanStyles[0].end)
    }

    @Test
    fun smartArtistAutoMappingMatchesSubjectRules() {
        assertEquals(4, com.example.art.ArtStyleMode.entries.size)
        assertEquals(
            com.example.art.ArtStyleMode.REALISTIC,
            com.example.art.resolveEffectiveArtStyle(com.example.art.ArtStyleMode.AUTO, DiscoveryCategory.STRANGE_ANIMALS)
        )
        assertEquals(
            com.example.art.ArtStyleMode.FANTASY,
            com.example.art.resolveEffectiveArtStyle(com.example.art.ArtStyleMode.AUTO, DiscoveryCategory.MYSTERIOUS_PLACES)
        )
        assertEquals(
            com.example.art.ArtStyleMode.PENCIL,
            com.example.art.resolveEffectiveArtStyle(com.example.art.ArtStyleMode.AUTO, DiscoveryCategory.HISTORICAL_SECRETS)
        )
    }
}
