
package com.dgs.servis.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dgs.servis.dto.MusteriOlusturRequest;
import com.dgs.servis.dto.MusteriResponse;
import com.dgs.servis.service.MusteriService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/musteriler")
public class MusteriController {

    private final MusteriService musteriService;

    public MusteriController(MusteriService musteriService) {
        this.musteriService = musteriService;
    }

    // Yeni müşteri oluşturma
    @PostMapping
    public ResponseEntity<MusteriResponse> olustur(
            @Valid @RequestBody MusteriOlusturRequest request) {

        MusteriResponse response = musteriService.olustur(request);

        URI konum = URI.create("/api/musteriler/" + response.id());

        return ResponseEntity.created(konum).body(response);
    }

    // Tüm müşterileri listeleme
    @GetMapping
    public ResponseEntity<List<MusteriResponse>> tumunuListele() {

        return ResponseEntity.ok(musteriService.tumunuListele());
    }

    // ID ile müşteri getirme
    @GetMapping("/{id}")
    public ResponseEntity<MusteriResponse> idIleGetir(
            @PathVariable Long id) {

        return ResponseEntity.ok(musteriService.idIleGetir(id));
    }

}
