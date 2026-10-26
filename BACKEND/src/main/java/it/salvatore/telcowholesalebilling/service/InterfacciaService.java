package it.salvatore.telcowholesalebilling.service;

import java.sql.Struct;
import java.sql.Types;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import it.salvatore.telcowholesalebilling.dto.EsitoInterfaccia;
import oracle.jdbc.OracleTypes;

@Service
public class InterfacciaService {																		//Creo la classe di tipo Service che gestirà l'insert di dati nella tabella INTERFACCIA

	private final JdbcTemplate jdbcTemplate;															//Creo la variabile JdbcTemplate con final, in modo tale che non cambi
	
	private final SimpleJdbcCall simpleJdbcCall;														//Creo la variabile SimpleJdbcCall con final, in modo tale che non cambi
	
	@Autowired
	public InterfacciaService(JdbcTemplate jdbcTemplate) {												//Questo è il costruttore della JDBC
		this.jdbcTemplate = jdbcTemplate;																//Inizializzo l'oggetto jdbcTemplate
		
		this.simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)											//Questo è l'oggetto simpleJdbcCall, serve a richiamare il PKG Oracle, Procedure Oracle, Funzioni																					
				.withCatalogName("PKG_IMPORT_ORDINI")											//Oracle, ecc. In questo caso gli dico di chiamare il PKG di import ordini con la sua procedure
				.withProcedureName("INSERISCI_INTERFACCIA")
				.withoutProcedureColumnMetaDataAccess()
				.declareParameters(
						new SqlParameter("p_id_risorsa", Types.NUMERIC),
					    new SqlParameter("p_tipo_contr", Types.NUMERIC),
					    new SqlParameter("p_code_contr", Types.NUMERIC),
					    new SqlParameter("p_code_ps", Types.NUMERIC),
					    new SqlParameter("p_code_caus", Types.VARCHAR),
					    new SqlParameter("p_data_dro", Types.DATE),
					    new SqlParameter("p_banda_up", Types.NUMERIC),
					    new SqlParameter("p_banda_down", Types.NUMERIC),
					    new SqlParameter("p_id_ordine_pvm", Types.VARCHAR),

					    new SqlOutParameter("p_esito", OracleTypes.STRUCT, "T_ESITO_SQL"));
	}
	
	public EsitoInterfaccia inserisciInInterfaccia(														//Metodo che accetta i parametri dell'ordine e chiama la simpleJdbcCall.execute()																					
	        Integer idRisorsa,																			//Al suo interno scriviamo i parametri corrispondeti alla firma della procedure
	        Integer tipoContr,
	        Integer codeContr,
	        Integer codePs,
	        String codeCaus,
	        java.sql.Date dataDro,
	        Integer bandaUp,
	        Integer bandaDown,
	        String idOrdinePvm) {

	    // Prepara i parametri della chiamata alla stored procedure
	    MapSqlParameterSource parametri = new MapSqlParameterSource()
	            .addValue("p_id_risorsa", idRisorsa)
	            .addValue("p_tipo_contr", tipoContr)
	            .addValue("p_code_contr", codeContr)
	            .addValue("p_code_ps", codePs)
	            .addValue("p_code_caus", codeCaus)
	            .addValue("p_data_dro", dataDro)
	            .addValue("p_banda_up", bandaUp)
	            .addValue("p_banda_down", bandaDown)
	            .addValue("p_id_ordine_pvm", idOrdinePvm);

	    Map<String, Object> out = simpleJdbcCall.execute(parametri);									//Inserisco una struttura dati tipo Map con chiave stringa e valore oggetto. In questo caso Spring
    																									//Invoca il package+Procedure Oracle, prende tutti i parametri OUT e INOUT e li restituisce dentro
    																									//la struttura Map
	    Object esitoObj = out.get("p_esito");															//Questo oggetto recupera il valore OUT della StoredProcedure. Questo è un Oracle STRUCT che 
	    																								//rappresenta il tipo SQL T_ESITO_SQL restituito dalla procedura
	    try {
	        Struct esitoStruct = (Struct) esitoObj;														//Converte il valore out p_esito in Struct
	        Object[] attr = esitoStruct.getAttributes();												//Estrae gli attributi dallo struct
	        Integer codiceEsito = ((Number) attr[0]).intValue();										//Estrazione del codice con position
	        String messaggioEsito = (String) attr[1];													//Estrazione del messaggio con position

	        System.out.println("CODICE ESITO: " + codiceEsito);											//Stampa del codice esito su console
	        System.out.println("MESSAGGIO ESITO: " + messaggioEsito);									//Stampa del messaggio esito su console
	        
	        return new EsitoInterfaccia(codiceEsito, messaggioEsito);									//Ritorno l'esito del pkg
	        
	    } catch (Exception e) {
	    	
	        return new EsitoInterfaccia(-1, "Errore nel parsing dell'esito Oracle" + e.getMessage());
	    }

	}
}
