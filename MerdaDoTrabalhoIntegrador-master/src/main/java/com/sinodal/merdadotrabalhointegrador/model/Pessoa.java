package com.sinodal.merdadotrabalhointegrador.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// HERANÇA: classe base abstrata; Usuario (e suas filhas) herdam id, nome e email
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 150)
    private String email;

    protected Pessoa(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    // POLIMORFISMO: cada subclasse concreta decide como se descrever
    public abstract String descreverPerfil();
}
