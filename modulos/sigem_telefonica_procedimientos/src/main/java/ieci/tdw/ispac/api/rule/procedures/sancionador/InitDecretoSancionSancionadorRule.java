package ieci.tdw.ispac.api.rule.procedures.sancionador;

import ieci.tdw.ispac.api.errors.ISPACRuleException;
import ieci.tdw.ispac.api.rule.IRuleContext;

/**
 * 
 * @author teresa
 * Inicia decreto sanci�n Procedimiento Sancionador
 *
 */

public class InitDecretoSancionSancionadorRule extends InitDecretoSancionadorRule {

	public boolean init(IRuleContext rulectx) throws ISPACRuleException
	{
		STR_entidad = "URB_SANCIONADOR";
		STR_extracto = "Resoluci�n de sanci�n del procedimiento Sancionador";
        return true;
    }
}
