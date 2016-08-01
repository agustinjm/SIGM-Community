package es.dipucr.contratacion.rule.comunicplace;

import ieci.tdw.ispac.api.IEntitiesAPI;
import ieci.tdw.ispac.api.IInvesflowAPI;
import ieci.tdw.ispac.api.errors.ISPACException;
import ieci.tdw.ispac.api.errors.ISPACRuleException;
import ieci.tdw.ispac.api.item.IItem;
import ieci.tdw.ispac.api.item.IItemCollection;
import ieci.tdw.ispac.api.rule.IRule;
import ieci.tdw.ispac.api.rule.IRuleContext;
import ieci.tdw.ispac.ispaclib.context.ClientContext;

import java.rmi.RemoteException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import es.dipucr.contratacion.client.beans.AnuncioAdjudicacionBean;
import es.dipucr.contratacion.client.beans.FormalizacionBean;
import es.dipucr.contratacion.client.beans.Garantia;
import es.dipucr.contratacion.client.beans.PersonalContacto;
import es.dipucr.contratacion.client.beans.PublicacionesOficialesBean;
import es.dipucr.contratacion.client.beans.ResultadoLicitacion;
import es.dipucr.contratacion.client.beans.SobreElectronico;
import es.dipucr.contratacion.common.DipucrFuncionesComunes;
import es.dipucr.contratacion.common.ServiciosWebContratacionFunciones;
import es.dipucr.contratacion.objeto.BOP;
import es.dipucr.contratacion.objeto.DatosContrato;
import es.dipucr.contratacion.objeto.DatosEmpresa;
import es.dipucr.contratacion.objeto.DatosLicitacion;
import es.dipucr.contratacion.objeto.DatosTramitacion;
import es.dipucr.contratacion.objeto.DiariosOficiales;
import es.dipucr.contratacion.objeto.Peticion;
import es.dipucr.contratacion.objeto.Solvencia;
import es.dipucr.contratacion.resultadoBeans.PublicationResult;
import es.dipucr.contratacion.resultadoBeans.Resultado;
import es.dipucr.contratacion.services.PlataformaContratacionProxy;
import es.dipucr.sigem.api.rule.common.utils.DipucrCommonFunctions;
import es.dipucr.sigem.api.rule.common.utils.EntidadesAdmUtil;
import es.dipucr.sigem.api.rule.common.utils.UsuariosUtil;

public class EnviarAnuncResultLicitacionPLACERule implements IRule{
	
	public static final Logger logger = Logger.getLogger(EnviarAnuncResultLicitacionPLACERule.class);

	public void cancel(IRuleContext arg0) throws ISPACRuleException {
	}

	public Object execute(IRuleContext rulectx) throws ISPACRuleException {
		try{
			
	
			
			//Compruebo que no se haya mandado antes el anuncio.
//			IItemCollection colAdjudicacion = entitiesAPI.getEntities("CONTRATACION_ADJUDICACION", rulectx.getNumExp());
//			Iterator <IItem> iterAdjudicacion = colAdjudicacion.iterator();
//			if(iterAdjudicacion.hasNext()){
//				IItem itemAdjudicacion = iterAdjudicacion.next();				
//				if(itemAdjudicacion.getString("CONTRATACION_ADJUDICACION")!=null && itemAdjudicacion.getString("CONTRATACION_ADJUDICACION").equals("NO")){
					logger.warn("INICIO!!!!!!!!!!!!!!!!!!!!");
				
					envioAnuncio(rulectx);		
					
//					logger.warn("TODO CORRECTOOOOO!!!!!!!!!!!!!!!!!!!!");
//				}
//			}
		} catch (ISPACException e) {
			logger.error("Error: " + rulectx.getNumExp() + ". " + e.getMessage(), e);
			throw new ISPACRuleException("Error: " + rulectx.getNumExp() + ". " + e.getMessage(), e);	
		}
					

		return  new Boolean(true);
	}
	

	private void envioAnuncio(IRuleContext rulectx) throws ISPACRuleException {
		try{
			// --------------------------------------------------------------------
				ClientContext cct = (ClientContext) rulectx.getClientContext();
				// --------------------------------------------------------------------
				PlataformaContratacionProxy platContratacion = new PlataformaContratacionProxy(ServiciosWebContratacionFunciones.getDireccionSW());
				
				AnuncioAdjudicacionBean resultadoLicitacion = new AnuncioAdjudicacionBean ();
				
				//Num Expediente
				resultadoLicitacion.setNumExpediente(rulectx.getNumExp());
				
				DatosContrato datContrato = DipucrFuncionesComunes.getDatosContrato(rulectx, rulectx.getNumExp());
				if(datContrato!=null){
					resultadoLicitacion.setObjetoContrato(datContrato.getObjetoContrato());
					resultadoLicitacion.setProcContratacion(datContrato.getProcedimientoContratacion());
					resultadoLicitacion.setTipoContrato(datContrato.getTipoContrato());
					resultadoLicitacion.setSubTipoContrato(datContrato.getSubTipoContrato());
					resultadoLicitacion.setTipoTramitacion(datContrato.getTipoTramitacion());
					resultadoLicitacion.setTramitacionGasto(datContrato.getTramitacionGasto());
					resultadoLicitacion.setCpv(datContrato.getCpv());
					resultadoLicitacion.setValorEstimadoContrato(datContrato.getValorEstimadoContrato());
				}
				
				DatosTramitacion datosTrami = DipucrFuncionesComunes.getDatosTramitacion(rulectx, rulectx.getNumExp());
				if(datosTrami!=null){
					//Introduccimos null porque en Diputación no tenemos este valor
					if(datosTrami.getPresentacionOfertas()!=null){
						resultadoLicitacion.setPresentacionOfertas(datosTrami.getPresentacionOfertas());
						resultadoLicitacion.setFechaPresentacionSolcitudesParticipacion(datosTrami.getPresentacionOfertas().getEndDate());
						resultadoLicitacion.setDuracionContrato(datosTrami.getDuracionContrato());
					}
				}
				
				resultadoLicitacion.setLicitador(datosTrami.getLicitador());
				resultadoLicitacion.setOfertasRecibidas(datosTrami.getOfertasRecibidas());
				
				
				Peticion peticion = DipucrFuncionesComunes.getPeticion(rulectx);
				resultadoLicitacion.setPresupuestoConIva(peticion.getPresupuestoConIva());
				resultadoLicitacion.setPresupuestoSinIva(peticion.getPresupuestoSinIva());
				
				DatosEmpresa datEmpresa = DipucrFuncionesComunes.getDatosEmpresa(rulectx, rulectx.getNumExp());
				resultadoLicitacion.setClasificacion(datEmpresa.getClasificacion());
				resultadoLicitacion.setCondLicit(datEmpresa.getCondLicit());
				resultadoLicitacion.setReqDecl(datEmpresa.getTipoDeclaracion());
				
				DatosLicitacion datosLicitacion= DipucrFuncionesComunes.getDatosLicitacion(rulectx);
				//Falta por introducir la entidad 'Criterios de adjudicación'
				resultadoLicitacion.setCriterios(datosLicitacion.getCritAdj());
				resultadoLicitacion.setTipoPresentacionOferta(datosLicitacion.getTipoPresentacionOferta());	
				resultadoLicitacion.setApliPesu(datosLicitacion.getAplicacionPres());
				resultadoLicitacion.setCriterios(datosLicitacion.getCritAdj());
				resultadoLicitacion.setVarOfert(datosLicitacion.getVariantes());
				resultadoLicitacion.setFundacionPrograma(datosLicitacion.getFundacionPrograma());
				resultadoLicitacion.setFormulaRevisionPrecios(datosLicitacion.getRevisionPrecios());
				
				SobreElectronico [] sobreElect = DipucrFuncionesComunes.getSobreElec(rulectx);
				resultadoLicitacion.setSobreElect(sobreElect);
				
				Solvencia solvencia = DipucrFuncionesComunes.getSolvencia(rulectx);
				resultadoLicitacion.setSolvenciaEconomica(solvencia.getSolvenciaEconomica());
				resultadoLicitacion.setSolvenciaTecn(solvencia.getSolvenciaTecn());	
				
				//garantias
				Garantia[] garantia = DipucrFuncionesComunes.getGarantias(rulectx);
				resultadoLicitacion.setGarantia(garantia);
				
				PersonalContacto[] persCon = DipucrFuncionesComunes.getPersonalContacto(rulectx);
				resultadoLicitacion.setPersonalContantoContratacion(persCon[0]);
				resultadoLicitacion.setPersonalContantoSecretaria(persCon[1]);
				
				//resultado y licitador
				ResultadoLicitacion licitacion = DipucrFuncionesComunes.getResultadoLicitacion(rulectx);
				resultadoLicitacion.setCodigoAdjudicacion(licitacion.getCodigoAdjudicacion());
				
				//Texto del acuerdo
				if(datosTrami.getTextoAcuerdo()!=null && datosTrami.getTextoAcuerdo()!=""){
					resultadoLicitacion.setTextoAcuerdo(datosTrami.getTextoAcuerdo());
				}
				String anuncioPublicarPLACE = "";
				
				//Documento de adjudicación
				//Compruebo que se haya seleccionado la opción de adjudicación
				//Ademas ańadir la fecha de adjudicacion y la fecha de formalización
				if(licitacion.getCodigoAdjudicacion().getValor().contains("Adjudicado")){
					
					anuncioPublicarPLACE = "AnuncioResultadoLicitacionAdjudicacion";
					
					//PONER EL DICTAMEN DE LA ADJUDICACIÓN*****************************************************************************************
					//Documento actaAdjudicacion = DipucrFuncionesComunes.getDocumento(rulectx, "Trámite Resolución", "Documentación de la Aprobación" ,"propuestaAdjudicacion");
					//resultadoLicitacion.setActaAdjudicacion(actaAdjudicacion);
				}
				if(licitacion.getCodigoAdjudicacion().getValor().contains("Formalizado")){
					anuncioPublicarPLACE = "AnuncioResultadoLicitacionFormalizacion";
					//Documento actaFormalizacion = DipucrFuncionesComunes.getDocumento(rulectx, "Contrato", "propuestaFormalizacion");
					FormalizacionBean formal = datosTrami.getFormalizacion();
					formal.setNumContrato(datContrato.getNumContrato());
					//formal.setDocContrato(actaFormalizacion);
					resultadoLicitacion.setFormalizacionContrato(formal);
				}
				else{
					
					logger.warn("No ha mandado el documento. Ya que el código de adjudicación no es el adecuado.");
				}
				
				PublicacionesOficialesBean publicacionesOficiales = null;			
				DiariosOficiales diariosOficiales =  DipucrFuncionesComunes.getDiariosOficiales(rulectx, anuncioPublicarPLACE);
				BOP bop = DipucrFuncionesComunes.getBOP(rulectx);			
				if(diariosOficiales !=null || bop!=null){
					publicacionesOficiales = new PublicacionesOficialesBean();
					if(diariosOficiales.getDoue() !=null){
						publicacionesOficiales.setEnviarDOUE(diariosOficiales.getDoue().isPublicarDOUE());
					}
					if(diariosOficiales.getBoe() !=null){
						publicacionesOficiales.setEnviarBOE(diariosOficiales.getBoe().isPublicarBOE());
					}
					if(bop!=null){
						publicacionesOficiales.setNombreOtrosDiarios(bop.getNombreBOP());
						publicacionesOficiales.setFechaPubOtrosDiarios(bop.getFechaPublicacion());
						publicacionesOficiales.setPublishURLOtrosDiarios(bop.getUrlPublicacion());
					}
					
					resultadoLicitacion.setDiarios(publicacionesOficiales);
				}
				
				resultadoLicitacion.setDiarios(publicacionesOficiales);
				
				//Envio de la petición de pliego
				String entidad = EntidadesAdmUtil.obtenerEntidad(cct);
				
				//Petición
				//String publishedByUser = UsuariosUtil.getDni(cct);
				//String publishedByUser = "99001215S";
				String publishedByUser = DipucrCommonFunctions.getVarGlobal("PUBLISHEDBYUSER");
				if(publishedByUser==null || publishedByUser.equals("")){
					publishedByUser = UsuariosUtil.getDni(cct);
				}
				Resultado resultado = platContratacion.anuncioAdjudicacion(entidad, resultadoLicitacion, publishedByUser);
				
				PublicationResult result = resultado.getPublicacion();
				
				if(result!=null){
					if(result.getResultCode().equals("OK")){					
						DipucrFuncionesComunes.crearDocPeticionOK_PLACE(resultado, rulectx, "Anuncio Resultado Licitación");
					}
		
					else{
						DipucrFuncionesComunes.errorPeticion(result, rulectx, "Anuncio Resultado Licitación");
					}
				}
				else{
					logger.error("Error al mandar el anuncio de adjudicación, contacte con el administrador: ");
					throw new ISPACRuleException("Error al mandar el anuncio de adjudicación, contacte con el administrador. ");
				}
				
		}
		catch(ISPACRuleException e){
			logger.error("Error: " + rulectx.getNumExp() + ". " + e.getMessage(), e);
			throw new ISPACRuleException("Error: " + rulectx.getNumExp() + ". " + e.getMessage(), e);	
		} catch (RemoteException e) {
			logger.error("Error: " + rulectx.getNumExp() + ". " + e.getMessage(), e);
			throw new ISPACRuleException("Error: " + rulectx.getNumExp() + ". " + e.getMessage(), e);	
		} catch (ISPACException e) {
			logger.error("Error: " + rulectx.getNumExp() + ". " + e.getMessage(), e);
			throw new ISPACRuleException("Error: " + rulectx.getNumExp() + ". " + e.getMessage(), e);	
		}
		
	}

	public boolean init(IRuleContext rulectx) throws ISPACRuleException {
		
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean validate(IRuleContext rulectx) throws ISPACRuleException {

		boolean resultado = true;
		
		try{
			
			/************************************************************************/
			ClientContext cct = (ClientContext) rulectx.getClientContext();
			IInvesflowAPI invesFlowAPI = cct.getAPI();
			IEntitiesAPI entitiesAPI = invesFlowAPI.getEntitiesAPI();
			 /***********************************************************************/
			
			//Compruebo que no se haya mandado antes el anuncio.
			IItemCollection colAdjudicacion = entitiesAPI.getEntities("CONTRATACION_ADJUDICACION", rulectx.getNumExp());
			Iterator <IItem> iterAdjudicacion = colAdjudicacion.iterator();
			if(iterAdjudicacion.hasNext()){
				IItem itemAdjudicacion = iterAdjudicacion.next();				
				if(itemAdjudicacion.getString("CONTRATACION_ADJUDICACION")!=null && itemAdjudicacion.getString("CONTRATACION_ADJUDICACION").equals("NO")){
					
			
					IItemCollection resLiciCollection = entitiesAPI.getEntities("CONTRATACION_ADJUDICACION", rulectx.getNumExp());
					Iterator <IItem> resLiciIterator = resLiciCollection.iterator();
					if(resLiciIterator.hasNext()){
						IItem resLic = resLiciIterator.next();
						if(resLic.getString("RES_LICITACION")!=null){
							String critSolv = resLic.getString("RES_LICITACION");
							String [] vcritSolv = critSolv.split(" - ");
							if(vcritSolv.length >1){
									String strQuery = "WHERE NUMEXP='" + rulectx.getNumExp() + "'";
									IItemCollection collectionTramit = entitiesAPI.queryEntities("CONTRATACION_DATOS_TRAMIT", strQuery);
									Iterator<IItem> itTra = collectionTramit.iterator();
									if (itTra.hasNext()) {
										IItem itemDatosContrato = itTra.next();
										StringBuffer mensaje = new StringBuffer("");
										if(itemDatosContrato.getDate("FECHA_ADJUDICACION")==null){
											resultado = false;
											mensaje.append("Fecha adjudicación; ");
										}
										if(itemDatosContrato.getString("TEXTO_ACUERDO")==null){
											resultado = false;
											mensaje.append("Texto del acuerdo; ");
										}
										
										if(!vcritSolv[0].equals("5") && !vcritSolv[0].equals("3") && !vcritSolv[0].equals("4")){
											if(itemDatosContrato.getString("NIF_ADJUDICATARIA")==null){
												resultado = false;
												mensaje.append("NIF; ");
											}
											if(itemDatosContrato.getString("TIPOIDENTIFICADOR")==null) {
												resultado = false;
											}
											if(itemDatosContrato.getString("EMP_ADJ_CONT")==null){
												resultado = false;
												mensaje.append("Empresa; ");
											}
											
											if(itemDatosContrato.getDate("FECHA_FIN_FORMALIZACION")==null){
												resultado = false;
												mensaje.append("Fecha fin formalización; ");
											}
											if(itemDatosContrato.getString("IMP_ADJ_CONIVA")==null){
												resultado = false;
												mensaje.append("Importe de Adjudicación (con IVA); ");
											}
											if(itemDatosContrato.getString("IMP_ADJ_SINIVA")==null){
												resultado = false;
												mensaje.append("Importe de Adjudicación (sin IVA); ");
											}
											if(itemDatosContrato.getString("DOMICILIO_NOTIF_ADJ")==null){
												resultado = false;
												mensaje.append("Domicilio de la adjudicataria a efectos de notificaciones; ");
											}
											if(itemDatosContrato.getString("CP")==null){
												resultado = false;
												mensaje.append("CP; ");
											}
											if(itemDatosContrato.getString("MOTIVACION")==null){
												resultado = false;
												mensaje.append("Motivación; ");
											}
											if(!mensaje.equals("")){
												rulectx.setInfoMessage("Falta por introducir en la entidad 'Datos de Tramitación': "+mensaje.toString());
											}
										}
										else{
											
											if(!mensaje.toString().equals("")){
												resultado = false;
												rulectx.setInfoMessage("Falta por introducir en la entidad 'Datos de Tramitación': "+mensaje.toString());
											}
											
										}
									}
							}
						}
					}
					else{
						resultado = false;
						rulectx.setInfoMessage("Falta por introducir en la entidad 'Resultado de la licitación' la resolución");
					}
				}
				else{
					resultado = false;
		        	rulectx.setInfoMessage("No se ha podido mandar el anuncio a la Plataforma de Contratación porque en la entidad 'Resultado de la Licitación'"
		        			+ " en el campo Envío anuncio es igual a SI");
				}
			}
		
		}catch(ISPACRuleException e){
			logger.error("Error: " + rulectx.getNumExp() + ". " + e.getMessage(), e);
			throw new ISPACRuleException("Error: " + rulectx.getNumExp() + ". " + e.getMessage(), e);	
		} catch (ISPACException e) {
			logger.error("Error: " + rulectx.getNumExp() + ". " + e.getMessage(), e);
			throw new ISPACRuleException("Error: " + rulectx.getNumExp() + ". " + e.getMessage(), e);	
		}

		return resultado;
	}

}
