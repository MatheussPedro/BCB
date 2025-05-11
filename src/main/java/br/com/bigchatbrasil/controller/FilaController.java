package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.Services.ClientService;
import br.com.bigchatbrasil.Services.MessageService;
import br.com.bigchatbrasil.model.Client;
import br.com.bigchatbrasil.model.Conversa;
import br.com.bigchatbrasil.model.FilaMensagem;
import br.com.bigchatbrasil.model.Message;
import br.com.bigchatbrasil.repository.ConversaRepository;
import br.com.bigchatbrasil.repository.FilaMensagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/fila")
public class FilaController {

    @Autowired
    private MessageService messageService;

    @Autowired
    FilaMensagemRepository filaMensagemRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    ConversaRepository conversaRepository;

    @Autowired
    private br.com.bigchatbrasil.repository.MessageRepository messageRepository;


    @GetMapping("/processar")
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
}
