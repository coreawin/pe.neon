package pe.neon.여운동._201712;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class ExcelReport {

	Workbook xlsWb = new XSSFWorkbook(); // Excel 2007 이상
	boolean isSCOPUS = false;

	public ExcelReport(boolean isSCOPUS) {
		this.isSCOPUS = isSCOPUS;
	}

	public static void main(String[] args) {

	}

	/**
	 * 빈도수 정보를 넣는다.<br>
	 * 엑셀의 시트명과 첫번째 칼럼명이 고정이다.<br>
	 *
	 * @param data
	 *
	 */
	public void createExcelSheetForCount(Map<Integer, Integer> data) {
		// Sheet 생성
		String title = "연도별 논문수";
		if (!isSCOPUS) {
			title = "연도별 특허수";
		}
		Sheet sheet1 = xlsWb.createSheet(title);
		CellStyle cellStyle = xlsWb.createCellStyle();
		cellStyle.setWrapText(true);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		Set<Integer> set = data.keySet();
		int rowIDX = 0;
		int cellIDX = 0;

		Row row = null;
		Cell cell = null;
		row = sheet1.createRow(rowIDX++);
		cell = row.createCell(cellIDX++);
		cell.setCellValue("YEAR");
		cell.setCellStyle(cellStyle);
		cell = row.createCell(cellIDX++);
		cell.setCellValue("Count");
		cell.setCellStyle(cellStyle);
		cellIDX = 0;
		for (Integer i : set) {
			int value = data.get(i);
			cellIDX = 0;
			row = sheet1.createRow(rowIDX++);
			cell = row.createCell(cellIDX++);
			cell.setCellValue(i);
			cell.setCellStyle(cellStyle);
			cell = row.createCell(cellIDX++);
			cell.setCellValue(value);
			cell.setCellStyle(cellStyle);
		}

	}

	/**
	 * 엑셀 시트를 생성하여 빈도수를 입력한다.
	 *
	 * @param sheetName
	 *            시트명
	 * @param data
	 *            출력 데이터
	 * @param excelFirstColumnNames
	 *            엑셀 첫번째 칼럼 제목
	 */
	public void createExcelSheetForCountCustom(String sheetName, Map<String, String> data,
											   String[] excelFirstColumnNames) {
		// Sheet 생성
		Sheet sheet1 = xlsWb.createSheet(sheetName);
		CellStyle cellStyle = xlsWb.createCellStyle();
		cellStyle.setWrapText(true);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		Set<String> set = data.keySet();
		int rowIDX = 0;
		int cellIDX = 0;

		Row row = null;
		Cell cell = null;
		row = sheet1.createRow(rowIDX++);

		for (String _ti : excelFirstColumnNames) {
			cell = row.createCell(cellIDX++);
			cell.setCellValue(_ti);
			cell.setCellStyle(cellStyle);
		}
		cellIDX = 0;
		for (String i : set) {
			String value = data.get(i);
			cellIDX = 0;
			row = sheet1.createRow(rowIDX++);
			cell = row.createCell(cellIDX++);
			cell.setCellValue(i);
			cell.setCellStyle(cellStyle);
			cell = row.createCell(cellIDX++);
			cell.setCellValue(Integer.parseInt(value));
			cell.setCellStyle(cellStyle);
		}

	}

	/**
	 * 엑셀 시트를 생성하여 빈도수를 입력한다.
	 *
	 * @param sheetName
	 *            시트명
	 * @param data
	 *            출력 데이터
	 * @param excelFirstColumnNames
	 *            엑셀 첫번째 칼럼 제목
	 */
	public void createExcelSheetForCountCustom3(String sheetName, Map<String, String[]> data,
												String[] excelFirstColumnNames) {
		// Sheet 생성
		Sheet sheet1 = xlsWb.createSheet(sheetName);
		CellStyle cellStyle = xlsWb.createCellStyle();
		cellStyle.setWrapText(true);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		Set<String> set = data.keySet();
		int rowIDX = 0;
		int cellIDX = 0;

		Row row = null;
		Cell cell = null;
		row = sheet1.createRow(rowIDX++);

		for (String _ti : excelFirstColumnNames) {
			cell = row.createCell(cellIDX++);
			cell.setCellValue(_ti);
			cell.setCellStyle(cellStyle);
		}
		cellIDX = 0;
		for (String i : set) {
			String[] value = data.get(i);
			cellIDX = 0;
			row = sheet1.createRow(rowIDX++);
			cell = row.createCell(cellIDX++);
			cell.setCellValue(i);
			cell.setCellStyle(cellStyle);
			for (String _value : value) {
				cell = row.createCell(cellIDX++);
				cell.setCellValue(_value);
				cell.setCellStyle(cellStyle);
			}
		}

	}

	/**
	 * 엑셀 시트를 생성하여 소수점 혹은 백분율 데이터를 입력한다.
	 *
	 * @param sheetName
	 *            시트명
	 * @param data
	 *            출력 데이터
	 * @param excelFirstColumnNames
	 *            엑셀 첫번째 칼럼 제목
	 */
	public void createExcelSheetForRateCustom(String sheetName, Map<String, String> data,
											  String[] excelFirstColumnNames) {
		createExcelSheetForRateCustom(sheetName, data, excelFirstColumnNames, "");
	}

	/**
	 * 엑셀 시트를 생성하여 소수점 혹은 백분율 데이터를 입력한다.
	 *
	 * @param sheetName
	 *            시트명
	 * @param data
	 *            출력 데이터
	 * @param excelFirstColumnNames
	 *            엑셀 첫번째 칼럼 제목
	 * @param type
	 *            % 입력시 %비율로 포맷팅 아무것도 입력안하면 소수점 표현.
	 */
	public void createExcelSheetForRateCustom(String sheetName, Map<String, String> data,
											  String[] excelFirstColumnNames, String type) {
		// Sheet 생성
		Sheet sheet1 = xlsWb.createSheet(sheetName);
		CellStyle cellStyle = xlsWb.createCellStyle();
		cellStyle.setWrapText(true);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);

		DataFormat format = xlsWb.createDataFormat();

		// cellStyle.set
		Set<String> set = data.keySet();
		int rowIDX = 0;
		int cellIDX = 0;

		Row row = null;
		Cell cell = null;
		row = sheet1.createRow(rowIDX++);
		for (String _title : excelFirstColumnNames) {
			cell = row.createCell(cellIDX++);
			cell.setCellValue(_title);
			cell.setCellStyle(cellStyle);
		}
		cellIDX = 0;
		for (String i : set) {
			String value = data.get(i);
			cellIDX = 0;
			row = sheet1.createRow(rowIDX++);
			cell = row.createCell(cellIDX++);
			cell.setCellValue(i);
			cell.setCellStyle(cellStyle);

			cellStyle = xlsWb.createCellStyle();
			cellStyle.setWrapText(true);
			cellStyle.setAlignment(HorizontalAlignment.CENTER);

			cell = row.createCell(cellIDX++);
			if ("1.0".trim().equals(value)) {
				cellStyle.setDataFormat(format.getFormat("##" + type));
			} else {
				cellStyle.setDataFormat(format.getFormat("0#.##0" + type));
			}
			cell.setCellValue(Double.parseDouble(value));
			cell.setCellStyle(cellStyle);
		}

	}

	/**
	 * 입력한 데이터의 순서대로 엑셀에 데이터를 쓴다. <br>
	 * 첫번째 데이터 칼러명이 고정되어 있다.<br>
	 *
	 * @param title
	 *            시트명
	 * @param data
	 *            list 문자열은 tab 구분자.
	 */
	public void createExcelSheetForDocumentList(String title, SortedMap<String, LinkedList<String>> data) {
		// Sheet 생성
		Sheet sheet1 = xlsWb.createSheet(title);
		CellStyle cellStyle = xlsWb.createCellStyle();
		cellStyle.setWrapText(true);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		Set<String> set = data.keySet();
		int rowIDX = 0;
		int cellIDX = 0;

		Row row = null;
		Cell cell = null;
		row = sheet1.createRow(rowIDX++);
		if (isSCOPUS) {
			String[] clm = new String[] { "EID", "Title", "Publication Year", "Author Keyword","Index Keyword",
					"Number of Citation", "Country", "Affiliation Name", "Source Title" };
			for (String _c : clm) {
				cell = row.createCell(cellIDX++);
				cell.setCellValue(_c);
				cell.setCellStyle(cellStyle);
			}
		} else {
			String[] clm = new String[] { "제목", "출원번호", "출원년도", "출원국가(특허청 X)" };
			for (String _c : clm) {
				cell = row.createCell(cellIDX++);
				cell.setCellValue(_c);
				cell.setCellStyle(cellStyle);
			}
		}
		cellIDX = 0;
		for (String key : set) {
			LinkedList<String> list = data.get(key);
//			System.out.println(key +"\t" + list.size());
			for (String column : list) {
				row = sheet1.createRow(rowIDX++);
				String[] cls = column.split("\t");
				cellIDX = 0;
				for (String ds : cls) {
					cell = row.createCell(cellIDX++);
					cell.setCellValue(ds.trim());
//					System.out.println(ds.trim());
					// cell.setCellStyle(cellStyle);
				}
//				System.out.println(rowIDX);
			}
		}

	}

	public void writeExcel(String path) {
		// excel 파일 저장
		FileOutputStream fileOut = null;
		try {
			File xlsFile = new File(path);
			fileOut = new FileOutputStream(xlsFile);
			xlsWb.write(fileOut);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileOut != null) {
				try {
					fileOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
