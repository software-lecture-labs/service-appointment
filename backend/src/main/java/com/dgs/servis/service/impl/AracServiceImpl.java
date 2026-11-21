package com.dgs.servis.service.impl;

import com.dgs.servis.dto.AracOlusturRequest;
import com.dgs.servis.dto.AracResponse;
import com.dgs.servis.entity.Arac;
import com.dgs.servis.entity.Musteri;
import com.dgs.servis.exception.KayitBulunamadiException;
import com.dgs.servis.exception.KayitZatenVarException;
import com.dgs.servis.repository.AracRepository;
import com.dgs.servis.repository.MusteriRepository;
import com.dgs.servis.service.AracService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Set;
import com.dgs.servis.exception.GecersizSiralamaAlaniException;

@Service
public class AracServiceImpl implements AracService {

    private final AracRepository aracRepository;
    private final MusteriRepository musteriRepository;

    public AracServiceImpl(AracRepository aracRepository, MusteriRepository musteriRepository) {
        this.aracRepository = aracRepository;
        this.musteriRepository = musteriRepository;
    }

        @Override
    @Transactional
    public AracResponse olustur(AracOlusturRequest request) {
        Musteri musteri = musteriRepository.findById(request.musteriId())
                .orElseThrow(() -> new KayitBulunamadiException("Müşteri", request.musteriId()));

        Arac arac = new Arac(request.plaka(), request.marka(), request.model(), musteri);

        if (aracRepository.existsByPlaka(arac.getPlaka())) {
            throw new KayitZatenVarException("Bu plakayla kayıtlı bir araç zaten var.");
        }

        Arac kaydedilenArac = aracRepository.save(arac);
        return yanitaDonustur(kaydedilenArac);
    }

    @Override
    @Transactional(readOnly = true)
    public AracResponse idIleGetir(Long id) {
        Arac arac = aracRepository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Araç", id));

        return yanitaDonustur(arac);
    }

@Override
@Transactional(readOnly = true)
public Page<AracResponse> tumunuListele(Pageable pageable) {
    Set<String> izinliAlanlar = Set.of("id", "plaka", "marka", "model");

    for (var siralama : pageable.getSort()) {
        if (!izinliAlanlar.contains(siralama.getProperty())) {
            throw new GecersizSiralamaAlaniException(
                    "Sıralama alanı id, plaka, marka veya model olmalıdır.");
        }
    }

    return aracRepository.findAll(pageable).map(this::yanitaDonustur);
}
    private AracResponse yanitaDonustur(Arac arac) {
        return new AracResponse(
                arac.getId(),
                arac.getMusteri().getId(),
                arac.getPlaka(),
                arac.getMarka(),
                arac.getModel()
        );
    }
}