package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.model.Message;
import br.com.bigchatbrasil.model.Conversa;
import br.com.bigchatbrasil.repository.MessageRepository;
import br.com.bigchatbrasil.repository.ConversaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversaRepository conversaRepository;

    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {

        Optional<Conversa> conversaOpt = conversaRepository
                .findByClientIdAndRecipientId(
                        message.getClient().getId(),
                        message.getRecipient().getId()
                );

        Conversa conversa;
        if (conversaOpt.isPresent()) {
            conversa = conversaOpt.get();
            conversa.setLastMessageContent(message.getText());
            conversa.setLastMessageTime(message.getTimestamp());
            conversa.setUnreadCount(conversa.getUnreadCount() + 1);
        } else {
            conversa = new Conversa();
            conversa.setClient(message.getClient());
            conversa.setRecipient(message.getRecipient());
            conversa.setRecipientName(message.getRecipient().getName());
            conversa.setLastMessageContent(message.getText());
            conversa.setLastMessageTime(message.getTimestamp());
            conversa.setUnreadCount(1);
        }

        conversaRepository.save(conversa);
        message.setConversation(conversa);

        Message savedMessage = messageRepository.save(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMessage);
    }

    private Conversa criarNovaConversa(Message message) {
        Conversa nova = new Conversa();
        nova.setClient(message.getClient());
        nova.setRecipient(message.getRecipient());
        nova.setRecipientName(message.getRecipient().getName());
        nova.setLastMessageContent(message.getText());
        nova.setLastMessageTime(message.getTimestamp());
        nova.setUnreadCount(1);
        return nova;
        }



    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        Optional<Message> message = messageRepository.findById(id);
        return message.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Message>> getMessages(@RequestParam Long conversationId) {
        return ResponseEntity.ok(messageRepository.findByConversation_Id(conversationId));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<String> getMessageStatus(@PathVariable Long id) {
        Optional<Message> message = messageRepository.findById(id);
        return message.map(m -> ResponseEntity.ok(m.getStatus())).orElse(ResponseEntity.notFound().build());
    }
}
