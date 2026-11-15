
package com.dgs.servis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "musteriler")
@Getter
@Setter
@NoArgsConstructor
public class Musteri {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ad boş olamaz")
@Size(max = 100)
@Column(nullable = false, length = 100)
private String ad;

@NotBlank(message = "Soyad boş olamaz")
@Size(max = 100)
@Column(nullable = false, length = 100)
private String soyad;

@NotBlank(message = "Telefon boş olamaz")
@Size(max = 20)
@Column(nullable = false, length = 20)
private String telefon;

@Email(message = "Geçerli bir e-posta adresi giriniz")
@Size(max = 255)
@Column(length = 255)
private String email;
}
