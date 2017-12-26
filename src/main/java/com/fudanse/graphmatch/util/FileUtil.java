package com.fudanse.graphmatch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;

public class FileUtil {
	
	public static CompilationUnit openCU(String filePath) {
		CompilationUnit cu = null;

		FileInputStream in = null;
		try {
			in = new FileInputStream(filePath);
			cu = JavaParser.parse(in); // 解析为语法树
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cu;
	}
	
	public static CompilationUnit openCU(File file) {
		CompilationUnit cu = null;

		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			cu = JavaParser.parse(in); // 解析为语法树
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cu;
	}

}
