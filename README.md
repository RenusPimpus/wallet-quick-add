# Wallet Quick Add

Prosta aplikacja Android do szybkiego dodawania wydatków do **Wallet by BudgetBakers**.

## Co potrafi

- przyjmuje tekst przez systemowe **Udostępnij** i **Przetwarzaj tekst**,
- wyciąga kwoty zapisane m.in. jako `42,50 zł`, `1 234,56 PLN` lub `1,234.56`,
- pobiera konta i kategorie z oficjalnego Wallet REST API,
- tworzy wydatek na wybranym koncie i w wybranej kategorii,
- przechowuje token zaszyfrowany kluczem z Android Keystore,
- obsługuje jasny i ciemny motyw systemowy.

## Instalacja i aktualizacje przez Obtainium

1. Dodaj w Obtainium aplikację z adresu:
   `https://github.com/RenusPimpus/wallet-quick-add`
2. Obtainium rozpozna źródło jako GitHub.
3. Zainstaluj najnowsze APK z sekcji **Releases**.
4. Kolejne wydania będą wykrywane przez Obtainium i instalowane jako aktualizacja.

Pierwsze podpisane wydanie ma inny podpis niż wcześniejsza wersja debug. Jeśli masz ją zainstalowaną, odinstaluj ją jeden raz przed instalacją wersji z Releases. Spowoduje to usunięcie zapisanego tokenu. Następne aktualizacje zachowają aplikację i jej dane.

## Ręczna instalacja

Otwórz sekcję [Releases](https://github.com/RenusPimpus/wallet-quick-add/releases), wybierz najnowszą wersję i pobierz plik `WalletQuickAdd-*.apk`.

## Konfiguracja

W Wallet wygeneruj osobisty token REST API (wymagany plan Premium), następnie wklej go wyłącznie do zainstalowanej aplikacji. Nie umieszczaj tokenu w repozytorium ani w zgłoszeniach GitHub.

Bazowy adres API: `https://rest.budgetbakers.com/wallet`.

## Podpis aplikacji

Wydania są automatycznie podpisywane w GitHub Actions. Pliku klucza, jego kopii zapasowej ani haseł nie wolno dodawać do repozytorium. Utrata klucza uniemożliwi aktualizowanie już zainstalowanej aplikacji tym samym pakietem.
