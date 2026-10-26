package it.salvatore.telcowholesalebilling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.salvatore.telcowholesalebilling.repository.OrdineIngressoRepository;
import it.salvatore.telcowholesalebilling.service.InterfacciaService;
import it.salvatore.telcowholesalebilling.service.XmlService;

@RestController
@RequestMapping("/api/interfaccia-ordini")
public class InterfacciaController {

	@Autowired
	private OrdineIngressoRepository repo;
	
	@Autowired
	private XmlService xmlService;
	
	@Autowired
	private InterfacciaService interfacciaService;
	
	
	@PostMapping(value = "/acquisizione")
	public ResponseEntity<?> acquisizioneOrdini() {
		
		try {
			
			return ResponseEntity.ok("Ordine acquisito correttamente in interfaccia");
		}
		catch (Exception e) {
			return ResponseEntity.status(500).body("Errore durante inserimento interfaccia: " + e.getMessage());
		}
	}
	
}
