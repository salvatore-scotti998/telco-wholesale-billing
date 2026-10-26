package it.salvatore.telcowholesalebilling.service;

import jakarta.xml.bind.JAXBContext;
import org.springframework.stereotype.Service;

import it.salvatore.telcowholesalebilling.xsd.Ordini;

import java.io.StringReader;
import jakarta.xml.bind.Unmarshaller;

@Service																	//Dependency Injection: questo @Service dice a Spring Boot che questa classe contiene logica applicativa, gestiscila tu
																			//e rendila disponibile ovunque con @Autowired. Spring crea un'istanza di questa classe (un bean) e la tiene pronta

public class XmlService {													//Classe che gestisce il parsing dell'XML

	public Ordini parseXml(String xml) throws Exception {				//Metodo parseXml che prende in ingresso l'xml come stringa e restituisce un oggetto java del tipo
																			//Ordini e lancia una eccezione nel caso in cui sia sbagliato qualcosa
		
		JAXBContext context = JAXBContext.newInstance(Ordini.class);		//JAXBContext è un traduttore tra XML e oggetti JAVA: in questo caso crea l'oggetto java context partendo dall'istanza Ordini
		Unmarshaller unmarshaller = context.createUnmarshaller();			//Unmarshaller è proprio il converter: prende l'XML, legge i tag, crea gli oggetti java e li popola con i valori corrispettivi
		
		return (Ordini) unmarshaller.unmarshal(new StringReader(xml));	//unmarshaller.unmarshal(..) legge il contenuto dell'xml preso in ingresso dal file new StringReader(xml). Ritorniamo un Object
																			//che viene castato in (Ordini) che corrisponde all'ordine completo già popolato
	}
}
