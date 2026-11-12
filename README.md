# Neon Coast Tema (Android)

Aplikacija: preview teme + dugme "Postavi temu" koje jednim klikom postavi
pozadinu na pocetni i zakljucani ekran. Dugme "Primijeni neonske ikonice"
primjenjuje ikonice preko Nova Launchera (Android ne dozvoljava aplikacijama
da mijenjaju ikonice u Samsungovom launcheru).

## Kako dobiti APK bez Android Studija (GitHub, besplatno)
1. Napravi racun na github.com -> "New repository" -> ime npr. neon-coast-tema -> Create.
2. Na stranici repozitorija: "uploading an existing file" -> prevuci SVE iz ovog foldera
   (ukljucujuci skriveni folder .github) -> Commit changes.
   Ako se .github ne prebaci: Add file -> Create new file -> ime
   `.github/workflows/build.yml` -> zalijepi sadrzaj tog fajla -> Commit.
3. Tab "Actions" -> sacekaj 3-5 min da "Napravi APK" postane zelen.
4. Klikni na taj build -> dolje "Artifacts" -> NeonCoastTema-APK -> skini zip -> unutra app-debug.apk.
5. Prebaci APK na telefon i instaliraj (dozvoli "Instaliraj nepoznate aplikacije").

## Sa Android Studijem
File -> Open -> izaberi ovaj folder -> Build -> Build App Bundle(s) / APK(s) -> Build APK(s).
