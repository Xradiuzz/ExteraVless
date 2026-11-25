package app.exteraless.settings;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ThemeActivity;
import org.telegram.ui.NotificationsSettingsActivity;

import app.exteraless.appearance.AppearanceConfig;
import app.exteraless.fun.ChatDanceMode;
import app.exteraless.fun.LizardPetOverlay;
import app.exteraless.notifications.NotificationStudio;
import app.exteraless.icons.IconPacksActivity;
import app.exteraless.legacyui.LegacyUiController;
import tw.nekomimi.nekogram.NekoConfig;
import tw.nekomimi.nekogram.config.ConfigItem;
import tw.nekomimi.nekogram.settings.BaseNekoSettingsActivity;
import tw.nekomimi.nekogram.ui.cells.HeaderCell;
import xyz.nextalone.nagram.NaConfig;

/** Dedicated home for all ExteraRoflan additions. */
public class OpenExteraRoflanActivity extends BaseNekoSettingsActivity {

    private static final int TYPE_ROFLAN_HEADER = 100;
    private int headerRow;
    private int legacyRow;
    private int danceRow;
    private int appIconsRow;
    private int iconPacksRow;
    private int rouletteRow;
    private int dividerRow;
    private int notificationStyleRow;
    private int notificationCaptionRow;
    private int notificationSoundRow;
    private int featureHubRow;

    // Первые шесть секторов — благоприятные, остальные — испытания.
    private static final String[] EVENT_LABELS = {
            "Удача!", "Тишина", "Ящерка", "Новая тема", "Чистый экран", "Покой",
            "Танцы", "Землетрясение", "Затемнение", "Переворот", "Тревога",
            "Закрытие", "Хаос тем", "Дрожь", "Сюрприз", "Проклятие"
    };

    public OpenExteraRoflanActivity() {
        super();
        AppearanceConfig.init();
    }

    @Override
    protected void updateRows() {
        super.updateRows();
        headerRow = addRow("roflanHeader");
        legacyRow = addRow("roflanLegacy");
        danceRow = addRow("roflanDance");
        appIconsRow = addRow("roflanAppIcons");
        iconPacksRow = addRow("roflanIconPacks");
        rouletteRow = addRow("roflanRoulette");
        notificationStyleRow = addRow("roflanNotificationStyle");
        notificationCaptionRow = addRow("roflanNotificationCaption");
        notificationSoundRow = addRow("roflanNotificationSound");
        featureHubRow = addRow("roflanFeatureHub");
        dividerRow = addRow();
    }

    @Override
    protected String getActionBarTitle() {
        return getString(R.string.RoflanCategoryTitle);
    }

    @Override
    protected String getKey() {
        return "exteraless_roflan";
    }

    @Override
    public int getSearchGuid() {
        return 25000;
    }

    @Override
    public int getSearchIcon() {
        return R.drawable.ic_roflan_chicken_outline;
    }

    @Override
    public String getSearchPrefix() {
        return "Roflan";
    }

    @Override
    protected BaseListAdapter createAdapter(Context context) {
        return new ListAdapter(context);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == legacyRow) {
            showLegacyPresets();
        } else if (position == danceRow) {
            boolean enabled = !ChatDanceMode.isEnabled();
            ChatDanceMode.setEnabled(enabled);
            Toast.makeText(getParentActivity(), enabled
                    ? getString(R.string.RoflanDanceEnabled)
                    : getString(R.string.RoflanDanceDisabled), Toast.LENGTH_SHORT).show();
            finishFragment();
        } else if (position == appIconsRow) {
            presentFragment(new app.exteraless.appicons.AppIconsActivity());
        } else if (position == iconPacksRow) {
            presentFragment(new IconPacksActivity());
        } else if (position == rouletteRow) {
            showAnimatedRoulette();
        } else if (position == notificationStyleRow) {
            showNotificationStyleDialog();
        } else if (position == notificationCaptionRow) {
            showNotificationCaptionDialog();
        } else if (position == notificationSoundRow) {
            presentFragment(new NotificationsSettingsActivity());
        } else if (position == featureHubRow) {
            presentFragment(new RoflanFeatureHubActivity());
        }
    }

    private class ListAdapter extends BaseListAdapter {
        ListAdapter(Context context) { super(context); }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ROFLAN_HEADER) {
                HeaderCell cell = new HeaderCell(mContext, getResourceProvider());
                cell.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                return new RecyclerListView.Holder(cell);
            }
            return super.onCreateViewHolder(parent, viewType);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == headerRow) return TYPE_ROFLAN_HEADER;
            if (position == dividerRow) return TYPE_SHADOW;
            return TYPE_DETAIL_SETTINGS;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            if (position == headerRow) {
                ((HeaderCell) holder.itemView).setText(getString(R.string.RoflanSectionHeader));
                return;
            }
            if (!(holder.itemView instanceof TextDetailSettingsCell)) return;
            TextDetailSettingsCell cell = (TextDetailSettingsCell) holder.itemView;
            if (position == legacyRow) {
                cell.setTextAndValueAndIcon(getString(R.string.RoflanLegacyTitle),
                        getString(R.string.RoflanLegacyInfo), R.drawable.msg_theme, true);
            } else if (position == danceRow) {
                cell.setTextAndValueAndIcon(getString(R.string.ExteraVlessDanceChats),
                        ChatDanceMode.isEnabled() ? getString(R.string.RoflanDanceEnabled) : getString(R.string.RoflanDanceDisabled),
                        R.drawable.msg_discussion, true);
            } else if (position == appIconsRow) {
                cell.setTextAndValueAndIcon(getString(R.string.RoflanAppIconsTitle),
                        getString(R.string.RoflanAppIconsInfo), R.drawable.msg_theme, true);
            } else if (position == iconPacksRow) {
                cell.setTextAndValueAndIcon(getString(R.string.OEAppearanceIconPacks),
                        getString(R.string.OEAppearanceIconPacksInfo), R.drawable.msg_sticker, true);
            } else if (position == rouletteRow) {
                cell.setTextAndValueAndIcon(getString(R.string.RoflanRouletteTitle),
                        getString(R.string.RoflanRouletteInfo), R.drawable.msg_filled_data_music, false);
            }
            else if (position == notificationStyleRow) {
                cell.setTextAndValueAndIcon("Стиль уведомлений",
                        NotificationStudio.STYLE_NAMES[NotificationStudio.getStyle()], R.drawable.msg_notifications, true);
            } else if (position == notificationCaptionRow) {
                String caption = NotificationStudio.getCaption();
                cell.setTextAndValueAndIcon("Своя подпись",
                        caption.isEmpty() ? "Не задана" : caption, R.drawable.msg_edit, true);
            } else if (position == notificationSoundRow) {
                cell.setTextAndValueAndIcon("Звуки уведомлений",
                        "Глобальные и для отдельных чатов", R.drawable.msg_notifications, false);
            }
        }
    }

    private void showNotificationStyleDialog() {
        Activity activity = getParentActivity();
        if (activity == null) return;
        new AlertDialog.Builder(activity)
                .setTitle("Стиль уведомлений")
                .setItems(NotificationStudio.STYLE_NAMES, (dialog, which) -> {
                    NotificationStudio.setStyle(which);
                    if (listAdapter != null) listAdapter.notifyDataSetChanged();
                    Toast.makeText(activity, "Стиль: " + NotificationStudio.STYLE_NAMES[which], Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.Cancel), null)
                .show();
    }

    private void showNotificationCaptionDialog() {
        Activity activity = getParentActivity();
        if (activity == null) return;
        EditText input = new EditText(activity);
        input.setSingleLine(true);
        input.setHint("Например: не забудь улыбнуться");
        input.setText(NotificationStudio.getCaption());
        new AlertDialog.Builder(activity)
                .setTitle("Своя подпись к уведомлениям")
                .setMessage("Подпись будет отображаться дополнительной строкой в шторке. Максимум 64 символа.")
                .setView(input)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    NotificationStudio.setCaption(input.getText().toString());
                    if (listAdapter != null) listAdapter.notifyDataSetChanged();
                })
                .setNeutralButton("Очистить", (dialog, which) -> {
                    NotificationStudio.setCaption("");
                    if (listAdapter != null) listAdapter.notifyDataSetChanged();
                })
                .setNegativeButton(getString(R.string.Cancel), null)
                .show();
    }

    private void showLegacyPresets() {
        if (getParentActivity() == null) return;
        CharSequence[] presets = {
                getString(R.string.RoflanLegacy2018),
                getString(R.string.RoflanLegacy2021),
                getString(R.string.RoflanLegacyModern)
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(getString(R.string.RoflanLegacyTitle));
        builder.setMessage(getString(R.string.RoflanLegacyInfo));
        builder.setItems(presets, (dialog, which) -> applyLegacyPreset(which));
        builder.setNegativeButton(getString(R.string.Cancel), null);
        showDialog(builder.create());
    }

    /**
     * Presets only touch appearance switches that are wired into the renderer.
     * 0 = legacy controls, 2 = Material 3 controls in this fork.
     */
    private void applyLegacyPreset(int preset) {
        LegacyUiController.apply(preset);
        if (getParentLayout() != null) {
            getParentLayout().rebuildAllFragmentViews(false, false);
        }
        if (listAdapter != null) listAdapter.notifyDataSetChanged();
        String[] names = {"Telegram 2018", "Telegram 2021", "Современный"};
        Toast.makeText(getParentActivity(), "Профиль интерфейса: " + names[Math.max(0, Math.min(2, preset))],
                Toast.LENGTH_LONG).show();
    }

    private void showAnimatedRoulette() {
        Activity activity = getParentActivity();
        if (activity == null) return;

        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(18), dp(12), dp(18), dp(8));

        RouletteWheel wheel = new RouletteWheel(activity);
        content.addView(wheel, new LinearLayout.LayoutParams(dp(276), dp(276)));

        TextView result = new TextView(activity);
        result.setText(getString(R.string.RoflanRouletteReady));
        result.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        result.setTextSize(16);
        result.setGravity(Gravity.CENTER);
        result.setPadding(0, dp(12), 0, dp(4));
        content.addView(result, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(getString(R.string.RoflanRouletteTitle))
                .setView(content)
                .setNegativeButton(getString(R.string.Cancel), null)
                .setPositiveButton(getString(R.string.RoflanRouletteSpin), null)
                .create();
        dialog.setOnShowListener(d -> {
            TextView spinButton = (TextView) dialog.getButton(-1);
            spinButton.setOnClickListener(v -> {
                if (wheel.isSpinning()) return;
                int chosen = (int) (Math.random() * EVENT_LABELS.length);
                spinButton.setEnabled(false);
                spinButton.setText(getString(R.string.RoflanRouletteSpinning));
                wheel.spinTo(chosen, () -> {
                    result.setText(getString(R.string.RoflanRouletteResult).replace("%1$s", EVENT_LABELS[chosen]));
                    spinButton.setText(getString(R.string.RoflanRouletteDone));
                    spinButton.setEnabled(true);
                    spinButton.setOnClickListener(done -> {
                        dialog.dismiss();
                        applyRouletteEvent(chosen);
                    });
                    AndroidUtilities.runOnUIThread(() -> {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                            applyRouletteEvent(chosen);
                        }
                    }, 1100);
                });
            });
        });
        showDialog(dialog);
    }

    private void applyRouletteEvent(int event) {
        Activity activity = getParentActivity();
        if (activity == null) return;
        View root = activity.getWindow().getDecorView();
        switch (event) {
            // Благоприятные сектора: никаких разрушительных действий.
            case 0:
                root.animate().alpha(0.72f).setDuration(140).withEndAction(() ->
                        root.animate().alpha(1f).setDuration(420).start()).start();
                Toast.makeText(activity, "🍀 Сегодня тебе везёт!", Toast.LENGTH_LONG).show();
                break;
            case 1:
                ChatDanceMode.setEnabled(false);
                Toast.makeText(activity, "Тишина и спокойствие. Танцы отключены.", Toast.LENGTH_SHORT).show();
                finishFragment();
                break;
            case 2:
                LizardPetOverlay.toggle(activity);
                Toast.makeText(activity, "У тебя появился маленький компаньон 🦎", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_BASIC));
                Toast.makeText(activity, "Выбери себе новую тему!", Toast.LENGTH_SHORT).show();
                break;
            case 4:
                root.animate().translationY(-dp(8)).alpha(0.9f).setDuration(180).withEndAction(() ->
                        root.animate().translationY(0).alpha(1f).setDuration(260).start()).start();
                Toast.makeText(activity, "Экран очищен от негатива.", Toast.LENGTH_SHORT).show();
                break;
            case 5:
                ChatDanceMode.setEnabled(false);
                root.animate().rotation(0).translationX(0).translationY(0).alpha(1f).scaleX(1f).scaleY(1f).setDuration(280).start();
                Toast.makeText(activity, "Режим покоя активирован.", Toast.LENGTH_SHORT).show();
                finishFragment();
                break;

            // Неблагоприятные сектора.
            case 6:
                ChatDanceMode.setEnabled(true);
                Toast.makeText(activity, getString(R.string.RoflanEventDance), Toast.LENGTH_SHORT).show();
                finishFragment();
                break;
            case 7:
                shake(root);
                Toast.makeText(activity, getString(R.string.RoflanEventEarthquake), Toast.LENGTH_SHORT).show();
                break;
            case 8:
                root.animate().alpha(0.04f).setDuration(180).withEndAction(() ->
                        root.animate().alpha(1f).setDuration(360).start()).start();
                Toast.makeText(activity, getString(R.string.RoflanEventBlackout), Toast.LENGTH_SHORT).show();
                break;
            case 9:
                root.animate().rotation(180f).setDuration(420).withEndAction(() ->
                        root.animate().rotation(0f).setDuration(420).start()).start();
                Toast.makeText(activity, getString(R.string.RoflanEventFlip), Toast.LENGTH_SHORT).show();
                break;
            case 10:
                root.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                Toast.makeText(activity, getString(R.string.RoflanEventAlarm), Toast.LENGTH_LONG).show();
                break;
            case 11:
                Toast.makeText(activity, getString(R.string.RoflanEventClose), Toast.LENGTH_SHORT).show();
                root.postDelayed(activity::finishAffinity, 500);
                break;
            case 12:
                presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_BASIC));
                Toast.makeText(activity, getString(R.string.RoflanEventTheme), Toast.LENGTH_SHORT).show();
                break;
            case 13:
                shake(root);
                root.postDelayed(() -> shake(root), 260);
                Toast.makeText(activity, "Интерфейс трясёт. Держись!", Toast.LENGTH_SHORT).show();
                break;
            case 14:
                LizardPetOverlay.toggle(activity);
                root.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
                Toast.makeText(activity, "Сюрприз! Лови ящерицу 🦎", Toast.LENGTH_SHORT).show();
                break;
            default:
                root.animate().alpha(0.12f).scaleX(0.98f).scaleY(0.98f).setDuration(220).withEndAction(() ->
                        root.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(520).start()).start();
                Toast.makeText(activity, "Проклятие тем. Сегодня без удачи.", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void shake(View root) {
        root.animate().translationX(dp(14)).setDuration(65).withEndAction(() ->
                root.animate().translationX(-dp(14)).setDuration(65).withEndAction(() ->
                        root.animate().translationX(dp(8)).setDuration(65).withEndAction(() ->
                                root.animate().translationX(0).setDuration(80).start()).start()).start()).start();
    }

    private static final class RouletteWheel extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF bounds = new RectF();
        private final String[] labels = {
                "LUCK", "CALM", "PET", "THEME", "CLEAN", "PEACE",
                "DANCE", "QUAKE", "DARK", "FLIP", "ALARM", "EXIT",
                "CHAOS", "SHAKE", "GIFT", "CURSE"
        };
        private final int[] colors = {
                0xFF65B979, 0xFF69A9E8, 0xFF77C9A0, 0xFF8CBA70,
                0xFF5BB8B1, 0xFF9ACB72, 0xFFDB8B58, 0xFFE16D62,
                0xFF9A6BC7, 0xFFCE668E, 0xFFE0A23F, 0xFFB94F59,
                0xFF7657A8, 0xFFD46F4D, 0xFF527FAE, 0xFF8E4C62
        };
        private float rotation;
        private boolean spinning;

        RouletteWheel(Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        boolean isSpinning() { return spinning; }

        void spinTo(int index, Runnable finished) {
            spinning = true;
            float targetDelta = (360f - ((rotation + index * 22.5f + 11.25f) % 360f)) % 360f;
            float target = 360f * 5f + targetDelta;
            ValueAnimator animator = ValueAnimator.ofFloat(rotation, rotation + target);
            animator.setDuration(4800);
            animator.setInterpolator(new DecelerateInterpolator(2.4f));
            animator.addUpdateListener(value -> {
                rotation = (float) value.getAnimatedValue();
                invalidate();
            });
            animator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(android.animation.Animator animation) {
                    spinning = false;
                    finished.run();
                }
            });
            animator.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float side = Math.min(getWidth(), getHeight());
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float radius = side * 0.43f;
            bounds.set(cx - radius, cy - radius, cx + radius, cy + radius);

            canvas.save();
            canvas.rotate(rotation, cx, cy);
            for (int i = 0; i < labels.length; i++) {
                paint.setColor(colors[i]);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawArc(bounds, -90f + i * 22.5f, 22.5f, true, paint);

                paint.setColor(0xFFFFFFFF);
                paint.setTextSize(dp(8));
                paint.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD));
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.save();
                canvas.rotate(i * 22.5f + 11.25f, cx, cy);
                canvas.drawText(labels[i], cx, cy - radius * 0.66f, paint);
                canvas.restore();
            }
            paint.setColor(0xFFFFFFFF);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(3));
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF243447);
            canvas.drawCircle(cx, cy, dp(22), paint);
            paint.setColor(0xFFFFFFFF);
            paint.setTextSize(dp(12));
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("ROFL", cx, cy + dp(4), paint);
            canvas.restore();

            paint.setColor(0xFFFFFFFF);
            Path pointer = new Path();
            pointer.moveTo(cx, cy - radius - dp(5));
            pointer.lineTo(cx - dp(12), cy - radius - dp(25));
            pointer.lineTo(cx + dp(12), cy - radius - dp(25));
            pointer.close();
            canvas.drawPath(pointer, paint);
        }
    }
}
