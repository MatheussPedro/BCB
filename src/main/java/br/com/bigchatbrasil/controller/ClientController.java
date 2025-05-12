package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.Services.ClientService;
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

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/me")
    public ResponseEntity<Object> getAuthenticatedClient() {
        Client client = ClientContextHolder.get();
        if (client == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado. Por favor, cadastre um.");
        }
        return ResponseEntity.ok(client);
    }

    @PostMapping
    public ResponseEntity<Object> createClient(@RequestBody Client client) {
        Optional<String> validationError = clientService.validateClient(client);
        if (validationError.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError.get());
        }

        Client savedClient = clientService.createClient(client);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cliente criado com sucesso. ID: " + savedClient.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getClientById(@PathVariable Long id) {
        return clientService.getClientById(id)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente com ID " + id + " não encontrado."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateClient(@PathVariable Long id, @RequestBody Client updatedClient) {
        Optional<String> result = clientService.updateClient(id, updatedClient);
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente com ID " + id + " não encontrado.");
        }
        String response = result.get();
        if (response.startsWith("Plano inválido")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<Object> getClientBalance(@PathVariable Long id) {
        return clientService.getClientById(id)
                .map(client -> ResponseEntity.ok((Object) clientService.getClientBalanceInfo(client)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Cliente com ID " + id + " não encontrado."));
    }

    @GetMapping
    public ResponseEntity<Object> getAllClients() {
        List<Client> clients = clientService.getAllClients();
        if (clients.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum cliente encontrado.");
        }
        return ResponseEntity.ok(clients);
    }
}