package br.com.bigchatbrasil.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 255)
    private String name;

    @Column(name = "tipo_pessoa", nullable = false, length = 2)
    private String documentType;

    @Column(name = "documento", nullable = false, length = 20, unique = true)
    private String documentId;

    @Column(name = "plano", nullable = false, length = 10)
    private String planType;

    @Column(name = "status", nullable = false)
    private Boolean active;

    @Column(name = "saldo", nullable = false)
    private Double saldo;

    @Column(name = "limite_dinheiro")
    private Double limiteDinheiro;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Double getLimiteDinheiro() {
        return limiteDinheiro;
    }

    public void setLimiteDinheiro(Double limiteDinheiro) {
        this.limiteDinheiro = limiteDinheiro;
    }

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }
}