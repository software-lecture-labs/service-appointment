package com.dgs.servis.controller;

import com.dgs.servis.dto.RandevuOlusturRequest;
import com.dgs.servis.dto.RandevuResponse;
import com.dgs.servis.service.RandevuService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;

@RestController
@RequestMapping("/api/randevular")
public class RandevuController {

    private final RandevuService randevuService;

    public RandevuController(RandevuService randevuService) {
        this.randevuService = randevuService;
    }

    @PostMapping
    public ResponseEntity<RandevuResponse> olustur(
            @Valid @RequestBody RandevuOlusturRequest request) {
        RandevuResponse randevu = randevuService.olustur(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(randevu);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RandevuResponse> idIleGetir(@PathVariable("id") Long id) {
        return ResponseEntity.ok(randevuService.idIleGetir(id));
    }

    @GetMapping
    public PagedModel<RandevuResponse> tumunuListele(
            @PageableDefault(size = 20, sort = { "randevuZamani", "id" }) Pageable pageable) {
        return new PagedModel<>(randevuService.tumunuListele(pageable));
    }

    @PatchMapping("/{id}/iptal")
    public ResponseEntity<RandevuResponse> iptalEt(@PathVariable("id") Long id) {
        return ResponseEntity.ok(randevuService.iptalEt(id));
    }
}
