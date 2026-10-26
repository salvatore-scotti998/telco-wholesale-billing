# TelcoWholesaleBilling

**Versione: 0.1.0-alpha.1**

Progetto personale nato dall'idea di realizzare una piattaforma di fatturazione wholesale nel settore delle telecomunicazioni.

Il modello previsto riceve ordini da un sistema esterno (Sistema A), li elabora ai fini della fatturazione, ne storicizza gli stati e trasmette gli esiti di fatturazione a SAP (Sistema B). Queste fasi costituiscono la direzione del progetto: l'alpha attuale si concentra sulla ricezione e sul salvataggio degli ordini XML.

## Funzionalità presenti nell'alpha

- Endpoint HTTP REST `POST /api/ordini/xml` con corpo XML.
- Conversione del messaggio XML in oggetti Java tramite JAXB.
- Salvataggio in Oracle dell'identificativo esterno, dell'XML originale, della data di arrivo, del tipo e del flag di acquisizione.
- Schema SQL con nove tabelle per il modello iniziale del dominio.
- Servizio Java e package PL/SQL per l'inserimento nella tabella `INTERFACCIA`, presenti ma non ancora collegati al flusso di acquisizione.

## Limiti attuali

- L'endpoint `/api/interfaccia-ordini/acquisizione` è uno scheletro: restituisce un messaggio senza eseguire l'elaborazione. Non costituisce una verifica dell'inserimento in interfaccia.
- Lo XSD consente più elementi `HEADER`, ma il controller considera soltanto il primo. Usare un solo `HEADER` per richiesta.
- Il parsing JAXB non esegue una validazione esplicita contro lo XSD.
- Gli errori di parsing e persistenza producono HTTP 500; la gestione delle risposte deve essere affinata.
- L'identificativo `ID_ORDINE_PVM` è mantenuto nel contratto XML e nel database come identificativo dell'ordine esterno. Deve essere univoco.
- Non sono presenti autenticazione, frontend, calcolo delle fatture o integrazione SAP. L'alpha è destinata a prove locali.
- La presenza di tabelle per tariffe e prodotti non implica un motore di fatturazione implementato.

## Tecnologie

Java 17, Spring Boot 3.5.7, JAXB, Spring Data JPA, JDBC, Oracle Database e PL/SQL. Il modello SQL è destinato a Oracle Database 21c.

## Struttura

```text
TelcoWholesaleBilling/
├── BACKEND/                    # Applicazione Spring Boot e schema XSD
├── DB/
│   ├── schema.sql              # Tabelle, vincoli, sequenze e trigger
│   ├── tipo_esito.sql          # Tipo SQL T_ESITO_SQL
│   └── PKG_IMPORT_ORDINI.sql    # Specifica e corpo del package
├── .gitignore
└── README.md
```

## Requisiti

- JDK 17; la compilazione configura Java 17 come destinazione.
- Oracle Database e un client SQL, ad esempio SQL Developer.
- Accesso Internet per scaricare Maven e le dipendenze al primo utilizzo del Maven Wrapper incluso.

Le classi JAXB sono generate dallo XSD durante la compilazione. Non modificare né pubblicare i file generati nella cartella `target`.

## Preparazione di Oracle

Predisporre uno schema dedicato vuoto, con quota nel tablespace e privilegi per creare sessioni, tabelle, sequenze, trigger, tipi SQL e procedure/package. Gli script non creano l'utente Oracle.

Nella connessione allo schema dedicato eseguire come script, nell'ordine:

1. `DB/schema.sql`
2. `DB/tipo_esito.sql`
3. `DB/PKG_IMPORT_ORDINI.sql`

In SQL Developer usare l'esecuzione script (F5), che gestisce i separatori `/` dei blocchi PL/SQL. Gli script sono destinati alla prima installazione e non sono migrazioni da rilanciare su uno schema già popolato. Non includono dati iniziali per i cataloghi.

Controllare gli errori di creazione con:

```sql
SELECT OBJECT_NAME, OBJECT_TYPE, STATUS
FROM USER_OBJECTS
WHERE STATUS <> 'VALID';

SELECT NAME, TYPE, LINE, POSITION, TEXT
FROM USER_ERRORS
ORDER BY NAME, TYPE, SEQUENCE;
```

`ddl-auto: none` impedisce a Hibernate di creare o aggiornare automaticamente la struttura. Il semplice avvio del backend non certifica che tutti gli oggetti Oracle siano corretti.

## Compilazione e avvio su Windows

Aprire PowerShell nella cartella `BACKEND`. Verificare il JDK effettivamente usato da Maven:

```powershell
.\mvnw.cmd -version
.\mvnw.cmd clean compile
```

Se Maven usa un JDK precedente a 17, impostare `JAVA_HOME` sul percorso di un JDK adatto e anteporre la sua cartella `bin` al `Path` della sessione. La configurazione Java di Eclipse e quella di PowerShell possono differire.

Configurare la connessione nella stessa finestra:

```powershell
$env:DB_URL = "jdbc:oracle:thin:@localhost:1521/XEPDB1"
$env:DB_USERNAME = "NOME_SCHEMA_DEDICATO"
$passwordDb = Read-Host "Password Oracle" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $passwordDb).Password
Remove-Variable passwordDb
.\mvnw.cmd spring-boot:run
```

Adattare host, porta, servizio e nome utente alla propria installazione. Le variabili valgono per questa sessione e i processi avviati da essa. Non salvare credenziali nei file versionati. Per arrestare l'applicazione usare `Ctrl+C`.

## Esempio di richiesta XML

Il seguente messaggio usa esclusivamente valori dimostrativi:

```xml
<Ordini>
  <HEADER>
    <ID_ORDINE_PVM>DEMO-0001</ID_ORDINE_PVM>
    <DATA_DRO>2026-10-26</DATA_DRO>
    <CODE_CAUS>01</CODE_CAUS>
    <ACCESSO>
      <TIPO_CONTR>50</TIPO_CONTR>
      <CODE_CONTR>100</CODE_CONTR>
      <ID_RISORSA>RISORSA-DEMO-001</ID_RISORSA>
      <TIPO_AZIONE>1</TIPO_AZIONE>
      <BANDA_UP>100</BANDA_UP>
      <BANDA_DOWN>1000</BANDA_DOWN>
    </ACCESSO>
  </HEADER>
</Ordini>
```

Salvare il messaggio in un file locale `ordine-demo.xml`. Da una seconda PowerShell, nella cartella che contiene il file, inviarlo al backend già avviato:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/ordini/xml" -Method Post -ContentType "application/xml" -InFile ".\ordine-demo.xml"
```

L'operazione inserisce una riga nel database configurato. La risposta attesa è `Ordine ricevuto correttamente.`. Per verificare il salvataggio, nella connessione allo stesso schema eseguire:

```sql
SELECT ID, ID_ORDINE_PVM, DATA_ARRIVO, FLAG_ACQUISIZIONE, TIPO_ORDINE,
       DBMS_LOB.GETLENGTH(XML_ORDINE) AS LUNGHEZZA_XML
FROM ORDINI_INGRESSO
WHERE ID_ORDINE_PVM = 'DEMO-0001';
```

Per ulteriori prove usare identificativi diversi: l'alpha non gestisce ancora le ritrasmissioni in modo idempotente. Il successo di questa prova verifica l'acquisizione XML, non l'elaborazione in interfaccia o la fatturazione.

## Verifiche

`clean compile` controlla generazione JAXB e compilazione dei sorgenti principali. Non esegue i test né verifica la connessione a Oracle. Il test Spring Boot presente richiede la configurazione del datasource e l'ambiente Oracle.

Prova manuale del flusso XML completata il 26 ottobre 2026 sullo schema locale esistente, adeguato ai nuovi nomi degli oggetti:

- avvio del backend e invio HTTP di un ordine XML dimostrativo;
- risposta positiva dell'endpoint;
- riscontro nella tabella ORDINI_INGRESSO dello stesso identificativo esterno, con ID generato, flag 0, tipo ordine 1 e XML salvato (410 caratteri).

Questa prova verifica ricezione, conversione JAXB e persistenza dell'ordine. Non verifica l'elaborazione in interfaccia o la fatturazione. L'installazione integrale degli script su uno schema vuoto e l'esecuzione della suite di test restano da verificare.

## Roadmap

- Validazione tecnica e funzionale degli ordini.
- Gestione di duplicati, errori e stati di elaborazione.
- Collegamento tra acquisizione XML e interfaccia di elaborazione.
- Applicazione delle tariffe e calcolo della fatturazione wholesale.
- Storicizzazione completa delle elaborazioni e delle fatture.
- Invio degli esiti di fatturazione a SAP e gestione delle risposte.

