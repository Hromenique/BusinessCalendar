package br.com.hrom.businesscalendar;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class BusinessCalendarTest {
	
	private static Set<Feriado> feriados;
	
	@BeforeClass
	public static void init(){
		feriados = new HashSet<Feriado>();
		feriados.add(new Feriado(25, 12, "Natal"));
		feriados.add(new Feriado(1, 1, "Ano Novo"));
		feriados.add(new Feriado(1, 10, "Dia do Programador"));
	}
	
	@Test
	public void calculaDuracaoDiasDiferentesComDataInicialMenorQueAFinal(){
		LocalTime inicioHorarioComercial = new LocalTime(8, 0);
		LocalTime fimHorarioComercial = new LocalTime(18, 0);
		BusinessCalendar bc = new BusinessCalendar(feriados, inicioHorarioComercial, fimHorarioComercial);
		
		DateTime inicio = new DateTime(2015, 12, 23, 0, 0);
		DateTime fim = new DateTime(2016, 1, 3, 0, 0);;
		Period periodo = bc.calculaDuracao(inicio, fim);
		System.out.println(periodo);
		assertEquals(2, periodo.getDays());
		
	}
	
	public void calculaDuracaoDiasDiferentesComDataInicialMaiorQueAFinal(){
		
	}
	
	public void calculaDuracaoMesmoDiaComDataInicialMenorQueAFinal(){
		
	}
	
	public void calculaDuracaoMesmoDiaComDataInicialMaiorQueAFinal() {

	}

}
