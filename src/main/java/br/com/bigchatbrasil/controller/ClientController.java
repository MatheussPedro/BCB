package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.model.Client;
import br.com.bigchatbrasil.repository.ClientRepository;
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

    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        System.out.println("Recebendo dados do cliente: " + client);
        if ("prepaid".equals(client.getPlanType())) {
            client.setBalance(100.00);
            client.setLimit(0.00);
        } else if ("postpaid".equals(client.getPlanType())) {
            client.setBalance(0.00);
            client.setLimit(100.00);
        }
        Client saved = clientRepository.save(client);
        System.out.println("Cliente salvo: " + saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
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
        Optional<Client> client = clientRepository.findById(id);
        if (client.isPresent()) {
            Client foundClient = client.get();
            if ("prepaid".equals(foundClient.getPlanType())) {
                return ResponseEntity.ok("Saldo: R$ " + foundClient.getBalance());
            } else {
                return ResponseEntity.ok("Limite: R$ " + foundClient.getLimit());
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        List<Client> clients = clientRepository.findAll();
        return ResponseEntity.ok(clients);
    }
}