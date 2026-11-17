# CineTicket

**CineTicket** je web aplikacija za upravljanje bioskopskim projekcijama i online prodaju karata. Korisnici mogu da pregledaju filmove i projekcije, izaberu sedišta, plate kartu online i dobiju elektronsku kartu, a administratori upravljaju sadržajem i prate poslovanje kroz analitički dashboard.

Projekat je razvijen kao deo diplomskog rada na Fakultetu tehničkih nauka u Novom Sadu.

---

##  Funkcionalnosti

###  Gost
- Pregled filmova i projekcija
- Registracija i prijava na sistem

###  Korisnik
- Izbor projekcije i sedišta u sali
- Kupovina karata i online plaćanje preko **Stripe**-a
- Automatski obračun popusta i povoljnijih paketa pri kupovini
- Pregled kupljenih karata i istorije porudžbina
- Slanje potvrde i karata na email
- Preporuke filmova na osnovu prethodnih kupovina
- Pregled i izmena korisničkog profila

###  Administrator
- Upravljanje filmovima, projekcijama i salama
- Analitički dashboard sa pregledom prodaje i statistikom poslovanja

###  Bezbednost
- Autentifikacija pomoću **JWT** tokena, sa **refresh token** mehanizmom
- Autorizacija zasnovana na ulogama (Gost, Korisnik, Administrator)
- Ograničavanje broja zahteva (rate limiting) radi zaštite od zloupotrebe
- Zakazani pozadinski zadaci (scheduler)

---

## Tehnologije

| Sloj | Tehnologije |
|------|-------------|
| **Backend** | Java, Spring Boot, Spring Security, Spring Data JPA |
| **Frontend** | Angular, TypeScript, SCSS |
| **Baza podataka** | PostgreSQL, Flyway (migracije) |
| **Plaćanje** | Stripe |
| **Autentifikacija** | JWT + refresh tokeni |
| **Build alati** | Maven, npm |

---

## Struktura projekta
```
├── src/main/java/com/cineticket/
│   ├── config/          # Konfiguracija (Spring Security i dr.)
│   ├── controller/      # REST kontroleri
│   ├── dto/             # Objekti za prenos podataka
│   ├── entity/          # JPA entiteti
│   ├── exception/       # Obrada grešaka
│   ├── repository/      # Pristup bazi podataka
│   ├── scheduler/       # Zakazani zadaci
│   ├── security/        # JWT, rate limiting
│   └── service/         # Poslovna logika
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/    # Flyway SQL migracije
├── src/test/            # Testovi
└── frontend/            # Angular aplikacija
    └── src/app/
        ├── core/        # Servisi, modeli, guard-ovi, interceptori
        ├── features/    # Stranice aplikacije
        └── shared/      # Zajedničke komponente i layout
```

---

## Pokretanje projekta

### Preduslovi
- Java 17+
- Node.js i npm
- PostgreSQL
- Stripe nalog (test ključevi)

### 1. Baza podataka
Napraviti PostgreSQL bazu:
```sql
CREATE DATABASE cineticket;
```
Tabele i početni podaci se kreiraju automatski pomoću Flyway migracija pri pokretanju backend-a.

### 2. Konfiguracija
Osetljivi podaci se ne čuvaju u repozitorijumu. Pre pokretanja postaviti sledeće promenljive okruženja:

```
DB_URL=jdbc:postgresql://localhost:5432/cineticket
DB_USERNAME=postgres
DB_PASSWORD=tvoja_lozinka
JWT_SECRET=tvoj_jwt_secret
STRIPE_SECRET_KEY=sk_test_...
MAIL_USERNAME=tvoj_email
MAIL_PASSWORD=tvoja_lozinka_za_mail
```

### 3. Backend
```bash
mvn spring-boot:run
```
Backend se pokreće na `http://localhost:8080`.

### 4. Frontend
```bash
cd frontend
npm install
ng serve
```
Aplikacija je dostupna na `http://localhost:4200`.
