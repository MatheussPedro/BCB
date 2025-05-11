package br.com.bigchatbrasil.Services;

import br.com.bigchatbrasil.model.Conversa;
import br.com.bigchatbrasil.model.Client;
import br.com.bigchatbrasil.repository.ConversaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConversaService {

    @Autowired
    private ConversaRepository conversaRepository;

    public List<Conversa> getConversationsByClientId(Long clientId) {
        Client client = new Client();
        client.setId(clientId);

        return conversaRepository.findByClient(client);
    }

    public Optional<Conversa> getConversaById(Long id) {
        return conversaRepository.findById(id);
    }
}