package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.Services.MessageService;
import br.com.bigchatbrasil.Services.ConversaService;
import br.com.bigchatbrasil.model.Conversa;
import br.com.bigchatbrasil.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/conversations")
public class ConversaController {

    @Autowired
    private ConversaService conversaService;

    @Autowired
    private MessageService messageService;

    @GetMapping
    public List<Conversa> getConversations(@RequestParam Long clientId) {
        return conversaService.getConversationsByClientId(clientId);
    }

    @GetMapping("/{id}")
    public Optional<Conversa> getConversaById(@PathVariable Long id) {
        return conversaService.getConversaById(id);
    }

    @GetMapping("/{id}/messages")
    public List<Message> getMessagesByConversation(@PathVariable Long id) {
        return messageService.getMessagesByConversationId(id);
    }
}