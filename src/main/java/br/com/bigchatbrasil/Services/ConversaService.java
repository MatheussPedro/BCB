package br.com.bigchatbrasil.Services;

import br.com.bigchatbrasil.model.Conversa;
import br.com.bigchatbrasil.model.Client;
import br.com.bigchatbrasil.model.Message;
import br.com.bigchatbrasil.repository.ConversaRepository;
import br.com.bigchatbrasil.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConversaService {

    @Autowired
    private ConversaRepository conversaRepository;

    @Autowired
    private MessageRepository messageRepository;

    public ResponseEntity<?> getConversationsByClientId(Long clientId) {
        Client client = new Client();
        client.setId(clientId);

        List<Conversa> conversations = conversaRepository.findByClient(client);

        if (conversations.isEmpty()) {
            String message = "Nenhuma conversa encontrada para o cliente com ID " + clientId + ".";
            System.out.println(message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        return ResponseEntity.ok(conversations);
    }

    public ResponseEntity<?> getConversaById(Long id) {
        Optional<Conversa> conversa = conversaRepository.findById(id);

        if (!conversa.isPresent()) {
            String message = "Conversa com ID " + id + " não encontrada.";
            System.out.println(message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        return ResponseEntity.ok(conversa.get());
    }

    public ResponseEntity<?> getMessagesByConversationId(Long conversationId) {
        List<Message> messages = messageRepository.findByConversation_Id(conversationId);

        if (messages.isEmpty()) {
            String message = "Nenhuma mensagem encontrada para a conversa com ID " + conversationId + ".";
            System.out.println(message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }

        return ResponseEntity.ok(messages);
    }
}
