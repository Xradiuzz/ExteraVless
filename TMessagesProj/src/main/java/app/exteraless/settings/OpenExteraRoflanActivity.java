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

import app.exteraless.appearance.AppearanceConfig;
import app.exteraless.fun.ChatDanceMode;
import app.exteraless.icons.IconPacksActivity;
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

    private static final String[] EVENT_LABELS = {
            "Закрытие Telegram", "Танцующие чаты", "Тишина", "Землетрясение",
            "Затемнение", "Переворот", "Тревога", "Хаос тем"
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
        }
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
        boolean classic2018 = preset == 0;
        boolean classic = preset != 2;

        AppearanceConfig.newLoadingStyle.setConfigBool(!classic);
        AppearanceConfig.newChatHeaderStyle.setConfigBool(false);
        AppearanceConfig.newNavigationBarStyle.setConfigBool(false);
        AppearanceConfig.m3ListItems.setConfigBool(false);
        AppearanceConfig.md3Player.setConfigBool(!classic);
        AppearanceConfig.md3MiniPlayer.setConfigBool(!classic);
        AppearanceConfig.iosNavigationBarStyle.setConfigBool(false);
        AppearanceConfig.iosChatHeader.setConfigBool(false);
        AppearanceConfig.squareFab.setConfigBool(!classic);
        AppearanceConfig.separateHeaders.setConfigBool(!classic);
        AppearanceConfig.sectionRadius.setConfigInt(classic2018 ? 0 : (classic ? 12 : 20));
        AppearanceConfig.dividerStyle.setConfigInt(1);
        AppearanceConfig.setCenterTitle(false);
        AppearanceConfig.gooeyAvatarAnimation.setConfigBool(!classic);
        NaConfig.INSTANCE.getSliderStyle().setConfigInt(classic ? 0 : 2);
        NaConfig.INSTANCE.getSwitchStyle().setConfigInt(classic ? 0
                : org.telegram.ui.Components.Switch.SWITCH_STYLE_M3);
        if (classic) {
            MainTabsLayoutCompat.restoreDefaultNavigation();
        }
        Theme.applyCommonTheme();
        if (getParentLayout() != null) {
            getParentLayout().rebuildAllFragmentViews(false, false);
        }
        if (listAdapter != null) listAdapter.notifyDataSetChanged();
        Toast.makeText(getParentActivity(), getString(R.string.RoflanLegacyApplied),
                Toast.LENGTH_LONG).show();
    }

    /** Keep the legacy preset free of a dependency on a particular tab implementation. */
    private static final class MainTabsLayoutCompat {
        static void restoreDefaultNavigation() {
            if (org.telegram.ui.MainTabsLayout.isBottomNavigationFloating()) {
                org.telegram.ui.MainTabsLayout.setBottomNavigationMode(
                        org.telegram.ui.MainTabsLayout.BOTTOM_NAVIGATION_MODE_SHOW);
            }
        }
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
            case 0:
                Toast.makeText(activity, getString(R.string.RoflanEventClose), Toast.LENGTH_SHORT).show();
                root.postDelayed(activity::finishAffinity, 350);
                break;
            case 1:
                ChatDanceMode.setEnabled(true);
                Toast.makeText(activity, getString(R.string.RoflanEventDance), Toast.LENGTH_SHORT).show();
                finishFragment();
                break;
            case 2:
                ChatDanceMode.setEnabled(false);
                Toast.makeText(activity, getString(R.string.RoflanEventCalm), Toast.LENGTH_SHORT).show();
                finishFragment();
                break;
            case 3:
                root.animate().translationX(dp(14)).setDuration(70).withEndAction(() ->
                        root.animate().translationX(-dp(14)).setDuration(70).withEndAction(() ->
                                root.animate().translationX(0).setDuration(70).start()).start()).start();
                Toast.makeText(activity, getString(R.string.RoflanEventEarthquake), Toast.LENGTH_SHORT).show();
                break;
            case 4:
                root.animate().alpha(0.06f).setDuration(160).withEndAction(() ->
                        root.animate().alpha(1f).setDuration(260).start()).start();
                Toast.makeText(activity, getString(R.string.RoflanEventBlackout), Toast.LENGTH_SHORT).show();
                break;
            case 5:
                root.animate().rotation(180f).setDuration(350).withEndAction(() ->
                        root.animate().rotation(0f).setDuration(350).start()).start();
                Toast.makeText(activity, getString(R.string.RoflanEventFlip), Toast.LENGTH_SHORT).show();
                break;
            case 6:
                root.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                Toast.makeText(activity, getString(R.string.RoflanEventAlarm), Toast.LENGTH_LONG).show();
                break;
            default:
                presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_BASIC));
                Toast.makeText(activity, getString(R.string.RoflanEventTheme), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private static final class RouletteWheel extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF bounds = new RectF();
        private final String[] labels = {
                "CLOSE", "DANCE", "CALM", "QUAKE", "DARK", "FLIP", "ALARM", "THEME"
        };
        private final int[] colors = {
                0xFFEA6B63, 0xFF4F9ED8, 0xFF70B77E, 0xFFE2A34A,
                0xFF6676C8, 0xFF9A70C8, 0xFFCE668E, 0xFF5D9F9A
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
            float targetDelta = (360f - ((rotation + index * 45f + 22.5f) % 360f)) % 360f;
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
                canvas.drawArc(bounds, -90f + i * 45f, 45f, true, paint);

                paint.setColor(0xFFFFFFFF);
                paint.setTextSize(dp(10));
                paint.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD));
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.save();
                canvas.rotate(i * 45f + 22.5f, cx, cy);
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
