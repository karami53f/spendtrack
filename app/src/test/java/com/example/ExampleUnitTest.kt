package com.example

import com.example.ui.util.JalaliCalendar
import com.example.ui.util.JalaliDate
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
  @Test
  fun testJalaliConversion() {
    // 2026/11/11 in Gregorian is 1405/08/20 in Jalali
    val jDate = JalaliCalendar.gregorianToJalali(2026, 11, 11)
    println("2026/11/11 in Jalali = ${jDate.year}/${jDate.month}/${jDate.day}")
    assertEquals(1405, jDate.year)
    assertEquals(8, jDate.month)
    assertEquals(20, jDate.day)

    val (gy, gm, gd) = JalaliCalendar.jalaliToGregorian(1405, 8, 20)
    println("1405/8/20 in Gregorian = $gy/$gm/$gd")
    assertEquals(2026, gy)
    assertEquals(11, gm)
    assertEquals(11, gd)

    val ts = JalaliDate(1405, 8, 20).toTimestamp(12, 0)
    val convertedBack = JalaliCalendar.fromTimestamp(ts)
    println("Converted back = ${convertedBack.year}/${convertedBack.month}/${convertedBack.day}")
    assertEquals(1405, convertedBack.year)
    assertEquals(8, convertedBack.month)
    assertEquals(20, convertedBack.day)
  }

  @Test
  fun testRoundTripAllYears() {
    // Test all years from 1395 to 1420
    for (year in 1395..1420) {
      for (month in 1..12) {
        val daysInMonth = JalaliCalendar.getDaysInMonth(year, month)
        val testDays = listOf(1, 15, daysInMonth)
        for (day in testDays) {
          val (gy, gm, gd) = JalaliCalendar.jalaliToGregorian(year, month, day)
          val back = JalaliCalendar.gregorianToJalali(gy, gm, gd)
          assertEquals("Failed on $year/$month/$day", year, back.year)
          assertEquals("Failed on $year/$month/$day", month, back.month)
          assertEquals("Failed on $year/$month/$day", day, back.day)

          // Also test timestamp round-trip
          val ts = JalaliDate(year, month, day).toTimestamp(10, 30, 0)
          val fromTs = JalaliCalendar.fromTimestamp(ts)
          assertEquals("TS failed on $year/$month/$day", year, fromTs.year)
          assertEquals("TS failed on $year/$month/$day", month, fromTs.month)
          assertEquals("TS failed on $year/$month/$day", day, fromTs.day)
        }
      }
    }
  }

  @Test
  fun testLeapYears() {
    assertTrue("1403 must be leap", JalaliCalendar.isLeapYear(1403))
    assertFalse("1404 is not leap", JalaliCalendar.isLeapYear(1404))
    assertFalse("1405 is not leap", JalaliCalendar.isLeapYear(1405))
    assertFalse("1407 is not leap", JalaliCalendar.isLeapYear(1407))
    assertTrue("1408 must be leap", JalaliCalendar.isLeapYear(1408))
  }
}
