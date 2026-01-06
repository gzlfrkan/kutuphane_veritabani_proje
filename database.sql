DROP TABLE IF EXISTS LOG_ISLEM CASCADE;
DROP TABLE IF EXISTS CEZA CASCADE;
DROP TABLE IF EXISTS ODUNC CASCADE;
DROP TABLE IF EXISTS KITAP CASCADE;
DROP TABLE IF EXISTS KATEGORI CASCADE;
DROP TABLE IF EXISTS UYE CASCADE;
DROP TABLE IF EXISTS KULLANICI CASCADE;
CREATE TABLE KULLANICI (
    KullaniciID SERIAL PRIMARY KEY,
    KullaniciAdi VARCHAR(50) NOT NULL UNIQUE,
    Sifre VARCHAR(100) NOT NULL,
    AdSoyad VARCHAR(100) NOT NULL,
    Rol VARCHAR(20) NOT NULL CHECK (Rol IN ('Admin', 'Gorevli'))
);
CREATE TABLE UYE (
    UyeID SERIAL PRIMARY KEY,
    Ad VARCHAR(50) NOT NULL,
    Soyad VARCHAR(50) NOT NULL,
    Telefon VARCHAR(20) NOT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    KayitTarihi DATE DEFAULT CURRENT_DATE,
    ToplamBorc DECIMAL(10, 2) DEFAULT 0
);
CREATE TABLE KATEGORI (
    KategoriID SERIAL PRIMARY KEY,
    KategoriAdi VARCHAR(50) NOT NULL UNIQUE
);
CREATE TABLE KITAP (
    KitapID SERIAL PRIMARY KEY,
    KitapAdi VARCHAR(200) NOT NULL,
    Yazar VARCHAR(100) NOT NULL,
    KategoriID INT REFERENCES KATEGORI(KategoriID),
    Yayinevi VARCHAR(100) NOT NULL,
    BasimYili INT NOT NULL,
    ToplamAdet INT NOT NULL CHECK (ToplamAdet >= 0),
    MevcutAdet INT NOT NULL CHECK (MevcutAdet >= 0)
);
CREATE TABLE ODUNC (
    OduncID SERIAL PRIMARY KEY,
    UyeID INT NOT NULL REFERENCES UYE(UyeID),
    KitapID INT NOT NULL REFERENCES KITAP(KitapID),
    KullaniciID INT NOT NULL REFERENCES KULLANICI(KullaniciID),
    OduncTarihi DATE NOT NULL DEFAULT CURRENT_DATE,
    SonTeslimTarihi DATE NOT NULL,
    TeslimTarihi DATE
);
CREATE TABLE CEZA (
    CezaID SERIAL PRIMARY KEY,
    OduncID INT NOT NULL REFERENCES ODUNC(OduncID),
    UyeID INT NOT NULL REFERENCES UYE(UyeID),
    Tutar DECIMAL(10, 2) NOT NULL,
    GecikmeGunu INT NOT NULL,
    OlusturmaTarihi DATE DEFAULT CURRENT_DATE
);
CREATE TABLE LOG_ISLEM (
    LogID SERIAL PRIMARY KEY,
    TabloAdi VARCHAR(50) NOT NULL,
    IslemTipi VARCHAR(20) NOT NULL,
    Aciklama TEXT,
    IslemTarihi TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_kitap_kategori ON KITAP(KategoriID);
CREATE INDEX idx_odunc_uye ON ODUNC(UyeID);
CREATE INDEX idx_odunc_kitap ON ODUNC(KitapID);
CREATE INDEX idx_odunc_teslim ON ODUNC(TeslimTarihi);
CREATE INDEX idx_ceza_uye ON CEZA(UyeID);
CREATE OR REPLACE FUNCTION sp_YeniOduncVer(
        p_UyeID INT,
        p_KitapID INT,
        p_KullaniciID INT
    ) RETURNS TEXT AS $$
DECLARE v_AktifOdunc INT;
v_MevcutAdet INT;
BEGIN
SELECT COUNT(*) INTO v_AktifOdunc
FROM ODUNC
WHERE UyeID = p_UyeID
    AND TeslimTarihi IS NULL;
IF v_AktifOdunc >= 5 THEN RETURN 'HATA: Uye maksimum 5 kitap odunc alabilir. Aktif odunc sayisi: ' || v_AktifOdunc;
END IF;
SELECT MevcutAdet INTO v_MevcutAdet
FROM KITAP
WHERE KitapID = p_KitapID;
IF v_MevcutAdet IS NULL THEN RETURN 'HATA: Kitap bulunamadi.';
END IF;
IF v_MevcutAdet <= 0 THEN RETURN 'HATA: Kitap stokta yok.';
END IF;
INSERT INTO ODUNC (
        UyeID,
        KitapID,
        KullaniciID,
        OduncTarihi,
        SonTeslimTarihi
    )
VALUES (
        p_UyeID,
        p_KitapID,
        p_KullaniciID,
        CURRENT_DATE,
        CURRENT_DATE + INTERVAL '15 days'
    );
RETURN 'BASARILI: Odunc islemi tamamlandi. Son teslim tarihi: ' || (CURRENT_DATE + INTERVAL '15 days')::DATE;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION sp_KitapTeslimAl(p_OduncID INT, p_TeslimTarihi DATE) RETURNS TEXT AS $$
DECLARE v_SonTeslimTarihi DATE;
v_UyeID INT;
v_GecikmeGunu INT;
v_CezaTutar DECIMAL(10, 2);
v_TeslimDurum DATE;
BEGIN
SELECT SonTeslimTarihi,
    UyeID,
    TeslimTarihi INTO v_SonTeslimTarihi,
    v_UyeID,
    v_TeslimDurum
FROM ODUNC
WHERE OduncID = p_OduncID;
IF v_SonTeslimTarihi IS NULL THEN RETURN 'HATA: Odunc kaydi bulunamadi.';
END IF;
IF v_TeslimDurum IS NOT NULL THEN RETURN 'HATA: Bu kitap zaten teslim edilmis.';
END IF;
UPDATE ODUNC
SET TeslimTarihi = p_TeslimTarihi
WHERE OduncID = p_OduncID;
IF p_TeslimTarihi > v_SonTeslimTarihi THEN v_GecikmeGunu := p_TeslimTarihi - v_SonTeslimTarihi;
v_CezaTutar := v_GecikmeGunu * 5.00;
INSERT INTO CEZA (OduncID, UyeID, Tutar, GecikmeGunu)
VALUES (p_OduncID, v_UyeID, v_CezaTutar, v_GecikmeGunu);
RETURN 'BASARILI: Kitap teslim alindi. ' || v_GecikmeGunu || ' gun gecikme, ' || v_CezaTutar || ' TL ceza eklendi.';
END IF;
RETURN 'BASARILI: Kitap zamaninda teslim alindi.';
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION sp_UyeOzetRapor(p_UyeID INT) RETURNS TABLE (
        ToplamOdunc BIGINT,
        AktifOdunc BIGINT,
        ToplamCeza DECIMAL(10, 2)
    ) AS $$ BEGIN RETURN QUERY
SELECT (
        SELECT COUNT(*)
        FROM ODUNC
        WHERE UyeID = p_UyeID
    ),
    (
        SELECT COUNT(*)
        FROM ODUNC
        WHERE UyeID = p_UyeID
            AND TeslimTarihi IS NULL
    ),
    COALESCE(
        (
            SELECT SUM(Tutar)
            FROM CEZA
            WHERE UyeID = p_UyeID
        ),
        0::DECIMAL(10, 2)
    );
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION fn_odunc_insert() RETURNS TRIGGER AS $$ BEGIN
UPDATE KITAP
SET MevcutAdet = MevcutAdet - 1
WHERE KitapID = NEW.KitapID;
INSERT INTO LOG_ISLEM (TabloAdi, IslemTipi, Aciklama)
VALUES (
        'ODUNC',
        'INSERT',
        'Yeni odunc: OduncID=' || NEW.OduncID || ', UyeID=' || NEW.UyeID || ', KitapID=' || NEW.KitapID
    );
RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER TR_ODUNC_INSERT
AFTER
INSERT ON ODUNC FOR EACH ROW EXECUTE FUNCTION fn_odunc_insert();
CREATE OR REPLACE FUNCTION fn_odunc_update_teslim() RETURNS TRIGGER AS $$ BEGIN IF OLD.TeslimTarihi IS NULL
    AND NEW.TeslimTarihi IS NOT NULL THEN
UPDATE KITAP
SET MevcutAdet = MevcutAdet + 1
WHERE KitapID = NEW.KitapID;
INSERT INTO LOG_ISLEM (TabloAdi, IslemTipi, Aciklama)
VALUES (
        'ODUNC',
        'UPDATE',
        'Kitap teslim alindi: OduncID=' || NEW.OduncID
    );
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER TR_ODUNC_UPDATE_TESLIM
AFTER
UPDATE ON ODUNC FOR EACH ROW EXECUTE FUNCTION fn_odunc_update_teslim();
CREATE OR REPLACE FUNCTION fn_ceza_insert() RETURNS TRIGGER AS $$ BEGIN
UPDATE UYE
SET ToplamBorc = ToplamBorc + NEW.Tutar
WHERE UyeID = NEW.UyeID;
INSERT INTO LOG_ISLEM (TabloAdi, IslemTipi, Aciklama)
VALUES (
        'CEZA',
        'INSERT',
        'Ceza eklendi: UyeID=' || NEW.UyeID || ', Tutar=' || NEW.Tutar || ' TL'
    );
RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER TR_CEZA_INSERT
AFTER
INSERT ON CEZA FOR EACH ROW EXECUTE FUNCTION fn_ceza_insert();
CREATE OR REPLACE FUNCTION fn_uye_delete_block() RETURNS TRIGGER AS $$
DECLARE v_AktifOdunc INT;
BEGIN
SELECT COUNT(*) INTO v_AktifOdunc
FROM ODUNC
WHERE UyeID = OLD.UyeID
    AND TeslimTarihi IS NULL;
IF v_AktifOdunc > 0 THEN RAISE EXCEPTION 'Uyenin aktif odunc kaydi var. Silinemez.';
END IF;
IF OLD.ToplamBorc > 0 THEN RAISE EXCEPTION 'Uyenin odenmemis borcu var. Silinemez.';
END IF;
RETURN OLD;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER TR_UYE_DELETE_BLOCK BEFORE DELETE ON UYE FOR EACH ROW EXECUTE FUNCTION fn_uye_delete_block();
INSERT INTO KULLANICI (KullaniciAdi, Sifre, AdSoyad, Rol)
VALUES (
        'admin',
        'admin123',
        'Sistem Yoneticisi',
        'Admin'
    );
INSERT INTO KULLANICI (KullaniciAdi, Sifre, AdSoyad, Rol)
VALUES ('gorevli1', '123456', 'Ahmet Yilmaz', 'Gorevli');
INSERT INTO KULLANICI (KullaniciAdi, Sifre, AdSoyad, Rol)
VALUES ('cool', 'cool', 'Cool Admin', 'Admin');
INSERT INTO KATEGORI (KategoriAdi)
VALUES ('Roman'),
    ('Bilim'),
    ('Tarih'),
    ('Edebiyat'),
    ('Teknoloji');
INSERT INTO UYE (Ad, Soyad, Telefon, Email)
VALUES ('Ali', 'Veli', '05551234567', 'ali@email.com'),
    (
        'Ayse',
        'Yilmaz',
        '05559876543',
        'ayse@email.com'
    ),
    (
        'Mehmet',
        'Demir',
        '05553334455',
        'mehmet@email.com'
    );
INSERT INTO KITAP (
        KitapAdi,
        Yazar,
        KategoriID,
        Yayinevi,
        BasimYili,
        ToplamAdet,
        MevcutAdet
    )
VALUES (
        'Suç ve Ceza',
        'Fyodor Dostoyevski',
        1,
        'İletişim Yayınları',
        2020,
        5,
        5
    ),
    (
        'Sefiller',
        'Victor Hugo',
        1,
        'Can Yayınları',
        2019,
        4,
        4
    ),
    (
        '1984',
        'George Orwell',
        1,
        'Can Yayınları',
        2021,
        6,
        6
    ),
    (
        'Hayvan Çiftliği',
        'George Orwell',
        1,
        'Can Yayınları',
        2020,
        5,
        5
    ),
    (
        'Tutunamayanlar',
        'Oğuz Atay',
        4,
        'İletişim Yayınları',
        2020,
        3,
        3
    ),
    (
        'Nutuk',
        'Mustafa Kemal Atatürk',
        3,
        'Türkiye İş Bankası',
        2018,
        10,
        10
    );