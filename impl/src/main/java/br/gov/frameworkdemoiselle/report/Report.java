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
package br.gov.frameworkdemoiselle.report;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Defines the common behavior for report objects.
 * 
 * @author SERPRO
 */
public interface Report extends Serializable{

	/**
	 * The source of object from the path. 
	 * 
	 * If you are using JasperReports it should returns JasperReport.
	 * 
	 * @return
	 */
	public Object getSource();
	
	/**
	 * Returns the especifc object from the vendor.
	 * 
	 * If you are using a JasperReports it should returns an JasperPrint.
	 * 
	 */
	public Object getReportObject();
	
	/**
	 * Gets the report's file path.
	 * 
	 * @return Path.
	 */
	public String getPath();

	/**
	 * Prepare the object that represents the report data ( In JasperReports case: JasperPrint)
	 * to be exported in any formats you want.
	 * 
	 * @param dataSource Datasource used to fill the report.
	 * @param param Parameters used to fill the report.
	 * @param type The report will be generated in this type.
	 * @return Generated report.
	 */
	public void prepare(Collection<?> dataSource, Map<String, Object> param);
	
	/**
	 * Generates a byte array containing the exported report.
	 * 
	 * @param dataSource Datasource used to fill the report.
	 * @param param Parameters used to fill the report.
	 * @param type The report will be generated in this type.
	 * @return Generated report.
	 */
	public byte[] export(Collection<?> dataSource, Map<String, Object> param, Type type);
	
	/**
	 * Generates a byte array containing the exported report.
	 * @param type The report will be generated in this type.
	 * @return Generated report.
	 */
	public byte[] export(Type type);

}
