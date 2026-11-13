package com.neoncoast.tema;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Upload selfija -> server (Stability AI) -> pozadina sa tvojim licem. */
public class FaceActivity extends Activity {

    private static final int PICK = 7;

    private ImageView selfieView, resultView;
    private Button pickBtn, genBtn, setBtn, saveBtn;
    private CheckBox consent;
    private ProgressBar progress;
    private TextView status;
    private String selfieB64;
    private Bitmap result;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(0xFF12051F);
        getWindow().setNavigationBarColor(0xFF12051F);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFF12051F);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int p = dp(20);
        root.setPadding(p, p, p, p);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Pozadina sa tvojim licem");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setShadowLayer(18, 0, 0, 0xFFFF4FA3);
        root.addView(title);

        TextView hint = new TextView(this);
        hint.setText("Izaberi selfie (lice jasno vidljivo, dobro svjetlo). Aplikacija napravi pozadinu u stilu teme sa tvojim licem. Traje oko 30–60 sekundi.");
        hint.setTextColor(0xFFE6CFE8);
        hint.setTextSize(15);
        hint.setPadding(0, dp(8), 0, dp(12));
        root.addView(hint);

        selfieView = new ImageView(this);
        selfieView.setAdjustViewBounds(true);
        selfieView.setMaxHeight(dp(220));
        selfieView.setContentDescription("Tvoj selfie");
        root.addView(selfieView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        pickBtn = button("Izaberi selfie", false);
        pickBtn.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
            startActivityForResult(Intent.createChooser(i, "Izaberi selfie"), PICK);
        });
        root.addView(pickBtn, params());

        consent = new CheckBox(this);
        consent.setText("Potvrđujem da je ovo moja slika, ili da imam dozvolu osobe sa slike.");
        consent.setTextColor(0xFFE6CFE8);
        consent.setButtonTintList(android.content.res.ColorStateList.valueOf(0xFFFF4FA3));
        consent.setPadding(0, dp(8), 0, 0);
        consent.setOnCheckedChangeListener((c, on) -> updateGen());
        root.addView(consent);

        genBtn = button("Generiši pozadinu", true);
        genBtn.setEnabled(false);
        genBtn.setAlpha(0.5f);
        genBtn.setOnClickListener(v -> generate());
        root.addView(genBtn, params());

        progress = new ProgressBar(this);
        progress.setVisibility(android.view.View.GONE);
        root.addView(progress);

        status = new TextView(this);
        status.setTextColor(0xFFE6CFE8);
        status.setTextSize(15);
        status.setPadding(0, dp(8), 0, dp(8));
        root.addView(status);

        resultView = new ImageView(this);
        resultView.setAdjustViewBounds(true);
        resultView.setContentDescription("Generisana pozadina");
        root.addView(resultView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setBtn = button("Postavi kao pozadinu", true);
        setBtn.setVisibility(android.view.View.GONE);
        setBtn.setOnClickListener(v -> setWallpaper());
        root.addView(setBtn, params());

        saveBtn = button("Sačuvaj u Galeriju", false);
        saveBtn.setVisibility(android.view.View.GONE);
        saveBtn.setOnClickListener(v -> saveToGallery());
        root.addView(saveBtn, params());

        setContentView(scroll);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != PICK || res != RESULT_OK || data == null || data.getData() == null) return;
        try (InputStream in = getContentResolver().openInputStream(data.getData())) {
            Bitmap bmp = BitmapFactory.decodeStream(in);
            if (bmp == null) throw new Exception();
            bmp = rotateByExif(data.getData(), bmp);
            float s = Math.min(1f, 1024f / Math.max(bmp.getWidth(), bmp.getHeight()));
            Bitmap small = Bitmap.createScaledBitmap(bmp, Math.round(bmp.getWidth() * s), Math.round(bmp.getHeight() * s), true);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            small.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            selfieB64 = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP);
            selfieView.setImageBitmap(small);
            pickBtn.setText("Promijeni selfie");
            status.setText("");
        } catch (Exception e) {
            Toast.makeText(this, "Ne mogu pročitati sliku.", Toast.LENGTH_LONG).show();
        }
        updateGen();
    }

    /** Okreni sliku kako je telefon snimio (EXIF), da selfie ne bude naopako. */
    private Bitmap rotateByExif(Uri uri, Bitmap bmp) {
        int deg = 0;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            android.media.ExifInterface exif = new android.media.ExifInterface(in);
            int o = exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION,
                    android.media.ExifInterface.ORIENTATION_NORMAL);
            if (o == android.media.ExifInterface.ORIENTATION_ROTATE_90) deg = 90;
            else if (o == android.media.ExifInterface.ORIENTATION_ROTATE_180) deg = 180;
            else if (o == android.media.ExifInterface.ORIENTATION_ROTATE_270) deg = 270;
        } catch (Exception ignored) {
        }
        if (deg == 0) return bmp;
        android.graphics.Matrix m = new android.graphics.Matrix();
        m.postRotate(deg);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
    }

    /** Doda http:// ako je zaboravljen u Config.java. */
    private static String serverUrl() {
        String u = Config.SERVER_URL.trim();
        if (!u.startsWith("http://") && !u.startsWith("https://")) u = "http://" + u;
        while (u.endsWith("/")) u = u.substring(0, u.length() - 1);
        return u;
    }

    private void updateGen() {
        boolean ok = selfieB64 != null && consent.isChecked();
        genBtn.setEnabled(ok);
        genBtn.setAlpha(ok ? 1f : 0.5f);
    }

    private void generate() {
        genBtn.setEnabled(false);
        genBtn.setAlpha(0.5f);
        progress.setVisibility(android.view.View.VISIBLE);
        status.setText("Crtam tvoju pozadinu… (30–60 s)");
        setBtn.setVisibility(android.view.View.GONE);
        saveBtn.setVisibility(android.view.View.GONE);

        new Thread(() -> {
            String err = null;
            Bitmap out = null;
            HttpURLConnection c = null;
            try {
                JSONObject body = new JSONObject();
                body.put("image", selfieB64);
                body.put("mimeType", "image/jpeg");
                body.put("consent", true);
                c = (HttpURLConnection) new URL(serverUrl() + "/api/face").openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(20000);
                c.setReadTimeout(180000);
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json");
                c.setRequestProperty("X-App-Token", Config.APP_TOKEN);
                try (OutputStream os = c.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
                int code = c.getResponseCode();
                InputStream is = code < 400 ? c.getInputStream() : c.getErrorStream();
                String txt = readAll(is);
                JSONObject j = new JSONObject(txt);
                if (code != 200) {
                    err = j.optString("error", "Greška " + code);
                } else {
                    byte[] img = Base64.decode(j.getString("image"), Base64.DEFAULT);
                    out = BitmapFactory.decodeByteArray(img, 0, img.length);
                    if (out == null) err = "Server je vratio neispravnu sliku.";
                }
            } catch (java.net.UnknownHostException | java.net.ConnectException e) {
                err = "Ne mogu do servera. Provjeri internet i adresu u Config.java.";
            } catch (java.net.SocketTimeoutException e) {
                err = "Server predugo odgovara. Probaj ponovo.";
            } catch (Exception e) {
                err = "Greška: " + e.getMessage();
            } finally {
                if (c != null) c.disconnect();
            }
            final String fe = err;
            final Bitmap fo = out;
            runOnUiThread(() -> {
                progress.setVisibility(android.view.View.GONE);
                updateGen();
                if (fe != null) {
                    status.setText(fe);
                    return;
                }
                result = fo;
                resultView.setImageBitmap(fo);
                status.setText("Gotovo!");
                setBtn.setVisibility(android.view.View.VISIBLE);
                saveBtn.setVisibility(android.view.View.VISIBLE);
            });
        }).start();
    }

    private void setWallpaper() {
        if (result == null) return;
        setBtn.setEnabled(false);
        new Thread(() -> {
            boolean ok;
            try {
                WallpaperManager.getInstance(this).setBitmap(result, null, true,
                        WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
                ok = true;
            } catch (Exception e) {
                ok = false;
            }
            final boolean d = ok;
            runOnUiThread(() -> {
                setBtn.setEnabled(true);
                Toast.makeText(this, d ? "Pozadina postavljena na početni i zaključani ekran."
                        : "Postavljanje nije uspjelo.", Toast.LENGTH_LONG).show();
            });
        }).start();
    }

    private void saveToGallery() {
        if (result == null) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(this, "Za ovo treba Android 10 ili noviji.", Toast.LENGTH_LONG).show();
            return;
        }
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.DISPLAY_NAME, "neon_coast_" + System.currentTimeMillis() + ".jpg");
        cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        cv.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Neon Coast");
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        try (OutputStream os = getContentResolver().openOutputStream(uri)) {
            result.compress(Bitmap.CompressFormat.JPEG, 95, os);
            Toast.makeText(this, "Sačuvano u Galeriju, album „Neon Coast“.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Snimanje nije uspjelo.", Toast.LENGTH_LONG).show();
        }
    }

    private static String readAll(InputStream is) throws Exception {
        if (is == null) return "{}";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[16384];
        int r;
        while ((r = is.read(buf)) > 0) bos.write(buf, 0, r);
        return bos.toString("UTF-8");
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

    private LinearLayout.LayoutParams params() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        lp.topMargin = dp(10);
        return lp;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
