package br.com.bigchatbrasil.controller;

import br.com.bigchatbrasil.Services.MessageService;
import br.com.bigchatbrasil.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fila")
public class FilaController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/processar")
    public ResponseEntity<?> processarFila() {
        List<Message> mensagens = messageService.getNextMessagesToProcess();

        if (mensagens.isEmpty()) return ResponseEntity.ok("Nenhuma mensagem para processar.");

        StringBuilder log = new StringBuilder("Processando mensagens:\n");
        for (Message msg : mensagens) {
            msg.setStatus("sent");
            log.append("Mensagem ID: ").append(msg.getId())
                    .append(" | Prioridade: ").append(msg.getPriority())
                    .append(" | Texto: ").append(msg.getText()).append("\n");
        }

        return ResponseEntity.ok(log.toString());
    }
}
