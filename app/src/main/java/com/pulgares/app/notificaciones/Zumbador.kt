package com.pulgares.app.notificaciones

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * El zumbido físico: tres sacudidas cortas y una larga, como el nudge del viejo
 * Messenger. La vibración ES el zumbido; lo demás (pantalla temblando, monigote
 * gigante) es el atrezzo.
 */
object Zumbador {

    private val PATRON = longArrayOf(0, 90, 60, 90, 60, 90, 140, 400)

    fun zumba(context: Context) {
        val vibrador = if (Build.VERSION.SDK_INT >= 31) {
            val gestor = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            gestor.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (!vibrador.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= 26) {
            vibrador.vibrate(VibrationEffect.createWaveform(PATRON, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrador.vibrate(PATRON, -1)
        }
    }
}
