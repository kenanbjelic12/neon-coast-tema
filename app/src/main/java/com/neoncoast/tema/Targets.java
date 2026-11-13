package com.neoncoast.tema;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.provider.Settings;

/** Koja ikonica otvara koju aplikaciju. */
final class Targets {

    static final String[] KEYS = {
            "telefon", "poruke", "kamera", "galerija",
            "kontakti", "pretrazivac", "mape", "muzika",
            "kalendar", "sat", "email", "kalkulator",
            "postavke", "prodavnica", "fajlovi", "vrijeme"
    };

    static String label(String k) {
        switch (k) {
            case "telefon": return "Telefon";
            case "poruke": return "Poruke";
            case "kamera": return "Kamera";
            case "galerija": return "Galerija";
            case "kontakti": return "Kontakti";
            case "pretrazivac": return "Internet";
            case "mape": return "Mape";
            case "muzika": return "Muzika";
            case "kalendar": return "Kalendar";
            case "sat": return "Sat";
            case "email": return "Email";
            case "kalkulator": return "Kalkulator";
            case "postavke": return "Postavke";
            case "prodavnica": return "Play Store";
            case "fajlovi": return "Fajlovi";
            case "vrijeme": return "Vrijeme";
        }
        return k;
    }

    static int icon(Context c, String k) {
        return c.getResources().getIdentifier("neon_" + k, "drawable", c.getPackageName());
    }

    /** Glavni intent za ikonicu (null = koristi samo pakete). */
    static Intent primary(String k) {
        switch (k) {
            case "telefon": return new Intent(Intent.ACTION_DIAL);
            case "poruke": return sel(Intent.CATEGORY_APP_MESSAGING);
            case "kamera": return new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            case "galerija": return sel(Intent.CATEGORY_APP_GALLERY);
            case "kontakti": return sel(Intent.CATEGORY_APP_CONTACTS);
            case "pretrazivac": return sel(Intent.CATEGORY_APP_BROWSER);
            case "mape": return sel(Intent.CATEGORY_APP_MAPS);
            case "muzika": return sel(Intent.CATEGORY_APP_MUSIC);
            case "kalendar": return sel(Intent.CATEGORY_APP_CALENDAR);
            case "sat": return new Intent(AlarmClock.ACTION_SHOW_ALARMS);
            case "email": return sel(Intent.CATEGORY_APP_EMAIL);
            case "kalkulator": return sel(Intent.CATEGORY_APP_CALCULATOR);
            case "postavke": return new Intent(Settings.ACTION_SETTINGS);
            case "prodavnica": return sel(Intent.CATEGORY_APP_MARKET);
        }
        return null;
    }

    /** Rezervne aplikacije (Samsung pa Google), ako glavni intent ne radi. */
    static String[] packages(String k) {
        switch (k) {
            case "galerija": return new String[]{"com.sec.android.gallery3d", "com.google.android.apps.photos"};
            case "muzika": return new String[]{"com.google.android.apps.youtube.music", "com.spotify.music", "com.sec.android.app.music"};
            case "kalkulator": return new String[]{"com.sec.android.app.popupcalculator", "com.google.android.calculator"};
            case "fajlovi": return new String[]{"com.sec.android.app.myfiles", "com.google.android.apps.nbu.files", "com.google.android.documentsui"};
            case "vrijeme": return new String[]{"com.sec.android.daemonapp", "com.google.android.apps.weather"};
            case "email": return new String[]{"com.google.android.gm", "com.samsung.android.email.provider"};
            case "kalendar": return new String[]{"com.samsung.android.calendar", "com.google.android.calendar"};
        }
        return new String[0];
    }

    /** Ako nista nije instalirano. */
    static Intent web(String k) {
        if ("vrijeme".equals(k)) return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=vrijeme"));
        return null;
    }

    private static Intent sel(String category) {
        return Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, category);
    }

    private Targets() {}
}
