package com.example.ui.util

import java.util.Calendar
import java.util.Locale

data class JalaliDate(val year: Int, val month: Int, val day: Int) {
    fun format(): String = String.format(Locale.US, "%04d/%02d/%02d", year, month, day)

    fun formatPersian(): String = DateTimeUtils.toPersianDigits(format())

    fun formatWithMonthName(): String {
        val monthNames = arrayOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
        )
        val mName = if (month in 1..12) monthNames[month - 1] else ""
        return "${DateTimeUtils.toPersianDigits(day.toString())} $mName ${DateTimeUtils.toPersianDigits(year.toString())}"
    }

    fun toTimestamp(hour: Int = 0, minute: Int = 0, second: Int = 0): Long {
        val (gy, gm, gd) = JalaliCalendar.jalaliToGregorian(year, month, day)
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(gy, gm - 1, gd, hour, minute, second)
        return cal.timeInMillis
    }
}

object JalaliCalendar {
    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    fun fromTimestamp(timestamp: Long): JalaliDate {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val gy = cal.get(Calendar.YEAR)
        val gm = cal.get(Calendar.MONTH) + 1
        val gd = cal.get(Calendar.DAY_OF_MONTH)
        return gregorianToJalali(gy, gm, gd)
    }

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gy2 = gy - 1600
        val gm2 = gm - 1
        val gd2 = gd - 1

        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400

        for (i in 0 until gm2) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm2 > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd2

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        for (i in 0 until 11) {
            val daysInM = if (i < 6) 31 else 30
            if (jDayNo >= daysInM) {
                jDayNo -= daysInM
                jm = i + 1
            } else {
                break
            }
        }
        val jd = jDayNo + 1
        return JalaliDate(jy, jm + 1, jd)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy - 979
        val jm2 = jm - 1
        val jd2 = jd - 1

        var jDayNo = 365 * jy2 + (jy2 / 33) * 8 + (jy2 % 33 + 3) / 4
        for (i in 0 until jm2) {
            jDayNo += if (i < 6) 31 else 30
        }
        jDayNo += jd2

        var gDayNo = jDayNo + 79

        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524

            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        val gDaysInMonth = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var i = 0
        while (i < 12 && gDayNo >= gDaysInMonth[i]) {
            gDayNo -= gDaysInMonth[i]
            i++
        }
        val gm = i + 1
        val gd = gDayNo + 1

        return Triple(gy, gm, gd)
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            month == 12 -> if (isLeapYear(year)) 30 else 29
            else -> 30
        }
    }

    fun isLeapYear(jy: Int): Boolean {
        val jyNorm = jy - 979
        val d1 = 365 * jyNorm + (jyNorm / 33) * 8 + (jyNorm % 33 + 3) / 4
        val nextYear = jyNorm + 1
        val d2 = 365 * nextYear + (nextYear / 33) * 8 + (nextYear % 33 + 3) / 4
        return (d2 - d1) == 366
    }
}
