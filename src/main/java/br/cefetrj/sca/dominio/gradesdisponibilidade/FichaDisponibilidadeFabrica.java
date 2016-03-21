package br.cefetrj.sca.dominio.gradesdisponibilidade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.cefetrj.sca.dominio.EnumDiaSemana;
import br.cefetrj.sca.dominio.ItemHorario;
import br.cefetrj.sca.dominio.Professor;
import br.cefetrj.sca.dominio.repositories.ProfessorRepositorio;

@Component
public class FichaDisponibilidadeFabrica {

	@Autowired
	ProfessorRepositorio professorRepositorio;

	/**
	 * Cria um objeto <code>FichaDisponibilidade</code>, com todas as
	 * informações necessárias para que um professor monte sua grade de
	 * disponibilidades para determinado período letivo..
	 * 
	 * @param matriculaProfessor
	 *            matrícula do professor para o qual criar a ficha de
	 *            disponibilidade.
	 * 
	 * @return ficha de disponibilidades recém construída.
	 */
	public FichaDisponibilidade criar(String matriculaProfessor) {
		Professor professor = professorRepositorio
				.findProfessorByMatricula(matriculaProfessor);
		return criar(professor);
	}

	public FichaDisponibilidade criar(Professor professor) {
		if (professor != null) {
			FichaDisponibilidade ficha = new FichaDisponibilidade(
					professor.getMatricula(), professor.getNome());
			ficha.definirHabilitacoes(professor.getHabilitacoes());
			ficha.definirTemposAula(ItemHorario.itens());
			ficha.definirDiasSemana(EnumDiaSemana.dias());
			return ficha;
		} else {
			return null;
		}
	}
}