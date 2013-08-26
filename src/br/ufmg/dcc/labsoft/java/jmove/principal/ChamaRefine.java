package br.ufmg.dcc.labsoft.java.jmove.principal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;

import br.ufmg.dcc.labsoft.java.jmove.approach.CalculateMediaApproach;
import br.ufmg.dcc.labsoft.java.jmove.ast.DeepDependencyVisitor;
import br.ufmg.dcc.labsoft.java.jmove.basic.AllEntitiesMapping;
import br.ufmg.dcc.labsoft.java.jmove.basic.CoefficientsResolution.CoefficientStrategy;
import br.ufmg.dcc.labsoft.java.jmove.methods.AllMethods;
import br.ufmg.dcc.labsoft.java.jmove.methods.Clazz;
import br.ufmg.dcc.labsoft.java.jmove.methods.MethodJMove;
import br.ufmg.dcc.labsoft.java.jmove.util.DCLUtil;
import br.ufmg.dcc.labsoft.java.jmove.utils.CandidateMap;
import br.ufmg.dcc.labsoft.java.jmove.utils.InternalClass;
import br.ufmg.dcc.labsoft.java.jmove.utils.PrintOutput;

public class ChamaRefine {

	CandidateMap map;
	IProject project;
	IJavaProject javaProject;
	List<DeepDependencyVisitor> allDeepDependency;
	int numberOfClass = 0;
	AllMethods allMethods;
	String activeProjectName;

	// private IJavaProject selectedProject;
	// private IPackageFragmentRoot selectedPackageFragmentRoot;
	// private IPackageFragment selectedPackageFragment;
	// private ICompilationUnit selectedCompilationUnit;
	// private IType selectedType;
	//
	// (IJavaProject iJavaProject, IProgressMonitor monitor) {
	// if(monitor != null)
	// monitor.beginTask("Parsing selected Java Project",
	// getNumberOfCompilationUnits(iJavaProject));

	public CandidateMap execute(IJavaProject iProject) {
		try {

			allDeepDependency = new ArrayList<DeepDependencyVisitor>();

			 activeProjectName = iProject.getElementName();

			project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(activeProjectName);
			javaProject = JavaCore.create(project);

			IWorkbench wb = PlatformUI.getWorkbench();
			IProgressService ps = wb.getProgressService();
			ps.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {

					try {
						monitor.beginTask("Parsing selected Java Project (1/4)",
								(DCLUtil.getClassNames(project)).size());

						for (String className : DCLUtil.getClassNames(project)) {
							// System.out.println("IMPORTANTE");
							// System.out.println(className);
							// System.out.println();
							if (className == null) {
								continue;
							}

							InternalClass.getInstance().putNewInternalClass(
									className);

							IFile resource = DCLUtil.getFileFromClassName(
									javaProject, className);
							ICompilationUnit unit = ((ICompilationUnit) JavaCore
									.create((IFile) resource));
							// System.out.println(unit);
							System.out.println("AST para "
									+ unit.getElementName());
							DeepDependencyVisitor deepDependency = new DeepDependencyVisitor(
									unit);
							allDeepDependency.add(deepDependency);
							numberOfClass++;

							Clazz.getInstance().insertMapping(unit);
							monitor.worked(1);

							if (monitor != null && monitor.isCanceled()) {
								if (monitor != null)
									monitor.done();
								throw new OperationCanceledException();
							}
						}
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			// for (String className : DCLUtil.getClassNames(project)) {
			// // System.out.println("IMPORTANTE");
			// // System.out.println(className);
			// // System.out.println();
			// if (className == null) {
			// continue;
			// }
			//
			// InternalClass.getInstance().putNewInternalClass(className);
			//
			// IFile resource = DCLUtil.getFileFromClassName(javaProject,
			// className);
			// ICompilationUnit unit = ((ICompilationUnit) JavaCore
			// .create((IFile) resource));
			// // System.out.println(unit);
			// System.out.println("AST para " + unit.getElementName());
			// DeepDependencyVisitor deepDependency = new DeepDependencyVisitor(
			// unit);
			// allDeepDependency.add(deepDependency);
			// numberOfClass++;
			//
			// Clazz.getInstance().insertMapping(unit);
			// }

			wb = PlatformUI.getWorkbench();
			ps = wb.getProgressService();
			ps.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {

					try {
					 allMethods = new AllMethods(allDeepDependency,monitor);
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

//			System.out.println(" inciando AllMethods");
//			AllMethods allMethods = new AllMethods(allDeepDependency);
//			System.out.println(" Terminou AllMethods");

			//List<MethodJMove> l = allMethods.getAllMethodsList();
//			for (int i = 0; i < l.size(); i++) {
//				boolean temApenas1 = true;
//				MethodJMove m1 = l.get(i);
//
//				for (int j = 0; j < l.size(); j++) {
//					MethodJMove m2 = l.get(j);
//
//					if (i != j
//							&& m1.getSourceClassID() == m2.getSourceClassID())
//						temApenas1 = false;
//
//				}
//				if (temApenas1)
//					PrintOutput.write(
//							AllEntitiesMapping.getInstance().getByID(
//									m1.getSourceClassID())
//									+ "\n", "classes");
//			}
//
//			PrintOutput.finish("classes");

			// FeatureEnvy.getInstance().sugestFeatureEnvyMoves(allMethods);
			// for (Method method : allMethods.getAllMethodsList()) {
			// System.out.println(method);
			// for (Integer ID : method.getMethodsAcessDependenciesID()) {
			// System.out.println(AllEntitiesMapping.getInstance().getByID(ID));
			//
			// }
			// System.out.println();
			// }

			// tornando visivel para o coletor de lixo
			allDeepDependency = null;

			// allMethods.excludeConstructors();
			// allMethods.excludeDependeciesLessThan(5);

			// ########## Method2Method begin

			// System.out.println("iniciando StatisticsMethod2Method");
			// StatisticsMethod2Method m2m = new StatisticsMethod2Method(
			// allMethods.getAllMethodsList());
			// System.out.println("Terminou StatisticsMethod2Method");

			
			wb = PlatformUI.getWorkbench();
			ps = wb.getProgressService();
			ps.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {

					
						CalculateMediaApproach mediaApproach = new CalculateMediaApproach(
								allMethods, activeProjectName, numberOfClass, monitor);
						allMethods = null;
						map = mediaApproach.calculate(CoefficientStrategy.SokalSneath2);
						
					
					
				}
			});
			
//			System.out.println("iniciando CalculateMediaApproach");
//			
//			CalculateMediaApproach mediaApproach = new CalculateMediaApproach(
//					allMethods, activeProjectName, numberOfClass);
//			
//			System.out.println("Terminou CalculateMediaApproach");
//
//			// m2m = null;
//			allMethods = null;
//
//			System.out.println("iniciando calculateForAllStrategies");
//			map = mediaApproach.calculate(CoefficientStrategy.SokalSneath2);
//			System.out.println("Terminou calculateForAllStrategies");
//			System.out.println("Recebeu o valor de map " + map);
//			// ########## Method2Method end
//
//			System.out.println("Fim");

			// Iterator<Entry<IMethod, ArrayList<String>>> it = CandidateMap
			// .getInstance().getCandidatesRefine().entrySet().iterator();
			// System.out.println("CandidateMap "
			// + CandidateMap.getInstance().getCandidatesRefine().size());
			// while (it.hasNext()) {
			// Entry<IMethod, ArrayList<String>> en = it.next();
			// System.out.println();
			// System.out.println(en.getKey());
			// for (String candidate : en.getValue()) {
			// System.out.println(candidate);
			// }
			// }
			// }
		} catch (Exception t) {
			t.printStackTrace();
		}

		System.out.println("retornar map valor= " + map);
		return map;

	}
}
