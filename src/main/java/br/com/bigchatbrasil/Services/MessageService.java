package br.com.bigchatbrasil.Services;
import br.com.bigchatbrasil.model.FilaMensagem;
import br.com.bigchatbrasil.model.Message;
import br.com.bigchatbrasil.repository.*;

import br.com.bigchatbrasil.model.Transaction;
import  br.com.bigchatbrasil.model.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversaRepository conversaRepository;

    @Autowired
    private FilaMensagemRepository filaMensagemRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientService clientService;

    public ResponseEntity<?> adicionarNaFila(FilaMensagem filaMensagem) {
        if (filaMensagem.getClient() == null || filaMensagem.getClient().getId() == null) {
            return ResponseEntity.badRequest().body("Cliente (remetente) não informado.");
        }

        if (filaMensagem.getRecipient() == null || filaMensagem.getRecipient().getId() == null) {
            return ResponseEntity.badRequest().body("Destinatário não informado.");
        }

        if (filaMensagem.getClient().getId().equals(filaMensagem.getRecipient().getId())) {
            return ResponseEntity.badRequest().body("Remetente e destinatário não podem ser o mesmo cliente.");
        }

        Optional<Client> existingClient = clientRepository.findById(filaMensagem.getClient().getId());
        if (!existingClient.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Remetente com ID " + filaMensagem.getClient().getId() + " não encontrado.");
        }

        Optional<Client> existingRecipient = clientRepository.findById(filaMensagem.getRecipient().getId());
        if (!existingRecipient.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Destinatário com ID " + filaMensagem.getRecipient().getId() + " não encontrado.");
        }

        filaMensagem.setTimestamp(LocalDateTime.now());
        filaMensagemRepository.save(filaMensagem);
        return ResponseEntity.ok("Mensagem adicionada à fila com sucesso.");
    }


    public ResponseEntity<?> processarFila() {
        List<FilaMensagem> fila = filaMensagemRepository.findPrioritized();

        if (fila.isEmpty()) {
            return ResponseEntity.ok("Nenhuma mensagem para processar.");
        }

        StringBuilder log = new StringBuilder("Processando mensagens:\n");

        for (FilaMensagem f : fila) {
            Client client = f.getClient();
            double custo = "urgent".equalsIgnoreCase(f.getPriority()) ? 0.50 : 0.25;

            if ("prepaid".equalsIgnoreCase(client.getPlanType())) {
                if (client.getBalance() == null || client.getBalance() < custo) {
                    log.append("Saldo insuficiente para o cliente ID ").append(client.getId()).append("\n");
                    continue;
                }
                client.setBalance(client.getBalance() - custo);
            } else if ("postpaid".equalsIgnoreCase(client.getPlanType())) {
                if (client.getLimit() == null || client.getLimit() < custo) {
                    log.append("Limite insuficiente para o cliente ID ").append(client.getId()).append("\n");
                    continue;
                }
                client.setLimit(client.getLimit() - custo);
            } else {
                log.append("Plano inválido para o cliente ID ").append(client.getId()).append("\n");
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

            filaMensagemRepository.delete(f);
            log.append("Mensagem processada: ").append(f.getText()).append("\n");
        }

        return ResponseEntity.ok(log.toString());
    }

    public List<Transaction> getTransactionHistory(Long clientId) {
        return transactionRepository.findByClientId(clientId);
    }

    public List<Message> getMessagesByConversationId(Long conversationId) {
        return messageRepository.findByConversation_Id(conversationId);
    }

    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }

}

