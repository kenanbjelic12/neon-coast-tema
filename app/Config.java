package com.neoncoast.tema;

/** Ovdje upisi adresu svog servera (bez / na kraju). */
final class Config {
    // Primjer: "https://pozadine.waveico.com"  ili za test na računaru: "http://192.168.1.10:3000"
    static final String SERVER_URL = "https://pozadine.waveico.com";

    // Mora biti isto kao APP_TOKEN na serveru
    static final String APP_TOKEN = "neon-coast-app";

    private Config() {}
}
