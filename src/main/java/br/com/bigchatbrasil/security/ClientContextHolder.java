package br.com.bigchatbrasil.security;

import br.com.bigchatbrasil.model.Client;

public class ClientContextHolder {
    private static final ThreadLocal<Client> CONTEXT = new ThreadLocal<>();

    public static void set(Client client) {
        CONTEXT.set(client);
    }

    public static Client get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
