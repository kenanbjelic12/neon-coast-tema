package com.neoncoast.tema;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/** Prečica sa početnog ekrana dolazi ovdje, a ova aktivnost otvori pravu aplikaciju. */
public class LaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String k = getIntent().getStringExtra("target");
        if (k == null || !tryOpen(k)) {
            Toast.makeText(this, "Aplikacija nije pronađena na ovom telefonu.", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private boolean tryOpen(String k) {
        if (start(Targets.primary(k))) return true;
        for (String p : Targets.packages(k)) {
            if (start(getPackageManager().getLaunchIntentForPackage(p))) return true;
        }
        return start(Targets.web(k));
    }

    private boolean start(Intent i) {
        if (i == null) return false;
        try {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
