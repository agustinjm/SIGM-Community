package ieci.tdw.ispac.api.rule.procedures.urbanismo;

import java.util.Iterator;

import ieci.tdw.ispac.api.IEntitiesAPI;
import ieci.tdw.ispac.api.IInvesflowAPI;
import ieci.tdw.ispac.api.errors.ISPACRuleException;
import ieci.tdw.ispac.api.item.IItem;
import ieci.tdw.ispac.api.item.IItemCollection;
import ieci.tdw.ispac.api.item.ITask;
import ieci.tdw.ispac.api.rule.IRule;
import ieci.tdw.ispac.api.rule.IRuleContext;
import ieci.tdw.ispac.api.rule.procedures.CommonFunctions;
import ieci.tdw.ispac.ispaclib.context.ClientContext;

public class InitTaskPublicacionRaminpRule implements IRule {

	public boolean init(IRuleContext rulectx) throws ISPACRuleException{
        return true;
    }

    public boolean validate(IRuleContext rulectx) throws ISPACRuleException{
        return true;
    }

    public Object execute(IRuleContext rulectx) throws ISPACRuleException
    {
    	try
    	{
			//----------------------------------------------------------------------------------------------
	        ClientContext cct = (ClientContext) rulectx.getClientContext();
	        IInvesflowAPI invesFlowAPI = cct.getAPI();
	        IEntitiesAPI entitiesAPI = invesFlowAPI.getEntitiesAPI();
	        //----------------------------------------------------------------------------------------------
	    	
	        //Actualiza el campo estado de la entidad
	        //de modo que permita mostrar los enlaces para crear Propuesta/Decreto
	        String numexp = rulectx.getNumExp();
	        IItemCollection col = entitiesAPI.getEntities("URB_LIC_RAMINP", numexp);
	        Iterator it = col.iterator();
	        if (it.hasNext())
	        {
		        IItem entidad = (IItem)it.next();
		        entidad.set("ESTADO", "Inicio");
		        entidad.store(cct);
	        }
	        
			//Creaci�n de variables de sesi�n para las plantillas
			int taskId = rulectx.getTaskId();
			ITask iTask = invesFlowAPI.getTask(taskId);
			String strTaskName = iTask.getString("NOMBRE");
			cct.setSsVariable("NOMBRE_TRAMITE", strTaskName);

	        //Generaci�n del anuncio a partir de la plantilla
			String strTpDocName = "Anuncio";
			String strTemplateName = "Anuncio BOP RAMINP";
	        CommonFunctions.generarDocumento(rulectx, strTpDocName, strTemplateName, null);
	        
	        //Borra las variables de sesion utilizadas
			cct.deleteSsVariable("NOMBRE_TRAMITE");
	    }
		catch(Exception e) 
	    {
	    	if (e instanceof ISPACRuleException)
	    	{
			    throw new ISPACRuleException(e);
	    	}
	    	throw new ISPACRuleException("No se ha podido iniciar la tarea de publicaci�n en BOP.",e);
	    }
		return new Boolean(true);
    }

	public void cancel(IRuleContext rulectx) throws ISPACRuleException{
    }
}
