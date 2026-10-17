
package com.dgs.servis.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dgs.servis.dto.MusteriOlusturRequest;
import com.dgs.servis.dto.MusteriResponse;
import com.dgs.servis.entity.Musteri;
import com.dgs.servis.exception.KayitBulunamadiException;
import com.dgs.servis.repository.MusteriRepository;
import com.dgs.servis.service.MusteriService;

@Service
@Transactional(readOnly = true)
public class MusteriServiceImpl implements MusteriService {

    private final MusteriRepository musteriRepository;

    public MusteriServiceImpl(MusteriRepository musteriRepository) {
        this.musteriRepository = musteriRepository;
    }

    @Override
    @Transactional
    public MusteriResponse olustur(MusteriOlusturRequest request) {

        Musteri musteri = new Musteri();

        musteri.setAd(request.ad());
        musteri.setSoyad(request.soyad());
        musteri.setTelefon(request.telefon());
        musteri.setEmail(request.email());

        Musteri kaydedilenMusteri = musteriRepository.save(musteri);

        return yanitaDonustur(kaydedilenMusteri);
    }

    @Override
    public MusteriResponse idIleGetir(Long id) {

        Musteri musteri = musteriRepository.findById(id)
                .orElseThrow(() ->
                        new KayitBulunamadiException("Müşteri", id)
                );

        return yanitaDonustur(musteri);
    }

    @Override
    public List<MusteriResponse> tumunuListele() {

        return musteriRepository.findAll()
                .stream()
                .map(this::yanitaDonustur)
                .toList();
    }

    private MusteriResponse yanitaDonustur(Musteri musteri) {

        return new MusteriResponse(
                musteri.getId(),
                musteri.getAd(),
                musteri.getSoyad(),
                musteri.getTelefon(),
                musteri.getEmail()
        );
    }
}
