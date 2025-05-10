package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.model.Client;
import br.com.bigchatbrasil.repository.ClientRepository;
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

    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        client.setSaldo(50.00);
        client.setLimiteDinheiro(50.00);
        Client saved = clientRepository.save(client);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(clientRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        Optional<Client> client = clientRepository.findById(id);
        return client.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @RequestBody Client updatedClient) {
        return clientRepository.findById(id)
                .map(client -> {
                    client.setName(updatedClient.getName());
                    client.setDocumentId(updatedClient.getDocumentId());
                    client.setDocumentType(updatedClient.getDocumentType());
                    client.setPlanType(updatedClient.getPlanType());
                    client.setActive(updatedClient.getActive());
                    return ResponseEntity.ok(clientRepository.save(client));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<String> getClientBalance(@PathVariable Long id) {
        return clientRepository.findById(id)
                .map(client -> {
                    if ("pre".equalsIgnoreCase(client.getPlanType())) {
                        return ResponseEntity.ok("Saldo: R$ " + client.getSaldo());
                    } else {
                        return ResponseEntity.ok("Limite: R$ " + client.getLimiteDinheiro());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}