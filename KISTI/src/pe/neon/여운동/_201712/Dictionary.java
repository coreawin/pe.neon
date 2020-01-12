package pe.neon.여운동._201712;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.*;

public class Dictionary {

	private static Dictionary instance = new Dictionary();
	private Map<String, String> asjcMap = new HashMap<String, String>();
	private Map<String, String> ksciipcMap = new HashMap<String, String>();
	private Map<String, double[]> 계수Map = new HashMap<String, double[]>();
	private Set<String> ksciipcExceptSet = new HashSet<String>();
	private Dictionary() {
		loadDic(getClass().getResourceAsStream("/yeo/resource/asjc.txt"), FileType.ASJC);
		loadDic(getClass().getResourceAsStream("/yeo/resource/KSCI_IPC.txt"), FileType.KSCIIPC);
		loadDic(getClass().getResourceAsStream("/yeo/resource/고용_부가가치_반응도_영향력.txt"), FileType.ETC);

	}

	public static Dictionary getInstance() {
		return instance;
	}

	public static void main(String... args) {
		System.out.println(Dictionary.getInstance().findKSCIIPC("H01M8/0228"));
		System.out.println(Dictionary.getInstance().findKSCIIPC("A61K8"));
		System.out.println(Dictionary.getInstance().findKSCIIPC("G03B42"));
		System.out.println(Dictionary.getInstance().findKSCIIPC("A44C23"));
		System.out.println(Dictionary.getInstance().findKSCIIPC("G03B"));
		System.out.println(Dictionary.getInstance().findKSCIIPC("G03B34"));
		System.out.println(Dictionary.getInstance().findKSCIIPC("G03B31"));

		SortedMap<String, String> testSet = new TreeMap<String, String>();
		testSet.put("A01B27/02", "0.6");
		testSet.put("E21D34", "0.3");
		testSet.put("A01G98/23", "0.1");
		System.out.println(Dictionary.getInstance().get계수계산(testSet));
		System.out.println(Dictionary.getInstance().findKSCIIPC("None"));
	}

	private void loadDic(InputStream is, FileType type) {
		BufferedInputStream bis = new BufferedInputStream(is);
		Scanner scan = new Scanner(bis);
		while (scan.hasNext()) {
			String line = scan.nextLine();
			String[] values = null;
			try {
				switch (type) {
					case ASJC:
						values = line.split("\t");
						asjcMap.put(values[0].trim().toUpperCase(), values[1].trim());
						break;
					case KSCIIPC:
						values = line.split("\t");
						if (values.length < 2)
							continue;
						String name = values[0].replaceAll("[;,\"]", " ").replaceAll("\\(.*?\\)", "")
								.replaceAll("\\s{1,}", " ").trim();
						String v1 = values[1].trim().replaceAll("[,\"]", " ").replaceAll("\\(.*?\\)", "")
								.replaceAll("\\s{1,}", " ").trim();
						// System.out.println(v1);
						String[] v1values = v1.split(" ");
						for (String _v1 : v1values) {
							// if(ksciipcMap.containsKey(_v1.trim())) {
							// System.out.println(_v1 +"\t" + name);
							// System.out.println(_v1 +"\t" + ksciipcMap.get(_v1.trim()));
							// System.out.println();
							// }else {
							if ("제외".equals(name.trim())) {
								ksciipcExceptSet.add(_v1.trim().toUpperCase());
							} else {
								ksciipcMap.put(_v1.trim().toUpperCase(), name.trim());
							}
							// }

						}
						break;
					case ETC:
						values = line.split("\t");
						/** 이름은 기호,띄어쓰기등을 제거한다. KSCIS-IPC와 KEY를 맞춰야 하기 때문인데 코드화가 안되어 있기때문 */
						try {
							String 상품명 = values[0].replaceAll("[;,\"]", " ").replaceAll("\\(.*?\\)", "")
									.replaceAll("\\s{1,}", "").trim();
							double 고용계수 = Double.parseDouble(values[1].trim());
							double 부가가치계수 = Double.parseDouble(values[2].trim());
							double 감응도 = Double.parseDouble(values[3].trim());
							double 영향도 = Double.parseDouble(values[4].trim());

							계수Map.put(상품명, new double[] { 고용계수, 부가가치계수, 감응도, 영향도 });
						} catch (Exception e) {
							// 첫라인은 칼럼정보라 이 오류가 나도 무시한다.
							System.err.println("파싱오류 : 첫라인은 무시해도 됨 > " + line);
						}
						break;
					default:
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(line);
				System.exit(-1);
			}
		}
		scan.close();
		System.out.println(계수Map);
	}

	public String getASJC(String asjcCode) {
		return asjcMap.get(asjcCode);
	}

	public String getKSCIIPC(String ipc) {
		return ksciipcMap.get(ipc);
	}

	public String getExceptKSCIIPC(String ipc) {
		return ksciipcMap.get(ipc);
	}

	public double[] get계수계산(Map<String, String> 분포율) {
		/** 순서대로 고용유발계수 부가가치유발계수(반올림) 감응도(반올림) 영향력(반올림)들의 합을 저장 */

		double[] resultTotals = new double[] { 0d, 0d, 0d, 0d };
		for (String _ipc : 분포율.keySet()) {

		}
		double[] totals = new double[] { 0d, 0d, 0d, 0d };
//		System.out.println("분포율.keySet() " + 분포율.keySet());
		for (String _ipc : 분포율.keySet()) {
			if("none".equalsIgnoreCase(_ipc.trim())) continue;
			char fChat = _ipc.toUpperCase().charAt(0);
			String productName = _ipc;
			if ((int)fChat >= (int)'A' && (int)fChat <= (int)'Z') {
				/* 알파벳으로 시작하면 IPC코드가 들어온거다. */
				productName = findKSCIIPC(_ipc);
			}

//			System.out.println("상품명 > " + productName);
			if (productName == null || "".equalsIgnoreCase(productName)) {
				continue;
			} else {
				String reProductName = productName.replaceAll("\\s", "").replaceAll("[,\";]", "");
				if (this.계수Map.containsKey(reProductName)) {
					double[] _value = get계수정보(reProductName);
					for (int idx = 0; idx < totals.length; idx++) {
						totals[idx] += (Double.parseDouble(분포율.get(_ipc)) * _value[idx]);
					}
//					System.out.println("상품명 항목 계산 완료 > " + productName);
				}
			}
		}
//		System.out.println("순서대로 고용유발계수	부가가치유발계수(반올림)	감응도(반올림)	영향력(반올림)");
//		for (double _d : totals) {
//			System.out.println("값 확인 " + _d);
//		}
		return totals;
	}

	public double[] get계수정보(String 상품명) {
		return this.계수Map.get(상품명);
	}

	public String findKSCIIPC(String ipc) {
		String result = "";
		if (ipc == null) {
			return "";
		}

		int ipcLength = ipc.length();
		while (ipcLength >= 4) {
			// System.out.println(ipc);
			result = Dictionary.getInstance().getKSCIIPC(ipc);
			if (result == null) {
				ipc = ipc.substring(0, ipc.length() - 1);
				ipcLength = ipc.length();
				continue;
			}
			break;
		}

		return result;
	}

	enum FileType {
		ASJC, KSCIIPC, ETC
	}

}
