package it.salvatore.telcowholesalebilling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data															//Genera Getter e setter per tutti i campi, equals(), hashCode() ecc
@NoArgsConstructor												//Genera un costruttore vuoto
@AllArgsConstructor												//Genera un costruttore completo tipo public EsitoInterfaccia(Integer codice, String messaggio) {ecc
public class EsitoInterfaccia {

	private Integer codice;
	private String messaggio;
}
