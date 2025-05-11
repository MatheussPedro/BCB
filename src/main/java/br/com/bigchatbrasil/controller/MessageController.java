package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.Services.ClientService;
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
    private FilaMensagemRepository filaMensagemRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversaRepository conversaRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ClientRepository clientRepository;

    @PostMapping("/fila")
    public ResponseEntity<?> adicionarNaFila(@RequestBody FilaMensagem filaMensagem) {
        if (filaMensagem.getClient() == null || filaMensagem.getClient().getId() == null) {
            return ResponseEntity.badRequest().body("Cliente não informado.");
        }

        Optional<Client> existingClient = clientRepository.findById(filaMensagem.getClient().getId());
        if (!existingClient.isPresent()) {
            String errorMessage = "Cliente com ID " + filaMensagem.getClient().getId() + " não encontrado.";
            System.out.println(errorMessage);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }

        filaMensagem.setTimestamp(LocalDateTime.now());
        filaMensagemRepository.save(filaMensagem);

        String successMessage = "Mensagem adicionada à fila com sucesso.";
        System.out.println(successMessage);
        return ResponseEntity.ok(successMessage);
    }

    @GetMapping("/fila/processar")
    public ResponseEntity<?> processarFila() {
        List<FilaMensagem> fila = filaMensagemRepository.findPrioritized();

        if (fila.isEmpty()) {
            String emptyMessage = "Nenhuma mensagem para processar.";
            System.out.println(emptyMessage);
            return ResponseEntity.ok(emptyMessage);
        }

        StringBuilder log = new StringBuilder("Processando mensagens:\n");

        for (FilaMensagem f : fila) {
            Client client = f.getClient();
            double custo = "urgent".equalsIgnoreCase(f.getPriority()) ? 0.50 : 0.25;

            if ("prepaid".equalsIgnoreCase(client.getPlanType())) {
                if (client.getBalance() == null || client.getBalance() < custo) {
                    String insufficientBalance = "Cliente com saldo insuficiente para processar a mensagem.";
                    System.out.println(insufficientBalance);
                    continue;
                }
                client.setBalance(client.getBalance() - custo);
            } else if ("postpaid".equalsIgnoreCase(client.getPlanType())) {
                if (client.getLimit() == null || client.getLimit() < custo) {
                    String insufficientLimit = "Cliente com limite insuficiente para processar a mensagem.";
                    System.out.println(insufficientLimit);
                    continue;
                }
                client.setLimit(client.getLimit() - custo);
            } else {
                String invalidPlan = "Plano do cliente não reconhecido. Ignorando mensagem.";
                System.out.println(invalidPlan);
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

        String processSuccess = "Mensagens processadas com sucesso.";
        System.out.println(processSuccess);
        return ResponseEntity.ok(log.toString());
    }

    @GetMapping("/historico/{clientId}")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable Long clientId) {
        List<Transaction> transactions = transactionRepository.findByClientId(clientId);

        if (transactions.isEmpty()) {
            String noTransactionsMessage = "Nenhuma transação encontrada para o cliente.";
            System.out.println(noTransactionsMessage);
            return ResponseEntity.ok(transactions);
        }

        System.out.println("Histórico de transações recuperado com sucesso para o cliente: " + clientId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping
    public ResponseEntity<List<Message>> getMessages(@RequestParam Long conversationId) {
        List<Message> messages = messageRepository.findByConversation_Id(conversationId);

        if (messages.isEmpty()) {
            String noMessagesMessage = "Nenhuma mensagem encontrada para a conversa.";
            System.out.println(noMessagesMessage);
            return ResponseEntity.ok(messages);
        }

        System.out.println("Mensagens recuperadas com sucesso para a conversa: " + conversationId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<String> getMessageStatus(@PathVariable Long id) {
        Optional<Message> message = messageRepository.findById(id);

        if (message.isPresent()) {
            String statusMessage = "Status da mensagem recuperado com sucesso.";
            System.out.println(statusMessage);
            return ResponseEntity.ok(message.get().getStatus());
        } else {
            String notFoundMessage = "Mensagem não encontrada.";
            System.out.println(notFoundMessage);
            return ResponseEntity.notFound().build();
        }
    }
}