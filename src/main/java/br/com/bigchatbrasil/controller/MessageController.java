package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.Services.ClientService;
import br.com.bigchatbrasil.model.Client;
import br.com.bigchatbrasil.model.Conversa;
import br.com.bigchatbrasil.model.FilaMensagem;
import br.com.bigchatbrasil.model.Message;
import br.com.bigchatbrasil.repository.ClientRepository;
import br.com.bigchatbrasil.repository.ConversaRepository;
import br.com.bigchatbrasil.repository.FilaMensagemRepository;
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
    private FilaMensagemRepository filaMensagemRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversaRepository conversaRepository;

    @PostMapping("/fila")
    public ResponseEntity<?> adicionarNaFila(@RequestBody FilaMensagem filaMensagem) {
        if (filaMensagem.getClient() == null || filaMensagem.getClient().getId() == null) {
            return ResponseEntity.badRequest().body("Cliente não informado.");
        }

        filaMensagem.setTimestamp(LocalDateTime.now()); // se tiver o campo
        filaMensagemRepository.save(filaMensagem);

        return ResponseEntity.ok("Mensagem adicionada à fila com sucesso.");
    }

    @GetMapping("/fila/processar")
    public ResponseEntity<?> processarFila() {
        List<FilaMensagem> fila = filaMensagemRepository.findPrioritized();

        if (fila.isEmpty()) return ResponseEntity.ok("Nenhuma mensagem para processar.");

        StringBuilder log = new StringBuilder("Processando mensagens:\n");

        for (FilaMensagem f : fila) {
            Client client = f.getClient();
            double custo = "urgent".equalsIgnoreCase(f.getPriority()) ? 0.50 : 0.25;

            if ("prepaid".equalsIgnoreCase(client.getPlanType())) {
                if (client.getBalance() == null || client.getBalance() < custo) continue;
                client.setBalance(client.getBalance() - custo);

            } else if ("postpaid".equalsIgnoreCase(client.getPlanType())) {
                if (client.getLimit() == null || client.getLimit() < custo) continue;
                client.setLimit(client.getLimit() - custo);
            } else {
                continue;
            }

            clientService.updateClient(client.getId(), client);

            Conversa conversa = conversaRepository.findByClientAndRecipient(client, f.getRecipient())
                    .orElseGet(() -> {
                        Conversa nova = new Conversa();
                        nova.setClient(client);
                        nova.setRecipient(f.getRecipient());
                        nova.setRecipientName(f.getRecipient().getName());
                        nova.setLastMessageContent(f.getText());
                        nova.setLastMessageTime(LocalDateTime.now());
                        nova.setUnreadCount(1);
                        return conversaRepository.save(nova);
                    });

            Message msg = new Message();
            msg.setClient(client);
            msg.setRecipient(f.getRecipient());
            msg.setText(f.getText());
            msg.setPriority(f.getPriority());
            msg.setType(f.getType());
            msg.setTimestamp(LocalDateTime.now());
            msg.setCost(custo);
            msg.setStatus("sent");
            msg.setConversation(conversa);

            messageRepository.save(msg);

            conversa.setLastMessageContent(f.getText());
            conversa.setLastMessageTime(msg.getTimestamp());
            conversa.setUnreadCount(conversa.getUnreadCount() + 1);
            conversaRepository.save(conversa);

            filaMensagemRepository.delete(f);

            log.append("Mensagem processada: ").append(msg.getText()).append("\n");
        }

        return ResponseEntity.ok(log.toString());
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