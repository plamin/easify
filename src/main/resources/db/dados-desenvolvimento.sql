-- Dados inteiramente fictícios para teste técnico no Oracle SQL Developer (F5).
-- Execute depois de schema.sql. O SAVEPOINT permite desfazer apenas estes inserts.
SAVEPOINT antes_exemplo_easify;
DECLARE
    v_analise_id NUMBER;
BEGIN
    INSERT INTO ANALISE (UUID_ANALISE, CLIENTE_NOME, CLIENTE_EMPRESA, VENDEDOR_NOME,
                         PRODUTO_RECOMENDADO, NIVEL_INTERESSE, RISCO_CHURN, PROXIMA_ACAO,
                         RESUMO, DATA_CRIACAO)
    VALUES (LOWER(REGEXP_REPLACE(RAWTOHEX(SYS_GUID()),
            '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5')),
            'Cliente fictício', 'Empresa fictícia', 'Vendedor fictício', EMPTY_CLOB(),
            'NAO_IDENTIFICADO', 'NAO_IDENTIFICADO', EMPTY_CLOB(),
            'Registro fictício para validação técnica.', CURRENT_TIMESTAMP)
    RETURNING ID INTO v_analise_id;

    INSERT INTO TRANSCRICAO (ANALISE_ID, TEXTO_ORIGINAL, DATA_REGISTRO)
    VALUES (v_analise_id, 'Transcrição fictícia: precisamos melhorar o atendimento.', CURRENT_TIMESTAMP);
    INSERT INTO ANALISE_DOR (ANALISE_ID, POSICAO, DESCRICAO)
    VALUES (v_analise_id, 0, 'Dor fictícia de atendimento');
    INSERT INTO ANALISE_CONCORRENTE (ANALISE_ID, POSICAO, DESCRICAO)
    VALUES (v_analise_id, 0, 'Concorrente fictício');
    INSERT INTO STATUS_ACOMPANHAMENTO (ANALISE_ID, STATUS, OBSERVACAO, DATA_MUDANCA)
    VALUES (v_analise_id, 'PENDENTE', 'Evento fictício', CURRENT_TIMESTAMP);
END;
/
-- O exemplo não mantém os registros no banco. IDs identity consumidos não retornam.
ROLLBACK TO antes_exemplo_easify;