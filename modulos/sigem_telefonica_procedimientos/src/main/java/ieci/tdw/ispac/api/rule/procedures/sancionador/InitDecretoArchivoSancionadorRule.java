package ieci.tdw.ispac.api.rule.procedures.sancionador;

import ieci.tdw.ispac.api.errors.ISPACRuleException;
import ieci.tdw.ispac.api.rule.IRuleContext;

/**
 * 
 * @author teresa
 * Inicia decreto archivo Procedimiento Sancionador
 *
 */

public class InitDecretoArchivoSancionadorRule extends InitDecretoSancionadorRule {

	public boolean init(IRuleContext rulectx) throws ISPACRuleException
	{
		STR_entidad = "URB_SANCIONADOR";
		STR_extracto = "Resolución de archivo del procedimiento Sancionador";
        return true;
    }
}
