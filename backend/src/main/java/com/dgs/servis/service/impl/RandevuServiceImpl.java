package com.dgs.servis.service.impl;

import com.dgs.servis.dto.RandevuOlusturRequest;
import com.dgs.servis.dto.RandevuResponse;
import com.dgs.servis.entity.Arac;
import com.dgs.servis.entity.Randevu;
import com.dgs.servis.exception.KayitBulunamadiException;
import com.dgs.servis.repository.AracRepository;
import com.dgs.servis.repository.RandevuRepository;
import com.dgs.servis.service.RandevuService;
import com.dgs.servis.exception.GecersizSiralamaAlaniException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RandevuServiceImpl implements RandevuService {

    private final AracRepository aracRepository;
    private final RandevuRepository randevuRepository;

    public RandevuServiceImpl(
            AracRepository aracRepository,
            RandevuRepository randevuRepository) {
        this.aracRepository = aracRepository;
        this.randevuRepository = randevuRepository;
    }

    @Override
    @Transactional
    public RandevuResponse olustur(RandevuOlusturRequest request) {
        Arac arac = aracRepository.findById(request.aracId())
                .orElseThrow(() -> new KayitBulunamadiException("Araç", request.aracId()));

        Randevu randevu = new Randevu(arac, request.randevuZamani(), request.servisTalebi());
        return yanitaDonustur(randevuRepository.save(randevu));
    }

    @Override
    @Transactional(readOnly = true)
    public RandevuResponse idIleGetir(Long id) {
        Randevu randevu = randevuRepository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Randevu", id));

        return yanitaDonustur(randevu);
    }

        @Override
    @Transactional(readOnly = true)
    public Page<RandevuResponse> tumunuListele(Pageable pageable) {
        for (var siralama : pageable.getSort()) {
            String alan = siralama.getProperty();
            if (!"id".equals(alan) && !"randevuZamani".equals(alan)) {
                throw new GecersizSiralamaAlaniException(
                        "Sıralama alanı id veya randevuZamani olmalıdır.");
            }
        }

        return randevuRepository.findAll(pageable).map(this::yanitaDonustur);
    }

    private RandevuResponse yanitaDonustur(Randevu randevu) {
        return new RandevuResponse(
                randevu.getId(),
                randevu.getArac().getId(),
                randevu.getRandevuZamani(),
                randevu.getServisTalebi()
        );
    }
}