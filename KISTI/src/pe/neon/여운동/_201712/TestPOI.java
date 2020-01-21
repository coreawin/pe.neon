package pe.neon.여운동._201712;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;

import java.io.*;
import java.util.LinkedList;

public class TestPOI {

	public static XWPFParagraph tableParagraph(XWPFTableCell tableCell, String value, boolean bold, int fontsize, boolean alignCenter) {
//		tableCell.setVerticalAlignment(XWPFVertAlign.CENTER);
		XWPFParagraph p1 = tableCell.getParagraphs().get(0);
		if(alignCenter) {
			p1.setAlignment(ParagraphAlignment.CENTER);
		}
		XWPFRun r1 = p1.createRun();
		r1.setBold(bold);
		r1.setText(value);
		r1.setFontSize(fontsize);
		r1.setFontFamily("나눔고딕");
		return p1;
	}
	public static XWPFParagraph tableParagraph1(XWPFTableCell tableCell, String value, boolean bold, int fontsize, boolean alignCenter) {
//		tableCell.setVerticalAlignment(XWPFVertAlign.CENTER);
		XWPFParagraph p1 = tableCell.getParagraphs().get(0);
		if(alignCenter) {
			p1.setAlignment(ParagraphAlignment.CENTER);
		}
		XWPFRun r1 = p1.createRun();
		r1.setColor("FFFFFF");
		r1.setBold(bold);
		r1.setText(value);
		r1.setFontSize(fontsize);
		r1.setFontFamily("나눔고딕");
		return p1;
	}

	public static XWPFParagraph tableParagraph(XWPFTableCell tableCell, String value, boolean bold, boolean alignCenter) {
		return tableParagraph(tableCell, value, bold, 10, alignCenter);
	}
	public static XWPFParagraph tableParagraph(XWPFTableCell tableCell, String value, boolean bold, int fontsize) {
		return tableParagraph(tableCell, value, bold, 10, false);
	}

	public static XWPFParagraph tableParagraph(XWPFTableCell tableCell, String value, boolean bold) {
		return tableParagraph(tableCell, value, bold, 10, false);
	}

	public static void main(String[] args) throws Exception {

		String mainColor = "5B9BD5";
		String subColor = "DEEAF6";

//		String mainColor = "70AD47";
//		String subColor = "E2EFD9";



		File file = new File("//tsclient/F/Documents/Project/2017/KISTI-글로벌학술특허정보분석플랫폼-이관재/여운동/흡수형/");
		File[] files = file.listFiles();

		// Blank Document
		XWPFDocument document = new XWPFDocument();

		// Write the Document in file system
		FileOutputStream out = new FileOutputStream(new File("흡수형.doc"));

		for (File f : files) {
			// System.out.println(f.getName());

			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String line = null;
			String name = f.getName();
			name = name.substring(name.indexOf("_") + 1, name.lastIndexOf("."));


			LinkedList<String> al = new LinkedList<String>();
			while ((line = br.readLine()) != null) {
				al.add(line);
			}

			if(al.size() < 2) {
				System.out.println("==> "+ name);
				continue;
			}
			System.out.println(name);
			// create table
			XWPFTable table = document.createTable();

			// create first row
			XWPFTableRow tableRowOne = table.getRow(0);
			tableRowOne.getCell(0).setColor(mainColor);
			tableRowOne.addNewTableCell().setText("");
			tableRowOne.addNewTableCell().setText("");
			tableParagraph1(tableRowOne.getCell(1), name, true, 16, true);
			tableRowOne.getCell(1).setVerticalAlignment(XWPFVertAlign.CENTER);
			tableRowOne.getCell(1).setColor(mainColor);
			tableRowOne.getCell(2).setColor(mainColor);

			// create second row
			XWPFTableRow tableRowTwo = table.createRow();
			tableRowTwo.getCell(0).setColor(subColor);
			tableRowTwo.getCell(1).setColor(subColor);
			tableRowTwo.getCell(2).setColor(subColor);
			tableParagraph(tableRowTwo.getCell(0), "기술어", true, true);
			tableParagraph(tableRowTwo.getCell(1), "상위(확장)기술", true, true);
			tableParagraph(tableRowTwo.getCell(2), "국가유사도", true, true);

			int cnt = 0;
			for(String line2 : al) {
				String[] values = line2.split("\t");
				if(cnt>0) {
					// create third row
					XWPFTableRow tableRowThree = table.createRow();
					if(cnt%2==0) {
						tableRowThree.getCell(0).setColor(subColor);
						tableRowThree.getCell(1).setColor(subColor);
						tableRowThree.getCell(2).setColor(subColor);
					}
					tableParagraph(tableRowThree.getCell(0), values[0], true);
					tableParagraph(tableRowThree.getCell(1), values[1], false, 9);
					tableParagraph(tableRowThree.getCell(2), values[2], false);
				}
				cnt ++;
			}
			// create paragraph
			XWPFParagraph paragraph = document.createParagraph();
			XWPFRun r = paragraph.createRun();
			r.addCarriageReturn();

			paragraph = document.createParagraph();
			paragraph.setPageBreak(true);
			br.close();
//			break;
		}

		document.write(out);
		out.close();
		System.out.println("create_table.docx written successully");
	}
}