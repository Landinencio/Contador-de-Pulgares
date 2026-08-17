package com.pulgares.app.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pulgares.app.MainActivity
import com.pulgares.app.R
import com.pulgares.app.avatar.Monigote
import com.pulgares.app.avatar.dibujaMonigote
import com.pulgares.app.frases.Frases
import com.pulgares.app.frases.Momento

/**
 * El Cobrador del Frac: el caballero con chistera que aparece en la barra de
 * notificaciones cuando debes dinero. Elegante y NO pesado: la cadencia la
 * decide [DecisionCobrador], nunca esta clase.
 */
object Cobrador {

    private const val CANAL = "cobrador"
    private const val AVISO_ID = 1931  // el año en que se estrenó Luces de Bohemia, por ponerle algo

    /** Lo que se le enseña al usuario. */
    data class Aviso(val titulo: String, val texto: String)

    /**
     * Redacta el aviso. Va aparte de [notifica] para poder probarlo sin Android:
     * la frase sale del catálogo del cobrador con el importe y los días reales.
     */
    fun redacta(deboCentimos: Long, diasDeuda: Int, semilla: Long): Aviso {
        val titulo = when {
            diasDeuda >= 60 -> "El Cobrador del Frac ya os conoce 🎩"
            diasDeuda >= 15 -> "El Cobrador del Frac insiste 🎩"
            else -> "El Cobrador del Frac 🎩"
        }
        val texto = Frases.para(
            Momento.COBRADOR,
            centimos = deboCentimos,
            dias = diasDeuda,
            semilla = semilla
        )
        return Aviso(titulo, texto)
    }

    /**
     * Publica el aviso con el retrato del cobrador. Si el usuario no ha dado el
     * permiso de notificaciones, no se hace nada y no pasa nada.
     */
    fun notifica(context: Context, aviso: Aviso) {
        val gestor = NotificationManagerCompat.from(context)
        if (!gestor.areNotificationsEnabled()) return

        preparaCanal(context)

        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(R.drawable.ic_cobrador_notif)
            .setLargeIcon(retrato())
            .setContentTitle(aviso.titulo)
            .setContentText(aviso.texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(aviso.texto))
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        runCatching { gestor.notify(AVISO_ID, notificacion) }
    }

    private fun preparaCanal(context: Context) {
        val canal = NotificationChannel(
            CANAL,
            "El Cobrador del Frac",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Recordatorios elegantes de lo que debes. Como mucho, uno cada dos días."
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    /**
     * El retrato del cobrador, dibujado con el mismo código del avatar de la
     * app. Sin imágenes: el monigote de la notificación es EL monigote.
     */
    fun retrato(lado: Int = 256): Bitmap {
        val bitmap = Bitmap.createBitmap(lado, lado, Bitmap.Config.ARGB_8888)
        CanvasDrawScope().draw(
            Density(1f),
            LayoutDirection.Ltr,
            Canvas(bitmap.asImageBitmap()),
            Size(lado.toFloat(), lado.toFloat())
        ) {
            dibujaMonigote(Monigote.ELCOBRADOR, conFondo = true, bailoteo = 0f)
        }
        return bitmap
    }
}
