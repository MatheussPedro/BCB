package br.com.bigchatbrasil.Services;

import br.com.bigchatbrasil.model.Conversa;
import br.com.bigchatbrasil.model.Message;
import br.com.bigchatbrasil.repository.ConversaRepository;
import br.com.bigchatbrasil.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Message sendMessage(Message message) {
        Conversa conversa = conversaRepository.findByClientAndRecipient(message.getClient(), message.getRecipient())
                .orElseGet(() -> {
                    Conversa newConversa = new Conversa();
                    newConversa.setClient(message.getClient());
                    newConversa.setRecipient(message.getRecipient());
                    newConversa.setRecipientName(message.getRecipient().getName());
                    newConversa.setLastMessageContent(message.getText());
                    newConversa.setLastMessageTime(LocalDateTime.now());
                    newConversa.setUnreadCount(1);
                    return conversaRepository.save(newConversa);
                });

        message.setTimestamp(LocalDateTime.now());
        if ("urgent".equalsIgnoreCase(message.getPriority())) {
            message.setCost(0.50);
        } else {
            message.setCost(0.25);
        }
        message.setStatus("queued");

        conversa.setLastMessageContent(message.getText());
        conversa.setLastMessageTime(message.getTimestamp());
        conversa.setUnreadCount(conversa.getUnreadCount() + 1);
        conversaRepository.save(conversa);

        return messageRepository.save(message);
    }

    public List<Message> getMessagesByConversationId(Long conversationId) {
        return messageRepository.findByConversation_Id(conversationId);
    }

    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }
}
