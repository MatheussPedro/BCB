package br.com.bigchatbrasil.repository;

import br.com.bigchatbrasil.model.Conversa;
import br.com.bigchatbrasil.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConversaRepository extends JpaRepository<Conversa, Long> {

    List<Conversa> findByClient(Client client);

    Optional<Conversa> findByClientAndRecipient(Client client, Client recipient);

    Optional<Conversa> findByClientIdAndRecipientId(Long clientId, Long recipientId);
}
