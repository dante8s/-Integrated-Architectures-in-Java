# SportCoach: integrovaná architektúra (Spring Boot + Thymeleaf)

Marketplace športových trénerov (podobne ako preply.com, ale s trénermi rôznych športov).
Bakalárska práca: porovnanie **integrovaného frontendu** (Thymeleaf priamo v Java projekte, tento repozitár)
so **samostatnou SPA** (bude samostatný projekt).

## Čo aplikácia vie

| Rola | Možnosti |
|---|---|
| Návštevník | katalóg trénerov s filtrami (šport, meno, prax) a stránkovaním, profil trénera, voľné termíny, recenzie, registrácia |
| Študent | rezervácia voľného termínu, „My bookings“ (zrušenie pred začiatkom lekcie), recenzia 1–5 ★ po ukončenej lekcii, zmena mena |
| Tréner | registrácia s profilom → čaká na schválenie administrátorom; termíny dostupnosti, žiadosti o rezerváciu (potvrdiť / zamietnuť / ukončiť), úprava profilu (cena, športy, popis) |
| Administrátor | schválenie / zamietnutie trénerov s uvedením dôvodu, CRUD športov |

## Technológie

Java 21, Spring Boot 4, Spring MVC + Thymeleaf, Bootstrap 5, Spring Security (session + form login),
Spring Data JPA (Hibernate), PostgreSQL 17 (Docker), Flyway, Bean Validation, JUnit 5 + Mockito + MockMvc.

## Spustenie

**Potrebné:** JDK 21 alebo novší a spustený **Docker Desktop**. Maven netreba inštalovať, projekt obsahuje `mvnw`.

```powershell
git clone https://github.com/dante8s/-Integrated-Architectures-in-Java.git
cd ./-Integrated-Architectures-in-Java

# s demo dátami (odporúčané na prezeranie)
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Na Linuxe/macOS: `./mvnw spring-boot:run -Dspring-boot.run.profiles=demo`.

Aplikácia sama spustí PostgreSQL v Dockeri (`compose.yaml`) a Flyway vytvorí tabuľky.
Po riadku `Started IntegratedArchitecturesApplication` otvorte **http://localhost:8081**.

Bez profilu `demo` (`.\mvnw.cmd spring-boot:run`) obsahuje databáza iba administrátora a športy.

Porty: aplikácia **8081**, PostgreSQL **5433**. Úplné vymazanie databázy: `docker compose down -v`.

## Účty

| Rola | Email | Heslo |
|---|---|---|
| Administrátor | `admin@sportcoach.local` | `admin123` |
| Študent | `student@demo.local` (Olena) | `demo1234` |
| Študent | `student2@demo.local` (Maksym) | `demo1234` |
| Tréner | `coach@demo.local` (Andrii, tenis) | `demo1234` |
| Tréner (čaká na schválenie) | `pending@demo.local` | `demo1234`: prihlásenie nie je možné, kým ho administrátor neschváli |

Demo účty existujú iba s profilom `demo`. Ďalší 4 tréneri (`coach2…coach5@demo.local`) majú rovnaké heslo.

## Čo vyskúšať

1. **Návštevník:** *Coaches* → filter podľa športu → tréner **Andrii Kovalenko** → *See free time and book* a *Reviews*.
2. **Študent** (`student@demo.local`): po prihlásení sa otvorí jeho stránka → *My bookings*: žiadosť čakajúca
   na trénera, ukončená lekcia bez recenzie (*Leave a review*), história. Zarezervujte si nový termín u ľubovoľného trénera.
3. **Tréner** (`coach@demo.local`): po prihlásení sa otvorí *My profile* (zmeňte cenu, v katalógu sa prejaví hneď) →
   *Booking requests*: *Confirm* / *Decline* novej žiadosti, *Complete* pre už odučenú lekciu → *My slots*: pridať termín.
4. **Administrátor:** *Coach approval* → schváliť alebo zamietnuť trénera **Dmytro Lysenko** (s dôvodom; zamietnutý
   tréner uvidí dôvod pri pokuse o prihlásenie) → *Sports*: pridať alebo upraviť šport.

## Architektúra

```
src/main/java/.../
├── user/       registrácia, vlastná stránka (/profile)
├── security/   SecurityConfig, prihlásenie/odhlásenie, presmerovanie po prihlásení podľa roly
├── coach/      katalóg, stránka trénera, profil trénera, schvaľovanie administrátorom
├── sport/      športy (CRUD administrátora)
├── booking/    termíny, rezervácie (študent aj tréner)
├── review/     recenzie
└── home/       úvodná stránka
src/main/resources/
├── templates/          Thymeleaf stránky (+ fragments/layout.html: head, navbar, footer)
├── db/migration/       Flyway V1–V5: schéma databázy
└── db/demo/            demo dáta (iba profil demo)
```

- **Tenké kontroléry, celá biznis logika v servisoch.** REST kontroléry pre SPA budú volať tie isté servisy,
  takže porovnanie architektúr sa bude týkať iba prezentačnej vrstvy.
- Šablóny dostávajú DTO (`record`), nie JPA entity (`spring.jpa.open-in-view=false`).
- Pravidlá závislé od databázy sú zdvojené obmedzeniami PostgreSQL: čiastočný unikátny index
  „jedna aktívna rezervácia na termín“, `UNIQUE` pre recenziu lekcie, `CHECK` pre hodnotenie.
- Bezpečnosť: CSRF vo všetkých formulároch, BCrypt, prístup podľa rolí (`/admin/**`, `/coach/**`, `/student/**`),
  stav trénera sa overuje až po zadaní správneho hesla.

## Testy

```powershell
.\mvnw.cmd test
```

44 testov: unit testy servisov (Mockito), integračné testy s reálnou PostgreSQL (repozitáre, databázové pravidlá)
a MockMvc testy bezpečnosti a stránok. Vyžaduje spustený Docker.

