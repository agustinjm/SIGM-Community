package ieci.tdw.ispac.api.rule.procedures.sancionador;

import java.util.Iterator;

import ieci.tdw.ispac.api.IEntitiesAPI;
import ieci.tdw.ispac.api.errors.ISPACRuleException;
import ieci.tdw.ispac.api.item.IItem;
import ieci.tdw.ispac.api.item.IItemCollection;
import ieci.tdw.ispac.api.rule.IRuleContext;

/**
 * 
 * @author teresa
 * Inicia solicitud BOP inicial del Procedimiento Sancionador 
 *
 */

public class InitSolicitudBopInicialSancionadorRule extends InitSolicitudBopSancionadorRule {

	public boolean init(IRuleContext rulectx) throws ISPACRuleException
	{
		STR_entidad = "URB_SANCIONADOR";
		STR_BOP_entidad = "";
		STR_BOP_urgencia = "Normal";
		STR_BOP_sumario = "Aprobación inicial de la Cuenta General";
		STR_BOP_observaciones = "Solicitud automática desde expediente: Procedimiento Sancionador";

		//La entidad es el ayuntamiento correspondiente
		try
		{
	        IEntitiesAPI entitiesAPI = rulectx.getClientContext().getAPI().getEntitiesAPI();
	        String numexp_solicitud = rulectx.getNumExp();	
	        String strQuery = "WHERE NUMEXP_HIJO='"+numexp_solicitud+"'";
	        IItemCollection col = entitiesAPI.queryEntities("SPAC_EXP_RELACIONADOS", strQuery);
	        Iterator it = col.iterator();
	        if (!it.hasNext())
	        {
	        	return true;
	        }
        	IItem relacion = (IItem)it.next();
        	String numexp_ent = relacion.getString("NUMEXP_PADRE");
        	col = entitiesAPI.getEntities(STR_entidad, numexp_ent);
	        it = col.iterator();
	        if (!it.hasNext())
	        {
	        	return true;
	        }
	        IItem entidad = (IItem)it.next();
	        STR_BOP_entidad = entidad.getString("MUNICIPIO");
	    }
		catch(Exception e) 
	    {
	    	if (e instanceof ISPACRuleException)
	    	{
			    throw new ISPACRuleException(e);
	    	}
	    	throw new ISPACRuleException("No se ha podido iniciar el trámite de publicación.",e);
	    }
		
		return true;
    }
}
