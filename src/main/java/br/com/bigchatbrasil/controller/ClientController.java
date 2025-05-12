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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado. Por favor, cadastre um.");
        }
        return ResponseEntity.ok(client);
    }

    @PostMapping
    public ResponseEntity<Object> createClient(@RequestBody Client client) {
        String docType = client.getDocumentType();
        String docId = client.getDocumentId();

        if (!"CPF".equalsIgnoreCase(docType) && !"CNPJ".equalsIgnoreCase(docType)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro: Tipo de documento inválido. Use 'CPF' ou 'CNPJ'.");
        }

        if ("CPF".equalsIgnoreCase(docType) && docId.length() != 11) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro: CPF deve conter exatamente 11 dígitos.");
        } else if ("CNPJ".equalsIgnoreCase(docType) && docId.length() != 14) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro: CNPJ deve conter exatamente 14 dígitos.");
        }

        if (clientRepository.findByDocumentId(docId).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro: Já existe um cliente com esse número de documento.");
        }

        String planType = client.getPlanType();
        if (!"prepaid".equalsIgnoreCase(planType) && !"postpaid".equalsIgnoreCase(planType)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro: Tipo de plano inválido. Use 'prepaid' ou 'postpaid'.");
        }

        if ("prepaid".equalsIgnoreCase(planType)) {
            client.setBalance(100.00);
            client.setLimit(0.00);
        } else {
            client.setBalance(0.00);
            client.setLimit(100.00);
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

        if ("prepaid".equals(updatedClient.getPlanType()) && updatedClient.getBalance() == null) {
            client.setBalance(100.00);
            client.setLimit(0.00);
        } else if ("prepaid".equals(updatedClient.getPlanType())) {
            client.setBalance(updatedClient.getBalance());
            client.setLimit(0.00);
        } else if ("postpaid".equals(updatedClient.getPlanType()) && updatedClient.getLimit() == null) {
            client.setBalance(0.00);
            client.setLimit(100.00);
        } else if ("postpaid".equals(updatedClient.getPlanType())) {
            client.setBalance(0.00);
            client.setLimit(updatedClient.getLimit());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Plano inválido.");
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