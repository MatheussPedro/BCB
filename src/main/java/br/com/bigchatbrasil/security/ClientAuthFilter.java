package br.com.bigchatbrasil.security;

import br.com.bigchatbrasil.model.Client;
import br.com.bigchatbrasil.repository.ClientRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ClientAuthFilter implements Filter {

    private final ClientRepository clientRepository;

    public ClientAuthFilter(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        String documentId = request.getHeader("X-Client-Document");

        if (documentId != null && !documentId.isBlank()) {
            clientRepository.findByDocumentId(documentId)
                    .ifPresent(ClientContextHolder::set);
        }

        try {
            chain.doFilter(req, res);
        } finally {
            ClientContextHolder.clear();
        }
    }
}