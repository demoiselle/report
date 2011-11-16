package br.gov.frameworkdemoiselle.report;

import java.util.HashMap;

import javax.inject.Inject;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.frameworkdemoiselle.junit.DemoiselleRunner;
import br.gov.frameworkdemoiselle.report.annotation.Path;
import br.gov.frameworkdemoiselle.report.mock.dao.PessoaDAO;

/**
 * Realizar testes de integração para o componente.
 * 
 * @author SERPRO
 */
@RunWith(DemoiselleRunner.class)
public class ReportIntegrationTest {

	@Inject
	private PessoaDAO pessoaDAO;

	@Inject
	@Path("report/RelatorioPessoas.jrxml")
	private Report report;

	/**
	 * Teste simples para verificar se o relatório é criado sem erros.
	 */
	@Test
	public void testIntegration() {
		try {
			byte[] relbytes = report.export(pessoaDAO.listarTudo(), new HashMap<String, Object>(), Type.PDF);
			Assert.assertTrue(relbytes != null);
			Assert.assertTrue(relbytes.length > 0);
		} catch (Exception e) {
			Assert.fail();
		}
	}
	
}
