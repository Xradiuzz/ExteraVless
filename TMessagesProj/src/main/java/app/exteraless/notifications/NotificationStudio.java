package app.exteraless.notifications;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;

import org.telegram.messenger.ApplicationLoader;
import app.exteraless.roflan.RoflanSuite;

import java.util.List;

/** User-facing notification presentation preferences. Sound routing remains Telegram-native. */
public final class NotificationStudio {
    public static final String[] STYLE_NAMES = {
            "Оригинальный Telegram", "Крупный текст", "Список сообщений",
            "Диалог", "Карточка с аватаром", "Компактный"
    };

    private static final String PREFS = "exteraless_notification_studio";
    private static final String KEY_STYLE = "style";
    private static final String KEY_CAPTION = "caption";

    private NotificationStudio() {}

    private static android.content.SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static int getStyle() {
        return Math.max(0, Math.min(STYLE_NAMES.length - 1, prefs().getInt(KEY_STYLE, 0)));
    }

    public static void setStyle(int style) {
        prefs().edit().putInt(KEY_STYLE, Math.max(0, Math.min(STYLE_NAMES.length - 1, style))).apply();
    }

    public static String getCaption() {
        return prefs().getString(KEY_CAPTION, "");
    }

    public static void setCaption(String caption) {
        String value = caption == null ? "" : caption.trim();
        if (value.length() > 64) value = value.substring(0, 64);
        prefs().edit().putString(KEY_CAPTION, value).apply();
    }

    public static void apply(NotificationCompat.Builder builder, String title, String body, Bitmap avatar) {
        if (builder == null) return;
        int automation = RoflanSuite.matchRule(body);
        if (automation == 0) builder.setColor(0xFF36A878).setPriority(NotificationCompat.PRIORITY_HIGH);
        else if (automation == 1) builder.setSilent(true);
        else if (automation == 2) builder.setPriority(NotificationCompat.PRIORITY_LOW);
        String caption = getCaption();
        if (!caption.isEmpty()) builder.setSubText(caption);
        else if (RoflanSuite.inboxEnabled()) {
            String[] categories = {"Важное", "Требует ответа", "Шум", "Позже"};
            builder.setSubText("Smart Inbox · " + categories[RoflanSuite.classify(body, false, true)]);
        } else builder.setSubText(null);

        switch (getStyle()) {
            case 1:
                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(title)
                        .bigText(body == null ? "" : body));
                break;
            case 2:
                builder.setStyle(new NotificationCompat.InboxStyle()
                        .setBigContentTitle(title)
                        .addLine(body == null ? "" : body)
                        .setSummaryText(caption.isEmpty() ? title : caption));
                break;
            case 3: {
                Person sender = new Person.Builder().setName(title == null ? "Telegram" : title).build();
                NotificationCompat.MessagingStyle style = new NotificationCompat.MessagingStyle(sender);
                style.setConversationTitle(title);
                style.addMessage(body == null ? "" : body, System.currentTimeMillis(), sender);
                builder.setStyle(style);
                break;
            }
            case 4:
                if (avatar != null) {
                    builder.setStyle(new NotificationCompat.BigPictureStyle()
                            .setBigContentTitle(title)
                            .bigPicture(avatar)
                            .bigLargeIcon((Bitmap) null));
                } else {
                    builder.setStyle(new NotificationCompat.BigTextStyle()
                            .setBigContentTitle(title)
                            .bigText(body == null ? "" : body));
                }
                break;
            case 5:
                builder.setStyle(null);
                break;
            default:
                // Keep the exact style selected by Telegram itself.
                break;
        }
    }
}
