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

import java.net.URL;
import java.util.Collection;
import java.util.Map;

import javax.enterprise.context.SessionScoped;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.report.Report;
import br.gov.frameworkdemoiselle.report.Type;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@SessionScoped
public class ReportImpl implements Report {

	private static final long serialVersionUID = -2678172269775864650L;

	private Logger logger;

	private JasperReport jasper;

	private JasperPrint print;

	private String path;

	private ResourceBundle bundle;

	/**
	 * It will load the report by the informed path.
	 * 
	 * @param name
	 * @param path
	 * @throws JRException
	 */
	public ReportImpl(String path) {
		this.logger = Beans.getReference(Logger.class);
		this.bundle = new ResourceBundleProducer().create("demoiselle-report-bundle");
		this.path = path;
	}
	
	@Override
	public Object getSource() {
		try {
			loadReport(path);
		} catch (Exception e) {
			throw new DemoiselleException(bundle.getString("exception-load", path),e);
		}
		return jasper;
	}

	@Override
	public Object getReportObject() {
		return print;
	}
	

	private final void loadReport(String path) {
		if (jasper == null) {
			String realPath = getRealPath(path);
			if (path != null && path.endsWith(JasperReportsExporter.NON_COMPILED_REPORT_EXTENSION)) {
				logger.warn(bundle.getString("recommend-use-jasper"));

				URL urlPossibleJasper = this
						.getClass()
						.getClassLoader()
						.getResource(
								path.replaceAll(JasperReportsExporter.NON_COMPILED_REPORT_EXTENSION,
										JasperReportsExporter.COMPILED_REPORT_EXTENSION));

				if (urlPossibleJasper != null) {
					logger.debug(bundle.getString("found-compiled-version", realPath));
					loadJasperFile(urlPossibleJasper.toString());
				} else {
					logger.debug(bundle.getString("not-found-compiled-version"));
					compileJRXML(realPath);
				}
			} else if (path != null && path.endsWith(JasperReportsExporter.COMPILED_REPORT_EXTENSION)) {
				loadJasperFile(realPath);
			} else {
				throw new DemoiselleException(bundle.getString("exception-extension-not-valid", realPath));
			}
		}
	}

	private void compileJRXML(String path) {
		try {
			jasper = JasperCompileManager.compileReport(path);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new DemoiselleException(bundle.getString("exception-compiling-jrxml-file", path));			
		}
	}
	
	private void loadJasperFile(String path) {
		try {
			jasper = (JasperReport) JRLoader.loadObject(path);
		} catch (JRException e) {
			throw new DemoiselleException(bundle.getString("exception-loading-jasper-file", path),e);
		}
	}

	private final String getRealPath(String relativePath) {
		URL url = this.getClass().getClassLoader().getResource(relativePath);
		if (url == null) {
			throw new DemoiselleException(bundle.getString("file-not-found"));
		}
		return url.getFile();
	}

	@Override
	public void prepare(Collection<?> dataSource, Map<String, Object> param) {
		logger.debug(bundle.getString("filling-report"));
		loadReport(path);		
		
		try {
			print = JasperFillManager.fillReport(jasper, param, new JRBeanCollectionDataSource(dataSource));
		} catch (JRException e) {
			throw new DemoiselleException(bundle.getString("filling-report-problem"), e);
		}
		
	}

	/**
	 * Inform if the method fill was already invoked, so its no need to rebuild the
	 * report, you can export it many times and types you want.
	 * 
	 * @return
	 */
	public Boolean isFilled() {
		return print != null;
	}

	/**
	 * 
	 * Fill the report then generate de output for the report usind the filled data.
	 * 
	 * Before call this method, is necessary to call the fill method.
	 * 
	 * @return
	 * @throws JRException 
	 */
	public byte[] export(Collection<?> dataSource, Map<String, Object> param, Type type){
		prepare(dataSource, param);
		return export(type);

	}

	/**
	 * Generate de output for the report usind the filled data.
	 * 
	 * Before call this method, is necessary to call the fill method.
	 * 
	 * @return
	 * @throws JRException 
	 */
	public byte[] export(Type type){
		if (!isFilled()) {
			throw new DemoiselleException(bundle.getString("exception-report-not-filled"));
		}
		return JasperReportsExporter.export(type, print).toByteArray();
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String getPath() {
		return this.path;
	}

}
