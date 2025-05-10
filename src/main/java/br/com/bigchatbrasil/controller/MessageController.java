package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.dto.MessageDTO;
import br.com.bigchatbrasil.model.Client;
import br.com.bigchatbrasil.model.Message;
import br.com.bigchatbrasil.repository.ClientRepository;
import br.com.bigchatbrasil.repository.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**/

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final ClientRepository clientRepository;

    public MessageController(MessageRepository messageRepository, ClientRepository clientRepository) {
        this.messageRepository = messageRepository;
        this.clientRepository = clientRepository;
    }

    @PostMapping
    public ResponseEntity<Message> createMessage(@RequestBody MessageDTO dto) {
        Optional<Client> clientOptional = clientRepository.findById(dto.getClientId());
        if (clientOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Optional<Client> recipientOptional = clientRepository.findById(dto.getRecipientId());
        if (recipientOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Message message = new Message();
        message.setClient(clientOptional.get());
        message.setRecipient(recipientOptional.get());
        message.setText(dto.getText());
        message.setType(dto.getType());
        message.setPriority(dto.getPriority());
        message.setCost(dto.getCost());
        message.setStatus(dto.getStatus());
        message.setTimestamp(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMessage);
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<List<Message>> getMessagesByClientId(@PathVariable Long clientId) {
        List<Message> messages = messageRepository.findByClientId(clientId);
        if (messages.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(messages);
    }
}