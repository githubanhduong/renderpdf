package Java;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfEncodings;
import com.itextpdf.text.pdf.PdfWriter;

import Entity.Agent;
import Entity.Airline;
import Entity.Commission;

public class RenderPDF {
	public static void main(String[] args) {
		renderPDFAGT(args);
		renderPDFAIR(args);
	}

	public static void renderPDFAGT(String[] args) {
		List<String> nameFiles = new ArrayList<String>();
		
//		File file1 = new File("D:/Aviahub/dw_2020");
//		File file2 = new File("D:/Aviahub/agent_AHoBsp.csv");
//		File file3 = new File("D:/Aviahub/072.Commission.STATS290.txt");
		File file1 = null;
		File file2 = null;
		File file3 = null;
		String filename1 = args[0];
		String filename2 = args[1];
		String filename3 = args[2];
		nameFiles.add(filename1);
		nameFiles.add(filename2);
		nameFiles.add(filename3);
		
		String dest = null;
		for (String nameFile : nameFiles) {
			if (nameFile.substring(nameFile.lastIndexOf("/") + 1).contains("dw")) {
				file1 = new File(nameFile);
			} else if (nameFile.substring(nameFile.lastIndexOf("/") + 1).contains("agent")) {
				file2 = new File(nameFile);
			} else {
				file3 = new File(nameFile);
				String[] parts = nameFile.split("/");
				dest = System.getProperty("user.dir") + "/" + parts[parts.length - 1].split("\\.")[0] + ".DW_DASAGT." + nameFile.split("\\.")[1] + ".imp.pdf";
			}
		}
//		String dest = "C:\\Users\\prima\\Downloads\\072.DW_DASAIR.Commission.imp.pdf; 

		Document document = new Document(PageSize.A4);

//			PdfWriter writer = new PdfWriter(dest);
		try {
			PdfWriter.getInstance(document, new FileOutputStream(dest));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//			exportPDFAir(response, filename1, filename2, filename3);

		List<Commission> listCommission = new ArrayList<Commission>();
		List<Airline> listAirline = new ArrayList<Airline>();
		List<Agent> listAgent = new ArrayList<Agent>();

		if (file3.exists()) {
			try {
				Scanner scanner = new Scanner(file3);

				while (scanner.hasNextLine()) {
					Commission commission = new Commission();
					String line = scanner.nextLine();
					commission.setCodeAirline(line.substring(0, 3)); // "072"
					commission.setIdentify(line.substring(3, 14)); // "00002029612"
					String part3 = line.substring(14, line.length());
					commission.setMoney(part3.replaceFirst("^0+", "")); // Remove leading zeros
					listCommission.add(commission);
				}

				scanner.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (file1.exists()) {

			try {
				Scanner fileScanner = new Scanner(file1);

				while (fileScanner.hasNextLine()) {
					Airline airline = new Airline();
					String lineIAST2901 = fileScanner.nextLine(); // Read the first line

					if (lineIAST2901.contains("IAST2901")) {
						airline.setNameAirline(lineIAST2901.substring(0, 20));
						airline.setCodeAirline(
								lineIAST2901.substring(lineIAST2901.length() - 3, lineIAST2901.length()));
						airline.setAddress1(lineIAST2901.substring(20, 40));
						airline.setAddress2(lineIAST2901.substring(40, lineIAST2901.length() - 11));
					}
					if (!fileScanner.hasNextLine()) {
						break; // If no more lines, break the loop
					}

					String lineIAST2902 = fileScanner.nextLine(); // Read the second line

					if (lineIAST2902.contains("IAST2902")) {
						airline.setTelephone(lineIAST2902.substring(0, 14));
						airline.setIdentify(lineIAST2902.substring(14, 28));
						airline.setYear(Integer.parseInt("20" + lineIAST2902.substring(40, 42)));
						listAirline.add(airline);
					}
				}

				fileScanner.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (file2.exists()) {
			try {
				Scanner fileScannerF2 = new Scanner(file2);

				while (fileScannerF2.hasNextLine()) {
					Agent agent = new Agent();
					String line = fileScannerF2.nextLine();
					agent.setCodeAgent(line.split("\\|")[0]);
					agent.setNomAgent(line.split("\\|")[1]);
					agent.setAddress1(line.split("\\|")[2]);
					agent.setAddress2(line.split("\\|")[3]);
					agent.setAddress3(line.split("\\|")[4]);

					listAgent.add(agent);
				}

				fileScannerF2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Font fontText = FontFactory.getFont(FontFactory.COURIER, 7);
		Font fontTextSubTotal = FontFactory.getFont(FontFactory.COURIER_BOLD, 8);
		ByteArrayOutputStream baosPDF = new ByteArrayOutputStream();

		document.open();
		document.setPageSize(PageSize.A4);
		document.newPage();

		Airline airlineCommision = new Airline();
		Agent agentCommision = new Agent();

		Optional<Airline> matchingAirline = listAirline.stream()
				.filter(airline -> airline.getCodeAirline().equals(listCommission.get(0).getCodeAirline())).findFirst();

		airlineCommision = matchingAirline.get();

		int year = airlineCommision.getYear();
		String identification = airlineCommision.getIdentify();
		String airlineInput = airlineCommision.getNameAirline();
		String adressInput3 = airlineCommision.getAddress2();
		String adressInput4 = airlineCommision.getAddress1();
		String telephone = airlineCommision.getTelephone();

		int i = 1;
		int subTotal = 0;
		NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
		
		for (Commission commission : listCommission) {

			Optional<Agent> matchingAgent = listAgent.stream()
					.filter(agent -> agent.getCodeAgent().equals(commission.getIdentify())).findFirst();

			if (matchingAgent.isPresent()) {
				agentCommision = matchingAgent.get();
			} else {
				agentCommision.setAddress1("");
				agentCommision.setAddress2("");
				agentCommision.setAddress3("");
				agentCommision.setNomAgent("");
			}

			String numberNom = commission.getIdentify();
			String adressInput = agentCommision.getAddress1();
			String adressInput1 = agentCommision.getAddress2();
			String adressInput2 = agentCommision.getAddress3();
			String nomAgentFormat = agentCommision.getNomAgent();

			int money = 0;
			if (!commission.getMoney().isEmpty()) {
				money = Integer.valueOf(commission.getMoney());
			} else {
				money = 0;
			}
			subTotal += money;
			
			String adress = String.format("%-35s", adressInput);
			String adress1 = String.format("%-35s", adressInput1);
			String adress2 = String.format("%-35s", adressInput2);
//				String trimmedNumber = numberNom.replaceFirst("^0+", ""); // Remove leading zeros
			String formattedNumber1 = String.format("%12s", numberNom);
//				String formattedNumber2 = String.format("%8s", trimmedNumber);
			String formattedMoney =  String.format("%12s", numberFormat.format(money));
			String nomAgent = String.format("%-27s",
					nomAgentFormat.substring(0, Math.min(nomAgentFormat.length(), 26)));

			String hyphenString0 = new String(new char[78]).replace('\0', '\u002D');
			String hyphenString1 = new String(new char[06]).replace('\0', '\u002D');
			String hyphenString2 = new String(new char[40]).replace('\0', '\u002D');
			String hyphenString3 = new String(new char[37]).replace('\0', '\u002D');
			String hyphenString4 = new String(new char[17]).replace('\0', '\u002D');

			document.newPage();
			String formattedNumber0 = String.format("%5d", i);

			try {
				document.add(new Phrase(String.valueOf("I M P O T S  " + year + " \u002D "
						+ "ETAT DES HONORAIRES,VACATIONS,COURTAGES,COMMISSIONS              D A S   A G E N C E S   (DAS II)\n"
						+ "                             RISTOURNES ET JETONS DE PRESENCE                       (DECLARATION AGENT)      (EX. 2460-"
						+ "1024)\n" + "                      DROITS D’AUTEUR ET D’INVENTEUR PAYES PENDANT L’ANNEE\n"
						+ ""), fontText));

				document.add(new Phrase(String.valueOf(
						"                                                                                RAISON SOCIALE : "
								+ airlineInput + "\n" + "   LE CADRE CI" + "\u002D"
								+ "APRES EST RESERVE A LA DECLARATION DES SOMMES CI" + "\u002D"
								+ "DESSUS          ADRESSE        :  " + adressInput4 + "\n"
								+ "   VISEES QUI ONT ETE VERSEES A DES PERSONNES N O N  S A L A R I E E S                           "
								+ adressInput3 + "\n"
								+ "                                                                                TELEPHONE      : "
								+ telephone + "\n"
								+ "                                                                                NO POSTE RESPONSABLE :\n"
								+ "                                                                                NO IDENTIFICATION    : "
								+ identification + "\n"),
						fontText));

				document.add(new Phrase(String.valueOf(
						"          ********************************************************************************************************\n"
								+ "           NO   I 	    D E S I G N A T I O N     D E S      B E N E F I C I A I R E S           I MONTANT DES     I\n"
								+ "          ORDRE I" + hyphenString0 + "I COMMISSIONS (5) I\n"
								+ "                I      N    O    M  (1)                  I    A D R E S S E    (3)             I RISTOURNES (EUR)I\n"
								+ "          " + hyphenString1 + "I" + hyphenString2 + "I" + hyphenString3 + "I"
								+ hyphenString4 + "I\n"),
						fontText));

				document.add(new Phrase(String.valueOf("          " + formattedNumber0 + " I" + formattedNumber1 + " "
						+ nomAgent + "I " + adress + " I " + formattedMoney + "    I\n" + "               "
						+ " I                                        " + "I " + adress1 + " I                " + " I\n"
						+ "               " + " I                                        " + "I " + adress2
						+ " I                " + " I\n"
						+ "          ******I****************************************I*************************************I*****************I\n"),
						fontText));
				
				String formattedSubTotal = String.format("%13s", numberFormat.format(subTotal));
				
				document.add(new Phrase(String.valueOf(
						  "                                                                                   "
						+ "I SUBTOTAL      I\n"
						+ "                                                                                   "
						+ "I " + formattedSubTotal + " I\n"
						+ "                                                                                   "
						+ "I               I\n"
						+ "                                                                                   "
						+ "I***************I"),
				fontTextSubTotal));
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			++i;

		}

		document.close();

	}

	public static void renderPDFAIR(String[] args) {
		List<String> nameFiles = new ArrayList<String>();
		
//		File file1 = new File("D:/Aviahub/dw_2020");
//		File file2 = new File("D:/Aviahub/agent_AHoBsp.csv");
//		File file3 = new File("D:/Aviahub/072.Commission.STATS290.txt");
		File file1 = null;
		File file2 = null;
		File file3 = null;
		String filename1 = args[0];
		String filename2 = args[1];
		String filename3 = args[2];
		nameFiles.add(filename1);
		nameFiles.add(filename2);
		nameFiles.add(filename3);
		
		String dest = null;
		for (String nameFile : nameFiles) {
			if (nameFile.substring(nameFile.lastIndexOf("/") + 1).contains("dw")) {
				file1 = new File(nameFile);
			} else if (nameFile.substring(nameFile.lastIndexOf("/") + 1).contains("agent")) {
				file2 = new File(nameFile);
			} else {
				file3 = new File(nameFile);
				String[] parts = nameFile.split("/");
				dest = System.getProperty("user.dir") + "/" + parts[parts.length -1].split("\\.")[0] + ".DW_DASAIR." + nameFile.split("\\.")[1] + ".imp.pdf";
			}
		}

		Document document = new Document(PageSize.A4);

		try {
			PdfWriter.getInstance(document, new FileOutputStream(dest));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		List<Commission> listCommission = new ArrayList<Commission>();
		List<Airline> listAirline = new ArrayList<Airline>();
		List<Agent> listAgent = new ArrayList<Agent>();

		if (file3.exists()) {
			try {
				Scanner scanner = new Scanner(file3);

				while (scanner.hasNextLine()) {
					Commission commission = new Commission();
					String line = scanner.nextLine();
					commission.setCodeAirline(line.substring(0, 3)); // "072"
					commission.setIdentify(line.substring(3, 14)); // "00002029612"
					String part3 = line.substring(14, line.length());
					commission.setMoney(part3.replaceFirst("^0+", "")); // Remove leading zeros
					listCommission.add(commission);
				}

				scanner.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (file1.exists()) {

			try {
				Scanner fileScanner = new Scanner(file1);

				while (fileScanner.hasNextLine()) {
					Airline airline = new Airline();
					String lineIAST2901 = fileScanner.nextLine(); // Read the first line

					if (lineIAST2901.contains("IAST2901")) {
						airline.setNameAirline(lineIAST2901.substring(0, 20));
						airline.setCodeAirline(
								lineIAST2901.substring(lineIAST2901.length() - 3, lineIAST2901.length()));
						airline.setAddress1(lineIAST2901.substring(20, 40));
						airline.setAddress2(lineIAST2901.substring(40, lineIAST2901.length() - 11));
					}
					if (!fileScanner.hasNextLine()) {
						break; // If no more lines, break the loop
					}

					String lineIAST2902 = fileScanner.nextLine(); // Read the second line

					if (lineIAST2902.contains("IAST2902")) {
						airline.setTelephone(lineIAST2902.substring(0, 14));
						airline.setIdentify(lineIAST2902.substring(14, 28));
						airline.setYear(Integer.parseInt("20" + lineIAST2902.substring(40, 42)));
						listAirline.add(airline);
					}
				}

				fileScanner.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (file2.exists()) {
			try {
				Scanner fileScannerF2 = new Scanner(file2);

				while (fileScannerF2.hasNextLine()) {
					Agent agent = new Agent();
					String line = fileScannerF2.nextLine();
					agent.setCodeAgent(line.split("\\|")[0]);
					agent.setNomAgent(line.split("\\|")[1]);
					agent.setAddress1(line.split("\\|")[2]);
					agent.setAddress2(line.split("\\|")[3]);
					agent.setAddress3(line.split("\\|")[4]);

					listAgent.add(agent);
				}

				fileScannerF2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Font fontText = FontFactory.getFont(FontFactory.COURIER, 7);
		Font fontTextSubTotal = FontFactory.getFont(FontFactory.COURIER_BOLD, 8);
		ByteArrayOutputStream baosPDF = new ByteArrayOutputStream();

		document.open();
		document.setPageSize(PageSize.A4);
		document.newPage();

		Airline airlineCommision = new Airline();
		Agent agentCommision = new Agent();

		Optional<Airline> matchingAirline = listAirline.stream()
				.filter(airline -> airline.getCodeAirline().equals(listCommission.get(0).getCodeAirline())).findFirst();

		airlineCommision = matchingAirline.get();

		int year = airlineCommision.getYear();
		String telephone = airlineCommision.getTelephone();
		String identification = airlineCommision.getIdentify();
		String airlineInput = airlineCommision.getNameAirline();
		String adressInput3 = airlineCommision.getAddress2();
		String adressInput4 = airlineCommision.getAddress1();

		int i = 1;
		int subTotal = 0;
		boolean condition = true;
		NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);

		while (condition) {
			String hyphenString0 = new String(new char[78]).replace('\0', '\u002D');
			String hyphenString1 = new String(new char[06]).replace('\0', '\u002D');
			String hyphenString2 = new String(new char[40]).replace('\0', '\u002D');
			String hyphenString3 = new String(new char[37]).replace('\0', '\u002D');
			String hyphenString4 = new String(new char[17]).replace('\0', '\u002D');

			try {
				document.add(new Phrase(String.valueOf("I M P O T S  " + year + " \u002D "
						+ "ETAT DES HONORAIRES,VACATIONS,COURTAGES,COMMISSIONS              D A S   A G E N C E S   (DAS II)\n"
						+ "                             RISTOURNES ET JETONS DE PRESENCE                       (DECLARATION AGENT)      (EX. 2460-"
						+ "1024)\n" + "                      DROITS D’AUTEUR ET D’INVENTEUR PAYES PENDANT L’ANNEE\n"
						+ ""), fontText));
				document.add(new Phrase(String.valueOf(
						"                                                                                RAISON SOCIALE : "
								+ airlineInput + "\n" + "   LE CADRE CI" + "\u002D"
								+ "APRES EST RESERVE A LA DECLARATION DES SOMMES CI" + "\u002D"
								+ "DESSUS          ADRESSE        :  " + adressInput4 + "\n"
								+ "   VISEES QUI ONT ETE VERSEES A DES PERSONNES N O N  S A L A R I E E S                           "
								+ adressInput3
								+ "                                                                                TELEPHONE      : "
								+ telephone + "\n"
								+ "                                                                                NO POSTE RESPONSABLE :\n"
								+ "                                                                                NO IDENTIFICATION    : "
								+ identification + "\n"),
						fontText));

				document.add(new Phrase(String.valueOf(
						"          ********************************************************************************************************\n"
								+ "           NO   I 	    D E S I G N A T I O N     D E S      B E N E F I C I A I R E S           I MONTANT DES     I\n"
								+ "          ORDRE I" + hyphenString0 + "I COMMISSIONS (5) I\n"
								+ "                I      N    O    M  (1)                  I    A D R E S S E    (3)             I RISTOURNES (EUR)I\n"
								+ "          " + hyphenString1 + "I" + hyphenString2 + "I" + hyphenString3 + "I"
								+ hyphenString4 + "I\n"),
						fontText));
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

//			for (Commission commission : listCommission) {
			for (int j = 0; j < 20 && i <= listCommission.size(); j++) {
				Commission commission = listCommission.get(i - 1);
				Optional<Agent> matchingAgent = listAgent.stream()
						.filter(agent -> agent.getCodeAgent().equals(commission.getIdentify())).findFirst();

				if (matchingAgent.isPresent()) {
					agentCommision = matchingAgent.get();
				} else {
					agentCommision.setAddress1("");
					agentCommision.setAddress2("");
					agentCommision.setAddress3("");
					agentCommision.setNomAgent("");
				}

				String numberNom = commission.getIdentify();
				String adressInput = agentCommision.getAddress1();
				String adressInput1 = agentCommision.getAddress2();
				String adressInput2 = agentCommision.getAddress3();
				String nomAgentFormat = agentCommision.getNomAgent();

				int money = 0;
				if (!commission.getMoney().isEmpty()) {
					money = Integer.valueOf(commission.getMoney());
				} else {
					money = 0;
				}
				subTotal += money;
				
				String adress = String.format("%-35s", adressInput);
				String adress1 = String.format("%-35s", adressInput1);
				String adress2 = String.format("%-35s", adressInput2);
				String formattedNumNom = String.format("%12s", numberNom);
				String formattedMoney =  String.format("%12s", numberFormat.format(money));
				String nomAgent = String.format("%-27s",
						nomAgentFormat.substring(0, Math.min(nomAgentFormat.length(), 26)));

				String formattedPaging = String.format("%5d", i);

				try {
					document.add(new Phrase(String.valueOf("          " + formattedPaging + " I" + formattedNumNom
							+ " " + nomAgent + "I " + adress + " I " + formattedMoney + "    I\n" + "               "
							+ " I                                        " + "I " + adress1 + " I                "
							+ " I\n" + "               " + " I                                        " + "I " + adress2
							+ " I                " + " I\n"
							+ "          ******I****************************************I*************************************I*****************I\n"),
							fontText));
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				++i;

			}
			
			String formattedSubTotal = String.format("%13s", numberFormat.format(subTotal));
			
			try {
				document.add(new Phrase(String.valueOf(
								  "                                                                                   "
								+ "I SUBTOTAL      I\n"
								+ "                                                                                   "
								+ "I " + formattedSubTotal + " I\n"
								+ "                                                                                   "
								+ "I               I\n"
								+ "                                                                                   "
								+ "I***************I"),
						fontTextSubTotal));
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (i <= listCommission.size()) {
		        document.newPage(); // Start a new page if there are more students
		    } else {
		        condition = false; // Exit the loop when all students are processed
		    }
		}
		document.close();

	}

}
