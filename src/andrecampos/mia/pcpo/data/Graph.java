/**
 * 
 */
package andrecampos.mia.pcpo.data;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/**
 * Estrutura de grafo em memoria
 * @author andrecampos
 */
public class Graph {

	public  final String	fileName;
	public	Arc[] 			arcs;
	public	Demand[] 		demands;
	public  Node[]			nodes;
	
	public Graph() throws FileNotFoundException {
		this("data/FCND.dat");
	}
	
	public Graph(String fileName) throws FileNotFoundException {
		super();
		this.fileName = fileName;
		loadData();
	}

	private void loadData() throws FileNotFoundException {
		Scanner scanner = new Scanner(new FileReader(fileName));
		carregarArcos(scanner);
		carregarDemandas(scanner);
	}

	private void carregarDemandas(Scanner scanner) {
		int qtdDemandas = scanner.nextInt();
		demands 		= new Demand[qtdDemandas];
		// leitura das demandas 
		for (int i = 0; i < demands.length; i++) {
			demands[i] = new Demand(scanner);
		}
	}

	private void carregarArcos(Scanner scanner) {
		int qtdNodes 	= scanner.nextInt();
		nodes 			= new Node[qtdNodes];
		for (int i = 0; i < qtdNodes; i++) {
			nodes[i] = new Node(i+1);
		} 
		int qtdArcs 	= scanner.nextInt();
		arcs = new Arc[qtdArcs];

		// leitura do conjunto de arcos 
		for (int i = 0; i < arcs.length; i++) {
			arcs[i] = new Arc(i,scanner);
			nodes[arcs[i].sNode-1].out.add(arcs[i]);
			nodes[arcs[i].tNode-1].in.add(arcs[i]);
		}
	}
	
	public int getSizeArcs() { 
		return arcs.length;
	}
	
	public int getSizeDemands() { 
		return demands.length;
	}
}
