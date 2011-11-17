package br.gov.frameworkdemoiselle.report.mock.dao;

import java.util.ArrayList;
import java.util.List;

import br.gov.frameworkdemoiselle.report.mock.model.Pessoa;
import br.gov.frameworkdemoiselle.stereotype.PersistenceController;

@PersistenceController
public class PessoaDAO {

	public List<Pessoa> listarTudo() {
		List<Pessoa> list = new ArrayList<Pessoa>();
		for (int i = 1; i <= 30; i++) {
			Pessoa p = new Pessoa();
			p.setCpf("1000" + i);
			p.setEmail("pessoa" + i + "@mail.com");
			p.setEndereco("Rua tal numero " + i);
			p.setIdade(20 + i);
			p.setNome("Pessoa" + i);
			p.setRg("081000" + i);
			list.add(p);
		}
		return list;
	}

}
