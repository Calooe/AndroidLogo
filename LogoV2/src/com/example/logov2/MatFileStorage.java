package com.example.logov2;

import java.io.File;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class MatFileStorage {

	final static String TAG = "[MatFileStorage]";

	public static final int READ = 0;
	public static final int WRITE = 1;

	// File elements
	private File file = null;
	private boolean isWrite = false;
	private Document doc = null;
	private Element rootElement = null;

	public MatFileStorage() {
	}

	// READING only
	public void open(String filePath) {
		try {
			file = new File(filePath);
			if (file == null || file.isFile() == false) {
				throw new Exception("Fichier Introuvable : " + filePath);
			} else {
				isWrite = false;
				doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
				doc.getDocumentElement().normalize();
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	// WRITING only
	public void create(String filePath) {
		try {
			file = new File(filePath);
			if (file == null) {
				Log.e(TAG, "Erreur d'écriture du fichier : " + filePath);
			} else {
				isWrite = true;
				doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				rootElement = doc.createElement("opencv_storage");
				doc.appendChild(rootElement);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	public Mat readMat(String tag) throws Exception {
		if (isWrite) {
			Log.e(TAG, "Fichier ouvert en écriture");
			return null;
		} else {

			NodeList nodelist = doc.getElementsByTagName(tag);
			Mat readMat = null;

			for (int i = 0; i < nodelist.getLength(); i++) {
				Node node = nodelist.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;

					String type_id = element.getAttribute("type_id");
					if ("opencv-matrix".equals(type_id) == false) {
						System.out.println("Fault type_id ");
					}

					String rowsStr = element.getElementsByTagName("rows").item(0).getTextContent();
					String colsStr = element.getElementsByTagName("cols").item(0).getTextContent();
					String dtStr = element.getElementsByTagName("dt").item(0).getTextContent();
					String dataStr = element.getElementsByTagName("data").item(0).getTextContent();

					int rows = Integer.parseInt(rowsStr);
					int cols = Integer.parseInt(colsStr);
					int type = CvType.CV_8U;

					Scanner s = new Scanner(dataStr);

					if ("f".equals(dtStr)) {
						type = CvType.CV_32F;
						readMat = new Mat(rows, cols, type);
						float fs[] = new float[1];
						for (int r = 0; r < rows; r++) {
							for (int c = 0; c < cols; c++) {
								if (s.hasNextFloat()) {
									fs[0] = s.nextFloat();
								} else {
									throw new Exception("Valeur non recoonu - ligne = " + r + " colonne = " + c);
								}
								readMat.put(r, c, fs);
							}
						}
					} else if ("i".equals(dtStr)) {
						type = CvType.CV_32S;
						readMat = new Mat(rows, cols, type);
						int is[] = new int[1];
						for (int r = 0; r < rows; r++) {
							for (int c = 0; c < cols; c++) {
								if (s.hasNextInt()) {
									is[0] = s.nextInt();
								} else {
									throw new Exception("Valeur non recoonu - ligne = " + r + " colonne = " + c);
								}
								readMat.put(r, c, is);
							}
						}
					} else if ("s".equals(dtStr)) {
						type = CvType.CV_16S;
						readMat = new Mat(rows, cols, type);
						short ss[] = new short[1];
						for (int r = 0; r < rows; r++) {
							for (int c = 0; c < cols; c++) {
								if (s.hasNextShort()) {
									ss[0] = s.nextShort();
								} else {
									throw new Exception("Valeur non recoonu - ligne = " + r + " colonne = " + c);
								}
								readMat.put(r, c, ss);
							}
						}
					} else if ("b".equals(dtStr)) {
						readMat = new Mat(rows, cols, type);
						byte bs[] = new byte[1];
						for (int r = 0; r < rows; r++) {
							for (int c = 0; c < cols; c++) {
								if (s.hasNextByte()) {
									bs[0] = s.nextByte();
								} else {
									throw new Exception("Valeur non recoonu - ligne = " + r + " colonne = " + c);
								}
								readMat.put(r, c, bs);
							}
						}
					}
				}
			}

			return readMat;
		}
	}

	public void writeMat(String tag, Mat mat) throws Exception {

		if (isWrite == false) {
			Log.e(TAG, "Fichier ouvert en lecture");
		} else {
			try {
				Element matrix = doc.createElement(tag);
				matrix.setAttribute("type_id", "opencv-matrix");
				rootElement.appendChild(matrix);

				Element rows = doc.createElement("rows");
				rows.appendChild(doc.createTextNode(String.valueOf(mat.rows())));

				Element cols = doc.createElement("cols");
				cols.appendChild(doc.createTextNode(String.valueOf(mat.cols())));

				Element dt = doc.createElement("dt");
				String dtStr = null;
				int type = mat.type();

				switch (type) {
				case CvType.CV_32F:
					dtStr = "f";
					break;
				case CvType.CV_32S:
					dtStr = "i";
					break;
				case CvType.CV_16S:
					dtStr = "s";
					break;
				case CvType.CV_8U:
					dtStr = "b";
					break;
				}
				dt.appendChild(doc.createTextNode(dtStr));

				Element data = doc.createElement("data");
				String dataStr = dataStringBuilder(mat);
				data.appendChild(doc.createTextNode(dataStr));

				// append all to matrix
				matrix.appendChild(rows);
				matrix.appendChild(cols);
				matrix.appendChild(dt);
				matrix.appendChild(data);

			} catch (Exception e) {
				throw e;
			}
		}
	}

	private String dataStringBuilder(Mat mat) {
		StringBuilder sb = new StringBuilder();
		int rows = mat.rows();
		int cols = mat.cols();
		int type = mat.type();

		if (type == CvType.CV_32F) {
			float fs[] = new float[1];
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					mat.get(r, c, fs);
					sb.append(String.valueOf(fs[0]));
					sb.append(' ');
				}
				sb.append('\n');
			}
		} else if (type == CvType.CV_32S) {
			int is[] = new int[1];
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					mat.get(r, c, is);
					sb.append(String.valueOf(is[0]));
					sb.append(' ');
				}
				sb.append('\n');
			}
		} else if (type == CvType.CV_16S) {
			short ss[] = new short[1];
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					mat.get(r, c, ss);
					sb.append(String.valueOf(ss[0]));
					sb.append(' ');
				}
				sb.append('\n');
			}
		} else if (type == CvType.CV_8U) {
			byte bs[] = new byte[1];
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					mat.get(r, c, bs);
					sb.append(String.valueOf(bs[0]));
					sb.append(' ');
				}
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	public void release() throws Exception {
		try {
			if (isWrite == false) {
				throw new Exception("Fichier ouvert en lecture");
			}

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);

			// Write to the XML file
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			// Indentation du fichier
			transformer.transform(source, result);
		} catch (Exception e) {
			throw e;
		}
	}
}
