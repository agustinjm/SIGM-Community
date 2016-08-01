package es.dipucr.contratacion.common.avisos;

import es.dipucr.sigem.api.rule.common.avisos.AvisoElectronico;
import ieci.tdw.ispac.api.errors.ISPACRuleException;
import ieci.tdw.ispac.api.rule.IRuleContext;

public class AvisoElectronicoIncidenciaContrato extends AvisoElectronico{
	
	public boolean init(IRuleContext rulectx) throws ISPACRuleException {
		sResponsable = "3-330";
		mensaje = "Ha sido a�adido una nueva Incidencia del contrato del expediente: "+rulectx.getNumExp();
		return true;
	}
}
