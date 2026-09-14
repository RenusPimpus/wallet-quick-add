# Wallet Quick Add

Prosta aplikacja Android do szybkiego dodawania wydatków do **Wallet by BudgetBakers**.

## Co potrafi

- przyjmuje tekst przez systemowe **Udostępnij** i **Przetwarzaj tekst**,
- wyciąga kwoty zapisane m.in. jako `42,50 zł`, `1 234,56 PLN` lub `1,234.56`,
- pobiera konta i kategorie z oficjalnego Wallet REST API,
- tworzy wydatek na wybranym koncie i w wybranej kategorii,
- przechowuje token zaszyfrowany kluczem z Android Keystore.

## Instalacja gotowego APK

1. Otwórz kartę **Actions** w repozytorium.
2. Wybierz najnowszy udany przebieg **Build Android APK**.
3. Pobierz artefakt `wallet-quick-add-debug-apk`.
4. Rozpakuj ZIP i uruchom `app-debug.apk` na telefonie.
5. Zezwól przeglądarce lub aplikacji Moje pliki na instalowanie z tego źródła, jeśli Android o to zapyta.

## Konfiguracja

W Wallet wygeneruj osobisty token REST API (wymagany plan Premium), następnie wklej go wyłącznie do zainstalowanej aplikacji. Nie umieszczaj tokenu w repozytorium ani w zgłoszeniach GitHub.

Bazowy adres API: `https://rest.budgetbakers.com/wallet`.

## Status

Wersja `0.1.0` jest prototypem. Pierwszy zapis sprawdź bezpośrednio w Wallet.
