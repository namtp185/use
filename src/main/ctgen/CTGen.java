package ctgen;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.tzi.use.parser.use.USECompiler;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.MMultiplicity;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.ocl.type.Type;

public class CTGen {
	public static void main(String args[]) {
		MModel model = null;

		if (args.length != 2) {
			System.out.println("the program need 2 arguments, input file and output file path");
			System.exit(1);
		}
		String fileInput = args[0];
		String fileOutput = args[1];
		
		// read input spec
		try  {
			FileInputStream iStream = new FileInputStream(fileInput);
			model = USECompiler.compileSpecification(iStream, fileInput, new PrintWriter(System.err), new ModelFactory());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (model == null) {
			System.exit(1);
		}
		
		generateClassifyingTerms(model, fileOutput);
		System.out.println("Generate complete");
	}
	
	private static void generateClassifyingTerms(MModel model, String fileName) {
		// Generate classifying terms for each attribute
		Map<MAttribute, List<DynamicExpression>> classifyingTerms = new LinkedHashMap<MAttribute, List<DynamicExpression>>();
		Map<MAssociationEnd, List<DynamicExpression>> classifyingTerms2 = new LinkedHashMap<MAssociationEnd, List<DynamicExpression>>();
		
		// 
		// Browse classes and attributes and add them to the map
		Collection<MClass> cls = model.classes();
		for(MClass mcls : cls) {
			String className = mcls.name();
			String pattern = "%s.allInstances->exists(%s | %s.%s%s)";
			Collection<MAttribute> attrs = mcls.allAttributes();
			for(MAttribute mattr: attrs) {
				Type t = mattr.type();
				String attrName = mattr.name();
				if(t.isTypeOfString()) {
					// null or non-null
					String[] conditions = new String[] {" = ''", " <> ''"};
					List<DynamicExpression> expressions = computeExpressions(className, attrName, conditions, pattern);
					classifyingTerms.put(mattr, expressions);
				} else if (t.isTypeOfInteger()) {
					String[] conditions = new String []{" = 0 ", " = 1 ", " > 1 "};
					List<DynamicExpression> expressions = computeExpressions(className, attrName, conditions, pattern);
					classifyingTerms.put(mattr, expressions);
				} else {
					
				}
			}
		}
		// Browse association and add them to the map
		Collection<MAssociation> assocs = model.associations();
		for(MAssociation mAssoc : assocs) {
			String pattern = "%s.allInstances->exists(%s | %s.%s -> size()%s)";
			
			MAssociationEnd mAssocEnd1 = mAssoc.associationEnds().get(0);
			MAssociationEnd mAssocEnd2 = mAssoc.associationEnds().get(1);

			String className1 = mAssocEnd1.cls().name();
			String className2 = mAssocEnd2.cls().name();
			

			String roleName1 = mAssocEnd1.nameAsRolename();	
			MMultiplicity mul1 = mAssocEnd1.multiplicity();
			int lower1 = mul1.getRanges().get(0).getLower();
			int upper1 = mul1.getRanges().get(0).getUpper();
			
			String roleName2 = mAssocEnd2.nameAsRolename();	
			MMultiplicity mul2 = mAssocEnd2.multiplicity();
			int lower2 = mul2.getRanges().get(0).getLower();
			int upper2 = mul2.getRanges().get(0).getUpper();
			
			
			String[] conditions1 = generateConditionsForAssCT(lower1, upper1);
			List<DynamicExpression> expressions1 = computeExpressions(className2, roleName1, conditions1, pattern);
			classifyingTerms2.put(mAssocEnd1, expressions1);
			
			String[] conditions2 = generateConditionsForAssCT(lower2, upper2);
			List<DynamicExpression> expressions2 = computeExpressions(className1, roleName2, conditions2, pattern);
			classifyingTerms2.put(mAssocEnd2, expressions2);
			
		}
		
		
		
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(fileName);
			// generate classifying terms for each attribute
			writer.println("Attribute");
			for(Map.Entry<MAttribute, List<DynamicExpression>> item: classifyingTerms.entrySet()) {
//				out.println("Attribute " + item.getKey().qualifiedName());
				for(DynamicExpression expression: item.getValue()) {
					writer.println(expression.toString());
				}
				writer.println();
			}
			// generate classifying terms for each association
			writer.println("-------------------------------------------------------------------------------");
			writer.println("Association");
			for(Map.Entry<MAssociationEnd, List<DynamicExpression>> item: classifyingTerms2.entrySet()) {
//				out.println("Association " + item.getKey().association().name() + ", association end " + item.getKey().name());
				for(DynamicExpression expression: item.getValue()) {
					writer.println(expression.toString());
				}
				writer.println();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}
	
	private static String[] generateConditionsForAssCT(int lower, int upper) {
		String[] conditions = new String[0];
		if (lower == 1 && upper == 1) {
			conditions = new String[] {" = 1 "};
		} else if (lower == 0 && upper == 1) {
			conditions = new String[] {" = 0 ", " = 1 "};
		} else if (lower == 0 && upper == MMultiplicity.MANY) {
			conditions = new String[] {" = 0 ", " = 1 ", " > 1 "};
		} else if (lower == 1 && upper == MMultiplicity.MANY) {
			conditions = new String[] {" = 1 ", " = 2 ", " > 2 "};
		} else if (lower >= 2) {
			if (upper == MMultiplicity.MANY) upper = lower + 5;
			conditions = new String[] {String.format(" = %s ", lower), String.format(" = %s ", upper)};
		}
		return conditions;
	}
	
	private static List<DynamicExpression> computeExpressions(String className, String attributeName, String[] conditions, String pattern) {
		List<DynamicExpression> result = new ArrayList<>();
		
		String refName = className.substring(0, 1).toLowerCase();
		for (String condition: conditions) {
			DynamicExpression d = new DynamicExpression(String.format(pattern, className, refName, refName, attributeName, condition));
			result.add(d);
		}
		
		return result;
	}
	
}
