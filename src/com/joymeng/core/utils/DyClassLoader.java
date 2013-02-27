package com.joymeng.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class DyClassLoader extends ClassLoader {

	ArrayList<String> classpath = new ArrayList<String>();

	/**
     * 
     */
	public DyClassLoader() {
		super(DyClassLoader.class.getClassLoader());
	}

	/**
	 * ������·��
	 * 
	 * @param s
	 */
	public void addClassPath(String s) {
		classpath.add(s);
	}

	/**
	 * ���������·��
	 */
	public void clearClassPath() {
		classpath.clear();
	}

	/**
	 * ������·���¼�����
	 * 
	 * @param className
	 * @return
	 */
	public Class<?> loadFromCustomRepository(String className) {
		byte[] classBytes = null;

		for (String dir : classpath) {

			// replace '.' in the class name with File.separatorChar & append
			// .class to the name
			String classFileName = className.replace('.', File.separatorChar);
			classFileName += ".class";
			try {
				File file = new File(dir + File.separatorChar + classFileName);
				if (file.exists()) {
					InputStream is = new FileInputStream(file);
					/** ���ļ������ֽ��ļ� */
					classBytes = new byte[is.available()];
					is.read(classBytes);
					break;
				}
			} catch (IOException ex) {
				System.out
						.println("IOException raised while reading class file data");
				ex.printStackTrace();
				return null;
			}
		}

		return this.defineClass(className, classBytes, 0, classBytes.length);// ������

	}

	/**
	 * 
	 * @param className
	 * @return
	 */
	public Class<?> loadFromSysAndCustomRepository(String className) {
		/** ȡ�������� */
		String classPath = System.getProperty("java.class.path");
		List<String> classRepository = new ArrayList<String>();
		/** ȡ�ø�·���µ������ļ��� */
		if ((classPath != null) && !(classPath.equals(""))) {
			StringTokenizer tokenizer = new StringTokenizer(classPath,
					File.pathSeparator);
			while (tokenizer.hasMoreTokens()) {
				classRepository.add(tokenizer.nextToken());
			}
		}
		Iterator<String> dirs = classRepository.iterator();
		byte[] classBytes = null;
		/** ����·���ϲ��Ҹ����Ƶ����Ƿ���ڣ���������ڼ������� */
		while (dirs.hasNext()) {
			String dir = (String) dirs.next();
			// replace '.' in the class name with File.separatorChar & append
			// .class to the name
			String classFileName = className.replace('.', File.separatorChar);
			classFileName += ".class";
			try {
				File file = new File(dir + File.separatorChar + classFileName);
				if (file.exists()) {
					InputStream is = new FileInputStream(file);
					/** ���ļ������ֽ��ļ� */
					classBytes = new byte[is.available()];
					is.read(classBytes);
					break;
				}
			} catch (IOException ex) {
				System.out
						.println("IOException raised while reading class file data");
				ex.printStackTrace();
				return null;
			}
		}
		return this.defineClass(className, classBytes, 0, classBytes.length);// ������

	}
}