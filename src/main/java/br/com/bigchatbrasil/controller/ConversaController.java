package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.Services.MessageService;
import br.com.bigchatbrasil.Services.ConversaService;
import br.com.bigchatbrasil.model.Conversa;
import br.com.bigchatbrasil.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/conversations")
public class ConversaController {

    @Autowired
    private ConversaService conversaService;

    @Autowired
    private MessageService messageService;

    @GetMapping
    public ResponseEntity<?> getConversations(@RequestParam Long clientId) {
        List<Conversa> conversations = conversaService.getConversationsByClientId(clientId);

        if (conversations.isEmpty()) {
            String message = "Nenhuma conversa encontrada para o cliente com ID " + clientId + ".";
            System.out.println(message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getConversaById(@PathVariable Long id) {
        Optional<Conversa> conversa = conversaService.getConversaById(id);

        if (!conversa.isPresent()) {
            String message = "Conversa com ID " + id + " não encontrada.";
            System.out.println(message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        return ResponseEntity.ok(conversa.get());
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<?> getMessagesByConversation(@PathVariable Long id) {
        List<Message> messages = messageService.getMessagesByConversationId(id);

        if (messages.isEmpty()) {
            String message = "Nenhuma mensagem encontrada para a conversa com ID " + id + ".";
            System.out.println(message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        return ResponseEntity.ok(messages);
    }
}