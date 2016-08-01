package ieci.tdw.ispac.api.rule.procedures.planeamiento;

import ieci.tdw.ispac.api.errors.ISPACRuleException;
import ieci.tdw.ispac.api.rule.IRuleContext;

/**
 * 
 * @author teresa
 * Inicia propuesta aprobación inicial Procedimiento Aprobación y Modificación del Plan de Ordenación Municipal
 *
 */

public class InitPropuestaInicialPlaneamientoRule extends InitPropuestaPlaneamientoRule {

	public boolean init(IRuleContext rulectx) throws ISPACRuleException
	{
		STR_entidad = "PLAN_POM";
		STR_extracto = "Resolución de aprobación inicial del procedimiento de Aprobación y Modificación del Plan de Ordenación Municipal";
        return true;
    }
}
