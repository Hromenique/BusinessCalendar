package br.com.hrom.businesscalendar;

import java.util.HashSet;
import java.util.Set;

import net.objectlab.kit.datecalc.common.DateCalculator;
import net.objectlab.kit.datecalc.common.DefaultHolidayCalendar;
import net.objectlab.kit.datecalc.common.HolidayCalendar;
import net.objectlab.kit.datecalc.common.HolidayHandlerType;
import net.objectlab.kit.datecalc.joda.LocalDateKitCalculatorsFactory;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;

public class BusinessCalendar {
	
	private LocalTime inicioHorarioComercial;
	private LocalTime fimHorarioComercial;
	private Period periodoComercial;
	private Set<Feriado> feriados;
	private DateCalculator<LocalDate> calendario;
	
	public BusinessCalendar(Set<Feriado> feriados, LocalTime inicioHorarioComercial, LocalTime fimHorarioComercial){
		this.feriados = feriados;
		this.inicioHorarioComercial = inicioHorarioComercial;
		this.fimHorarioComercial = fimHorarioComercial;
		this.periodoComercial = new Period(inicioHorarioComercial, fimHorarioComercial);
	}
	
	public BusinessCalendar(Set<Feriado> feriados){
		this(feriados, new LocalTime(0, 0, 0, 0), new LocalTime(23, 59, 59, 999));
	}
	
	public Period calculaDuracao(DateTime inicio, DateTime fim){
		this.calendario = criaCalendario(inicio.getYear(), fim.getYear());
		
		if(saoMesmoDia(inicio, fim)){
			if(calendario.isNonWorkingDay(inicio.toLocalDate())){
				return Period.ZERO;
			}
			
			return new Period(inicio, fim);
		}		
		
		Period periodoDoPrimeiroDia = calculaPeriodoAteFimHorarioComercial(inicio, inicioHorarioComercial, fimHorarioComercial);
		Period periodoDoDiaFinal = calculaPeriodoAteInicioHorarioComercial(fim, inicioHorarioComercial, fimHorarioComercial);		
		Period peridoDoSegundoDiaAoPenultimoDia = Period.ZERO;
		
		DateTime inicioSegundoDia = inicio.plusDays(1).withTime(inicioHorarioComercial);
		DateTime fimPenultimoDia = fim.minusDays(1).withTime(fimHorarioComercial);
		if(!saoMesmoDia(inicioSegundoDia, fim)){
			int quantDiasNaoUteis = calculaQuantDiasNaoUteis(inicioSegundoDia.toLocalDate(), fimPenultimoDia.toLocalDate());
			Period periodoNaoTrabalhado = this.periodoComercial.multipliedBy(quantDiasNaoUteis);
			peridoDoSegundoDiaAoPenultimoDia = new Period(inicioSegundoDia, fimPenultimoDia).minus(periodoNaoTrabalhado);
		}
		
		return periodoDoPrimeiroDia.plus(peridoDoSegundoDiaAoPenultimoDia).plus(periodoDoDiaFinal);
	}
	
	private DateCalculator<LocalDate> criaCalendario(int... anos){
		Set<LocalDate> dataDosFeriados = new HashSet<LocalDate>();
		
		for(int ano : anos){
			for(Feriado feriado : feriados){
				try{
				LocalDate dataFeriado = new LocalDate(ano, feriado.getMes(), feriado.getDia());
				dataDosFeriados.add(dataFeriado);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		HolidayCalendar<LocalDate> calendarioDeFeriados = new DefaultHolidayCalendar<LocalDate>(dataDosFeriados);
		LocalDateKitCalculatorsFactory.getDefaultInstance().registerHolidays("BR", calendarioDeFeriados);
		DateCalculator<LocalDate> calendario = LocalDateKitCalculatorsFactory.getDefaultInstance().getDateCalculator("BR", HolidayHandlerType.FORWARD);
		
		return calendario;
	}
	
	private Period calculaPeriodoAteInicioHorarioComercial(DateTime data, LocalTime inicioHorarioComercial, LocalTime fimHorarioComercial) {
		LocalTime horario = data.toLocalTime();
		
		if(horario.isBefore(inicioHorarioComercial) || calendario.isNonWorkingDay(data.toLocalDate())){
			return Period.ZERO;
		}
		if(horario.isAfter(fimHorarioComercial)){
			return this.periodoComercial;
		}
		
		return new Period(inicioHorarioComercial, horario);
	}

	private Period calculaPeriodoAteFimHorarioComercial(DateTime data, LocalTime inicioHorarioComerical, LocalTime fimHorarioComercial){
		LocalTime horario = data.toLocalTime();
		if(horario.isAfter(fimHorarioComercial) || calendario.isNonWorkingDay(data.toLocalDate())){
			return Period.ZERO;
		}
		
		if(horario.isBefore(inicioHorarioComerical)){
			return this.periodoComercial;
		}
		
		return new Period(horario, fimHorarioComercial);
	}	
	
	private int calculaQuantDiasNaoUteis(LocalDate  inicio, LocalDate  fim){
		int diasNaoUteis = 0;
		
		while(inicio.isBefore(fim)){
			if(calendario.isNonWorkingDay(inicio)){
				diasNaoUteis++;
			}
			inicio = inicio.plusDays(1);
		}
		
		return diasNaoUteis;
	}
	

	private boolean saoMesmoDia(DateTime data1, DateTime data2){
		return data1.toLocalDate().isEqual(data2.toLocalDate());
	}

}
