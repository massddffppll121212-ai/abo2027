package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("خاتم سليمان", appName)
  }

  @Test
  fun `offline progress store persists points and discoveries`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val store = com.example.data.OfflineProgressStore(context)
    val saved = com.example.model.UserProgressState(
      explorerName = "سليمان",
      points = 250,
      discoveredIds = setOf("octopus_three_hearts", "socotra_island")
    )
    store.save(saved)
    val loaded = store.load()
    assertEquals("سليمان", loaded.explorerName)
    assertEquals(250, loaded.points)
    assertEquals(com.example.model.ExplorerRank.SENIOR, loaded.rank)
    assertEquals(2, loaded.discoveredIds.size)
  }
}
