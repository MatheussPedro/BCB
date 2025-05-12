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

    public Optional<String> validateClient(Client client) {
        String docType = client.getDocumentType();
        String docId = client.getDocumentId();

        if (!"CPF".equalsIgnoreCase(docType) && !"CNPJ".equalsIgnoreCase(docType)) {
            return Optional.of("Erro: Tipo de documento inválido. Use 'CPF' ou 'CNPJ'.");
        }

        if ("CPF".equalsIgnoreCase(docType) && docId.length() != 11) {
            return Optional.of("Erro: CPF deve conter exatamente 11 dígitos.");
        } else if ("CNPJ".equalsIgnoreCase(docType) && docId.length() != 14) {
            return Optional.of("Erro: CNPJ deve conter exatamente 14 dígitos.");
        }

        if (clientRepository.findByDocumentId(docId).isPresent()) {
            return Optional.of("Erro: Já existe um cliente com esse número de documento.");
        }

        String planType = client.getPlanType();
        if (!"prepaid".equalsIgnoreCase(planType) && !"postpaid".equalsIgnoreCase(planType)) {
            return Optional.of("Erro: Tipo de plano inválido. Use 'prepaid' ou 'postpaid'.");
        }

        return Optional.empty();
    }

    public Client configureInitialPlanValues(Client client) {
        if ("prepaid".equalsIgnoreCase(client.getPlanType())) {
            client.setBalance(100.00);
            client.setLimit(0.00);
        } else {
            client.setBalance(0.00);
            client.setLimit(100.00);
        }
        return client;
    }

    public Client createClient(Client client) {
        return clientRepository.save(configureInitialPlanValues(client));
    }

    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public String getClientBalanceInfo(Client client) {
        if ("prepaid".equals(client.getPlanType())) {
            return "Cliente encontrado. Saldo: R$ " + client.getBalance();
        } else if ("postpaid".equals(client.getPlanType())) {
            return "Cliente encontrado. Limite: R$ " + client.getLimit();
        }
        return "Plano inválido.";
    }

    public Optional<String> updateClient(Long id, Client updatedClient) {
        return clientRepository.findById(id).map(existingClient -> {
            existingClient.setName(updatedClient.getName());
            existingClient.setDocumentId(updatedClient.getDocumentId());
            existingClient.setDocumentType(updatedClient.getDocumentType());
            existingClient.setPlanType(updatedClient.getPlanType());
            existingClient.setActive(updatedClient.getActive());

            if ("prepaid".equals(updatedClient.getPlanType())) {
                existingClient.setBalance(updatedClient.getBalance() == null ? 100.0 : updatedClient.getBalance());
                existingClient.setLimit(0.0);
            } else if ("postpaid".equals(updatedClient.getPlanType())) {
                existingClient.setBalance(0.0);
                existingClient.setLimit(updatedClient.getLimit() == null ? 100.0 : updatedClient.getLimit());
            } else {
                return "Plano inválido.";
            }

            clientRepository.save(existingClient);
            return "Cliente atualizado com sucesso. ID: " + existingClient.getId();
        });
    }
}
