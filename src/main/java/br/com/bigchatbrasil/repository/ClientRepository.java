package br.com.bigchatbrasil.repository;

import br.com.bigchatbrasil.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByDocumentId(String documentId);
}
