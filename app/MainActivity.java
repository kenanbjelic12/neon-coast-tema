package com.neoncoast.tema;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

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
                .setPositiveButton("Dodaj ikonice", (d, w) -> applyIcons())
                .setNegativeButton("Kasnije", null)
                .show();
    }

    /** Otvara ekran sa neonskim ikonicama za pocetni ekran. */
    private void applyIcons() {
        startActivity(new Intent(this, IconsActivity.class));
    }
}
