package it.salvatore.telcowholesalebilling.ws;

import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import it.salvatore.telcowholesalebilling.dto.EsitoInterfaccia;
import it.salvatore.telcowholesalebilling.model.OrdineIngresso;
import it.salvatore.telcowholesalebilling.repository.OrdineIngressoRepository;
import it.salvatore.telcowholesalebilling.service.InterfacciaService;
import it.salvatore.telcowholesalebilling.service.XmlService;
import it.salvatore.telcowholesalebilling.xsd.ACCESSO;
import it.salvatore.telcowholesalebilling.xsd.HEADER;
import it.salvatore.telcowholesalebilling.xsd.Ordini;

//XML → Controller → XmlService → Oggetto Java → Controller → Service / DB;
@RestController																						//Dice a Spring di mappare questa classe come controller di tipo REST;
@RequestMapping("/api/ordini")																	//Questo corrisponde all'URI da inserire per effettuare la chiamata, definisce il path base dell'endpoint;
public class OrdiniXmlController {	


	@Autowired																						//Autowired richiama per Dependency Injection l'interfaccia Repository;
	private OrdineIngressoRepository repo;															//Creo l'oggetto repo di tipo OrdineIngressoRepository;

	@Autowired																						//Autowired richiama per Dependency Injection l'interfaccia Repository;
	private XmlService xmlService;																	//Creo l'oggetto xmlService per collegare il service al controller;
	
	@PostMapping(value = "/xml", consumes = MediaType.APPLICATION_XML_VALUE)				//PostMapping aggiunge all'URI il percorso /xml per questa chiamata POST e utilizza come corpo della 
																									//chiamata un XML. In questo caso indica che il controller riceve l'XML in un metodo POST;
	public ResponseEntity<String> acquisizioneOrdine(@RequestBody String xml) {						//Creo l'oggetto per la risposta HTTP con in ingresso il body, tipo stringa, che è l'XML in questo caso;

		try {																						//Con il try-catch proteggiamo tutta la logica del WS in caso di errore;

			Ordini ordine = xmlService.parseXml(xml);											//Richiamo il service JAXB che effettua il parsing dell'XML e lo converte nell'oggetto Ordini, cioè la struttura
																									//vera e propria dell'XML;

			HEADER header = ordine.getHEADER().get(0);												//JAXB genera una lista List<HEADER>; questa alpha elabora soltanto il primo header (get(0));
																									//Ora leggo tutti i dati dell'Header;
			String idOrdinePvm = header.getIDORDINEPVM();											//Leggo il PVM inviato da Sistema A e lo associo a idOrdinePvm;
			XMLGregorianCalendar dataDro = header.getDATADRO();										//Leggo la dataDro inviata da Sistema A e la associo a dataDro di tipo XMLGregorianCalendar(un tipo XML di date);		
			String codeCaus = header.getCODECAUS();													//Leggo il codeCaus inviato da Sistema A e lo associo a codeCaus;


			java.sql.Date dataDroOracle = new java.sql.Date(										//Effettuo il parser della dataDro in dataDroOracle, passandola quindi da gregorianCalendar al tipo java.sql.Date;
					dataDro.toGregorianCalendar().getTimeInMillis());

			ACCESSO accesso = header.getACCESSO();													//Leggo l'oggetto che contiene l'ACCESSO, quindi il body dell'XML
																									//Ora leggo tutti i dati del body(ACCESSO);
			String tipoContr   = accesso.getTIPOCONTR();                 							//Il tipo contr, quindi il servizio
			Integer codeContr  = accesso.getCODECONTR().intValue();      							//BigInteger → Integer, questo JAXB lo esprime come BigInteger perchè xs:integer lo interpreta così, effettuiamo il
																									//parsing in integer per Oracle;
			String idRisorsa   = accesso.getIDRISORSA();											//Id risorsa;
			String tipoAzione  = accesso.getTIPOAZIONE();                							//Tipo_azione: 1 attivazione, 2 cessazione, 3 variazione;
			Integer bandaUp    = accesso.getBANDAUP().intValue();        							//BigInteger → Integer	Stesso discorso del code_contr;
			Integer bandaDown  = accesso.getBANDADOWN().intValue();      							//BigInteger → Integer	Stesso discorso del code_contr;


			OrdineIngresso entity = new OrdineIngresso();												//Creo l'entity dal tipo OrdineIngresso che verrà salvato nel DB;
																									//Ora setto i campi della tabella ORDINI_INGRESSO;
			entity.setIdOrdinePvm(idOrdinePvm);														//Setto l'ID_ORDINE_PVM col valore letto in precedenza dall'Header dell'XML;
			entity.setXmlOrdine(xml);           													//Setto l'XML nel campo CLOB XML_ORDINE;			
			entity.setDataArrivo(new java.sql.Date(System.currentTimeMillis()));					//Imposta DATA_ARRIVO usando l'orologio dell'applicazione.
			entity.setFlagAcquisizione("0");														//Setto il campo FLAG_ACQUISIZIONE a 0 (valori possibili 0=in attesa di essere acquisito, 1=acquisito,
																									//P=parcheggiato, Z=Scartato);
			entity.setTipoOrdine(1);																//Setto il campo TIPO_ORDINE con valore 1 (1=ordine di risorsa);

			repo.save(entity);																		//Salva l'entity tramite il repository JPA; Hibernate gestisce la persistenza in Oracle.

			return ResponseEntity.ok("Ordine ricevuto correttamente.");								
		}
		catch (Exception e) {																		//In caso di eccezione nel parsing o nel salvataggio restituisce HTTP 500 con il messaggio di errore.
			return ResponseEntity.status(500).body("Errore durante il parsing XML: " + e.getMessage());
		}
	}
}
