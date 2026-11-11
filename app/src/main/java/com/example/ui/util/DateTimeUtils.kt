package com.example.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)
    private val tomanNumberFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))

    fun toPersianDigits(input: String): String {
        val persianDigits = arrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val builder = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                builder.append(persianDigits[ch - '0'])
            } else {
                builder.append(ch)
            }
        }
        return builder.toString()
    }

    fun formatDateTime(timestamp: Long): String {
        val jDate = JalaliCalendar.fromTimestamp(timestamp)
        val timeStr = toPersianDigits(timeFormatter.format(Date(timestamp)))
        return "${jDate.formatPersian()} • $timeStr"
    }

    fun formatDate(timestamp: Long): String {
        val jDate = JalaliCalendar.fromTimestamp(timestamp)
        return jDate.formatPersian()
    }

    fun formatDateWithMonthName(timestamp: Long): String {
        val jDate = JalaliCalendar.fromTimestamp(timestamp)
        return jDate.formatWithMonthName()
    }

    fun formatTime(timestamp: Long): String {
        return toPersianDigits(timeFormatter.format(Date(timestamp)))
    }

    fun formatToman(amount: Long): String {
        val raw = tomanNumberFormat.format(amount)
        return "${toPersianDigits(raw)} تومان"
    }

    fun formatTomanRaw(amount: Long): String {
        return toPersianDigits(tomanNumberFormat.format(amount))
    }

    fun getIconForName(iconName: String): ImageVector {
        val normalized = iconName.lowercase(Locale.ROOT)
        return when {
            normalized.contains("بنزین") || normalized.contains("gas") || normalized.contains("سوخت") -> Icons.Default.LocalGasStation
            normalized.contains("پیتزا") || normalized.contains("pizza") -> Icons.Default.LocalPizza
            normalized.contains("ماشین") || normalized.contains("تعمیر") || normalized.contains("هزینه‌های ماشین") || normalized.contains("هزینه های ماشین") -> Icons.Default.Build
            normalized.contains("نان") || normalized.contains("restaurant") || normalized.contains("غذا") -> Icons.Default.Restaurant
            normalized.contains("شیر") || normalized.contains("قهوه") || normalized.contains("cafe") -> Icons.Default.LocalCafe
            normalized.contains("ناهار") || normalized.contains("شام") || normalized.contains("fastfood") -> Icons.Default.Fastfood
            normalized.contains("سوپرمارکت") || normalized.contains("خرید") || normalized.contains("shopping_cart") -> Icons.Default.ShoppingCart
            normalized.contains("اینترنت") || normalized.contains("wifi") -> Icons.Default.Wifi
            normalized.contains("تسویه") || normalized.contains("حقوق") || normalized.contains("بدهی") || normalized.contains("payments") -> Icons.Default.Payments
            normalized.contains("باشگاه") || normalized.contains("ورزش") || normalized.contains("gym") -> Icons.Default.FitnessCenter
            else -> Icons.Default.ShoppingBag
        }
    }
}
