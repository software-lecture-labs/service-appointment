package com.dgs.servis.controller;

import com.dgs.servis.dto.AracOlusturRequest;
import com.dgs.servis.dto.AracResponse;
import com.dgs.servis.service.AracService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(arac);
    }
}
