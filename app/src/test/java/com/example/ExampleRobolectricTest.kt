package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.BePresentDatabase
import com.example.data.repository.BePresentRepository
import com.example.ui.viewmodel.BePresentViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    assertEquals("BePresent", appName)
  }

  @Test
  fun `test database and viewmodel initialization`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = Room.inMemoryDatabaseBuilder(
        context,
        BePresentDatabase::class.java
    ).allowMainThreadQueries().build()

    val repository = BePresentRepository(database)
    val viewModel = BePresentViewModel(repository)
    assertNotNull(viewModel)
    database.close()
  }
}
