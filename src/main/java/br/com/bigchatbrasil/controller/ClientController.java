package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.model.Client;
import br.com.bigchatbrasil.repository.ClientRepository;
import br.com.bigchatbrasil.security.ClientContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientRepository clientRepository;

    public ClientController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<Object> getAuthenticatedClient() {
        Client client = ClientContextHolder.get();
        if (client == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado. Por favor, faça login.");
        }
        return ResponseEntity.ok(client);
    }

    @PostMapping
    public ResponseEntity<Object> createClient(@RequestBody Client client) {
        if (client.getPlanType() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro: Tipo de plano não especificado.");
        }

        if ("prepaid".equals(client.getPlanType())) {
            client.setBalance(100.00);
            client.setLimit(0.00);
        } else if ("postpaid".equals(client.getPlanType())) {
            client.setBalance(0.00);
            client.setLimit(100.00);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro: Tipo de plano inválido.");
        }

        Client saved = clientRepository.save(client);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cliente criado com sucesso. ID: " + saved.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getClientById(@PathVariable Long id) {
        Optional<Client> client = clientRepository.findById(id);
        if (client.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente com ID " + id + " não encontrado.");
        }
        return ResponseEntity.ok(client.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateClient(@PathVariable Long id, @RequestBody Client updatedClient) {
        Optional<Client> existingClient = clientRepository.findById(id);

        if (existingClient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente com ID " + id + " não encontrado para atualização.");
        }

        Client client = existingClient.get();
        client.setName(updatedClient.getName());
        client.setDocumentId(updatedClient.getDocumentId());
        client.setDocumentType(updatedClient.getDocumentType());
        client.setPlanType(updatedClient.getPlanType());
        client.setActive(updatedClient.getActive());

        if ("prepaid".equals(client.getPlanType())) {
            client.setBalance(100.00);
            client.setLimit(0.00);
        } else if ("postpaid".equals(client.getPlanType())) {
            client.setBalance(0.00);
            client.setLimit(100.00);
        }

        clientRepository.save(client);
        return ResponseEntity.ok("Cliente atualizado com sucesso. ID: " + client.getId());
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<Object> getClientBalance(@PathVariable Long id) {
        Optional<Client> client = clientRepository.findById(id);
        if (client.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente com ID " + id + " não encontrado.");
        }

        Client foundClient = client.get();
        String message = "Cliente encontrado. ";

        if ("prepaid".equals(foundClient.getPlanType())) {
            message += "Saldo: R$ " + foundClient.getBalance();
        } else if ("postpaid".equals(foundClient.getPlanType())) {
            message += "Limite: R$ " + foundClient.getLimit();
        }

        return ResponseEntity.ok(message);
    }

    @GetMapping
    public ResponseEntity<Object> getAllClients() {
        List<Client> clients = clientRepository.findAll();
        if (clients.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum cliente encontrado.");
        }
        return ResponseEntity.ok(clients);
    }
}