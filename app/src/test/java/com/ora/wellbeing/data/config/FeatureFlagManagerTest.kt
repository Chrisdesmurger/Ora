package com.ora.wellbeing.data.config

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

/**
 * FIX(user-dynamic): Tests unitaires pour FeatureFlagManager
 *
 * Démontre comment tester le comportement des feature flags.
 */
class FeatureFlagManagerTest {

    private lateinit var context: Context
    private lateinit var featureFlagManager: FeatureFlagManager

    private val testFlagsJson = """
        {
          "version": "1.0.0-test",
          "description": "Test configuration",
          "flags": {
            "SYNC_STATS_FROM_ROOM": {
              "enabled": true,
              "description": "Test sync flag",
              "category": "data",
              "risk_level": "medium"
            },
            "ADVANCED_VIDEO_PLAYER": {
              "enabled": false,
              "description": "Test video flag",
              "category": "media",
              "risk_level": "high"
            },
            "DEBUG_LOGGING": {
              "enabled": true,
              "description": "Test debug flag",
              "category": "debug",
              "risk_level": "low"
            }
          }
        }
    """.trimIndent()

    @Before
    fun setup() {
        // Mock Android Context
        context = mockk(relaxed = true)

        // Mock assets pour retourner notre JSON de test
        val assetManager = mockk<android.content.res.AssetManager>(relaxed = true)
        every { context.assets } returns assetManager
        every { assetManager.open("flags.json") } returns ByteArrayInputStream(testFlagsJson.toByteArray())

        featureFlagManager = FeatureFlagManager(context)
    }

    @Test
    fun `isEnabled returns true for enabled flag`() {
        // FIX(user-dynamic): Test flag activé
        val result = featureFlagManager.isEnabled(FeatureFlag.SYNC_STATS_FROM_ROOM)

        assertTrue("SYNC_STATS_FROM_ROOM should be enabled", result)
    }

    @Test
    fun `isEnabled returns false for disabled flag`() {
        // FIX(user-dynamic): Test flag désactivé
        val result = featureFlagManager.isEnabled(FeatureFlag.ADVANCED_VIDEO_PLAYER)

        assertFalse("ADVANCED_VIDEO_PLAYER should be disabled", result)
    }

    @Test
    fun `isEnabled returns default value when flag not in JSON`() {
        // FIX(user-dynamic): Test fallback sur valeur par défaut
        // NETWORK_SYNC n'est pas dans notre testFlagsJson
        val result = featureFlagManager.isEnabled(FeatureFlag.NETWORK_SYNC)

        // Devrait retourner la valeur par défaut (false pour NETWORK_SYNC)
        assertEquals("Should return default value", FeatureFlag.NETWORK_SYNC.defaultValue, result)
    }

    @Test
    fun `getAllFlags returns all flags with correct states`() {
        // FIX(user-dynamic): Test récupération de tous les flags
        val allFlags = featureFlagManager.getAllFlags()

        assertNotNull("All flags should not be null", allFlags)
        assertTrue("Should contain SYNC_STATS_FROM_ROOM", allFlags.containsKey(FeatureFlag.SYNC_STATS_FROM_ROOM))
        assertTrue("Should contain ADVANCED_VIDEO_PLAYER", allFlags.containsKey(FeatureFlag.ADVANCED_VIDEO_PLAYER))

        // Vérifier les états
        assertEquals(true, allFlags[FeatureFlag.SYNC_STATS_FROM_ROOM])
        assertEquals(false, allFlags[FeatureFlag.ADVANCED_VIDEO_PLAYER])
    }

    @Test
    fun `getFlagMetadata returns correct metadata`() {
        // FIX(user-dynamic): Test récupération métadonnées
        val metadata = featureFlagManager.getFlagMetadata(FeatureFlag.SYNC_STATS_FROM_ROOM)

        assertNotNull("Metadata should not be null", metadata)
        assertEquals("Test sync flag", metadata?.description)
        assertEquals("data", metadata?.category)
        assertEquals("medium", metadata?.riskLevel)
    }

    @Test
    fun `reload refreshes flags cache`() {
        // FIX(user-dynamic): Test rechargement des flags
        // Premier chargement
        val firstResult = featureFlagManager.isEnabled(FeatureFlag.SYNC_STATS_FROM_ROOM)

        // Recharger
        featureFlagManager.reload()

        // Deuxième chargement (devrait relire le JSON)
        val secondResult = featureFlagManager.isEnabled(FeatureFlag.SYNC_STATS_FROM_ROOM)

        assertEquals("Results should be consistent after reload", firstResult, secondResult)
    }

    @Test
    fun `handles missing JSON file gracefully`() {
        // FIX(user-dynamic): Test comportement si fichier absent
        // Mock pour simuler fichier manquant
        val contextWithMissingFile = mockk<Context>(relaxed = true)
        val assetManager = mockk<android.content.res.AssetManager>(relaxed = true)
        every { contextWithMissingFile.assets } returns assetManager
        every { assetManager.open("flags.json") } throws java.io.FileNotFoundException()

        val manager = FeatureFlagManager(contextWithMissingFile)

        // Devrait fallback sur valeurs par défaut
        val result = manager.isEnabled(FeatureFlag.SYNC_STATS_FROM_ROOM)
        assertEquals("Should use default value when file missing", FeatureFlag.SYNC_STATS_FROM_ROOM.defaultValue, result)
    }

    @Test
    fun `handles malformed JSON gracefully`() {
        // FIX(user-dynamic): Test comportement JSON invalide
        val malformedJson = "{ invalid json }"

        val contextWithBadJson = mockk<Context>(relaxed = true)
        val assetManager = mockk<android.content.res.AssetManager>(relaxed = true)
        every { contextWithBadJson.assets } returns assetManager
        every { assetManager.open("flags.json") } returns ByteArrayInputStream(malformedJson.toByteArray())

        val manager = FeatureFlagManager(contextWithBadJson)

        // Devrait fallback sur valeurs par défaut sans crash
        val result = manager.isEnabled(FeatureFlag.DEBUG_LOGGING)
        assertEquals("Should use default value when JSON malformed", FeatureFlag.DEBUG_LOGGING.defaultValue, result)
    }

    @Test
    fun `all default values are safe`() {
        // FIX(user-dynamic): Test que les defaults sont sécurisés
        // Features à risque HIGH doivent être false par défaut
        assertFalse("High-risk ADVANCED_VIDEO_PLAYER should default to false",
            FeatureFlag.ADVANCED_VIDEO_PLAYER.defaultValue)

        assertFalse("High-risk NETWORK_SYNC should default to false",
            FeatureFlag.NETWORK_SYNC.defaultValue)

        // Features stables LOW peuvent être true
        assertTrue("Low-risk BADGE_SYSTEM can default to true",
            FeatureFlag.BADGE_SYSTEM.defaultValue)
    }

    @Test
    fun `fromKey finds correct flag`() {
        // FIX(user-dynamic): Test recherche par clé
        val flag = FeatureFlag.fromKey("SYNC_STATS_FROM_ROOM")

        assertNotNull("Should find flag by key", flag)
        assertEquals(FeatureFlag.SYNC_STATS_FROM_ROOM, flag)
    }

    @Test
    fun `fromKey returns null for unknown key`() {
        // FIX(user-dynamic): Test clé inconnue
        val flag = FeatureFlag.fromKey("UNKNOWN_FLAG_KEY")

        assertEquals(null, flag)
    }
}
