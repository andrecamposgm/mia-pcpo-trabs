package andrecampos.mia.pcpo.data;

import java.util.Scanner;

/**
 * Cada uma das m linhas seguintes possui uma quintupla que caracteriza um arco: i	j	custoFixo	custoVariavel	capacidade
 * @author Andre Campos
 */
public class Arc {

	public final int id;
	public final int sNode; // i
	public final int tNode; // j
	public final int fixedCost; // f(i,j)
	public final int variableCost; // c(i,j)
	public final int capacity; // u(i,j)

	public Arc(int id, Scanner scanner) {
		this.id = id;
		sNode = scanner.nextInt();
		tNode = scanner.nextInt();
		fixedCost = scanner.nextInt();
		variableCost = scanner.nextInt();
		capacity = scanner.nextInt();
	}

	public String name() {
		return "("+(sNode+1)+","+(tNode+1)+")";
	}

}