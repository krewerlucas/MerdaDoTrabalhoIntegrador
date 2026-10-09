package com.sinodal.merdadotrabalhointegrador.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "registros_acesso")
@Getter
@Setter
@NoArgsConstructor
public class RegistroAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ASSOCIAÇÃO: cada registro aponta para o usuário que tentou entrar
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private boolean sucesso;

    public RegistroAcesso(Usuario usuario, boolean sucesso) {
        this.usuario = usuario;
        this.sucesso = sucesso;
        this.dataHora = LocalDateTime.now();
    }
}
