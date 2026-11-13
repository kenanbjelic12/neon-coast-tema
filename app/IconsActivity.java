package com.neoncoast.tema;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Mreza neonskih ikonica: dodir = dodaj precicu na pocetni ekran. */
public class IconsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(0xFF12051F);
        getWindow().setNavigationBarColor(0xFF12051F);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFF12051F);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Neonske ikonice");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setShadowLayer(18, 0, 0, 0xFFFF4FA3);
        root.addView(title);

        TextView sam = new TextView(this);
        sam.setText("Samsung (Theme Park) \u2013 ikonice svuda, i u meniju aplikacija:\n"
                + "1. Sa\u010duvaj ikonice u Galeriju.\n"
                + "2. Otvori Theme Park \u2192 Ikona \u2192 napravi novi paket.\n"
                + "3. Za svaku aplikaciju izaberi sliku iz albuma \u201eNeon Coast\u201c.\n"
                + "4. Sa\u010duvaj i primijeni.");
        sam.setTextColor(0xFFE6CFE8);
        sam.setTextSize(15);
        sam.setPadding(0, dp(10), 0, dp(10));
        root.addView(sam);

        Button save = button("Sa\u010duvaj ikonice u Galeriju", true);
        save.setOnClickListener(v -> saveToGallery(save));
        root.addView(save, btnParams());

        Button tp = button("Otvori Theme Park", false);
        tp.setOnClickListener(v -> openThemePark());
        root.addView(tp, btnParams());

        TextView novaT = new TextView(this);
        novaT.setText("Jednim klikom \u2013 Nova Launcher");
        novaT.setTextColor(0xFFFFFFFF);
        novaT.setTextSize(18);
        novaT.setTypeface(Typeface.DEFAULT_BOLD);
        novaT.setPadding(0, dp(24), 0, 0);
        root.addView(novaT);

        TextView novaH = new TextView(this);
        novaH.setText("Nova postane tvoj po\u010detni ekran i primijeni sve neonske ikonice odjednom, "
                + "i na po\u010detnom ekranu i u meniju aplikacija. Boje sistema se ne mijenjaju.");
        novaH.setTextColor(0xFFE6CFE8);
        novaH.setTextSize(15);
        novaH.setPadding(0, dp(8), 0, 0);
        root.addView(novaH);

        Button nova = button("Primijeni preko Nova Launchera", false);
        nova.setOnClickListener(v -> applyNova());
        root.addView(nova, btnParams());

        TextView other = new TextView(this);
        other.setText("Drugi telefoni \u2013 pre\u010dice na po\u010detnom ekranu");
        other.setTextColor(0xFFFFFFFF);
        other.setTextSize(18);
        other.setTypeface(Typeface.DEFAULT_BOLD);
        other.setPadding(0, dp(24), 0, 0);
        root.addView(other);

        TextView hint = new TextView(this);
        hint.setText("Dodirni ikonicu i potvrdi „Dodaj“. Nova ikonica se pojavi na početnom ekranu. "
                + "Stare ikonice ukloni sa početnog ekrana (ostaju u meniju aplikacija).");
        hint.setTextColor(0xFFE6CFE8);
        hint.setTextSize(15);
        hint.setPadding(0, dp(8), 0, dp(16));
        root.addView(hint);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        root.addView(grid, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        for (String k : Targets.KEYS) {
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);
            cell.setPadding(0, dp(8), 0, dp(12));
            cell.setClickable(true);
            cell.setFocusable(true);
            TypedValue tv = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, tv, true);
            cell.setBackgroundResource(tv.resourceId);
            cell.setContentDescription("Dodaj ikonicu " + Targets.label(k));

            ImageView img = new ImageView(this);
            img.setImageResource(Targets.icon(this, k));
            cell.addView(img, new LinearLayout.LayoutParams(dp(64), dp(64)));

            TextView lbl = new TextView(this);
            lbl.setText(Targets.label(k));
            lbl.setTextColor(0xFFFFFFFF);
            lbl.setTextSize(12);
            lbl.setGravity(Gravity.CENTER);
            lbl.setPadding(0, dp(6), 0, 0);
            cell.addView(lbl);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0;
            grid.addView(cell, lp);

            cell.setOnClickListener(v -> pin(k));
        }
        setContentView(scroll);
    }

    private void pin(String k) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(this, "Ovaj Android je prestar za dodavanje prečica.", Toast.LENGTH_LONG).show();
            return;
        }
        ShortcutManager sm = getSystemService(ShortcutManager.class);
        if (sm == null || !sm.isRequestPinShortcutSupported()) {
            Toast.makeText(this, "Tvoj početni ekran ne podržava dodavanje prečica.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent open = new Intent(this, LaunchActivity.class)
                .setAction(Intent.ACTION_VIEW)
                .putExtra("target", k);
        ShortcutInfo info = new ShortcutInfo.Builder(this, "neon_" + k)
                .setShortLabel(Targets.label(k))
                .setIcon(Icon.createWithResource(this, Targets.icon(this, k)))
                .setIntent(open)
                .build();
        sm.requestPinShortcut(info, null);
    }

    private Button button(String text, boolean primary) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTextColor(primary ? 0xFF12051F : 0xFF5EF2E4);
        b.setBackgroundResource(primary ? R.drawable.btn_pink : R.drawable.btn_teal);
        return b;
    }

    private LinearLayout.LayoutParams btnParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        lp.topMargin = dp(10);
        return lp;
    }

    /** Snima sve ikonice i pozadinu u Pictures/Neon Coast (vidi se u Galeriji). */
    private void saveToGallery(Button btn) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(this, "Za ovo treba Android 10 ili noviji.", Toast.LENGTH_LONG).show();
            return;
        }
        btn.setEnabled(false);
        btn.setText("Snimam...");
        new Thread(() -> {
            int ok = 0;
            for (String k : Targets.KEYS) {
                if (saveRes(Targets.icon(this, k), "neon_" + k + ".png", "image/png")) ok++;
            }
            saveRes(R.drawable.pozadina, "neon_coast_pozadina.jpg", "image/jpeg");
            final int n = ok;
            runOnUiThread(() -> {
                btn.setEnabled(true);
                btn.setText(n > 0 ? "Sa\u010duvano \u2713 (" + n + " ikonica)" : "Sa\u010duvaj ikonice u Galeriju");
                Toast.makeText(this, n > 0
                        ? "Ikonice su u Galeriji, album \u201eNeon Coast\u201c."
                        : "Snimanje nije uspjelo.", Toast.LENGTH_LONG).show();
            });
        }).start();
    }

    private boolean saveRes(int resId, String name, String mime) {
        if (resId == 0) return false;
        ContentResolver cr = getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.DISPLAY_NAME, name);
        cv.put(MediaStore.Images.Media.MIME_TYPE, mime);
        cv.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Neon Coast");
        cv.put(MediaStore.Images.Media.IS_PENDING, 1);
        Uri uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        if (uri == null) return false;
        try (InputStream in = getResources().openRawResource(resId);
             OutputStream out = cr.openOutputStream(uri)) {
            byte[] buf = new byte[16384];
            int r;
            while ((r = in.read(buf)) > 0) out.write(buf, 0, r);
        } catch (Exception e) {
            cr.delete(uri, null, null);
            return false;
        }
        cv.clear();
        cv.put(MediaStore.Images.Media.IS_PENDING, 0);
        cr.update(uri, cv, null, null);
        return true;
    }

    /** Theme Park, pa Good Lock, pa Galaxy Store stranica Good Locka. */
    private void openThemePark() {
        String[] pkgs = {"com.samsung.android.themedesigner", "com.samsung.android.goodlock"};
        for (String p : pkgs) {
            Intent i = getPackageManager().getLaunchIntentForPackage(p);
            if (i != null) {
                startActivity(i);
                return;
            }
        }
        Toast.makeText(this, "Prvo instaliraj Good Lock, pa u njemu Theme Park.", Toast.LENGTH_LONG).show();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("samsungapps://ProductDetail/com.samsung.android.goodlock")));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://galaxystore.samsung.com/detail/com.samsung.android.goodlock")));
        }
    }

    private static final String NOVA = "com.teslacoilsw.launcher";

    /** Nova Launcher: primijeni ovaj paket ikonica jednim klikom. */
    private void applyNova() {
        if (getPackageManager().getLaunchIntentForPackage(NOVA) == null) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Treba Nova Launcher")
                    .setMessage("Instaliraj besplatni Nova Launcher iz Play Storea, postavi ga kao po\u010detni ekran, "
                            + "pa se vrati ovdje i klikni ponovo.")
                    .setPositiveButton("Otvori Play Store", (d, w) -> {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + NOVA)));
                        } catch (Exception e) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=" + NOVA)));
                        }
                    })
                    .setNegativeButton("Zatvori", null)
                    .show();
            return;
        }
        Intent i = new Intent("com.teslacoilsw.launcher.APPLY_ICON_THEME");
        i.setPackage(NOVA);
        i.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_TYPE", "GO");
        i.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_PACKAGE", getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Nova nije prihvatila paket. U Novi: Postavke \u2192 Izgled \u2192 Paket ikonica \u2192 Neon Coast Tema.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
