package com.iyas.budgetin.data.preferences

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

class AppPreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "budgetin_prefs"
        private const val KEY_LAST_APP_MONTH = "last_app_month"
        private const val KEY_LAST_APP_YEAR = "last_app_year"
        private const val KEY_HISTORY_MONTH = "history_selected_month"
        private const val KEY_HISTORY_YEAR = "history_selected_year"
        private const val KEY_CHARTS_MONTH = "charts_selected_month" // -1 means null (all months)
        private const val KEY_CHARTS_YEAR = "charts_selected_year"
    }

    init {
        checkAndSyncMonthRollover()
    }

    /**
     * Checks if the calendar month or year has changed compared to the last recorded app session.
     * When a new month rolls in (or on initial setup), resets default filters to the current month & year.
     * Returns true if a rollover occurred and filters were reset, false otherwise.
     */
    fun checkAndSyncMonthRollover(): Boolean {
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        val lastMonth = prefs.getInt(KEY_LAST_APP_MONTH, -1)
        val lastYear = prefs.getInt(KEY_LAST_APP_YEAR, -1)

        if (lastMonth != currentMonth || lastYear != currentYear) {
            prefs.edit()
                .putInt(KEY_LAST_APP_MONTH, currentMonth)
                .putInt(KEY_LAST_APP_YEAR, currentYear)
                .putInt(KEY_HISTORY_MONTH, currentMonth)
                .putInt(KEY_HISTORY_YEAR, currentYear)
                .putInt(KEY_CHARTS_MONTH, currentMonth)
                .putInt(KEY_CHARTS_YEAR, currentYear)
                .apply()
            return true
        }
        return false
    }

    fun getHistorySelectedMonth(): Int {
        checkAndSyncMonthRollover()
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        return prefs.getInt(KEY_HISTORY_MONTH, currentMonth)
    }

    fun getHistorySelectedYear(): Int {
        checkAndSyncMonthRollover()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return prefs.getInt(KEY_HISTORY_YEAR, currentYear)
    }

    fun setHistorySelectedMonth(month: Int) {
        prefs.edit().putInt(KEY_HISTORY_MONTH, month).apply()
    }

    fun setHistorySelectedYear(year: Int) {
        prefs.edit().putInt(KEY_HISTORY_YEAR, year).apply()
    }

    fun getChartsSelectedMonth(): Int? {
        checkAndSyncMonthRollover()
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val month = prefs.getInt(KEY_CHARTS_MONTH, currentMonth)
        return if (month == -1) null else month
    }

    fun getChartsSelectedYear(): Int {
        checkAndSyncMonthRollover()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return prefs.getInt(KEY_CHARTS_YEAR, currentYear)
    }

    fun setChartsSelectedMonth(month: Int?) {
        prefs.edit().putInt(KEY_CHARTS_MONTH, month ?: -1).apply()
    }

    fun setChartsSelectedYear(year: Int) {
        prefs.edit().putInt(KEY_CHARTS_YEAR, year).apply()
    }
}
