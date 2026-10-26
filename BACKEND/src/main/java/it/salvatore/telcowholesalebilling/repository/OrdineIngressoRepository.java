package it.salvatore.telcowholesalebilling.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import it.salvatore.telcowholesalebilling.model.OrdineIngresso;

public interface OrdineIngressoRepository extends JpaRepository<OrdineIngresso, Long>{		//Creo l'interfaccia Repository che fa da blueprint alla classe JPARepository, che mappa i valori del model
																							//OrdineIngresso con la corrispettiva tabella Oracle

}
