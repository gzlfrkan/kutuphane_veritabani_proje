# Kutuphane Yonetim Sistemi

## Kurulum

### 1. Veritabani Kurulumu
1. PostgreSQL kurun
2. `kutuphane` adinda bir veritabani olusturun:
   ```sql
   CREATE DATABASE kutuphane;
   ```
3. `database.sql` dosyasini calistirin:
   ```bash
   psql -U postgres -d kutuphane -f database.sql
   ```

### 2. Baglanti Ayarlari
`Main.java` icindeki `getConnection()` metodunda:
- URL: `jdbc:postgresql://localhost:5432/kutuphane`
- User: `postgres`
- Password: `postgres`

Gerekirse bu degerleri kendi sisteminize gore degistirin.

### 3. Derleme ve Calistirma
```bash
cd src
javac -cp ".;postgresql-42.7.4.jar" Main.java
java -cp ".;postgresql-42.7.4.jar" Main
```

PostgreSQL JDBC driver indir: https://jdbc.postgresql.org/download/

## Varsayilan Giris Bilgileri
- Kullanici: `admin`
- Sifre: `admin123`

veya

- Kullanici: `gorevli1`
- Sifre: `123456`
