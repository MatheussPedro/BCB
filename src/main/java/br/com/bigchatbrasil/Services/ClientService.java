package br.com.bigchatbrasil.Services;

import br.com.bigchatbrasil.model.Client;
import br.com.bigchatbrasil.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public Client createClient(Client client) {
        return clientRepository.save(client);
    }

    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    public Optional<Client> getClientByDocumentId(String documentId) {
        return clientRepository.findByDocumentId(documentId);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client checkClientBalance(Long id) {
        Client client = clientRepository.findById(id).orElse(null);

        if (client != null && client.getBalance() == null) {
            client.setBalance(0.0);
            clientRepository.save(client);
        }

        return client;
    }

    public Optional<Client> updateClient(Long id, Client updatedClient) {
        return clientRepository.findById(id).map(existingClient -> {
            existingClient.setName(updatedClient.getName());
            existingClient.setDocumentId(updatedClient.getDocumentId());
            existingClient.setDocumentType(updatedClient.getDocumentType());
            existingClient.setPlanType(updatedClient.getPlanType());
            existingClient.setBalance(updatedClient.getBalance());
            existingClient.setLimit(updatedClient.getLimit());
            existingClient.setActive(updatedClient.getActive());
            return clientRepository.save(existingClient);
        });
    }

    public boolean deleteClient(Long id) {
        if (clientRepository.existsById(id)) {
            clientRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
