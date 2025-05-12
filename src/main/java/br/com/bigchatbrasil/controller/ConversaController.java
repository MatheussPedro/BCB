package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.Services.ConversaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conversations")
public class ConversaController {

    @Autowired
    private ConversaService conversaService;

    @GetMapping
    public ResponseEntity<?> getConversations(@RequestParam Long clientId) {
        return conversaService.getConversationsByClientId(clientId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getConversaById(@PathVariable Long id) {
        return conversaService.getConversaById(id);
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<?> getMessagesByConversation(@PathVariable Long id) {
        return conversaService.getMessagesByConversationId(id);
    }
}
