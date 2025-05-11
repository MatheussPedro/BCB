package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.Services.ClientService;
import br.com.bigchatbrasil.model.*;
import br.com.bigchatbrasil.repository.*;
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

    @Autowired
    private TransactionRepository transactionRepository;

    @PostMapping("/fila")
    public ResponseEntity<?> adicionarNaFila(@RequestBody FilaMensagem filaMensagem) {
        if (filaMensagem.getClient() == null || filaMensagem.getClient().getId() == null) {
            return ResponseEntity.badRequest().body("Cliente não informado.");
        }

        filaMensagem.setTimestamp(LocalDateTime.now());
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

            Transaction transaction = new Transaction();
            transaction.setClient(client);
            transaction.setAmount(custo);
            transaction.setTimestamp(LocalDateTime.now());
            transaction.setDescription("Envio de mensagem - " + f.getPriority() + " custo");
            transaction.setType("debit");

            transactionRepository.save(transaction);


            log.append("Mensagem processada: ").append(f.getText()).append("\n");
            filaMensagemRepository.delete(f);
        }

        return ResponseEntity.ok(log.toString());
    }

    @GetMapping("/historico/{clientId}")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable Long clientId) {
        List<Transaction> transactions = transactionRepository.findByClientId(clientId);
        return ResponseEntity.ok(transactions);
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