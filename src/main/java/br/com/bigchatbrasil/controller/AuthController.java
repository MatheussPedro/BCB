package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.model.Client;
import br.com.bigchatbrasil.repository.ClientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ClientRepository clientRepository;

    public AuthController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @PostMapping
    public ResponseEntity<Object> authenticate(@RequestBody AuthRequest request) {
        return clientRepository.findByDocumentId(request.getDocumentId())
                .map(client -> ResponseEntity.ok((Object) client))
                .orElseGet(() -> ResponseEntity.status(401).body("Cliente não encontrado"));
    }

    public static class AuthRequest {
        private String documentId;

        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
    }
}