package br.cefetrj.sca.infra.cargadados;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.cefetrj.sca.dominio.Aluno;
import br.cefetrj.sca.dominio.AlunoFabrica;

@Component
public class ImportadorAlunos {

	String colunas[] = { "COD_CURSO", "CURSO", "VERSAO_CURSO", "CPF",
			"MATR_ALUNO", "NOME_PESSOA", "FORMA_EVASAO", "COD_TURMA",
			"COD_DISCIPLINA", "NOME_DISCIPLINA", "ANO", "PERIODO", "SITUACAO",
			"CH_TOTAL", "CREDITOS", "MEDIA_FINAL", "NUM_FALTAS" };

	/**
	 * Dicionário de pares (matrícula, objeto da classe aluno) de cada aluno.
	 */
	private HashMap<String, Aluno> alunos_matriculas = new HashMap<>();

	@Autowired
	private AlunoFabrica alunoFabrica;

	public ImportadorAlunos() {
	}

	public void run() {
		String planilhaMatriculas = "./planilhas/historicos-escolares/11.02.05.99.60.xls";
		run(planilhaMatriculas);
	}

	public void run(String arquivoPlanilha) {
		System.out.println("ImportadorDiscentes.run()");
		try {
			ImportadorAlunos iim = new ImportadorAlunos();
			iim.importarPlanilha(arquivoPlanilha);
			iim.gravarDadosImportados();
		} catch (BiffException | IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Feito!");
	}

	public void gravarDadosImportados() {
		System.out.println("Realizando a persistência de objetos Aluno...");

		EntityManager em = ImportadorTudo.entityManager;

		em.getTransaction().begin();

		/**
		 * Realiza a persistência dos objetos Aluno.
		 */
		int adicionados = 0;
		Set<String> matriculas = alunos_matriculas.keySet();
		for (String matricula : matriculas) {
			Aluno aluno;
			try {
				Query queryAluno;
				queryAluno = em
						.createQuery("from Aluno a where a.matricula = :matricula");
				queryAluno.setParameter("matricula", matricula);
				aluno = (Aluno) queryAluno.getSingleResult();
			} catch (NoResultException e) {
				aluno = null;
			}

			if (aluno == null) {
				em.persist(alunos_matriculas.get(matricula));
				adicionados++;
			}
		}

		em.getTransaction().commit();

		System.out.println("Foram adicionados " + adicionados + " alunos.");
	}

	public void importarPlanilha(String inputFile) throws BiffException,
			IOException {
		File inputWorkbook = new File(inputFile);
		importarPlanilha(inputWorkbook);
	}

	public void importarPlanilha(File inputWorkbook) throws BiffException,
			IOException {
		Workbook w;

		List<String> colunasList = Arrays.asList(colunas);
		System.out.println("Realizando leitura da planilha...");

		WorkbookSettings ws = new WorkbookSettings();
		ws.setEncoding("Cp1252");
		w = Workbook.getWorkbook(inputWorkbook, ws);
		Sheet sheet = w.getSheet(0);

		for (int i = 1; i < sheet.getRows(); i++) {

			String codigoCurso = sheet.getCell(
					colunasList.indexOf("COD_CURSO"), i).getContents();
			String numeroVersaoCurso = sheet.getCell(
					colunasList.indexOf("VERSAO_CURSO"), i).getContents();

			String aluno_matricula = sheet.getCell(
					colunasList.indexOf("MATR_ALUNO"), i).getContents();
			String aluno_nome = sheet.getCell(
					colunasList.indexOf("NOME_PESSOA"), i).getContents();
			String aluno_cpf = sheet.getCell(colunasList.indexOf("CPF"), i)
					.getContents();

			if (aluno_cpf == null || aluno_cpf.isEmpty()) {
				System.out
						.println("CPF não fornecido para aluno " + aluno_nome);
			} else {
				alunoFabrica = (AlunoFabrica) ImportadorTudo.context
						.getBean(AlunoFabrica.class);
				Aluno aluno = alunoFabrica.criar(aluno_nome, aluno_matricula,
						aluno_cpf, codigoCurso, numeroVersaoCurso);
				alunos_matriculas.put(aluno_matricula, aluno);
			}

		}
		System.out.println("Dados lidos com sucesso!");
	}
}
