package br.gov.frameworkdemoiselle.report;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.report.internal.implementation.JasperReportsExporter;
import br.gov.frameworkdemoiselle.report.internal.implementation.ReportImpl;
import br.gov.frameworkdemoiselle.report.mock.model.Pessoa;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class, JRLoader.class, JasperFillManager.class, JasperReportsExporter.class,
		JasperCompileManager.class })
public class ReportImplTest {

	private Report report;

	private ResourceBundle bundle;

	private Logger logger;

	@Before
	public void before() {
		logger = PowerMock.createMock(Logger.class);
		bundle = PowerMock.createMock(ResourceBundle.class);
	}

	/**
	 * Quando se passa como parâmetro para o construtor do ReportImpl um path contendo um arquivo que não é um
	 * relatório, o componente deve lançar uma exceção informativa.
	 */
	@Test
	@Ignore
	public void testReportWithWrongExtension() {
		PowerMock.mockStatic(LoggerProducer.class);
		EasyMock.expect(LoggerProducer.create(Logger.class)).andReturn(logger);
		PowerMock.replayAll();

		report = new ReportImpl("readme.txt");

		try {
			report.getSource();
			Assert.fail("Deveria levantar exceção.");
		} catch (Throwable e) {

		}
	}

	/**
	 * Quando o usuário informa um arquivo .jrxml mas já existe um .jasper, então deve usar o .jasper direto, visando
	 * poupar processamento.
	 */
	@Test
	@Ignore
	public void testExportReportJrxmlButJasperExists() {

		// Criando objetos que serão passados como parâmetro para o método a ser testado
		// "export(datasource,params,type)".
		List<Pessoa> datasource = new ArrayList<Pessoa>();
		Map<String, Object> params = new HashMap<String, Object>();
		Type type = Type.PDF;

		// Mocks que serão retornados nas chamadas internas.
		JasperReport jasper = EasyMock.createMock(JasperReport.class);
		JasperPrint print = EasyMock.createMock(JasperPrint.class);

		// Esperar as chamadas de debug e warn ao log.
		logger.debug(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		logger.warn(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();

		// O Beans é chamado para obter a referência ao Logger.
		PowerMock.mockStatic(Beans.class);
		EasyMock.expect(Beans.getReference(Logger.class)).andReturn(logger).anyTimes();
		PowerMock.replay(Beans.class); // Feito imediatamente, pois na instanciação de ReportImpl ele já é usado.

		report = new ReportImpl("report/Pessoas.jrxml");

		// Aguardar as chamadas ao bundle.
		Whitebox.setInternalState(report, "bundle", bundle);
		try {
			PowerMock.expectPrivate(bundle, "handleGetObject", EasyMock.anyObject(String.class)).andReturn("")
					.anyTimes();
		} catch (Exception e1) {
			Assert.fail();
		}
		EasyMock.expect(bundle.getString(EasyMock.anyObject(String.class))).andReturn("").anyTimes();
		EasyMock.expect(bundle.getString(EasyMock.anyObject(String.class), EasyMock.anyObject())).andReturn("")
				.anyTimes();

		// Tem que ser feita uma chamada a JRLoader.loadObject() para obter a instância do relatório em formato .jasper
		// que foi encontrado no classpath.
		PowerMock.mockStatic(JRLoader.class);
		try {
			EasyMock.expect(JRLoader.loadObject(EasyMock.anyObject(BufferedInputStream.class))).andReturn(jasper);
		} catch (JRException e2) {
			Assert.fail();
		}

		// Configurando para aguardar as chamadas seguintes para tratamento do relatório.
		PowerMock.mockStatic(JasperFillManager.class);
		try {
			EasyMock.expect(
					JasperFillManager.fillReport(EasyMock.anyObject(JasperReport.class), EasyMock.anyObject(Map.class),
							EasyMock.anyObject(JRBeanCollectionDataSource.class))).andReturn(print);
		} catch (JRException e2) {
			Assert.fail();
		}

		// O Beans será chamado de novo no JasperReportsExporter para obter uma referência ao Logger.
		PowerMock.mockStatic(Beans.class);
		EasyMock.expect(Beans.getReference(Logger.class)).andReturn(logger).anyTimes();

		PowerMock.mockStatic(JasperReportsExporter.class);
		ByteArrayOutputStream stream = PowerMock.createMock(ByteArrayOutputStream.class);

		EasyMock.expect(JasperReportsExporter.export(type, print)).andReturn(stream);
		byte[] expected = "test".getBytes();
		EasyMock.expect(stream.toByteArray()).andReturn(expected);

		PowerMock.replayAll();
		try {
			byte[] result = report.export(datasource, params, type);
			Assert.assertEquals(expected, result);
		} catch (RuntimeException e) {
			Assert.fail();
		} catch (Exception e) {
			Assert.fail();
		}

		// Importante verificar se a sequência de chamadas foi a definida acima.
		PowerMock.verifyAll();

	}

	/**
	 * Quando o usuário informa um arquivo .jrxml e não encontra o .jasper no classpath. Então, deve compilar um novo
	 * .jasper e partir dele.
	 */
	@Test
	@Ignore
	public void testExportReportJrxmlButJasperDoNotExists() {

		// Criando objetos que serão passados como parâmetro para o método a ser testado
		// "export(datasource,params,type)".
		List<Pessoa> datasource = new ArrayList<Pessoa>();
		Map<String, Object> params = new HashMap<String, Object>();
		Type type = Type.PDF;

		// Mocks que serão retornados nas chamadas internas.
		JasperReport jasper = EasyMock.createMock(JasperReport.class);
		JasperPrint print = EasyMock.createMock(JasperPrint.class);

		// Esperar as chamadas de debug e warn ao log.
		logger.debug(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		logger.warn(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();

		// O Beans é chamado para obter a referência ao Logger.
		PowerMock.mockStatic(Beans.class);
		EasyMock.expect(Beans.getReference(Logger.class)).andReturn(logger).anyTimes();
		PowerMock.replay(Beans.class); // Feito imediatamente, pois na instanciação de ReportImpl ele já é usado.

		report = new ReportImpl("report/PessoasNaoExisteJasper.jrxml");

		// Aguardar as chamadas ao bundle.
		Whitebox.setInternalState(report, "bundle", bundle);
		try {
			PowerMock.expectPrivate(bundle, "handleGetObject", EasyMock.anyObject(String.class)).andReturn("")
					.anyTimes();
		} catch (Exception e1) {
			Assert.fail();
		}
		EasyMock.expect(bundle.getString(EasyMock.anyObject(String.class))).andReturn("").anyTimes();
		EasyMock.expect(bundle.getString(EasyMock.anyObject(String.class), EasyMock.anyObject())).andReturn("")
				.anyTimes();

		// Tem que ser feita uma chamada a JasperCompileManager.compileReport() para compilar o .jrxml para .jasper.
		PowerMock.mockStatic(JasperCompileManager.class);
		try {
			EasyMock.expect(JasperCompileManager.compileReport(EasyMock.anyObject(String.class))).andReturn(jasper);
		} catch (JRException e2) {
			Assert.fail();
		}

		// Configurando para aguardar as chamadas seguintes para tratamento do relatório.
		PowerMock.mockStatic(JasperFillManager.class);
		try {
			EasyMock.expect(
					JasperFillManager.fillReport(EasyMock.anyObject(JasperReport.class), EasyMock.anyObject(Map.class),
							EasyMock.anyObject(JRBeanCollectionDataSource.class))).andReturn(print);
		} catch (JRException e2) {
			Assert.fail();
		}

		// O Beans será chamado de novo no JasperReportsExporter para obter uma referência ao Logger.
		PowerMock.mockStatic(Beans.class);
		EasyMock.expect(Beans.getReference(Logger.class)).andReturn(logger).anyTimes();

		PowerMock.mockStatic(JasperReportsExporter.class);
		ByteArrayOutputStream stream = PowerMock.createMock(ByteArrayOutputStream.class);

		EasyMock.expect(JasperReportsExporter.export(type, print)).andReturn(stream);
		byte[] expected = "test".getBytes();
		EasyMock.expect(stream.toByteArray()).andReturn(expected);

		PowerMock.replayAll();
		try {
			byte[] result = report.export(datasource, params, type);
			Assert.assertEquals(expected, result);
		} catch (RuntimeException e) {
			Assert.fail();
		} catch (Exception e) {
			Assert.fail();
		}

		// Importante verificar se a sequência de chamadas foi a definida acima.
		PowerMock.verifyAll();

	}

	/**
	 * Quando o usuário informa um arquivo .jasper
	 */
	@Test
	@Ignore
	public void testExportReportJasper() {

		// Criando objetos que serão passados como parâmetro para o método a ser testado
		// "export(datasource,params,type)".
		List<Pessoa> datasource = new ArrayList<Pessoa>();
		Map<String, Object> params = new HashMap<String, Object>();
		Type type = Type.PDF;

		// Mocks que serão retornados nas chamadas internas.
		JasperReport jasper = EasyMock.createMock(JasperReport.class);
		JasperPrint print = EasyMock.createMock(JasperPrint.class);

		// Esperar as chamadas de debug e warn ao log.
		logger.debug(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		logger.warn(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();

		// O Beans é chamado para obter a referência ao Logger.
		PowerMock.mockStatic(Beans.class);
		EasyMock.expect(Beans.getReference(Logger.class)).andReturn(logger).anyTimes();
		PowerMock.replay(Beans.class); // Feito imediatamente, pois na instanciação de ReportImpl ele já é usado.

		report = new ReportImpl("report/Pessoas.jasper");

		// Aguardar as chamadas ao bundle.
		Whitebox.setInternalState(report, "bundle", bundle);
		try {
			PowerMock.expectPrivate(bundle, "handleGetObject", EasyMock.anyObject(String.class)).andReturn("")
					.anyTimes();
		} catch (Exception e1) {
			Assert.fail();
		}
		EasyMock.expect(bundle.getString(EasyMock.anyObject(String.class))).andReturn("").anyTimes();
		EasyMock.expect(bundle.getString(EasyMock.anyObject(String.class), EasyMock.anyObject())).andReturn("")
				.anyTimes();

		// Tem que ser feita uma chamada a JRLoader.loadObject(jasper).
		PowerMock.mockStatic(JRLoader.class);
		try {
			EasyMock.expect(JRLoader.loadObject(EasyMock.anyObject(BufferedInputStream.class))).andReturn(jasper);
		} catch (JRException e2) {
			Assert.fail();
		}

		// Configurando para aguardar as chamadas seguintes para tratamento do relatório.
		PowerMock.mockStatic(JasperFillManager.class);
		try {
			EasyMock.expect(
					JasperFillManager.fillReport(EasyMock.anyObject(JasperReport.class), EasyMock.anyObject(Map.class),
							EasyMock.anyObject(JRBeanCollectionDataSource.class))).andReturn(print);
		} catch (JRException e2) {
			Assert.fail();
		}

		// O Beans será chamado de novo no JasperReportsExporter para obter uma referência ao Logger.
		PowerMock.mockStatic(Beans.class);
		EasyMock.expect(Beans.getReference(Logger.class)).andReturn(logger).anyTimes();

		PowerMock.mockStatic(JasperReportsExporter.class);
		ByteArrayOutputStream stream = PowerMock.createMock(ByteArrayOutputStream.class);

		EasyMock.expect(JasperReportsExporter.export(type, print)).andReturn(stream);
		byte[] expected = "test".getBytes();
		EasyMock.expect(stream.toByteArray()).andReturn(expected);

		PowerMock.replayAll();
		try {
			byte[] result = report.export(datasource, params, type);
			Assert.assertEquals(expected, result);
		} catch (RuntimeException e) {
			Assert.fail();
		} catch (Exception e) {
			Assert.fail();
		}

		// Importante verificar se a sequência de chamadas foi a definida acima.
		PowerMock.verifyAll();

	}

}
