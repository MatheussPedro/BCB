CREATE TABLE clientes (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    tipo_pessoa VARCHAR(2) CHECK (tipo_pessoa IN ('PF', 'PJ')),
    documento VARCHAR(20) UNIQUE NOT NULL,
    plano VARCHAR(10) CHECK (plano IN ('pre', 'pos')) NOT NULL,
    status BOOLEAN DEFAULT TRUE
);

CREATE TABLE mensagens (
    id SERIAL PRIMARY KEY,
    cliente_id INT REFERENCES clientes(id),
    destinatario VARCHAR(255) NOT NULL,
    texto TEXT NOT NULL,
    tipo VARCHAR(10) CHECK (tipo IN ('SMS', 'WhatsApp')) NOT NULL,
    prioridade VARCHAR(10) CHECK (prioridade IN ('normal', 'urgente')) NOT NULL,
    custo DECIMAL(5,2) NOT NULL,
    status VARCHAR(20) CHECK (status IN ('enfileirada', 'processando', 'enviada', 'falha')) DEFAULT 'enfileirada',
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transacoes (
    id SERIAL PRIMARY KEY,
    cliente_id INT REFERENCES clientes(id),
    tipo_transacao VARCHAR(10) CHECK (tipo_transacao IN ('debito', 'credito')),
    valor DECIMAL(10,2) NOT NULL,
    descricao TEXT,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE limites_mensais (
    id SERIAL PRIMARY KEY,
    cliente_id INT REFERENCES clientes(id),
    mes_ano VARCHAR(7),
    limite_total DECIMAL(10,2) NOT NULL,
    consumo DECIMAL(10,2) DEFAULT 0
);