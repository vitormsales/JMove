package br.ufmg.dcc.labsoft.java.jmove.approach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import br.ufmg.dcc.labsoft.java.jmove.basic.AllEntitiesMapping;
import br.ufmg.dcc.labsoft.java.jmove.basic.CoefficientsResolution;
import br.ufmg.dcc.labsoft.java.jmove.basic.Parameters;
import br.ufmg.dcc.labsoft.java.jmove.basic.CoefficientsResolution.CoefficientStrategy;
import br.ufmg.dcc.labsoft.java.jmove.methods.AllMethods;
import br.ufmg.dcc.labsoft.java.jmove.methods.MethodJMove;
import br.ufmg.dcc.labsoft.java.jmove.methods.MethodObjects;
import br.ufmg.dcc.labsoft.java.jmove.methods.StatisticsMethod2Method;
import br.ufmg.dcc.labsoft.java.jmove.utils.CandidateMap;
import br.ufmg.dcc.labsoft.java.jmove.utils.ClazzUtil;
import br.ufmg.dcc.labsoft.java.jmove.utils.MoveMethod;
import br.ufmg.dcc.labsoft.java.jmove.utils.PrintOutput;

public class CalculateMediaApproach {

	final int indexCORRETA = 0;
	final int indexSUGESTAO = 1;
	final int indexERRADO = 3;
	CandidateMap candidateMap;

	private class ClassAtributes { // classe interna usada somente dentro da
									// classe
		int classID;
		int numberOfMethods;
		double similarityIndice;

		public ClassAtributes(int classID) {
			super();
			this.classID = classID;
			this.numberOfMethods = 0;
			this.similarityIndice = 0;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ClassAtributes) {
				// TODO Auto-generated method stub
				ClassAtributes other = (ClassAtributes) obj;
				if (this.classID != other.classID) {
					return false;
				}
				return true;
			}
			return false;
		}

	}

	// private Map<Pair<MethodRefine, MethodRefine>, Parameters> allParameters;
	private AllMethods allMethods;

	// ###### variaveis para excrita
	private String activeProjectName;

	private String blindAdress;
	private String sugestionAdress;
	private String indicationAdress;
	private String menor3dep;
	private IProgressMonitor monitor;
	private int contaGetters = 0;
	private int contaMethods = 0;
	// private int numberOfClass;

	private StatisticsMethod2Method stats;
	private boolean needCalculateAll;

	public CalculateMediaApproach(AllMethods allMethods,
			String activeProjectName, int numberOfClass,
			IProgressMonitor monitor) {
		// TODO Auto-generated constructor stub
		// this.allParameters = allParameters;
		this.allMethods = allMethods;
		// this.numberOfClass = numberOfClass;
		this.monitor = monitor;
		// ###### variaveis para escrita
		this.activeProjectName = activeProjectName;
		this.blindAdress = activeProjectName + "SaidaBlind";
		this.sugestionAdress = activeProjectName + "SaidaSugestao";
		this.menor3dep = activeProjectName + "menor3dep";

		needCalculateAll = false;
		stats = new StatisticsMethod2Method(allMethods.getAllMethodsList());

	}

	public CandidateMap calculate(CoefficientStrategy strategy) {

		monitor.beginTask(
				"Calculating methods similarity for selected Java Project (3/4)",
				(allMethods.getAllMethodsList().size()));

		candidateMap = new CandidateMap();

		// ##########Escreve a estrategia usada
		System.out.println();
		System.out.println(strategy);
		System.out.println();
		// ####### end

		indicationAdress = activeProjectName + " " + strategy + " indication";

		int contador[] = { 0, 0, 0, 0, 0 };

		PrintOutput.write("\n " + strategy + "\nMetodos com menos que "
				+ allMethods.getNumberOfExcluded()
				+ " dependencias excluidos\n", blindAdress);
		PrintOutput.write("\n " + strategy + "\nMetodos com menos que "
				+ allMethods.getNumberOfExcluded()
				+ " dependencias excluidos\n", sugestionAdress);
		PrintOutput.write("\n " + strategy + "\nMetodos com menos que "
				+ allMethods.getNumberOfExcluded()
				+ " dependencias excluidos\n", indicationAdress);

		// Pair<MethodRefine, MethodRefine> pair = new Pair<MethodRefine,
		// MethodRefine>();
		CoefficientsResolution resolution = new CoefficientsResolution();

		List<ClassAtributes> allClassSimilarity = new ArrayList<ClassAtributes>();

		int total = allMethods.getAllMethodsList().size();

		PrintOutput.write("num of methods "
				+ allMethods.getAllMethodsList().size() + "\n", menor3dep);

		int contZero = 0;
		int contAll = 0;

		for (int i = 0; i < allMethods.getAllMethodsList().size(); i++) {

			MethodJMove sourceMethod = allMethods.getAllMethodsList().get(i);

			IMethod im = allMethods.getIMethod(sourceMethod);

			try {
				if (im != null && !im.isConstructor() && !im.isMainMethod()
						&& im.isResolved()) {
					contAll++;
				}
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		IMethod im = null;
		for (int i = 0; i < allMethods.getAllMethodsList().size(); i++) {

			monitor.worked(1);
			MethodJMove sourceMethod = allMethods.getAllMethodsList().get(i);

			// #### tira metodos pequenos
			if (sourceMethod.getMethodsDependencies().size() < 4) {

				String nameMethod = AllEntitiesMapping.getInstance().getByID(
						sourceMethod.getNameID());

				im = allMethods.getIMethod(sourceMethod);

				String name[] = nameMethod.split("::");

				try {
					if (sourceMethod.getMethodsDependencies().size() == 3) {
						if (im != null && !im.isConstructor()
								&& !im.isMainMethod() && im.isResolved()) {
							if (name[1].startsWith("get")
									|| name[1].startsWith("set")) {
								contaGetters++;
							}

							if ((name[1].startsWith("get") || name[1]
									.startsWith("set"))
									&& sourceMethod.getMethodsDependencies()
											.size() == 0) {
								contZero++;
							}
							contaMethods++;
							PrintOutput.write(nameMethod
									+ " size dep "
									+ sourceMethod.getMethodsDependencies()
											.size() + "\n", menor3dep);

							PrintOutput.write(name[1] + "\n", "menor3dep2");
							PrintOutput.write(name[0] + "\n", "menor3dep3");

							// for (Integer chaves : sourceMethod
							// .getMethodsDependencies()) {
							// PrintOutput.write(AllEntitiesMapping.getInstance()
							// .getByID(chaves) + "\n", menor3dep);
							// }
						}
					}
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				PrintOutput.write("\n \n", menor3dep);

				continue;

			}

			// if (sourceMethod.getMethodsDependencies().size() == 3) {
			// if (hasOnlyOne(MethodObjects.getInstance()
			// .getMethodDeclaration(im))) {
			// System.out.println("only one " + im.getElementName());
			// PrintOutput.write("only one " + im.getElementName() +" \n",
			// "apenas1");
			// continue;
			// }
			//
			// }
			// #### end

			// #########begin conta somente aqueles que algum move é possivel

			int source = sourceMethod.getNameID();
			if (!allMethods.getMoveIspossible().contains(source)) {
				continue;

			}

			// ########end;

			allClassSimilarity.clear();

			System.out.printf("Media approach  %f" + " %c \n",
					(100) * (i / (float) total), '%');
			// System.out.println("Calculando o metodo " + sourceMethod);

			for (int j = 0; j < allMethods.getAllMethodsList().size(); j++) {

				if (i != j) {

					MethodJMove targetMethod = allMethods.getAllMethodsList()
							.get(j);
					int tagertClassID = targetMethod.getSourceClassID();

					ClassAtributes atributes = new ClassAtributes(tagertClassID);

					if (allClassSimilarity.contains(atributes)) {
						int index = allClassSimilarity.indexOf(atributes);
						atributes = allClassSimilarity.get(index);

					} else {

						allClassSimilarity.add(atributes);

					}

					atributes.numberOfMethods++;
					// pair.setFirst(sourceMethod);
					// pair.setSecond(targetMethod);

					Parameters parameters = stats.calculateParameters(
							sourceMethod, targetMethod);

					atributes.similarityIndice += resolution.calculate(
							parameters, strategy);

					// atributes.similarityIndice += resolution.calculate(
					// allParameters.get(pair), strategy);
				}

			}
			for (ClassAtributes classAtributes : allClassSimilarity) {
				classAtributes.similarityIndice /= classAtributes.numberOfMethods;
			}

			Collections.sort(allClassSimilarity, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					ClassAtributes c1 = (ClassAtributes) o1;
					ClassAtributes c2 = (ClassAtributes) o2;
					return Double.compare(c2.similarityIndice,
							c1.similarityIndice);
				}
			});

			normalize(allClassSimilarity);

			writeTraceIndications(sourceMethod, allClassSimilarity);
			// System.out.println("candidate antres de blind   "+candidateMap);
			blindAnalisys(allClassSimilarity, sourceMethod, contador);
			// System.out.println("candidate depois de blind   "+candidateMap);
			if (monitor != null && monitor.isCanceled()) {
				if (monitor != null)
					monitor.done();
				throw new OperationCanceledException();
			}
		}

		PrintOutput.write(" Numero de sugestoes " + contador[indexSUGESTAO]
				+ " \n", sugestionAdress);

		writeStatisticsBlind(contador);
		writeExcelFormat(contador, strategy);

		PrintOutput.write("Num of metods \n" + contaMethods, menor3dep);

		PrintOutput.write("Num of get and set \n" + contaGetters, menor3dep);

		PrintOutput.write("Num of metods com deped zero naum get e set \n "
				+ contZero, menor3dep);

		PrintOutput.write("Num of metods \n" + contAll, menor3dep);

		if (!needCalculateAll) {

			PrintOutput.finish(blindAdress);

			PrintOutput.finish(sugestionAdress);

			PrintOutput.finish(indicationAdress);

			PrintOutput.finish(menor3dep);
		}
		System.out.println("candidate map final  " + candidateMap);
		return candidateMap;

	}

	private void normalize(List<ClassAtributes> allClassSimilarity) {
		// TODO Auto-generated method stub

		double bigger = allClassSimilarity.get(0).similarityIndice;
		double minor = allClassSimilarity.get(0).similarityIndice;

		for (int i = 0; i < allClassSimilarity.size(); i++) {
			if (allClassSimilarity.get(i).similarityIndice > bigger) {
				bigger = allClassSimilarity.get(i).similarityIndice;
			} else if (allClassSimilarity.get(i).similarityIndice < minor) {
				minor = allClassSimilarity.get(i).similarityIndice;
			}
		}

		bigger -= minor;

		for (int i = 0; i < allClassSimilarity.size(); i++) {
			allClassSimilarity.get(i).similarityIndice -= minor;
			allClassSimilarity.get(i).similarityIndice /= bigger;
		}

	}

	// private boolean checkPossibleSugestion(MethodRefine sourceMethod,
	// List<ClassAtributes> allClassSimilarity, int posMax) {
	// // TODO Auto-generated method stub
	//
	// ClassAtributes classOriginal = new ClassAtributes(
	// sourceMethod.getSourceClassID());
	//
	// int MyPosition = allClassSimilarity.indexOf(classOriginal);
	//
	// if (MyPosition > posMax) {
	// int[] CandidateClassID = new int[posMax];
	//
	// for (int i = 0; i < posMax; i++) {
	// CandidateClassID[i] = allClassSimilarity.get(i).classID;
	// }
	//
	// return moveIsPossible(sourceMethod, CandidateClassID);
	//
	// }
	//
	// return false;
	//
	// }

	private boolean moveIsPossible(MethodJMove sourceMethod,
			int[] CandidateClassID) {

		List<String> possiblesCandidateList = MoveMethod
				.getpossibleRefactoring(allMethods.getIMethod(sourceMethod));

		if (possiblesCandidateList.size() > 0) {
			// System.out.println("METODO " + sourceMethod);
			for (int idCandidates : CandidateClassID) {
				// System.out.println("posso mover para " + idCandidates
				// + " idCandidates");
				for (String possibleCandidates : possiblesCandidateList) {

					Integer valor = AllEntitiesMapping.getInstance().getByName(
							possibleCandidates);

					// System.out.println("CANDIDATO atual" + possibleCandidates
					// + "id " + valor);

					int classPossibleID;

					if (valor != null) {
						classPossibleID = valor;
					} else {
						classPossibleID = treatyClassNameID(possibleCandidates);
					}

					// System.out.println("CANDIDATO valor tratado " +
					// possibleCandidates
					// + "id " + valor);

					if (idCandidates == classPossibleID) {

						PrintOutput.write(" Mover " + sourceMethod
								+ " para classe " + possibleCandidates + "\n",
								sugestionAdress);

						// System.out.println("SUGESTAO   Mover " + sourceMethod
						// + " para classe " + possibleCandidates + "\n");

						return true;
					}
				}
			}

			System.out.println();

		}
		return false;
	}

	private int treatyClassNameID(String possibleCandidates) {
		// TODO Auto-generated method stub

		int indexBegin = possibleCandidates.indexOf('<');

		if (indexBegin > 0) {
			String treatyName = possibleCandidates.substring(0, indexBegin);
			return AllEntitiesMapping.getInstance().getByName(treatyName);

		}

		indexBegin = possibleCandidates.indexOf('[');
		if (indexBegin > 0) {
			String treatyName = possibleCandidates.substring(0, indexBegin);
			return AllEntitiesMapping.getInstance().getByName(treatyName);

		}

		return -1;
	}

	private void writeExcelFormat(int[] contador, CoefficientStrategy strategy) {
		// TODO Auto-generated method stub

		float total = 0;

		total += contador[indexCORRETA] + contador[indexERRADO];

		String excell = "Excell" + activeProjectName;

		PrintOutput.write("\n" + strategy + "\t ", excell);
		PrintOutput.write(contador[indexCORRETA] + "\t ", excell);
		PrintOutput.write(contador[indexSUGESTAO] + "\t ", excell);
		PrintOutput.write(contador[indexERRADO] + "\t ", excell);
		PrintOutput.write((int) total + "\t ", excell);
	}

	private void writeStatisticsBlind(int[] contador) {
		float total = 0;

		if (contador[indexERRADO] == 0) {
			contador[indexERRADO] = 1;
		}

		total += contador[indexCORRETA] + contador[indexERRADO];

		PrintOutput.write("Correto " + contador[indexCORRETA] + " " + 100
				* contador[indexCORRETA] / total + "%\n", blindAdress);
		PrintOutput.write("Sugestões " + contador[indexSUGESTAO] + " " + 100
				* contador[indexSUGESTAO] / contador[indexERRADO] + "%\n",
				blindAdress);
		PrintOutput.write("Erros " + contador[indexERRADO] + " " + 100
				* contador[indexERRADO] / total + "%\n", blindAdress);
		PrintOutput.write("Total " + (int) total + " " + 100 * total / total
				+ "%\n", blindAdress);

	}

	// private void blindAnalisysBinary(List<ClassAtributes> allClassSimilarity,
	// Method sourceMethod, int[] contador) {
	// // TODO Auto-generated method stub
	//
	// ClassAtributes classOriginal = new ClassAtributes(
	// sourceMethod.getSourceClassID());
	//
	// final int POSICAOMAXIMA = 3;
	// final double PORCENTAGEM = 0.03;
	// final double MAXIMAPORCENTAGEM = 0.10;
	//
	// int classOriginalIndex = allClassSimilarity.indexOf(classOriginal);
	// int index = 0;
	// double firstOfAll = allClassSimilarity.get(index).similarityIndice;
	// double first = 1;
	// double next = 1;
	//
	// if (classOriginalIndex < 0) {
	// System.out.println("Nao aplicavel");
	// return;
	// }
	//
	// while (((first - next) / first) < PORCENTAGEM || index <= POSICAOMAXIMA)
	// {
	//
	// ClassAtributes classAtributesFirst = allClassSimilarity.get(index);
	// first = classAtributesFirst.similarityIndice;
	// // System.out.println("First " + first);
	// index++;
	//
	// if (index > allClassSimilarity.size()) {
	// index++;
	// // System.out.println(classOriginalIndex);
	// break;
	// }
	//
	// ClassAtributes classAtributesSecond = allClassSimilarity.get(index);
	// next = classAtributesSecond.similarityIndice;
	// // System.out.println("next " + next);
	//
	// if (classOriginalIndex < index) {
	// // System.out.println("Paraou no break pos " + index);
	// break;
	// }
	//
	// if ((firstOfAll - next) / firstOfAll > MAXIMAPORCENTAGEM
	// && index >= POSICAOMAXIMA) {
	// // System.out.println("Paraou no MAXIMAPORCENTAGEM pos " +
	// // index);
	// break;
	// }
	//
	// }
	// // System.out.println("classOriginalIndex " + classOriginalIndex);
	// // System.out.println("index " + index);
	// if (classOriginalIndex < index) {
	// contador[indexCORRETA]++;
	// } else {
	// contador[indexERRADO]++;
	// }
	// // System.out.println();
	// if (checkPossibleSugestion(sourceMethod, allClassSimilarity, index)) {
	// contador[indexSUGESTAO]++;
	// }
	//
	// }

	private void blindAnalisys(List<ClassAtributes> allClassSimilarity,
			MethodJMove sourceMethod, int[] contador) {
		// TODO Auto-generated method stub

		ClassAtributes classOriginal = new ClassAtributes(
				sourceMethod.getSourceClassID());

		IMethod iMethod = allMethods.getIMethod(sourceMethod);

		int indexOf = allClassSimilarity.indexOf(classOriginal);

		// considera as três primeirsas posições no ranking
		final int POSICAOMAXIMA = 2;
		final double VALORALTO = 0.75;

		if (indexOf > POSICAOMAXIMA) {
			contador[indexERRADO]++;

			for (int i = 0; i < indexOf; i++) {
				ClassAtributes classAtributes = allClassSimilarity.get(i);
				String candidate = AllEntitiesMapping.getInstance().getByID(
						classAtributes.classID);
				candidateMap.putCandidateOnList(iMethod, candidate);
			}

		} else {
			contador[indexCORRETA]++;

			for (int i = 0; i < indexOf; i++) {
				ClassAtributes classAtributes = allClassSimilarity.get(i);
				ClassAtributes classAtributesSource = allClassSimilarity
						.get(indexOf);
				if (classAtributesSource.similarityIndice < VALORALTO) {
					String candidate = AllEntitiesMapping.getInstance()
							.getByID(classAtributes.classID);
					candidateMap.putCandidateOnList(iMethod, candidate);
				}
			}

		}

		// if (checkPossibleSugestion(sourceMethod, allClassSimilarity,
		// POSICAOMAXIMA)) {
		// contador[indexSUGESTAO]++;
		// }

	}

	private boolean hasOnlyOne(MethodDeclaration md) {

		List<?> statList = null;

		if (md.getBody() != null) {

			statList = md.getBody().statements();
			if (statList == null || statList.size() == 0) {
				// PrintOutput
				// .write(ClazzUtil.getUnitClazzName(iMethod
				// .getCompilationUnit())
				// + "."
				// + iMethod.getElementName()
				// + " "
				// + "pequeno na sentença\n", "size");
				return true;
			}

			if (statList.size() < 2) {
				for (Object st : statList) {

					if (st instanceof ForStatement) {
						return false;
					} else if (st instanceof IfStatement) {
						return false;
					} else if (st instanceof SwitchCase) {
						return false;
					} else if (st instanceof SwitchStatement) {
						return false;
					} else if (st instanceof TryStatement) {
						return false;
					} else if (st instanceof WhileStatement) {
						return false;
					} else if (st instanceof DoStatement) {
						return false;
					} else if (st instanceof EnhancedForStatement) {
						return false;
					}
					// PrintOutput.write(
					// ClazzUtil.getUnitClazzName(iMethod
					// .getCompilationUnit())
					// + "."
					// + iMethod.getElementName()
					// + " "
					// + "pequeno na sentença sem if\n", "size");

				}
				return true;
			}
		}
		return false;
	}

	private void writeTraceIndications(MethodJMove sourceMethod,
			List<ClassAtributes> allClassSimilarity) {
		// TODO Auto-generated method stub
		String method = AllEntitiesMapping.getInstance().getByID(
				sourceMethod.getNameID());

		String classe = AllEntitiesMapping.getInstance().getByID(
				sourceMethod.getSourceClassID());

		ClassAtributes classOriginal = new ClassAtributes(
				sourceMethod.getSourceClassID());

		PrintOutput.write("Similaridade para método " + method + "\n",
				indicationAdress);

		PrintOutput.write("Ranking classe original " + classe + " "
				+ (allClassSimilarity.indexOf(classOriginal) + 1) + "º \n",
				indicationAdress);

		for (ClassAtributes classAtributes : allClassSimilarity) {
			PrintOutput.write(
					AllEntitiesMapping.getInstance().getByID(
							classAtributes.classID)
							+ " ", indicationAdress);

			PrintOutput.write(classAtributes.similarityIndice + "\n",
					indicationAdress);
		}
		PrintOutput.write("\n", indicationAdress);

	}

	public void calculateForAllStrategies() {

		needCalculateAll = true;

		for (CoefficientStrategy strategy : CoefficientsResolution
				.AllCoefficientStrategy()) {
			calculate(strategy);
		}

		PrintOutput.finish(indicationAdress);

		PrintOutput.finish(blindAdress);

		PrintOutput.finish(sugestionAdress);

		PrintOutput.finish(menor3dep);
	}
}
