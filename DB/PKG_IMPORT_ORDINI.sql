CREATE OR REPLACE PACKAGE PKG_IMPORT_ORDINI AS

    PROCEDURE INSERISCI_INTERFACCIA (
    p_id_risorsa IN NUMBER,
    p_tipo_contr IN NUMBER,
    p_code_contr IN NUMBER,
    p_code_ps IN NUMBER,
    p_code_caus IN VARCHAR2,
    p_data_dro IN DATE,
    p_banda_up IN NUMBER,
    p_banda_down IN NUMBER,
    p_id_ordine_pvm IN VARCHAR2,
    p_esito OUT T_ESITO_SQL
    );

END PKG_IMPORT_ORDINI;
/


CREATE OR REPLACE PACKAGE BODY PKG_IMPORT_ORDINI AS

  PROCEDURE INSERISCI_INTERFACCIA (
    p_id_risorsa    IN NUMBER,
    p_tipo_contr    IN NUMBER,
    p_code_contr    IN NUMBER,
    p_code_ps       IN NUMBER,
    p_code_caus     IN VARCHAR2,
    p_data_dro      IN DATE,
    p_banda_up      IN NUMBER,
    p_banda_down    IN NUMBER,
    p_id_ordine_pvm IN VARCHAR2,
    p_esito         OUT T_ESITO_SQL
  ) IS
  BEGIN

    ----------------------------------------------------------------
    -- 1) Inserimento in INTERFACCIA (senza CODE_IRTF)
    ----------------------------------------------------------------
    INSERT INTO INTERFACCIA (
      ID_RISORSA,
      TIPO_CONTR,
      CODE_CONTR,
      CODE_PS,
      CODE_CAUS,
      DATA_DRO,
      DATA_ACQ_RICH,
      FLAG_ACQ_RICH,
      BANDA_UP,
      BANDA_DOWN,
      ID_ORDINE_PVM
    )
    VALUES (
      p_id_risorsa,
      p_tipo_contr,
      p_code_contr,
      p_code_ps,
      p_code_caus,
      p_data_dro,
      SYSDATE,
      '0',
      p_banda_up,
      p_banda_down,
      p_id_ordine_pvm
    );

    ----------------------------------------------------------------
    -- 2) Esito OK
    ----------------------------------------------------------------
    p_esito := T_ESITO_SQL(1, 'Ordine inserito correttamente in INTERFACCIA');

    COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      ----------------------------------------------------------------
      -- 3) Errore tecnico
      ----------------------------------------------------------------
      p_esito := T_ESITO_SQL(2, 'Errore tecnico: ' || SQLERRM);
      ROLLBACK;

  END INSERISCI_INTERFACCIA;

END PKG_IMPORT_ORDINI;
/
