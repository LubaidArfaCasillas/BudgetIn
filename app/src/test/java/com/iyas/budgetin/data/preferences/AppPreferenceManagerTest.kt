package com.iyas.budgetin.data.preferences

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class AppPreferenceManagerTest {

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var fakeContext: FakeContext
    private lateinit var preferenceManager: AppPreferenceManager

    @Before
    fun setup() {
        fakePrefs = FakeSharedPreferences()
        fakeContext = FakeContext(fakePrefs)
        preferenceManager = AppPreferenceManager(fakeContext)
    }

    @Test
    fun `test initial month defaults to current month on first open`() {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        assertEquals(currentMonth, preferenceManager.getHistorySelectedMonth())
        assertEquals(currentYear, preferenceManager.getHistorySelectedYear())
        assertEquals(currentMonth, preferenceManager.getChartsSelectedMonth())
        assertEquals(currentYear, preferenceManager.getChartsSelectedYear())
    }

    @Test
    fun `test independent month persistence between History and Charts`() {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // Set History to August (7)
        val historyMonth = if (currentMonth > 0) currentMonth - 1 else 11
        preferenceManager.setHistorySelectedMonth(historyMonth)
        preferenceManager.setHistorySelectedYear(currentYear)

        // Verify History is updated, Charts remains currentMonth
        assertEquals(historyMonth, preferenceManager.getHistorySelectedMonth())
        assertEquals(currentMonth, preferenceManager.getChartsSelectedMonth())

        // Set Charts to null (all months)
        preferenceManager.setChartsSelectedMonth(null)
        assertNull(preferenceManager.getChartsSelectedMonth())
        assertEquals(historyMonth, preferenceManager.getHistorySelectedMonth())
    }

    @Test
    fun `test month rollover resets screens to new current month`() {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // Simulate user picking August for history
        val prevMonth = if (currentMonth > 0) currentMonth - 1 else 11
        val prevYear = if (currentMonth > 0) currentYear else currentYear - 1

        // Manually simulate old stored state from previous month
        fakePrefs.edit()
            .putInt("last_app_month", prevMonth)
            .putInt("last_app_year", prevYear)
            .putInt("history_selected_month", prevMonth)
            .putInt("history_selected_year", prevYear)
            .putInt("charts_selected_month", prevMonth)
            .putInt("charts_selected_year", prevYear)
            .apply()

        // When a new month is detected
        val rolledOver = preferenceManager.checkAndSyncMonthRollover()
        assertTrue("Rollover should have been detected", rolledOver)

        // History and Charts should now automatically default to currentMonth and currentYear
        assertEquals(currentMonth, preferenceManager.getHistorySelectedMonth())
        assertEquals(currentYear, preferenceManager.getHistorySelectedYear())
        assertEquals(currentMonth, preferenceManager.getChartsSelectedMonth())
        assertEquals(currentYear, preferenceManager.getChartsSelectedYear())
    }

    // In-memory fake SharedPreferences for pure JVM unit testing
    class FakeSharedPreferences : SharedPreferences {
        private val map = mutableMapOf<String, Any>()

        override fun getAll(): MutableMap<String, *> = HashMap(map)
        override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            @Suppress("UNCHECKED_CAST") (map[key] as? MutableSet<String> ?: defValues)
        override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
        override fun contains(key: String?): Boolean = map.containsKey(key)
        override fun edit(): SharedPreferences.Editor = FakeEditor(map)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        class FakeEditor(private val map: MutableMap<String, Any>) : SharedPreferences.Editor {
            private val temp = mutableMapOf<String, Any?>()
            private var clearAll = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) temp[key] = value
                return this
            }
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
                if (key != null) temp[key] = values
                return this
            }
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                if (key != null) temp[key] = value
                return this
            }
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                if (key != null) temp[key] = value
                return this
            }
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                if (key != null) temp[key] = value
                return this
            }
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                if (key != null) temp[key] = value
                return this
            }
            override fun remove(key: String?): SharedPreferences.Editor {
                if (key != null) temp[key] = null
                return this
            }
            override fun clear(): SharedPreferences.Editor {
                clearAll = true
                return this
            }
            override fun commit(): Boolean {
                apply()
                return true
            }
            override fun apply() {
                if (clearAll) map.clear()
                temp.forEach { (k, v) ->
                    if (v == null) map.remove(k) else map[k] = v
                }
            }
        }
    }

    class FakeContext(private val prefs: SharedPreferences) : android.content.ContextWrapper(null) {
        override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = prefs
    }
}
