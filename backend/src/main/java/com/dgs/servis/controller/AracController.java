package com.dgs.servis.controller;

import com.dgs.servis.dto.AracOlusturRequest;
import com.dgs.servis.dto.AracResponse;
import com.dgs.servis.service.AracService;

import jakarta.validation.Valid;

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
import java.net.URI;
@RestController
@RequestMapping("/api/araclar")
public class AracController {

    private final AracService aracService;

    public AracController(AracService aracService) {
        this.aracService = aracService;
    }

    @PostMapping
    public ResponseEntity<AracResponse> olustur(
            @Valid @RequestBody AracOlusturRequest request) {
        AracResponse arac = aracService.olustur(request);
        URI konum = URI.create("/api/araclar/" + arac.id());
return ResponseEntity.created(konum).body(arac);
    }
    @GetMapping("/{id}")
public ResponseEntity<AracResponse> idIleGetir(@PathVariable("id") Long id) {
    return ResponseEntity.ok(aracService.idIleGetir(id));
}
@GetMapping
public PagedModel<AracResponse> tumunuListele(
        @PageableDefault(size = 20, sort = "id") Pageable pageable) {
    return new PagedModel<>(aracService.tumunuListele(pageable));
}
}
