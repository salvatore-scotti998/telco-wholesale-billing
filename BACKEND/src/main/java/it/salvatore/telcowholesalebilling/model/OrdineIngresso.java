package it.salvatore.telcowholesalebilling.model;
import jakarta.persistence.*;
import lombok.Data;


@Entity															//Questo mappa la classe java come JPA;
@Table(name = "ORDINI_INGRESSO")								//Questo indica la tabella Oracle da usare;
@Data															//Lombok genera getter, setter e gli altri metodi previsti da @Data.
public class OrdineIngresso {

	@Id															//Questo dice ad Hibernate che questo è un oggetto ID;
	@GeneratedValue(
		    strategy = GenerationType.SEQUENCE,
		    generator = "ordiniIngressoGenerator"
		)
		@SequenceGenerator(
		    name = "ordiniIngressoGenerator",
		    sequenceName = "SEQ_ORDINI_INGRESSO",
		    allocationSize = 1
		)		//Hibernate ottiene l'ID dalla sequenza Oracle SEQ_ORDINI_INGRESSO, configurata con allocationSize = 1.
	@Column(name = "ID")										//Colonna con nome ID;
	private Long id;											//Campo ID Long, perchè è un numero grande e con Oracle usiamo le classi Wrapper e non i primitivi perchè i primitivi non contengon il null
																//mentre nel DB il null è presente. Quindi JPA = tipi Wrapper;
	
	@Column(name = "DATA_ARRIVO")								//Colonna con nome DATA_ARRIVO;
	private java.sql.Date dataArrivo;							//Campo java.sql.Date per rappresentare la data; questo tipo non conserva la componente oraria.
	
	@Column(name = "ID_ORDINE_PVM")								//Colonna con nome ID_ORDINE_PVM;
	private String idOrdinePvm;									//Campo con tipo Stringa per stringhe tipo "pvm:xxxxx-x";
	
	@Column(name = "DATA_ACQUISIZIONE")							//Colonna con nome DATA_ACQUISIZIONE;
	private java.sql.Date dataAcquisizione;						//Stesso discorso per DATA_ARRIVO;
	
	@Lob														//@Lob dice a JPA di usare un CLOB;
	@Column(name = "XML_ORDINE")								//Colonna con nome XML_ORDINE;
	private String xmlOrdine;									//Campo con tipo String, converte tutto l'xml in una stringa;
	
	@Column(name = "FLAG_ACQUISIZIONE")							//Colonna con nome FLAG_ACQUISIZIONE;
	private String flagAcquisizione;							//Tipo stringa per questo flag perchè può essere 1, 0, P, Z il suo valore; 
		
	@Column(name = "TIPO_ORDINE")								//Colonna con nome TIPO_ORDINE;
	private Integer tipoOrdine;									//Tipo Wrapper Integer come nel caso ID, e indica la tipologia dell'ordine. Il valore può essere 1, 2, 3 ecc...
	
}
