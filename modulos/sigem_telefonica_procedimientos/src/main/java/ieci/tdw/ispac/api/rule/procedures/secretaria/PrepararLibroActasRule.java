package ieci.tdw.ispac.api.rule.procedures.secretaria;

import ieci.tdw.ispac.api.IEntitiesAPI;
import ieci.tdw.ispac.api.IInvesflowAPI;
import ieci.tdw.ispac.api.errors.ISPACRuleException;
import ieci.tdw.ispac.api.item.IItem;
import ieci.tdw.ispac.api.item.IItemCollection;
import ieci.tdw.ispac.api.rule.IRule;
import ieci.tdw.ispac.api.rule.IRuleContext;
import ieci.tdw.ispac.ispaclib.context.ClientContext;

import java.util.Date;
import java.util.Iterator;

public class PrepararLibroActasRule implements IRule {
	
	protected String STR_DocActa = "Borrador de Acta de Pleno";
	
	public boolean init(IRuleContext rulectx) throws ISPACRuleException
	{
		try
		{
        	IItem iLibro = rulectx.getItem();
        	String strOrgano = iLibro.getString("ORGANO");
        	if (strOrgano.compareTo("PLEN")==0)
        	{
        		STR_DocActa = "Borrador de Acta de Pleno";
        	}
        	else if (strOrgano.compareTo("JGOB")==0)
        	{
        		STR_DocActa = "Borrador de Acta de Junta";
        	}
        	else if (strOrgano.compareTo("COMI")==0)
        	{
        		STR_DocActa = "Borrador de Acta de Comisi�n";
        	}
        	else if (strOrgano.compareTo("MESA")==0)
        	{
        		STR_DocActa = "Borrador de Acta de Mesa";
        	}
		}
		catch(Exception e) 
		{
        	if (e instanceof ISPACRuleException)
			    throw new ISPACRuleException(e);
        	throw new ISPACRuleException("No se ha podido iniciar la lista de actas. Rellene todos los datos del Libro de Actas",e);
		}
		return true;
    }

    public boolean validate(IRuleContext rulectx) throws ISPACRuleException{
        return true;
    }

    public Object execute(IRuleContext rulectx) throws ISPACRuleException{
    	try{
			//----------------------------------------------------------------------------------------------
	        ClientContext cct = (ClientContext) rulectx.getClientContext();
	        IInvesflowAPI invesFlowAPI = cct.getAPI();
	        IEntitiesAPI entitiesAPI = invesFlowAPI.getEntitiesAPI();
	        //----------------------------------------------------------------------------------------------

	        //Borramos la lista de actas
	        String strQuery = "WHERE NUMEXP='" + rulectx.getNumExp() + "'";
	        IItemCollection collAllProps = entitiesAPI.queryEntities("SECR_ACTAS", strQuery);
	        Iterator itAllProps = collAllProps.iterator();
        	IItem iProp = null;
	        while(itAllProps.hasNext()) {
	        	iProp = ((IItem)itAllProps.next());
	        	iProp.delete(cct);
	        }
	        
	        //Obtenemos los documentos de acta con fecha superior a la indicada
	        IItem datosLibro = rulectx.getItem();
	        int year = datosLibro.getInt("YEAR");
	        strQuery="WHERE NOMBRE='"+STR_DocActa+"' and fdoc>'"+String.valueOf(year)+"-01-01'";
	        IItemCollection docs = entitiesAPI.queryEntities("SPAC_DT_DOCUMENTOS", strQuery);
	        Iterator it = docs.iterator();
	        IItem doc=null, acta=null;
	        String strId, strNombre, strNumExp;
	        Date fecha;
	        while (it.hasNext())
	        {
	        	//Generamos una entrada en la lista de actas por cada documento encontrado
	        	doc = (IItem)it.next();
	        	strId = doc.getString("ID");
	        	strNombre = doc.getString("NOMBRE");
	        	strNumExp = doc.getString("NUMEXP");
	        	IItemCollection sesion = entitiesAPI.getEntities("SECR_SESION", strNumExp);
	        	fecha = ((IItem)sesion.toList().get(0)).getDate("FECHA");
	        	acta = entitiesAPI.createEntity("SECR_ACTAS",rulectx.getNumExp());
	        	acta.set("IDDOC", strId);
	        	acta.set("NOMBRE", strNombre);
	        	acta.set("FECHA", fecha);
	        	acta.store(cct);
	        }
        	return new Boolean(true);
    		
        } catch(Exception e) {
        	if (e instanceof ISPACRuleException)
			    throw new ISPACRuleException(e);
        	throw new ISPACRuleException("No se ha podido crear la lista de actas",e);
        }
    }

	public void cancel(IRuleContext rulectx) throws ISPACRuleException{
    }
	
}