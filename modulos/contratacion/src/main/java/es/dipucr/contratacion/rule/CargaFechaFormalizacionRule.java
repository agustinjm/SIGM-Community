package es.dipucr.contratacion.rule;

import ieci.tdw.ispac.api.errors.ISPACRuleException;
import ieci.tdw.ispac.api.rule.IRuleContext;
import es.dipucr.contratacion.common.CargaFechaCampoTablaRule;

public class CargaFechaFormalizacionRule extends CargaFechaCampoTablaRule{
	public boolean init(IRuleContext rulectx) throws ISPACRuleException {
		tablaInsertarDatos = "CONTRATACION_DATOS_TRAMIT";
		campotablaInsertarDatos = "F_CONTRATO";
		return true;
	}
}
