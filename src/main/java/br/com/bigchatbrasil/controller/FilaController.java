package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.Services.ClientService;
import br.com.bigchatbrasil.Services.MessageService;
import br.com.bigchatbrasil.model.*;
import br.com.bigchatbrasil.repository.ConversaRepository;
import br.com.bigchatbrasil.repository.FilaMensagemRepository;
import br.com.bigchatbrasil.repository.TransactionRepository;
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
    private FilaMensagemRepository filaMensagemRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ConversaRepository conversaRepository;

    @Autowired
    private br.com.bigchatbrasil.repository.MessageRepository messageRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/processar")
    public ResponseEntity<?> processarFila() {

        List<FilaMensagem> fila = filaMensagemRepository.findPrioritized();
        StringBuilder log = new StringBuilder("Processando mensagens:\n");

        if (fila.isEmpty()) {
            log.append("Nenhuma mensagem para processar.\n");
            return ResponseEntity.ok(log.toString());
        }

        for (FilaMensagem f : fila) {
            Client client = f.getClient();
            double custo = "urgent".equalsIgnoreCase(f.getPriority()) ? 0.50 : 0.25;

            log.append("Processando mensagem para cliente: ").append(client.getId()).append("\n");
            log.append("Prioridade: ").append(f.getPriority()).append(", Custo: ").append(custo).append("\n");

            if ("prepaid".equalsIgnoreCase(client.getPlanType())) {
                if (client.getBalance() == null || client.getBalance() < custo) {
                    log.append("Cliente com plano pré-pago não tem saldo suficiente. Ignorando mensagem.\n");
                    continue;
                }
                client.setBalance(client.getBalance() - custo);
                log.append("Saldo atualizado para cliente com plano pré-pago: ").append(client.getBalance()).append("\n");
            } else if ("postpaid".equalsIgnoreCase(client.getPlanType())) {
                if (client.getLimit() == null || client.getLimit() < custo) {
                    log.append("Cliente com plano pós-pago não tem limite suficiente. Ignorando mensagem.\n");
                    continue;
                }
                client.setLimit(client.getLimit() - custo);
                log.append("Limite atualizado para cliente com plano pós-pago: ").append(client.getLimit()).append("\n");
            } else {
                log.append("Plano do cliente não reconhecido. Ignorando mensagem.\n");
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

            log.append("Conversa encontrada/criada: ").append(conversa.getId()).append("\n");

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

            log.append("Mensagem criada e salva: ").append(msg.getText()).append("\n");

            conversa.setLastMessageContent(f.getText());
            conversa.setLastMessageTime(msg.getTimestamp());
            conversa.setUnreadCount(conversa.getUnreadCount() + 1);
            conversaRepository.save(conversa);

            log.append("Conversa atualizada: Última mensagem: ").append(conversa.getLastMessageContent()).append("\n");

            Transaction transaction = new Transaction();
            transaction.setClient(client);
            transaction.setAmount(custo);
            transaction.setTimestamp(LocalDateTime.now());
            transaction.setDescription("Envio de mensagem - " + f.getPriority() + " custo");
            transaction.setType("debit");

            if ("prepaid".equalsIgnoreCase(client.getPlanType())) {
                transaction.setBalanceAfterTransaction(client.getBalance());
            } else if ("postpaid".equalsIgnoreCase(client.getPlanType())) {
                transaction.setLimitAfterTransaction(client.getLimit());
            }

            transactionRepository.save(transaction);

            log.append("Transação registrada: ID do cliente: ").append(client.getId())
                    .append(", Tipo: ").append(transaction.getType())
                    .append(", Valor: ").append(transaction.getAmount()).append("\n");

            filaMensagemRepository.delete(f);
            log.append("Mensagem removida da fila: ").append(f.getText()).append("\n");
        }

        return ResponseEntity.ok(log.toString());
    }
}