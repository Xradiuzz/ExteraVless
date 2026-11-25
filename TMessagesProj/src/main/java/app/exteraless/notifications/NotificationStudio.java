package app.exteraless.notifications;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;

import org.telegram.messenger.ApplicationLoader;
import app.exteraless.roflan.RoflanSuite;
import app.exteraless.roflan.RoflanExtraTools;

import java.util.List;

/** User-facing notification presentation preferences. Sound routing remains Telegram-native. */
public final class NotificationStudio {
    public static final String DEFAULT_CAPTION = "Здесь могла быть ваша реклама";
    public static final String[] STYLE_NAMES = {
            "Оригинальный Telegram", "Крупный текст", "Список сообщений",
            "Диалог", "Карточка с аватаром", "Компактный",
            "Минималистичный", "Акцентный", "Мой стиль"
    };

    private static final String PREFS = "exteraless_notification_studio";
    private static final String KEY_STYLE = "style";
    private static final String KEY_CAPTION = "caption";
    private static final String KEY_ACCENT = "custom_accent";
    private static final String KEY_CUSTOM_LAYOUT = "custom_layout";

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
        return prefs().getString(KEY_CAPTION, DEFAULT_CAPTION);
    }

    public static void setCaption(String caption) {
        String value = caption == null ? DEFAULT_CAPTION : caption.trim();
        if (value.isEmpty()) value = DEFAULT_CAPTION;
        if (value.length() > 64) value = value.substring(0, 64);
        prefs().edit().putString(KEY_CAPTION, value).apply();
    }

    public static int getCustomAccentColor() {
        return prefs().getInt(KEY_ACCENT, 0xFFFFFFFF);
    }

    public static void setCustomAccentColor(int color) {
        prefs().edit().putInt(KEY_ACCENT, color).apply();
    }

    public static int getCustomLayout() {
        return Math.max(0, Math.min(3, prefs().getInt(KEY_CUSTOM_LAYOUT, 1)));
    }

    public static void setCustomLayout(int layout) {
        prefs().edit().putInt(KEY_CUSTOM_LAYOUT, Math.max(0, Math.min(3, layout))).apply();
    }

    public static String previewBody(int style) {
        switch (style) {
            case 1: return "Расширенный текст: привет! Посмотри, я отправил тебе несколько фотографий.";
            case 2: return "• Привет!  • Как дела?  • Увидимся вечером?";
            case 3: return "Алексей: Привет! Как насчёт встретиться сегодня?";
            case 4: return "Карточка с аватаром и расширенным содержимым сообщения";
            case 5: return "Привет!";
            case 6: return "Короткое уведомление без лишних деталей.";
            case 7: return "Акцентный стиль с выбранным цветом.";
            case 8: return getCustomLayout() == 0 ? "Мой стиль · оригинальный макет" : getCustomLayout() == 2 ? "Мой стиль · список сообщений" : getCustomLayout() == 3 ? "Мой стиль · диалог" : "Мой стиль · крупный текст";
            default: return "Привет! Это пример уведомления в стиле Telegram.";
        }
    }

    public static void apply(NotificationCompat.Builder builder, String title, String body, Bitmap avatar) {
        if (builder == null) return;
        RoflanExtraTools.record(title, body);
        int automation = RoflanSuite.matchRule(body);
        if (automation == 0) builder.setColor(0xFF36A878).setPriority(NotificationCompat.PRIORITY_HIGH);
        else if (automation == 1) builder.setSilent(true);
        else if (automation == 2) builder.setPriority(NotificationCompat.PRIORITY_LOW);
        String caption = getCaption();
        if (!caption.isEmpty()) builder.setSubText(caption);
        else if (RoflanExtraTools.looksSuspicious(body)) builder.setSubText("⚠️ Проверь ссылку перед открытием");
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
                builder.setColor(0xFFFFFFFF);
                break;
            case 6:
                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(title)
                        .bigText(body == null ? "" : body)
                        .setSummaryText("Telegram"));
                break;
            case 7:
                builder.setColor(0xFFFFFFFF);
                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(title)
                        .bigText(body == null ? "" : body));
                break;
            case 8:
                builder.setColor(getCustomAccentColor());
                switch (getCustomLayout()) {
                    case 0:
                        builder.setStyle(null);
                        break;
                    case 2:
                        builder.setStyle(new NotificationCompat.InboxStyle()
                                .setBigContentTitle(title)
                                .addLine(body == null ? "" : body)
                                .setSummaryText("ExteraRoflan"));
                        break;
                    case 3: {
                        Person sender = new Person.Builder().setName(title == null ? "Telegram" : title).build();
                        NotificationCompat.MessagingStyle customConversation = new NotificationCompat.MessagingStyle(sender);
                        customConversation.setConversationTitle(title);
                        customConversation.addMessage(body == null ? "" : body, System.currentTimeMillis(), sender);
                        builder.setStyle(customConversation);
                        break;
                    }
                    default:
                        builder.setStyle(new NotificationCompat.BigTextStyle()
                                .setBigContentTitle(title)
                                .bigText(body == null ? "" : body)
                                .setSummaryText("ExteraRoflan"));
                        break;
                }
                break;
            default:
                // Keep the exact style selected by Telegram itself.
                break;
        }
    }
}
