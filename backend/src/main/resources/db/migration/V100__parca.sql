CREATE TABLE parca (
    id               BIGSERIAL      PRIMARY KEY,
    kod              VARCHAR(50)    NOT NULL,
    ad               VARCHAR(150)   NOT NULL,
    stok_adedi       INTEGER        NOT NULL,
    birim_fiyat      NUMERIC(12, 2) NOT NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    olusturma_zamani TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uk_parca_kod UNIQUE (kod),
    CONSTRAINT ck_parca_stok_adedi CHECK (stok_adedi >= 0),
    CONSTRAINT ck_parca_birim_fiyat CHECK (birim_fiyat >= 0)
);
