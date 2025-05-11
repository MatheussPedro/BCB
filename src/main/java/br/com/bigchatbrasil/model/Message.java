package br.com.bigchatbrasil.model;
import br.com.bigchatbrasil.model.Conversa;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensagens")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private Client recipient;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversa conversation;

    @Column(name = "texto", nullable = false)
    private String text;

    @Column(name = "tipo", nullable = false)
    private String type;

    @Column(name = "prioridade", nullable = false)
    private String priority;

    @Column(name = "custo", nullable = false)
    private Double cost;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime timestamp;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getRecipient() {
        return recipient;
    }

    public void setRecipient(Client recipient) {
        this.recipient = recipient;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Conversa getConversation() {
        return conversation;
    }

    public void setConversation(Conversa conversation) {
        this.conversation = conversation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}