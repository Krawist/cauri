package com.cauri.core.capture.reminder

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.cauri.core.capture.R
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.repository.CategoryRepository
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.format.readableName
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/** Default implementation for [ReminderNotifier]. */
// One builder per notification and per intent it sends.
@Suppress("TooManyFunctions")
@Singleton
internal class ReminderNotifierImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val categories: CategoryRepository,
) : ReminderNotifier {
    override suspend fun show(operation: PendingOperation): Boolean {
        val manager = channel() ?: return false
        val name = operation.counterparty?.let(::readableName)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(DesignR.drawable.logo_coin_silhouette)
            .setColor(ContextCompat.getColor(context, DesignR.color.notification_accent))
            .setContentIntent(openHome())
            .setAutoCancel(true)
        if (operation.stayMinutes != null) visit(builder, operation, name) else payment(builder, operation, name)
        // The permission is checked just before.
        @Suppress("MissingPermission")
        manager.notify(notificationId(operation.transactionId), builder.build())
        return true
    }

    override suspend fun showSummary(count: Int): Boolean {
        val manager = channel() ?: return false
        val open = openSummary()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(DesignR.drawable.logo_coin_silhouette)
            .setColor(ContextCompat.getColor(context, DesignR.color.notification_accent))
            .setContentTitle(context.resources.getQuantityString(R.plurals.reminder_summary_title, count, count))
            .setContentText(context.getString(R.string.reminder_summary_text))
            .setContentIntent(open)
            .addAction(0, context.getString(R.string.reminder_open), open)
            .setAutoCancel(true)
        // The permission is checked just before.
        @Suppress("MissingPermission")
        manager.notify(SUMMARY_NOTIFICATION, builder.build())
        return true
    }

    override fun dismiss(transactionId: Long) =
        NotificationManagerCompat.from(context).cancel(notificationId(transactionId))

    // « Marché Mokolo : combien as-tu dépensé ? », the usual amount, « Rien » and « Autre montant ».
    private suspend fun visit(builder: NotificationCompat.Builder, operation: PendingOperation, name: String?) {
        val category = operation.categoryId?.let { id -> categories.categories.first().firstOrNull { it.id == id } }
        val details = listOfNotNull(
            context.getString(R.string.reminder_visit_stay, operation.stayMinutes ?: 0),
            category?.name,
            context.getString(R.string.reminder_cash),
        ).joinToString(" · ")
        builder.setContentTitle(
            context.getString(R.string.reminder_visit_title, name.orEmpty()),
        ).setContentText(details)
        operation.proposedAmount?.let { amount ->
            builder.addAction(
                0,
                context.getString(R.string.reminder_amount, formatFcfa(amount)),
                reply(operation.transactionId, ReminderReplyReceiver.Reply.AMOUNT) {
                    putExtra(ReminderReplyReceiver.EXTRA_AMOUNT, amount)
                },
            )
        }
        builder.addAction(
            0,
            context.getString(R.string.reminder_nothing),
            reply(operation.transactionId, ReminderReplyReceiver.Reply.NOTHING) {},
        )
        val input = RemoteInput.Builder(ReminderReplyReceiver.EXTRA_TYPED)
            .setLabel(context.getString(R.string.reminder_other_amount_hint))
            .build()
        builder.addAction(
            NotificationCompat.Action.Builder(
                0,
                context.getString(R.string.reminder_other_amount),
                // The typed amount is filled in by Android: the intent stays mutable.
                reply(operation.transactionId, ReminderReplyReceiver.Reply.TYPED, mutable = true) {},
            ).addRemoteInput(input).build(),
        )
    }

    // « 5 000 F payés à Boulangerie Saker », « MoMo · c'est quoi ? », the two likely categories and « Autre », which
    // opens the card's edit sheet.
    private suspend fun payment(builder: NotificationCompat.Builder, operation: PendingOperation, name: String?) {
        val amount = formatFcfa(operation.amount + operation.fees)
        builder.setContentTitle(
            name?.let { context.getString(R.string.reminder_payment_title, amount, it) }
                ?: context.getString(R.string.reminder_payment_title_alone, amount),
        ).setContentText(context.getString(R.string.reminder_payment_text, operation.accountName))
        val likely: List<Category> = categories.predicted(operation = operation, limit = CATEGORIES)
        likely.forEachIndexed { index, category ->
            builder.addAction(
                0,
                category.name,
                reply(operation.transactionId, ReminderReplyReceiver.Reply.CATEGORY, choice = index) {
                    putExtra(ReminderReplyReceiver.EXTRA_CATEGORY, category.id)
                    putExtra(ReminderReplyReceiver.EXTRA_PROPOSED_CATEGORY, likely.first().id)
                },
            )
        }
        builder.addAction(0, context.getString(R.string.reminder_other_category), openEdit(operation.transactionId))
    }

    // Opens the app on the edit sheet of the card [transactionId]; the app clears the notification.
    private fun openEdit(transactionId: Long): PendingIntent {
        val intent = checkNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName)) {
            "Cauri has a launcher activity"
        }.setAction(EditFromNotification.ACTION_EDIT_TRANSACTION)
            .putExtra(EditFromNotification.EXTRA_TRANSACTION, transactionId)
            .putExtra(EditFromNotification.EXTRA_NOTIFICATION, notificationId(transactionId))
        return PendingIntent.getActivity(
            context,
            notificationId(transactionId),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    // The answer of a button, to the receiver; one request per card, answer and [choice] among the buttons of the same
    // answer, so that none replaces another.
    private fun reply(
        transactionId: Long,
        reply: ReminderReplyReceiver.Reply,
        mutable: Boolean = false,
        choice: Int = 0,
        extras: Intent.() -> Unit,
    ): PendingIntent {
        val intent = Intent(context, ReminderReplyReceiver::class.java)
            .setAction("${ReminderReplyReceiver.ACTION}.${reply.name}.$choice.$transactionId")
            .putExtra(ReminderReplyReceiver.EXTRA_TRANSACTION, transactionId)
            .putExtra(ReminderReplyReceiver.EXTRA_REPLY, reply.name)
            .apply(extras)
        val flags = if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(
            context,
            (notificationId(transactionId) * REPLIES + reply.ordinal) * CATEGORIES + choice,
            intent,
            flags or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    // The manager once the channel exists; `null` when Cauri may not show notifications.
    private fun channel(): NotificationManagerCompat? {
        val manager = NotificationManagerCompat.from(context)
        val allowed = manager.areNotificationsEnabled() &&
            (
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
                )
        if (!allowed) return null
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName(context.getString(R.string.reminder_channel))
                .setDescription(context.getString(R.string.reminder_channel_description))
                .build(),
        )
        return manager
    }

    // Opens the app on « Ce soir ».
    private fun openSummary(): PendingIntent {
        val intent = checkNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName)) {
            "Cauri has a launcher activity"
        }.setAction(EveningSummary.ACTION_OPEN_SUMMARY)
        return PendingIntent.getActivity(
            context,
            SUMMARY_NOTIFICATION,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    // Opens the app on Accueil, where the card waits.
    private fun openHome(): PendingIntent {
        val intent = checkNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName)) {
            "Cauri has a launcher activity"
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun notificationId(transactionId: Long) = (NOTIFICATION_BASE + transactionId % NOTIFICATION_SPAN).toInt()

    private companion object {
        const val CHANNEL_ID = "reminders"

        // Two categories and « Autre »: Android shows three buttons at most.
        const val CATEGORIES = 2
        const val REPLIES = 4
        const val SUMMARY_NOTIFICATION = 9_999
        const val NOTIFICATION_BASE = 10_000L
        const val NOTIFICATION_SPAN = 100_000L
    }
}
