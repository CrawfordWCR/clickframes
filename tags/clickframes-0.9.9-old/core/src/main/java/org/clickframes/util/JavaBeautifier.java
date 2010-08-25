package org.clickframes.util;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.QualifiedNameExpr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JavaBeautifier {
	public static void beautify(File input, File output) throws IOException,
			ParseException {
		// creates an input stream for the file to be parsed
		FileInputStream in = new FileInputStream(input);

		CompilationUnit cu;
		try {
			// parse the file
			cu = JavaParser.parse(in);

			orderImports(cu);
		} finally {
			in.close();
		}

		// prints the resulting compilation unit to default system output
		System.out.println(cu.toString());
	}

	private static void orderImports(CompilationUnit cu) {
		List<ImportDeclaration> oldImportDeclarations = cu.getImports();

		Collections.sort(oldImportDeclarations,
				new Comparator<ImportDeclaration>() {
					@Override
					public int compare(ImportDeclaration o1,
							ImportDeclaration o2) {
						String fullImportName1 = getFullyQualifiedImport(o1);
						String fullImportName2 = getFullyQualifiedImport(o2);

						return fullImportName1.compareTo(fullImportName2);
					}
				});
	}

	/**
	 * get the full package name as would be printed
	 * 
	 * @param importDeclaration
	 * @return
	 */
	private static String getFullyQualifiedImport(
			ImportDeclaration importDeclaration) {
		NameExpr nameExpr = importDeclaration.getName();

		String buf = nameExpr.getName();

		while (nameExpr instanceof QualifiedNameExpr) {
			nameExpr = ((QualifiedNameExpr) nameExpr).getQualifier();
			buf = nameExpr.getName() + "." + buf;
		}

		return buf;
	}
}