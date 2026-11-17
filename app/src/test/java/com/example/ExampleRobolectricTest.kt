package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AppSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context and verify automotive sections`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Car Audio DSP Pro", appName)
        assertEquals(9, AppSection.entries.size)
        assertFalse(AppSection.entries.any { it.route.contains("radio", ignoreCase = true) })
    }
}
