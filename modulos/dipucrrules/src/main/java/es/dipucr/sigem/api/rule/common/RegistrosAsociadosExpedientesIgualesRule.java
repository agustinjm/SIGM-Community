package es.dipucr.sigem.api.rule.common;

import ieci.tdw.ispac.api.errors.ISPACException;
import ieci.tdw.ispac.api.errors.ISPACRuleException;
import ieci.tdw.ispac.api.impl.RegisterAPI;
import ieci.tdw.ispac.api.item.IItem;
import ieci.tdw.ispac.api.item.IItemCollection;
import ieci.tdw.ispac.api.rule.IRule;
import ieci.tdw.ispac.api.rule.IRuleContext;
import ieci.tdw.ispac.ispaclib.context.ClientContext;
import ieci.tdw.ispac.ispaclib.utils.StringUtils;

import java.util.Iterator;

import org.apache.log4j.Logger;

import es.dipucr.sigem.api.rule.common.utils.DocumentosUtil;
import es.dipucr.sigem.api.rule.common.utils.EntidadesAdmUtil;
import es.dipucr.sigem.api.rule.common.utils.ExpedientesUtil;
import es.dipucr.sigem.api.rule.common.utils.ParticipantesUtil;

public class RegistrosAsociadosExpedientesIgualesRule implements IRule{
	
	private static final Logger logger = Logger.getLogger(RegistrosAsociadosExpedientesIgualesRule.class);
	
	/**
	 * Ticket #352# [Teresa] SIGEM Regla para unir dos registros uno de entrada y otro de salida en diferentes expedientes. 
	 * **/

	public void cancel(IRuleContext rulectx) throws ISPACRuleException {
		
	}

	public Object execute(IRuleContext rulectx) throws ISPACRuleException {
		logger.info("INICIO - " + this.getClass().getName());
		try {
			// APIs
			ClientContext cct = (ClientContext) rulectx.getClientContext();
			
			String numexp = rulectx.getNumExp();
			String entidad = EntidadesAdmUtil.obtenerEntidad(cct);
			
			IItem item = ExpedientesUtil.getExpediente(cct, numexp);
			
			if(item != null){
				String numRegEntrada = item.getString("NREG");
				String nombre = item.getString("IDENTIDADTITULAR");
				String nif = item.getString("NIFCIFTITULAR");
				
				String dniRegEntrada = "";

				StringBuffer nombreSinDni = new StringBuffer("");
				String [] identidad = null;
				
				if(StringUtils.isNotEmpty(numRegEntrada)){
					//El nombre que se recibe es "05694855D FELIPE MOYANO ENRIQUEZ DE SALAMANCA" por lo tanto hay que quitar el dni
					if(StringUtils.isEmpty(nif)){
						//Controlar qu� hacer si el nombre es vac�o ya que la consulta de abajo va a hacer un like '%%' y esto devuelve todo
						if(StringUtils.isNotEmpty(nombre)){
							identidad = nombre.split(" ");
							//Cojo todo menos la primera posicion que el dni						
							dniRegEntrada = "";
							dniRegEntrada = identidad[0];
							//empiezo en la posicion 2 para que al final no tenga espacios en blanco
							nombreSinDni.append(identidad[1]);
											
							for(int i = 2; i < identidad.length; i++){
								nombreSinDni.append(" "+identidad[i]);
							}
						}
					}
					else{
						dniRegEntrada = nif;
						nombreSinDni = new StringBuffer(nombre);
					}
					
					IItemCollection icDoc = DocumentosUtil.getDocumentos(cct, numexp, "TP_REG = 'SALIDA'", "FDOC DESC");
					Iterator<?> it = icDoc.iterator();
					if(it.hasNext()){
						while(it.hasNext()){
							IItem itemNoti = (IItem) it.next();							
							String nRegSalida = itemNoti.getString("NREG");
							if(StringUtils.isNotEmpty(nRegSalida)){
								String destino_id = itemNoti.getString("DESTINO_ID");
								if(StringUtils.isNotEmpty(destino_id)){								
									//Comprobaci�n de que el usuario de destino id sea el mismo en el registro de entrada.
									IItemCollection icInt = ParticipantesUtil.getParticipantes(cct, numexp, "ID_EXT='"+destino_id+"'", "");
									Iterator<?> itInt = icInt.iterator();
									String nDoc = "";
									if(itInt.hasNext()){
										IItem iInter = (IItem)itInt.next();
										nDoc = iInter.getString("NDOC");
									}
									if(StringUtils.isNotEmpty(nDoc) && dniRegEntrada.equals(nDoc)){
										//Actualizar la tabla scr_regasoc de la bbdd registro.
										AccesoBBDDRegistro accsRegistro = new AccesoBBDDRegistro(entidad);
										int idRegEnt = accsRegistro.getIdReg(RegisterAPI.BOOK_TYPE_INPUT, numRegEntrada);
										int idRegSal = accsRegistro.getIdReg(RegisterAPI.BOOK_TYPE_OUTPUT, nRegSalida);
										accsRegistro.setInsertRegistroAsociado(RegisterAPI.BOOK_TYPE_INPUT, idRegEnt, RegisterAPI.BOOK_TYPE_OUTPUT, idRegSal);
									}
								}
							}
						}
					}
					else{
						rulectx.setInfoMessage("No existe registro de salida");
					}
				}
				else{
					rulectx.setInfoMessage("No existe registro de entrada");
				}
			}
			
			logger.info("FIN - " + this.getClass().getName());
		} catch (ISPACException e) {
			logger.error("Se produjo una excepci�n. " + e.getMessage(), e);
			throw new ISPACRuleException("Se produjo una excepci�n. " + e.getMessage(), e);
		}
		return new Boolean(true);
	}
	public boolean init(IRuleContext rulectx) throws ISPACRuleException {
		return true;
	}

	public boolean validate(IRuleContext rulectx) throws ISPACRuleException {
		return true;
	}

}
