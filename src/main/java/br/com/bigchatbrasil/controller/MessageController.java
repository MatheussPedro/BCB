package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.Services.ClientService;
import br.com.bigchatbrasil.Services.MessageService;
import br.com.bigchatbrasil.model.Client;
import br.com.bigchatbrasil.model.Conversa;
import br.com.bigchatbrasil.model.Message;
import br.com.bigchatbrasil.repository.ConversaRepository;
import br.com.bigchatbrasil.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversaRepository conversaRepository;

    @Autowired
    private ClientService clientService;

    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody Message message) {
        Client clientInput = message.getClient();
        if (clientInput == null || clientInput.getId() == null) {
            return ResponseEntity.badRequest().body("Cliente não informado ou ID ausente.");
        }

        Optional<Client> optionalClient = clientService.getClientById(clientInput.getId());
        if (optionalClient.isEmpty()) {
            return ResponseEntity.badRequest().body("Cliente não encontrado.");
        }
        Client client = optionalClient.get();

        if (clientInput.getPlanType() != null && !clientInput.getPlanType().equals(client.getPlanType())) {
            client.setPlanType(clientInput.getPlanType());
            clientService.updateClient(client.getId(), client);
        }

        System.out.println("Cliente encontrado: " + client.getPlanType());

        double custo = "urgent".equalsIgnoreCase(message.getPriority()) ? 0.50 : 0.25;

        if ("prepaid".equalsIgnoreCase(client.getPlanType())) {
            if (client.getBalance() == null || client.getBalance() < custo) {
                return ResponseEntity.badRequest().body("Saldo insuficiente ou não def");
            }
            client.setBalance(client.getBalance() - custo);

        } else if ("postpaid".equalsIgnoreCase(client.getPlanType())) {
            if (client.getLimit() == null || client.getLimit() < custo) {
                return ResponseEntity.badRequest().body("Limite insuficiente ou não definido.");
            }
            client.setLimit(client.getLimit() - custo);

        } else {
            System.out.println("Tipo de plano desconhecido: " + client.getPlanType());
            return ResponseEntity.badRequest().body("Tipo de plano desconhecido: " + client.getPlanType());
        }

        clientService.updateClient(client.getId(), client);

        Conversa conversa = conversaRepository.findByClientAndRecipient(client, message.getRecipient())
                .orElseGet(() -> {
                    Conversa nova = new Conversa();
                    nova.setClient(client);
                    nova.setRecipient(message.getRecipient());
                    nova.setRecipientName(message.getRecipient().getName());
                    nova.setLastMessageContent(message.getText());
                    nova.setLastMessageTime(LocalDateTime.now());
                    nova.setUnreadCount(1);
                    return conversaRepository.save(nova);
                });

        message.setTimestamp(LocalDateTime.now());
        message.setCost(custo);
        message.setStatus("queued");
        message.setConversation(conversa);

        conversa.setLastMessageContent(message.getText());
        conversa.setLastMessageTime(message.getTimestamp());
        conversa.setUnreadCount(conversa.getUnreadCount() + 1);
        conversaRepository.save(conversa);

        System.out.println("Mensagem salva: " + message.getText());

        return ResponseEntity.ok(messageRepository.save(message));
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

    @Autowired
    private MessageService messageService;

    @PostMapping("/process")
    public ResponseEntity<String> processMessages() {
        messageService.processMessages();
        return ResponseEntity.ok("Mensagens processadas com sucesso");
    }

}