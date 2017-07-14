/*
Copyright Hiroki Ueda

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package jp.ac.utokyo.karkinos.cmdutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jp.ac.utokyo.rcast.karkinos.exec.CopyNumberInterval;

public class CnvAndHBVinteg {

	public static void main(String[] arg) throws Exception {

		List<File> list = new ArrayList<File>();
		//

		String chrArm = "/GLUSTER_DIST/data/users/ueda/genome/hg19ChrArm.txt";

		List<ChrArmBean> armlist = getChrArm(chrArm);

	//	String indir = "/GLUSTER_DIST/data/users/ueda/ICGC/karkinosResult/thcc";
		
		String indir = "/GLUSTER_DIST/data/users/ueda/ICGC/karkinosResult/thcc,"
				+ "/GLUSTER_DIST/data/users/ueda/ICGC/karkinosResult/ncc,"
				+ "/GLUSTER_DIST/data/users/ueda/ICGC/karkinosResult/goss,"
				+ "/GLUSTER_DIST/data/users/ueda/ICGC/karkinosResult/hcc-jp";
//		
//		String indir =  "/GLUSTER_DIST/data/users/ueda/ICGC/karkinosResult/goss,"
//			+ "/GLUSTER_DIST/data/users/ueda/ICGC/karkinosResult/hcc-jp";
		
		
		
		String out ="/GLUSTER_DIST/data/users/ueda/ICGC/hbvintegjunc.txt";
		FileWriter fw = new FileWriter(new File(out));
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
		
		for (String s : indir.split(",")) {
			File f = new File(s);
			// chage to reccursive file search
			searchRecursive(f, list);
		}
		for (String s : indir.split(",")) {
			File f = new File(s);
			// chage to reccursive file search
			searchRecursive2(f, list);
		}
		
		Set<String> s = new LinkedHashSet<String>();
		for (File f : list) {

			put(f,s);			

		}
		//
		
		for(String ss:s){
			pw.println(ss);
		}		
		pw.close();
		
	}

	private static void put(File f, Set<String> set) throws Exception {
		
		
		String sampleid = (f.getName().replaceAll("_cnvdata.vcf", ""));
		sampleid = (sampleid.replaceAll("_textdata.txt", ""));
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		String s = null;
		while ((s = br.readLine()) != null) {

		
			//
			if (s.endsWith("fromDepth")) {
				
				String[] sa = s.split("\t");
				String chr = sa[0];
				int start = Integer.parseInt(sa[1].replaceAll(",",""));
				int end = Integer.parseInt(sa[2].replaceAll(",",""));
				
				String id = sampleid+"\t"+chr+"\t"+start;
				String id2 = sampleid+"\t"+chr+"\t"+end;
				
				//
				set.add(id);
				set.add(id2);

			}

		}
		//
		
		br.close();
		
	}

	private static void print(File f) throws Exception {
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		String s = null;
		String samplename = f.getName().replaceAll("_cnvdata.vcf", "");
		
		while ((s = br.readLine()) != null) {

		
			//
			if (s.endsWith("fromDepth")) {

				String[] sa = s.split("\t");
				//System.out.println(s);
				//
				int st = Integer.parseInt(sa[1].replaceAll(",", ""));
				int ed = Integer.parseInt(sa[2].replaceAll(",", ""));
			}

		}
		//
		
		br.close();
		
	}

	private static List<ChrArmBean> getChrArm(String chrArm) throws Exception {

		//
		List<ChrArmBean> armlist = new ArrayList<ChrArmBean>();
		
		BufferedReader br = new BufferedReader(new FileReader(chrArm));
		String s = null;
		while ((s = br.readLine()) != null) {
			
			//
			String[] sa = s.split("\t");
			String chr = sa[0];
			int start = Integer.parseInt(sa[2]);
			int end = Integer.parseInt(sa[3]);
			
			ChrArmBean bean = new ChrArmBean();
			bean.setChr(chr);
			bean.setStart(start);
			bean.setEnd(end);
			armlist.add(bean);
		}		
		return armlist;
		
	}

	private static void printInfo(File f,List<ChrArmBean> armlist) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(f));
		String s = null;
		System.out.print(f.getName().replaceAll("_cnvdata.vcf", ""));
		int countcnv = 0;

		float tcused = 0;
		float ploidy = 0;
		List<ChrArmBean> cnvlist = new ArrayList<ChrArmBean>();
		
		while ((s = br.readLine()) != null) {

			if (s.startsWith("tc used")) {
				String[] sa = s.split("\t");
				tcused = Float.parseFloat(sa[1]);
			}
			if (s.startsWith("ploidy")) {
				String[] sa = s.split("\t");
				ploidy = Float.parseFloat(sa[1]);
			}

			//
			if (s.endsWith("fromDepth")) {

				String[] sa = s.split("\t");
				//System.out.println(s);
				//
				int st = Integer.parseInt(sa[1].replaceAll(",", ""));
				int ed = Integer.parseInt(sa[2].replaceAll(",", ""));
				
				float cn = Float.parseFloat(sa[3].replaceAll("n=", ""));
				if(ploidy<cn){
					ploidy=cn;
				}
				
				ChrArmBean bean = new ChrArmBean();
				bean.setChr(sa[0]);
				bean.setStart(st);
				bean.setEnd(ed);
				bean.setCn((int)cn);
				cnvlist.add(bean);

			}

		}
		//
		int countcnv2 = getOverCNVCnt(cnvlist,armlist,ploidy>5);		
		
		System.out.println("\t" + tcused + "\t" + ploidy + "\t" + countcnv2);
		br.close();

	}

	private static int getOverCNVCnt(List<ChrArmBean> cnvlist,
			List<ChrArmBean> armlist,boolean triploid) {
		
		int cnt = 0;
		
		for(ChrArmBean arm:armlist){
			
			if(arm.getChr().contains("6")){
				if(arm.getStart() > 100){
					//System.out.println("here");
				}
			}
			int overraplen = 0;
			//
			for(ChrArmBean cnvbean:cnvlist){
				
				//
				if(cnvbean.getChr().contains("6")){
					//System.out.println("here");
				}
				int ollen = overlap(arm,cnvbean,triploid);
				if( ollen > 0){
					
					//
					overraplen = overraplen + ollen;
					
				}
				
			}
			int len = Math.abs(arm.getEnd()-arm.getStart());
			double r = (double)overraplen/(double)len;
			if(r>0.8){
				//System.out.println(arm.getChr()+"\t"+arm.getStart());
				cnt++;
			}
			
		}
		return cnt;
		
	}

	private static int overlap(ChrArmBean arm, ChrArmBean cnvbean,boolean triploid) {
		
		String chr0 =  cnvbean.chr;
		if(!chr0.contains("chr")){
			chr0 = "chr"+chr0;
		}
		
		if(arm.chr.equals(chr0)){
			
			//
			int cn = cnvbean.getCn();
			
			if(arm.end>=cnvbean.start){
				if(arm.start<=cnvbean.end){
					//
					int s = Math.max(arm.start, cnvbean.start);
					int e = Math.min(arm.end, cnvbean.end);
					if(triploid){
						if(cn==4)return 0;
					}else{
						if(cn==2)return 0;
					}					
					return e-s;
				}				
			}
			
		}		
		return 0;
	}

	private static boolean searchRecursive(File f, final List<File> list) {

		File[] listFiles = f.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {

				if (name.startsWith("."))
					return false;
				boolean target = dir.isDirectory()
						|| name.contains("textdata.txt");
				if (!target)
					return false;
				//
				String absPath = dir.getAbsolutePath() + File.separator + name;
				if (new File(absPath).isFile()) {
					return true;
				} else {
					return searchRecursive(new File(absPath), list);
				}
			}

		});

		if (listFiles == null)
			return false;
		for (File ff : listFiles) {
			if (ff.isFile() && ff.getName().contains("textdata.txt")) {

				// System.out.println(ff.getName());
				list.add(ff);
			}
		}

		return true;

	}
	
	private static boolean searchRecursive2(File f, final List<File> list) {

		File[] listFiles = f.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {

				if (name.startsWith("."))
					return false;
				boolean target = dir.isDirectory()
						|| name.contains("cnvdata.vcf");
				if (!target)
					return false;
				//
				String absPath = dir.getAbsolutePath() + File.separator + name;
				if (new File(absPath).isFile()) {
					return true;
				} else {
					return searchRecursive(new File(absPath), list);
				}
			}

		});

		if (listFiles == null)
			return false;
		for (File ff : listFiles) {
			if (ff.isFile() && ff.getName().contains("cnvdata.vcf")) {

				// System.out.println(ff.getName());
				list.add(ff);
			}
		}

		return true;

	}

}
