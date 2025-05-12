package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.Services.ClientService;
import br.com.bigchatbrasil.Services.MessageService;
import br.com.bigchatbrasil.model.*;
import br.com.bigchatbrasil.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/fila")
    public ResponseEntity<?> adicionarNaFila(@RequestBody FilaMensagem filaMensagem) {
        return messageService.adicionarNaFila(filaMensagem);
    }

    @GetMapping("/fila/processar")
    public ResponseEntity<?> processarFila() {
        return messageService.processarFila();
    }

    @GetMapping("/historico/{clientId}")
    public ResponseEntity<?> getTransactionHistory(@PathVariable Long clientId) {
        List<Transaction> transactions = messageService.getTransactionHistory(clientId);
        if (transactions.isEmpty()) {
            return ResponseEntity.ok("Nenhuma transação encontrada para o cliente.");
        }
        return ResponseEntity.ok(transactions);
    }

    @GetMapping
    public ResponseEntity<List<Message>> getMessages(@RequestParam Long conversationId) {
        List<Message> messages = messageService.getMessagesByConversationId(conversationId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<String> getMessageStatus(@PathVariable Long id) {
        Optional<Message> message = messageService.getMessageById(id);
        return message.map(m -> ResponseEntity.ok(m.getStatus()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
