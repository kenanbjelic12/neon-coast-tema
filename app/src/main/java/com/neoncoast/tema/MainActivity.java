package com.neoncoast.tema;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String NOVA = "com.teslacoilsw.launcher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(0xFF12051F);
        getWindow().setNavigationBarColor(0xFF12051F);

        Button theme = findViewById(R.id.btnTheme);
        theme.setOnClickListener(v -> applyWallpaper(theme));
        findViewById(R.id.btnIcons).setOnClickListener(v -> applyIcons());
    }

    /** Postavlja pozadinu na pocetni i zakljucani ekran jednim klikom. */
    private void applyWallpaper(Button btn) {
        btn.setEnabled(false);
        btn.setText("Postavljam...");
        new Thread(() -> {
            boolean ok;
            try {
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pozadina);
                WallpaperManager wm = WallpaperManager.getInstance(this);
                wm.setBitmap(bmp, null, true, WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
                ok = true;
            } catch (Exception e) {
                ok = false;
            }
            final boolean done = ok;
            runOnUiThread(() -> {
                btn.setEnabled(true);
                btn.setText(done ? "Tema postavljena ✓" : "Postavi temu");
                Toast.makeText(this,
                        done ? "Pozadina je postavljena na početni i zaključani ekran."
                             : "Postavljanje nije uspjelo. Probaj ponovo.",
                        Toast.LENGTH_LONG).show();
                if (done) askIcons();
            });
        }).start();
    }

    private void askIcons() {
        new AlertDialog.Builder(this)
                .setTitle("Neonske ikonice")
                .setMessage("Pozadina je postavljena. Želite li primijeniti i neonske ikonice?")
                .setPositiveButton("Primijeni", (d, w) -> applyIcons())
                .setNegativeButton("Kasnije", null)
                .show();
    }

    /** Ikonice: Android dozvoljava samo da ih launcher primijeni (npr. Nova Launcher). */
    private void applyIcons() {
        Intent nova = new Intent("com.teslacoilsw.launcher.APPLY_ICON_THEME");
        nova.setPackage(NOVA);
        nova.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_TYPE", "GO");
        nova.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_PACKAGE", getPackageName());
        nova.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(nova);
        } catch (ActivityNotFoundException e) {
            new AlertDialog.Builder(this)
                    .setTitle("Treba Nova Launcher")
                    .setMessage("Android ne dozvoljava aplikacijama da same mijenjaju ikonice u Samsungovom "
                            + "launcheru. Instaliraj besplatni Nova Launcher, postavi ga kao glavni, "
                            + "pa ponovo klikni „Primijeni neonske ikonice“.")
                    .setPositiveButton("Otvori Play Store", (d, w) -> openStore())
                    .setNegativeButton("Zatvori", null)
                    .show();
        }
    }

    private void openStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + NOVA)));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + NOVA)));
        }
    }
}
