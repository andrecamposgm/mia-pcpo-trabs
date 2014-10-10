package andrecampos.mia.pcpo.data;

import java.util.Scanner;

/**
 * Cada uma das k linhas seguintes possui uma tripla que caracteriza uma demanda: origemDaDemanda	destinoDemanda	demanda	
 * @author Andre Campos 
 */
public class Demand {

	public int sDemand; 	// sk
	public int tDemand; 	// tk
	public int demand; 		// dk

	public Demand(Scanner scanner) {
		sDemand = scanner.nextInt();
		tDemand = scanner.nextInt();
		demand 	= scanner.nextInt();
	}

}