package es.dipucr.sigem.api.rule.common.avisos;

import ieci.tdw.ispac.api.IEntitiesAPI;
import ieci.tdw.ispac.api.IInvesflowAPI;
import ieci.tdw.ispac.api.errors.ISPACRuleException;
import ieci.tdw.ispac.api.item.IItem;
import ieci.tdw.ispac.api.item.IStage;
import ieci.tdw.ispac.api.rule.IRule;
import ieci.tdw.ispac.api.rule.IRuleContext;
import ieci.tdw.ispac.ispaclib.context.IClientContext;
import es.dipucr.sigem.api.rule.common.utils.ExpedientesUtil;

public class DipucrAvisoNuevaFaseRule implements IRule {
	
	public static String _NUEVAFASE_MESSAGE = "Nueva Fase";

	public boolean init(IRuleContext rulectx) throws ISPACRuleException {
		return true;
	}

	public boolean validate(IRuleContext rulectx) throws ISPACRuleException {
		return true;
	}

	public Object execute(IRuleContext rulectx) throws ISPACRuleException {
		
		try {
			
			IClientContext ctx = rulectx.getClientContext();
			IInvesflowAPI invesflowAPI = ctx.getAPI();
			IEntitiesAPI entitiesAPI = invesflowAPI.getEntitiesAPI();
						
			String sRespFase = null;
			String responsableId = "";
			
			String numexp = rulectx.getNumExp();
			
			IItem itemProcess = invesflowAPI.getProcess(numexp);
			String sRespProceso = itemProcess.getString("ID_RESP");
			
			IStage stage = invesflowAPI.getStage(rulectx.getStageId());
			sRespFase = stage.getString("ID_RESP");
			
			//Tomamos el responsable de la fase actual, si no existe, el responsable del procedimiento
			if(sRespFase != null && !sRespFase.equals("")){
				responsableId = sRespFase;
			}
			else{
				responsableId = sRespProceso;
			}
					
			
			int processId = invesflowAPI.getProcess(numexp).getInt("ID");
			String nombreProcedimiento = invesflowAPI.getProcedure(rulectx.getProcedureId()).getString("NOMBRE");
			IItem itemExpediente = ExpedientesUtil.getExpediente(ctx, numexp);
			String asunto = itemExpediente.getString("ASUNTO");
			String message = _NUEVAFASE_MESSAGE + ": Se ha iniciado una nueva fase en el expediente " + numexp +" ("+nombreProcedimiento+").<br/>Asunto: " + asunto;
			
			AvisosUtil.generarAviso(entitiesAPI, processId, numexp, message, responsableId, ctx);
		
			return true;
			
		} catch (Exception e){
			
			throw new ISPACRuleException
				("Error al insertar el aviso de nueva fase.", e);
		}
	}

	public void cancel(IRuleContext rulectx) throws ISPACRuleException {

	}
}
