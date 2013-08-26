package br.ufmg.dcc.labsoft.java.jmove.suggestion;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import br.ufmg.dcc.labsoft.java.jmove.methods.MethodObjects;
import br.ufmg.dcc.labsoft.java.jmove.utils.ClazzUtil;
import br.ufmg.dcc.labsoft.java.jmove.utils.MoveMethod;
import br.ufmg.dcc.labsoft.java.jmove.utils.PrintOutput;

public class PreConditionsAnalyser {

	private Map<IMethod, List<String>> candidatesTable = new HashMap<IMethod, List<String>>();

	public boolean methodCanBeMoved(IMethod iMethod) {
		// TODO Auto-generated method stub
		MethodDeclaration md = MethodObjects.getInstance()
				.getMethodDeclaration(iMethod);

		System.out.println("override md " + overridesMethod(md));
		return !overridesMethod(md) && !hasAssigment(iMethod);
	}

	public boolean satisfiesAllConditions(IMethod iMethod,
			ICompilationUnit targetiCompilation) {
		// TODO Auto-generated method stub
		MethodDeclaration md = MethodObjects.getInstance()
				.getMethodDeclaration(iMethod);

		if (isValidTarget(iMethod, targetiCompilation)) {
			System.out.println("override md " + overridesMethod(md));
			return isEclipseValidTarget(iMethod, targetiCompilation);
		}
		return false;
	}

	private boolean isValidTarget(IMethod iMethod,
			ICompilationUnit targetiCompilation) {
		// TODO Auto-generated method stub
		Set<IVariableBinding> localDeclarationSet = MethodObjects.getInstance()
				.getAllLocalDeclarationSet(iMethod);

		String targetClass = ClazzUtil.getUnitClazzName(targetiCompilation);
		for (IVariableBinding iVar : localDeclarationSet) {
			if (targetClass.equals(iVar.getType().getQualifiedName())) {
				System.out.println("Deve REtornar Falso");
				return false;
			}

		}

		return true;
	}

	private boolean overridesMethod(MethodDeclaration md) {
		IMethodBinding methodBinding = md.resolveBinding();
		ITypeBinding declaringClassTypeBinding = methodBinding
				.getDeclaringClass();
		Set<ITypeBinding> typeBindings = new LinkedHashSet<ITypeBinding>();
		ITypeBinding superClassTypeBinding = declaringClassTypeBinding
				.getSuperclass();
		if (superClassTypeBinding != null)
			typeBindings.add(superClassTypeBinding);
		ITypeBinding[] interfaceTypeBindings = declaringClassTypeBinding
				.getInterfaces();
		for (ITypeBinding interfaceTypeBinding : interfaceTypeBindings)
			typeBindings.add(interfaceTypeBinding);

		return overridesMethod(typeBindings, md);
	}

	private boolean overridesMethod(Set<ITypeBinding> typeBindings,
			MethodDeclaration md) {
		IMethodBinding methodBinding = md.resolveBinding();
		Set<ITypeBinding> superTypeBindings = new LinkedHashSet<ITypeBinding>();
		for (ITypeBinding typeBinding : typeBindings) {
			ITypeBinding superClassTypeBinding = typeBinding.getSuperclass();
			if (superClassTypeBinding != null)
				superTypeBindings.add(superClassTypeBinding);
			ITypeBinding[] interfaceTypeBindings = typeBinding.getInterfaces();
			for (ITypeBinding interfaceTypeBinding : interfaceTypeBindings)
				superTypeBindings.add(interfaceTypeBinding);
			if (typeBinding.isInterface()) {
				IMethodBinding[] interfaceMethodBindings = typeBinding
						.getDeclaredMethods();
				for (IMethodBinding interfaceMethodBinding : interfaceMethodBindings) {
					if (methodBinding.overrides(interfaceMethodBinding)
							|| methodBinding.toString().equals(
									interfaceMethodBinding.toString()))
						return true;
				}
			} else {
				IMethodBinding[] superClassMethodBindings = typeBinding
						.getDeclaredMethods();
				for (IMethodBinding superClassMethodBinding : superClassMethodBindings) {
					if (methodBinding.overrides(superClassMethodBinding)
							|| methodBinding.toString().equals(
									superClassMethodBinding.toString()))
						return true;
				}
			}
		}
		if (!superTypeBindings.isEmpty()) {
			return overridesMethod(superTypeBindings, md);
		} else
			return false;
	}

	public boolean isEclipseValidTarget(IMethod myIMethod,
			ICompilationUnit targetiCompilation) {

		List<String> candidates;
		if (candidatesTable.containsKey(myIMethod)) {
			candidates = candidatesTable.get(myIMethod);
		} else {
			candidatesTable.clear();
			candidates = MoveMethod.getpossibleRefactoring(myIMethod);
			candidatesTable.put(myIMethod, candidates);
		}

		System.out.println(ClazzUtil.getUnitClazzName(targetiCompilation));

		if (candidates.contains(ClazzUtil.getUnitClazzName(targetiCompilation))) {
			return true;
		}

		return false;
	}

	public boolean hasAssigment(IMethod ime) {
		if (MethodHasAssigment.getInstance().contains(ime)) {
			return true;
		}

		return false;
	}

//	private boolean notHasOnlyOne(MethodDeclaration md) {
//
//		List<?> statList = null;
//
//		IJavaElement javaElementA = md.resolveBinding().getJavaElement();
//		IMethod iMethod = null;
//		if (javaElementA instanceof IMethod) {
//			iMethod = (IMethod) javaElementA;
//		}
//
//		if (md.getBody() != null) {
//
//			statList = md.getBody().statements();
//			if (statList == null || statList.size() == 0) {
//				PrintOutput
//						.write(ClazzUtil.getUnitClazzName(iMethod
//								.getCompilationUnit())
//								+ "."
//								+ iMethod.getElementName()
//								+ " "
//								+ "pequeno na sentença\n", "size");
//				return false;
//			}
//
//			if (statList.size() < 2) {
//				for (Object st : statList) {
//
//					if (st instanceof ForStatement) {
//						return true;
//					} else if (st instanceof IfStatement) {
//						return true;
//					} else if (st instanceof SwitchCase) {
//						return true;
//					} else if (st instanceof SwitchStatement) {
//						return true;
//					} else if (st instanceof TryStatement) {
//						return true;
//					} else if (st instanceof WhileStatement) {
//						return true;
//					} else if (st instanceof DoStatement) {
//						return true;
//					} else if (st instanceof EnhancedForStatement) {
//						return true;
//					}
//					PrintOutput.write(
//							ClazzUtil.getUnitClazzName(iMethod
//									.getCompilationUnit())
//									+ "."
//									+ iMethod.getElementName()
//									+ " "
//									+ "pequeno na sentença sem if\n", "size");
//					return false;
//				}
//			}
//		}
//		return true;
//	}
}
