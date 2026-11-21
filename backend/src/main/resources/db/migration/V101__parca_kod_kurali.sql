-- Parça kodu entity'deki biçimle saklanır: baştaki/sondaki boşluksuz ve büyük harf (Parca.kodNormallestir).
-- Kod bu kurala uymadan başka bir yoldan (elle SQL, içe aktarma) yazılırsa p-001 ile P-001 iki ayrı
-- kayıt olurdu. Kural veritabanında da zorunlu tutulur. Boş kodu da bu kural reddeder.

-- Varsa eski kayıtlar önce aynı biçime getirilir. Normalleştirince iki kayıt aynı koda düşerse
-- UNIQUE kısıtı migration'ı durdurur: bu, elle çözülmesi gereken gerçek bir çift kayıttır.
UPDATE parca
   SET kod = upper(regexp_replace(kod, '^\s+|\s+$', '', 'g'))
 WHERE kod !~ '^\S(.*\S)?$' OR kod <> upper(kod);

ALTER TABLE parca
    ADD CONSTRAINT ck_parca_kod_normal CHECK (kod ~ '^\S(.*\S)?$' AND kod = upper(kod));
