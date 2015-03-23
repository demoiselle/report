/*
 * Demoiselle Framework
 * Copyright (C) 2010 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.report.internal.implementation;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.report.Type;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public class JasperReportsExporter {

	public static final String NON_COMPILED_REPORT_EXTENSION = ".jrxml";

	public static final String COMPILED_REPORT_EXTENSION = ".jasper";

	private static Logger logger;

	public static synchronized ByteArrayOutputStream export(Type type, JasperPrint print) {
		if (logger == null) {
			logger = LoggerProducer.create("br.gov.frameworkdemoiselle.report.internal.implementation");
		}
		ResourceBundle bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-report-bundle"));

		logger.fine(bundle.getString("generating-report", type.name()));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			switch (type) {
				case CSV:
					logger.fine(bundle.getString("exporting-report", "csv"));
					JRCsvExporter exporterCSV = new JRCsvExporter();
					exporterCSV.setParameter(JRCsvExporterParameter.JASPER_PRINT, print);
					exporterCSV.setParameter(JRCsvExporterParameter.OUTPUT_STREAM, outputStream);
					exporterCSV.exportReport();
					break;
				case HTML:
					logger.fine(bundle.getString("exporting-report", "html"));
					JRHtmlExporter exporterHTML = new JRHtmlExporter();
					exporterHTML.setParameter(JRHtmlExporterParameter.JASPER_PRINT, print);
					exporterHTML.setParameter(JRHtmlExporterParameter.OUTPUT_STREAM, outputStream);
					exporterHTML.exportReport();
					break;
				case ODT:
					logger.fine(bundle.getString("exporting-report", "odt"));
					JROdtExporter exporterODT = new JROdtExporter();
					exporterODT.setParameter(JRExporterParameter.JASPER_PRINT, print);
					exporterODT.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
					exporterODT.exportReport();
					break;
				case PDF:
					logger.fine(bundle.getString("exporting-report", "pdf"));
					JRPdfExporter exporterPDF = new JRPdfExporter();
					exporterPDF.setParameter(JRPdfExporterParameter.JASPER_PRINT, print);
					exporterPDF.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, outputStream);
					exporterPDF.setParameter(JRPdfExporterParameter.IS_CREATING_BATCH_MODE_BOOKMARKS, Boolean.TRUE);
					exporterPDF.setParameter(JRPdfExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
					exporterPDF.exportReport();
					break;
				case TXT:
					logger.fine(bundle.getString("exporting-report", "txt"));
					JRTextExporter exporterTXT = new JRTextExporter();
					exporterTXT.setParameter(JRTextExporterParameter.JASPER_PRINT, print);
					exporterTXT.setParameter(JRTextExporterParameter.OUTPUT_STREAM, outputStream);
					exporterTXT.setParameter(JRTextExporterParameter.CHARACTER_WIDTH, new Float(30));
					exporterTXT.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT, new Float(30));
					exporterTXT.exportReport();
					break;
				case RTF:
					logger.fine(bundle.getString("exporting-report", "rtf"));
					JRRtfExporter exporterRTF = new JRRtfExporter();
					exporterRTF.setParameter(JRExporterParameter.JASPER_PRINT, print);
					exporterRTF.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
					exporterRTF.exportReport();
				case XLS:
					logger.fine(bundle.getString("exporting-report", "xls"));
					JRXlsExporter exporterXLS = new JRXlsExporter();
					exporterXLS.setParameter(JRExporterParameter.JASPER_PRINT, print);
					exporterXLS.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
					exporterXLS.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
					exporterXLS.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
					exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
					exporterXLS.exportReport();
					break;
				case ODS:
					logger.fine(bundle.getString("exporting-report", "ods"));
					  JROdsExporter exporterODS = new JROdsExporter();
					  exporterODS.setParameter(JRExporterParameter.JASPER_PRINT, print);
					  exporterODS.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
					  exporterODS.exportReport();
					  break; 
				default:
					throw new DemoiselleException(bundle.getString("exception-reportimpl-not-found", type.name()));
			}
		} catch (JRException jre) {
			throw new DemoiselleException(bundle.getString("exception-generating-report", type.name()), jre);
		}
		return outputStream;
	}

}
