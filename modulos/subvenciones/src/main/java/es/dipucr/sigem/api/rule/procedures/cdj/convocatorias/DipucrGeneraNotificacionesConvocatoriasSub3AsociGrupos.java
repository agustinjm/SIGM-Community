package es.dipucr.sigem.api.rule.procedures.cdj.convocatorias;


import ieci.tdw.ispac.api.IEntitiesAPI;
import ieci.tdw.ispac.api.IGenDocAPI;
import ieci.tdw.ispac.api.IInvesflowAPI;
import ieci.tdw.ispac.api.IProcedureAPI;
import ieci.tdw.ispac.api.errors.ISPACException;
import ieci.tdw.ispac.api.errors.ISPACInfo;
import ieci.tdw.ispac.api.errors.ISPACRuleException;
import ieci.tdw.ispac.api.item.IItem;
import ieci.tdw.ispac.api.item.IItemCollection;
import ieci.tdw.ispac.api.messages.Messages;
import ieci.tdw.ispac.api.rule.IRule;
import ieci.tdw.ispac.api.rule.IRuleContext;
import ieci.tdw.ispac.ispaclib.context.ClientContext;
import ieci.tdw.ispac.ispaclib.context.IClientContext;
import ieci.tdw.ispac.ispaclib.gendoc.openoffice.OpenOfficeHelper;
import ieci.tdw.ispac.ispaclib.util.FileTemporaryManager;
import ieci.tdw.ispac.ispaclib.utils.MimetypeMapping;
import ieci.tdw.ispac.ispaclib.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.util.Calendar;
import com.sun.star.awt.FontWeight;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.style.ParagraphAdjust;
import com.sun.star.table.XCell;
import com.sun.star.text.ParagraphVertAlign;
import com.sun.star.text.TableColumnSeparator;
import com.sun.star.text.VertOrientation;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextTable;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XInterface;
import com.sun.star.util.XSearchDescriptor;
import com.sun.star.util.XSearchable;

import es.dipucr.sigem.api.rule.common.utils.DocumentosUtil;
import es.dipucr.sigem.api.rule.common.utils.ExpedientesUtil;
import es.dipucr.sigem.api.rule.common.utils.ParticipantesUtil;
import es.dipucr.sigem.api.rule.procedures.ConstantesString;
import es.dipucr.sigem.api.rule.procedures.Constants;

public class DipucrGeneraNotificacionesConvocatoriasSub3AsociGrupos implements IRule {

    public static final Logger LOGGER = Logger.getLogger(DipucrGeneraNotificacionesConvocatoriasSub3AsociGrupos.class);

    protected String tramite = "Notificaci�n 2�";
    protected String tipoDocumento = "Notificaci�n";
    protected String refTablas = "%TABLA1%";

    protected String plantilla = "";

    public final static String BANDAS = "BANDA";
    public final static String FOLCLORE = "AGRUPACI�N";
    public final static String POPROCK = "POP-ROCK";

    public boolean init(IRuleContext rulectx) throws ISPACRuleException {
        LOGGER.info(ConstantesString.INICIO + this.getClass().getName());
        try {
            IClientContext cct = rulectx.getClientContext();

            plantilla = DocumentosUtil.getPlantillaDefecto(cct, rulectx.getTaskProcedureId());
        } catch (ISPACException e) {
            LOGGER.error(ConstantesString.LOGGER_ERROR + " al recuperar la plantilla espec�fica del expediente: " + rulectx.getNumExp() + ". " + e.getMessage(), e);
            throw new ISPACRuleException(ConstantesString.LOGGER_ERROR + " al recuperar la plantilla espec�fica del expediente: " + rulectx.getNumExp() + ". " + e.getMessage(), e);
        }
        LOGGER.info(ConstantesString.FIN + this.getClass().getName());
        return true;
    }

    public void cancel(IRuleContext rulectx) throws ISPACRuleException {
        
    }

    public Object execute(IRuleContext rulectx) throws ISPACRuleException {
        LOGGER.info(ConstantesString.INICIO + this.getClass().getName());
        try {
            // ----------------------------------------------------------------------------------------------
            ClientContext cct = (ClientContext) rulectx.getClientContext();
            IInvesflowAPI invesFlowAPI = cct.getAPI();
            IEntitiesAPI entitiesAPI = invesFlowAPI.getEntitiesAPI();
            IGenDocAPI genDocAPI = invesFlowAPI.getGenDocAPI();
            IProcedureAPI procedureAPI = cct.getAPI().getProcedureAPI();
            // ----------------------------------------------------------------------------------------------

            String numexp = rulectx.getNumExp();
            String extractoDecreto = "";

            ArrayList<String[]> expedientesResolucion = new ArrayList<String[]>();

            String strQuery = "WHERE NUMEXP_PADRE='" + numexp + "'";
            IItemCollection expRelCol = entitiesAPI.queryEntities(Constants.TABLASBBDD.SPAC_EXP_RELACIONADOS, strQuery);
            Iterator<?> expRelIt = expRelCol.iterator();
            if (expRelIt.hasNext()) {
                while (expRelIt.hasNext()) {
                    IItem expRel = (IItem) expRelIt.next();
                    // Solo trabajamos con aquellos expedientes en estado
                    // RESOLUCION - RS
                    String numexpHijo = expRel.getString("NUMEXP_HIJO");
                    try {
                        IItem expHijo = ExpedientesUtil.getExpediente(cct, numexpHijo);

                        if (expHijo != null && ("AP".equals(expHijo.get("ESTADOADM")) || "RC".equals(expHijo.get("ESTADOADM")))){

                            Iterator<?> expResolucion = entitiesAPI.getEntities("DPCR_RESOL_SOL_CONV_SUB", numexpHijo).iterator();
                            if (expResolucion.hasNext()) {
                                IItem resolucion = (IItem) expResolucion.next();

                                double importe = 0;
                                String cifAsociacion = "";
                                String motivoDenegacion = "";
                                String nombreAsociacion = resolucion.getString("NOMBREGRUPOASOCIACION");
                                if (StringUtils.isNotEmpty(nombreAsociacion)){
                                    importe += parseDouble(numexpHijo, resolucion.getString("IMPORTE"));

                                    motivoDenegacion = resolucion.getString("MOTIVO_RECHAZO");
                                    if(StringUtils.isEmpty(motivoDenegacion)){
                                        motivoDenegacion = "";
                                    }

                                    cifAsociacion = resolucion.getString("CIFGRUPOASOCIACION");
                                    if(StringUtils.isEmpty(cifAsociacion)){
                                        cifAsociacion = "";
                                    }
                                    String[] aux = { numexpHijo, BANDAS, "" + importe, cifAsociacion, nombreAsociacion, motivoDenegacion };
                                    expedientesResolucion.add(aux);
                                }
                                
                                importe = 0;
                                cifAsociacion = "";
                                motivoDenegacion = "";
                                nombreAsociacion = resolucion.getString("NOMBREGRUPOASOCIACION2");
                                if (StringUtils.isNotEmpty(nombreAsociacion)){
                                    importe += parseDouble(numexpHijo, resolucion.getString("IMPORTE2"));

                                    motivoDenegacion = resolucion.getString("MOTIVO_RECHAZO2");
                                    if(StringUtils.isEmpty(motivoDenegacion)){
                                        motivoDenegacion = "";
                                    }
                                    cifAsociacion = resolucion.getString("CIFGRUPOASOCIACION2");
                                    if(StringUtils.isEmpty(cifAsociacion)){
                                        cifAsociacion = "";
                                    }
                                    
                                    String[] aux2 = { numexpHijo, FOLCLORE, "" + importe, cifAsociacion, nombreAsociacion, motivoDenegacion };
                                    expedientesResolucion.add(aux2);
                                }
                                
                                importe = 0;
                                cifAsociacion = "";
                                motivoDenegacion = "";
                                nombreAsociacion = resolucion.getString("NOMBREGRUPOASOCIACION3");
                                if (StringUtils.isNotEmpty(nombreAsociacion)){
                                    importe += parseDouble(numexpHijo, resolucion.getString("IMPORTE3"));

                                    motivoDenegacion = resolucion.getString("MOTIVO_RECHAZO3");
                                    if(StringUtils.isEmpty(motivoDenegacion)){
                                        motivoDenegacion = "";
                                    }
                                    cifAsociacion = resolucion.getString("CIFGRUPOASOCIACION3");
                                    if(StringUtils.isEmpty(cifAsociacion)){
                                        cifAsociacion = "";
                                    }
                                    
                                    String[] aux3 = { numexpHijo, POPROCK, "" + importe, cifAsociacion, nombreAsociacion, motivoDenegacion };
                                    expedientesResolucion.add(aux3);
                                }
                            }
                        }
                    } catch (ISPACRuleException e) {
                        LOGGER.error(ConstantesString.LOGGER_ERROR + " al recuperar los datos del expediente: " + numexpHijo + ". " + e.getMessage(), e);
                        throw new ISPACRuleException(ConstantesString.LOGGER_ERROR + " al recuperar los datos del expediente: " + numexpHijo + ". ", e);
                    } catch (ISPACException e) {
                        LOGGER.error(ConstantesString.LOGGER_ERROR + " al recuperar los datos del expediente: " + numexpHijo + ". " + e.getMessage(), e);
                        throw new ISPACRuleException(ConstantesString.LOGGER_ERROR + " al recuperar los datos del expediente: " + numexpHijo + ". ", e);
                    } catch (NumberFormatException e) {
                        LOGGER.error(ConstantesString.LOGGER_ERROR + " al recuperar los datos del expediente: " + numexpHijo + ". " + e.getMessage(), e);
                        throw new ISPACRuleException(ConstantesString.LOGGER_ERROR + " al recuperar los datos del expediente: " + numexpHijo + ". ", e);
                    }
                }
            }

            strQuery = "WHERE NUMEXP_PADRE='" + numexp + "' ORDER BY SUBSTRING(NUMEXP_HIJO,\"position\"(NUMEXP_HIJO::TEXT, '/'::TEXT)+1)::INTEGER DESC";
            IItemCollection expRelCol2 = entitiesAPI.queryEntities(Constants.TABLASBBDD.SPAC_EXP_RELACIONADOS, strQuery);
            Iterator<?> expRelIt2 = expRelCol2.iterator();
            if (expRelIt2.hasNext()) {
                while (expRelIt2.hasNext() && "".equals(extractoDecreto)) {
                    IItem expRel2 = (IItem) expRelIt2.next();
                    // Solo trabajamos con aquellos expedientes en estado
                    // RESOLUCION - RS
                    String numexpHijo = expRel2.getString("NUMEXP_HIJO");

                    if ("".equals(extractoDecreto)) {
                        IItemCollection expHijoCol2 = entitiesAPI.getEntities("SGD_DECRETO", numexpHijo);
                        Iterator<?> expHijoIt = expHijoCol2.iterator();
                        if (expHijoIt.hasNext()){
                            extractoDecreto = ((IItem) expHijoIt.next()).getString("EXTRACTO_DECRETO");
                        }
                    }
                }
            }

            if (!expedientesResolucion.isEmpty()) {
                for (int i = 0; i < expedientesResolucion.size(); i++){
                    generaNotificacion(expedientesResolucion.get(i), rulectx, cct, entitiesAPI, genDocAPI, procedureAPI, extractoDecreto);
                }
            }
        } catch (ISPACException e) {
            LOGGER.error(e.getMessage(), e);
        }

        LOGGER.info(ConstantesString.FIN + this.getClass().getName());
        return true;
    }
    
    private double parseDouble(String numexp, String valor){
        double dValor = 0;
        try {
            dValor += Double.parseDouble(valor);
        } catch (NumberFormatException e) {
            LOGGER.error(ConstantesString.LOGGER_ERROR + " al recuperar el importe del expediente: " + numexp + ", " + e.getMessage(), e);
            dValor = 0;
        }
        return dValor;
    }

    private void generaNotificacion(String[] datos, IRuleContext rulectx, ClientContext cct, IEntitiesAPI entitiesAPI, IGenDocAPI genDocAPI,
            IProcedureAPI procedureAPI, String extractoDecreto) {
        try {
            IItem entityDocument = null;
            int documentId = 0;
            int documentTypeId = 0;
            int templateId = 0;
            int taskId = rulectx.getTaskId();
            Object connectorSession = null;
            String sFileTemplate = null;

            String nombre = "";
            String dirnot = "";
            String cPostal = "";
            String localidad = "";
            String caut = "";
            String recurso = "";
            String idExt = "";

            String numexp = datos[0];
            String tipo = datos[1];

            connectorSession = genDocAPI.createConnectorSession();

            IItem processTask = entitiesAPI.getTask(rulectx.getTaskId());
            int idTramCtl = processTask.getInt("ID_TRAM_CTL");

            IItemCollection taskTpDocCollection = (IItemCollection) procedureAPI.getTaskTpDoc(idTramCtl);
            Iterator<?> it = taskTpDocCollection.iterator();
            while (it.hasNext()) {
                IItem taskTpDoc = (IItem) it.next();
                if ((taskTpDoc.getString("CT_TPDOC:NOMBRE").trim()).equalsIgnoreCase((tipoDocumento).trim())) {
                    documentTypeId = taskTpDoc.getInt("TASKTPDOC:ID_TPDOC");
                }
            }
            cct.beginTX();
            // Comprobamos que haya encontrado el Tipo de documento
            if (documentTypeId != 0) {
                // Comprobar que el tipo de documento tiene asociado una
                // plantilla
                IItemCollection tpDocsTemplatesCollection = (IItemCollection) procedureAPI.getTpDocsTemplates(documentTypeId);
                if (tpDocsTemplatesCollection == null || tpDocsTemplatesCollection.toList().isEmpty()) {
                    throw new ISPACInfo(Messages.getString(ConstantesString.LOGGER_ERROR + ".decretos.acuses.tpDocsTemplates"));
                } else {
                    Iterator<?> docs = tpDocsTemplatesCollection.iterator();
                    boolean encontrado = false;
                    while (docs.hasNext() && !encontrado) {
                        IItem tpDocsTemplate = (IItem) docs.next();
                        if (((String) tpDocsTemplate.get("NOMBRE")).trim().equalsIgnoreCase(plantilla.trim())) {
                            templateId = tpDocsTemplate.getInt("ID");
                            encontrado = true;
                        }
                    }

                    IItemCollection tramitesCollection = cct.getAPI().getEntitiesAPI()
                            .queryEntities(Constants.TABLASBBDD.SPAC_DT_TRAMITES, "WHERE NUMEXP='" + rulectx.getNumExp() + "' AND NOMBRE='" + tramite + "'");
                    List<?> tramitesList = tramitesCollection.toList();

                    // Recuperamos el participante del expediente que estamos
                    // resolviendo
                    IItemCollection participantesCollection = ParticipantesUtil.getParticipantes(cct, numexp, "", "");
                    Iterator<?> participantesIterator = participantesCollection.iterator();
                    if (participantesIterator.hasNext()) {
                        IItem participante = (IItem) participantesIterator.next();
                        if (participante != null) {
                            // A�adir a la session los datos para poder utilizar
                            // <ispactag sessionvar='var'> en la plantilla
                            if ((String) participante.get("NOMBRE") != null) {
                                nombre = (String) participante.get("NOMBRE");
                            } else {
                                nombre = "";
                            }
                            if ((String) participante.get("DIRNOT") != null) {
                                dirnot = (String) participante.get("DIRNOT");
                            } else {
                                dirnot = "";
                            }
                            if ((String) participante.get("C_POSTAL") != null) {
                                cPostal = (String) participante.get("C_POSTAL");
                            } else {
                                cPostal = "";
                            }
                            if ((String) participante.get("LOCALIDAD") != null) {
                                localidad = (String) participante.get("LOCALIDAD");
                            } else {
                                localidad = "";
                            }
                            if ((String) participante.get("CAUT") != null) {
                                caut = (String) participante.get("CAUT");
                            } else {
                                caut = "";
                            }
                            if ((String) participante.get("RECURSO") != null) {
                                recurso = (String) participante.get("RECURSO");
                            } else {
                                recurso = "";
                            }
                            /**
                             * INICIO[Teresa] Ticket#106#: a�adir el campo
                             * id_ext
                             * **/
                            if ((String) participante.get("ID_EXT") != null) {
                                idExt = (String) participante.get("ID_EXT");
                            } else {
                                idExt = "";
                            }
                            /**
                             * FIN[Teresa] Ticket#106#: a�adir el campo id_ext
                             * **/

                            // Obtener el sustituto del recurso en la tabla
                            // SPAC_VLDTBL_RECURSOS
                            String sqlQueryPart = "WHERE VALOR = '" + recurso + "'";
                            IItemCollection colRecurso = entitiesAPI.queryEntities("DPCR_RECURSOS", sqlQueryPart);
                            if (colRecurso.iterator().hasNext()) {
                                IItem iRecurso = (IItem) colRecurso.iterator().next();
                                recurso = iRecurso.getString("SUSTITUTO");
                            }
                            /**
                             * INICIO ##Ticket #172 SIGEM decretos y secretaria,
                             * modificar el recurso
                             * **/
                            if ("".equals(recurso)) {
                                recurso += es.dipucr.sigem.api.rule.procedures.Constants.SECRETARIAPROC.sinRECUSO;
                            } else {
                                recurso += es.dipucr.sigem.api.rule.procedures.Constants.SECRETARIAPROC.conRECUSO;
                            }

                            cct.setSsVariable("ANIO", "" + Calendar.getInstance().get(Calendar.YEAR));
                            cct.setSsVariable("NRESOLUCIONPARCIAL", "" + (tramitesList.size()));
                            cct.setSsVariable("NOMBRE_TRAMITE", processTask.getString("NOMBRE"));
                            cct.setSsVariable("NOMBRETRABAJADOR", nombre);
                            cct.setSsVariable("NOMBREBENEFICIARIO", idExt);
                            cct.setSsVariable("NUMEXPSOLICITUD", numexp);
                            cct.setSsVariable("RECURSO", recurso);

                            cct.setSsVariable("NOMBRE", nombre);
                            cct.setSsVariable("DIRNOT", dirnot);
                            cct.setSsVariable("C_POSTAL", cPostal);
                            cct.setSsVariable("LOCALIDAD", localidad);
                            cct.setSsVariable("CAUT", caut);

                            String estadoAdm = "";

                            IItem expediente = ExpedientesUtil.getExpediente(cct, numexp);

                            if (expediente != null) {
                                estadoAdm = expediente.getString("ESTADOADM");
                            }

                            String textoResol = "";

                            if ("AP".equals(estadoAdm)) {
                                textoResol = "El Diputado Delegado del �rea de Cultura, Deportes, Juventud y Participaci�n propone la concesi�n de subvenci�n, conforme al siguiente detalle: ";
                            } else {
                                textoResol = "El Diputado Delegado del �rea de Cultura, Deportes, Juventud y Participaci�n propone la denegaci�n de la siguiente solicitud, por el motivo que se indica: ";
                            }

                            cct.setSsVariable("TEXTO_RESOLUCION", textoResol);
                            cct.setSsVariable("EXTRACTO_DECRETO", extractoDecreto);

                            String cabecera = "";
                            String textoModificado = "";
                            String textoCache = "";
                            String textoCacheCant = "";
                            String textoPagoAyto = "";
                            String textoPagoAyto1 = "";
                            String textoPagoAyto2 = "";
                            String textoPagoAyto3 = "";
                            String textoPagoDipu = "";
                            String textoPagoDipu1 = "";
                            String textoPagoDipu2 = "";
                            String textoPagoDipu3 = "";

                            IItemCollection datosDineroCollection = entitiesAPI.getEntities("DPCR_DATOS_CONV_CULT_PROV", rulectx.getNumExp());

                            Iterator<?> datosDineroIterator = datosDineroCollection.iterator();
                            if (datosDineroIterator.hasNext()) {
                                IItem datosDinero = (IItem) datosDineroIterator.next();

                                textoCache = "CACH� BANDAS";
                                textoPagoAyto = "PAGO DEL AYUNTAMIENTO";
                                textoPagoDipu = "PAGO DE LA DIPUTACI�N";

                                if (tipo.equals(BANDAS)) {
                                    cabecera = "Bandas de M�sica";
                                    textoModificado = datosDinero.getString("TEXTO_MODIFICADO1");
                                    textoCacheCant = new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_CACHE_CANT1")) + " �";
                                    textoPagoAyto1 = "TIPO A) " + new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_AYTO11")) + " �";
                                    textoPagoAyto2 = "TIPO B) " + new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_AYTO12")) + " �";
                                    textoPagoAyto3 = "TIPO C) " + new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_AYTO13")) + " �";
                                    textoPagoDipu1 = new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_DIPU11")) + " �";
                                    textoPagoDipu2 = new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_DIPU12")) + " �";
                                    textoPagoDipu3 = new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_DIPU13")) + " �";
                                } else if (tipo.equals(FOLCLORE)) {
                                    cabecera = "Agrupaciones Folcl�ricas";
                                    textoModificado = datosDinero.getString("TEXTO_MODIFICADO2");
                                    textoCacheCant = new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_CACHE_CANT2")) + " �";
                                    textoPagoAyto1 = "TIPO A) " + new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_AYTO21")) + " �";
                                    textoPagoAyto2 = "TIPO B) " + new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_AYTO22")) + " �";
                                    textoPagoAyto3 = "TIPO C) " + new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_AYTO23")) + " �";
                                    textoPagoDipu1 = new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_DIPU21")) + " �";
                                    textoPagoDipu2 = new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_DIPU22")) + " �";
                                    textoPagoDipu3 = new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_DIPU23")) + " �";
                                } else if (tipo.equals(POPROCK)) {
                                    cabecera = "Pop-Rock";
                                    textoModificado = datosDinero.getString("TEXTO_MODIFICADO3");
                                    textoCacheCant = new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_CACHE_CANT3")) + " �";
                                    textoPagoAyto1 = "TIPO A) " + new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_AYTO31")) + " �";
                                    textoPagoAyto2 = "TIPO B) " + new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_AYTO32")) + " �";
                                    textoPagoAyto3 = "TIPO C) " + new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_AYTO33")) + " �";
                                    textoPagoDipu1 = new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_DIPU31")) + " �";
                                    textoPagoDipu2 = new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_DIPU32")) + " �";
                                    textoPagoDipu3 = new DecimalFormat("#,##0.00").format(datosDinero.getDouble("TEXTO_PAGO_DIPU33")) + " �";
                                }
                            }
                            cct.setSsVariable("CABECERA", cabecera);
                            cct.setSsVariable("TEXTO_MODIFICACION", textoModificado);
                            cct.setSsVariable("TEXTO_CACHE", textoCache);
                            cct.setSsVariable("textoCacheCant", textoCacheCant);
                            cct.setSsVariable("textoPagoAyto", textoPagoAyto);
                            cct.setSsVariable("textoPagoAyto1", textoPagoAyto1);
                            cct.setSsVariable("textoPagoAyto2", textoPagoAyto2);
                            cct.setSsVariable("textoPagoAyto3", textoPagoAyto3);
                            cct.setSsVariable("textoPagoDipu", textoPagoDipu);
                            cct.setSsVariable("textoPagoDipu1", textoPagoDipu1);
                            cct.setSsVariable("textoPagoDipu2", textoPagoDipu2);
                            cct.setSsVariable("textoPagoDipu3", textoPagoDipu3);

                            connectorSession = genDocAPI.createConnectorSession();

                            IItem entityDocumentT = genDocAPI.createTaskDocument(taskId, documentTypeId);
                            int documentIdT = entityDocumentT.getKeyInt();

                            IItem entityTemplateT = genDocAPI.attachTaskTemplate(connectorSession, taskId, documentIdT, templateId);

                            String infoPagT = entityTemplateT.getString("INFOPAG");
                            entityTemplateT.store(cct);

                            entityDocument = genDocAPI.createTaskDocument(taskId, documentTypeId);
                            documentId = entityDocument.getKeyInt();

                            sFileTemplate = DocumentosUtil.getFile(cct, infoPagT, null, null).getName();

                            // Generar el documento a partir la plantilla
                            IItem entityTemplate = genDocAPI.attachTaskTemplate(connectorSession, taskId, documentId, templateId, sFileTemplate);

                            // Referencia al fichero del documento en el gestor
                            // documental
                            String docref = entityTemplate.getString("INFOPAG");
                            String sMimetype = genDocAPI.getMimeType(connectorSession, docref);
                            entityTemplate.set("EXTENSION", MimetypeMapping.getExtension(sMimetype));
                            String templateDescripcion = entityTemplate.getString("DESCRIPCION");
                            templateDescripcion = templateDescripcion + " - " + numexp + " - " + tipo;
                            entityTemplate.set("DESCRIPCION", templateDescripcion);
                            entityTemplate.set("DESTINO", nombre);
                            entityTemplate.set("DESTINO_ID", idExt);

                            entityTemplate.store(cct);

                            cct.deleteSsVariable("ANIO");
                            cct.deleteSsVariable("NRESOLUCIONPARCIAL");
                            cct.deleteSsVariable("NOMBRE_TRAMITE");
                            cct.deleteSsVariable("NOMBRETRABAJADOR");
                            cct.deleteSsVariable("NOMBREBENEFICIARIO");
                            cct.deleteSsVariable("NUMEXPSOLICITUD");
                            cct.deleteSsVariable("RECURSO");

                            cct.deleteSsVariable("NOMBRE");
                            cct.deleteSsVariable("DIRNOT");
                            cct.deleteSsVariable("C_POSTAL");
                            cct.deleteSsVariable("LOCALIDAD");
                            cct.deleteSsVariable("CAUT");

                            cct.deleteSsVariable("TEXTO_RESOLUCION");
                            cct.deleteSsVariable("EXTRACTO_DECRETO");

                            cct.deleteSsVariable("CABECERA");
                            cct.deleteSsVariable("TEXTO_MODIFICACION");
                            cct.deleteSsVariable("TEXTO_CACHE");
                            cct.deleteSsVariable("textoCacheCant");
                            cct.deleteSsVariable("textoPagoAyto");
                            cct.deleteSsVariable("textoPagoAyto1");
                            cct.deleteSsVariable("textoPagoAyto2");
                            cct.deleteSsVariable("textoPagoAyto3");
                            cct.deleteSsVariable("textoPagoDipu");
                            cct.deleteSsVariable("textoPagoDipu1");
                            cct.deleteSsVariable("textoPagoDipu2");
                            cct.deleteSsVariable("textoPagoDipu3");

                            if (null != refTablas && !"".equals(refTablas)) {
                                insertaTablas(genDocAPI, docref, rulectx, documentId, refTablas, entitiesAPI, datos);
                            }

                            entityTemplateT.delete(cct);
                            entityDocumentT.delete(cct);

                            DocumentosUtil.deleteFile(sFileTemplate);
                        }
                    }
                }
            }
            cct.endTX(true);
        } catch (ISPACRuleException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (ISPACException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void insertaTablas(IGenDocAPI gendocAPI, String docref, IRuleContext rulectx, int documentId, String string, IEntitiesAPI entitiesAPI, String[] datos) {

        Object connectorSession = null;
        OpenOfficeHelper ooHelper = null;
        try {
            // Abre el documento
            String extension = "odt";
            String fileName = FileTemporaryManager.getInstance().newFileName("." + extension);

            fileName = FileTemporaryManager.getInstance().getFileTemporaryPath() + "/" + fileName;

            OutputStream out = new FileOutputStream(fileName);
            connectorSession = gendocAPI.createConnectorSession();
            gendocAPI.getDocument(connectorSession, docref, out);
            File file = new File(fileName);
            ooHelper = OpenOfficeHelper.getInstance();
            XComponent xComponent = ooHelper.loadDocument("file://" + fileName);

            String[] refTabla = refTablas.split(",");
            for (int i = 0; i < refTabla.length; i++) {
                insertaTabla(rulectx, xComponent, refTabla[i], entitiesAPI, datos);
            }

            // Guarda el documento
            String fileNameOut = FileTemporaryManager.getInstance().newFileName(".odt");
            fileNameOut = FileTemporaryManager.getInstance().getFileTemporaryPath() + "/" + fileNameOut;
            String mime = "application/vnd.oasis.opendocument.text";
            OpenOfficeHelper.saveDocument(xComponent, "file://" + fileNameOut, "");
            File fileOut = new File(fileNameOut);
            InputStream in = new FileInputStream(fileOut);
            gendocAPI.setDocument(connectorSession, documentId, docref, in, (int) (fileOut.length()), mime);

            // Borra archivos temporales
            file.delete();
            fileOut.delete();
            DocumentosUtil.deleteFile(fileName);
            if (in != null){
                in.close();
            }
            if (out != null){
                out.close();
            }
            ooHelper.dispose();
        } catch (ISPACException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } catch (java.lang.Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public boolean validate(IRuleContext rulectx) throws ISPACRuleException {
        return true;
    }

    public void insertaTabla(IRuleContext rulectx, XComponent component, String refTabla, IEntitiesAPI entitiesAPI, String[] datos) {
        String ayuntamiento = "";
        String nreg = "";
        String cif = "";
        String proyecto = "";
        String motivoDenegacion = "";
        String cifAsociacion = "";
        String nombreAsociacion = "";

        try {

            String numexp = datos[0];
            String importe = datos[2];
            cifAsociacion = datos[3];
            nombreAsociacion = datos[4];
            motivoDenegacion = datos[5];
            
            boolean rechazado = StringUtils.isNotEmpty(motivoDenegacion);

            IItem expediente = ExpedientesUtil.getExpediente(rulectx.getClientContext(), numexp);

            String tituloCol4 = "";
            if (rechazado) {
                tituloCol4 = "MOTIVO DENEGACI�N";
            } else {
                tituloCol4 = "IMPORTE";
            }

            int numFilas = 1;

            ayuntamiento = expediente.getString("IDENTIDADTITULAR");
            nreg = expediente.getString("NREG");
            cif = expediente.getString("NIFCIFTITULAR");

            if (StringUtils.isEmpty(ayuntamiento)){
                ayuntamiento = "";
            }
            if (StringUtils.isEmpty(cif)){
                cif = "";
            }
            proyecto = "";

            IItem solicitud = (IItem) entitiesAPI.getEntities("DPCR_SOL_CONV_SUB", expediente.getString("NUMEXP")).iterator().next();
            proyecto = solicitud.getString("FINALIDAD");

            if (StringUtils.isEmpty(proyecto)){
                proyecto = "";
            }

            // Busca la posici�n de la tabla y coloca el cursor ah�
            // Usaremos el localizador %TABLA1%
            XTextDocument xTextDocument = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, component);
            XText xText = xTextDocument.getText();
            XSearchable xSearchable = (XSearchable) UnoRuntime.queryInterface(XSearchable.class, component);
            XSearchDescriptor xSearchDescriptor = xSearchable.createSearchDescriptor();
            xSearchDescriptor.setSearchString("%TABLA1%");
            XInterface xSearchInterface = null;
            XTextRange xSearchTextRange = null;
            xSearchInterface = (XInterface) xSearchable.findFirst(xSearchDescriptor);
            XTextTable xTable = null;

            if (null != xSearchInterface) {
                // Cadena encontrada, la borro antes de insertar la tabla
                xSearchTextRange = (XTextRange) UnoRuntime.queryInterface(XTextRange.class, xSearchInterface);
                xSearchTextRange.setString("");

                // Inserta una tabla de 4 columnas y tantas filas
                // como nuevas liquidaciones haya mas una de cabecera
                XMultiServiceFactory xDocMSF = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, xTextDocument);
                Object xObject = xDocMSF.createInstance("com.sun.star.text.TextTable");
                xTable = (XTextTable) UnoRuntime.queryInterface(XTextTable.class, xObject);

                // A�adimos 3 filas m�s para las dos de la cabecera de la tabla
                // y uno para la celda final
                xTable.initialize(numFilas + 1, 5);
                XTextContent xTextContent = (XTextContent) UnoRuntime.queryInterface(XTextContent.class, xTable);
                xText.insertTextContent(xSearchTextRange, xTextContent, false);

                if (!rechazado) {
                    colocaColumnas1(xTable);
                } else {
                    colocaColumnas2(xTable);
                }

                // Rellena la cabecera de la tabla
                setHeaderCellText(xTable, "A1", "N�M. REGISTRO");
                setHeaderCellText(xTable, "B1", "AYUNTAMIENTO");
                setHeaderCellText(xTable, "C1", "GRUPO / ASOCIACI�N");
                setHeaderCellText(xTable, "D1", "C.I.F");
                setHeaderCellText(xTable, "E1", tituloCol4);

                if (!rechazado) {
                    setCellText(xTable, "A" +  (2), nreg);
                    setCellText(xTable, "B" +  (2), ayuntamiento);
                    setCellText(xTable, "C" +  (2), nombreAsociacion);
                    setCellText(xTable, "D" +  (2), cifAsociacion);       
                    setCellText(xTable, "E" +  (2), "" + importe);

                } else {
                    setCellText(xTable, "A" +  (2), nreg);
                    setCellText(xTable, "B" +  (2), ayuntamiento);
                    setCellText(xTable, "C" +  (2), nombreAsociacion);
                    setCellText(xTable, "D" +  (2), cifAsociacion);
                    setCellText(xTable, "E" +  (2), motivoDenegacion);
                } 
            }
        } catch (ISPACException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void setHeaderCellText(XTextTable xTextTable, String cellName, String strText) throws UnknownPropertyException, PropertyVetoException, IllegalArgumentException, WrappedTargetException {
        XCell xCell = xTextTable.getCellByName(cellName);
        XText xCellText = (XText) UnoRuntime.queryInterface(XText.class, xTextTable.getCellByName(cellName));

        // Propiedades
        XTextCursor xTC = xCellText.createTextCursor();
        XPropertySet xTPS = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xTC);
        xTPS.setPropertyValue("CharFontName", "Arial");
        xTPS.setPropertyValue("CharHeight", new Float(8.0));
        xTPS.setPropertyValue("CharWeight", new Float(FontWeight.BOLD));
        xTPS.setPropertyValue("ParaAdjust", ParagraphAdjust.CENTER);
        xTPS.setPropertyValue("ParaVertAlignment", ParagraphVertAlign.BOTTOM);
        xTPS.setPropertyValue("ParaTopMargin", new Short((short) 60));
        xTPS.setPropertyValue("ParaBottomMargin", new Short((short) 60));
        XPropertySet xCPS = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xCell);
        xCPS.setPropertyValue("VertOrient", new Short(VertOrientation.CENTER));
        xCPS.setPropertyValue("BackColor", Integer.valueOf(0xC0C0C0));

        // Texto de la celda
        xCellText.setString(strText);
    }

    private void setCellText(XTextTable xTextTable, String cellName, String strText) throws UnknownPropertyException, PropertyVetoException, IllegalArgumentException, WrappedTargetException {
        XCell xCell = xTextTable.getCellByName(cellName);
        XText xCellText = (XText) UnoRuntime.queryInterface(XText.class, xCell);

        // Propiedades
        XTextCursor xTC = xCellText.createTextCursor();
        XPropertySet xTPS = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xTC);
        xTPS.setPropertyValue("CharFontName", "Arial");
        xTPS.setPropertyValue("CharHeight", new Float(8.0));
        xTPS.setPropertyValue("ParaAdjust", ParagraphAdjust.CENTER);
        xTPS.setPropertyValue("ParaVertAlignment", ParagraphVertAlign.BOTTOM);
        xTPS.setPropertyValue("ParaTopMargin", new Short((short) 0));
        xTPS.setPropertyValue("ParaBottomMargin", new Short((short) 0));
        XPropertySet xCPS = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xCell);
        xCPS.setPropertyValue("VertOrient", new Short(VertOrientation.CENTER));

        // Texto de la celda
        xCellText.setString(strText);
    }

    private void colocaColumnas1(XTextTable xTextTable) {

        XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xTextTable);

        // Get table Width and TableColumnRelativeSum properties values
        int iWidth;
        try {
            iWidth = (Integer) xPS.getPropertyValue("Width");

            short sTableColumnRelativeSum = (Short) xPS.getPropertyValue("TableColumnRelativeSum");

            // Get table column separators
            Object xObj = xPS.getPropertyValue("TableColumnSeparators");

            TableColumnSeparator[] xSeparators = (TableColumnSeparator[]) UnoRuntime.queryInterface(TableColumnSeparator[].class, xObj);

            // Calculamos el tama�o que le queremos dar a la celda
            // Se empieza colocando de la �ltima a la primera
            double dRatio = (double) sTableColumnRelativeSum / (double) iWidth;
            double dRelativeWidth = (double) 15000 * dRatio;

            // Last table column separator position
            double dPosition = sTableColumnRelativeSum - dRelativeWidth;

            // Set set new position for all column separators
            // N�mero de separadores
            int i = xSeparators.length - 1;
            xSeparators[i].Position = (short) Math.ceil(dPosition);

            i--;
            dRelativeWidth = (double) 15000 * dRatio;
            dPosition -= dRelativeWidth;
            xSeparators[i].Position = (short) Math.ceil(dPosition);

            i--;
            dRelativeWidth = (double) 40000 * dRatio;
            dPosition -= dRelativeWidth;
            xSeparators[i].Position = (short) Math.ceil(dPosition);

            // Do not forget to set TableColumnSeparators back! Otherwise, it
            // doesn't work.
            xPS.setPropertyValue("TableColumnSeparators", xSeparators);
        } catch (UnknownPropertyException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (WrappedTargetException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (PropertyVetoException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void colocaColumnas2(XTextTable xTextTable) {

        XPropertySet xPS = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xTextTable);

        // Get table Width and TableColumnRelativeSum properties values
        int iWidth;
        try {
            iWidth = (Integer) xPS.getPropertyValue("Width");

            short sTableColumnRelativeSum = (Short) xPS.getPropertyValue("TableColumnRelativeSum");

            // Get table column separators
            Object xObj = xPS.getPropertyValue("TableColumnSeparators");

            TableColumnSeparator[] xSeparators = (TableColumnSeparator[]) UnoRuntime.queryInterface(TableColumnSeparator[].class, xObj);

            // Calculamos el tama�o que le queremos dar a la celda
            // Se empieza colocando de la �ltima a la primera
            double dRatio = (double) sTableColumnRelativeSum / (double) iWidth;
            double dRelativeWidth = (double) 30000 * dRatio;

            // Last table column separator position
            double dPosition = sTableColumnRelativeSum - dRelativeWidth;

            // Set set new position for all column separators
            // N�mero de separadores
            int i = xSeparators.length - 1;
            xSeparators[i].Position = (short) Math.ceil(dPosition);

            i--;
            dRelativeWidth = (double) 15000 * dRatio;
            dPosition -= dRelativeWidth;
            xSeparators[i].Position = (short) Math.ceil(dPosition);

            i--;
            dRelativeWidth = (double) 30000 * dRatio;
            dPosition -= dRelativeWidth;
            xSeparators[i].Position = (short) Math.ceil(dPosition);

            i--;
            dRelativeWidth = (double) 15000 * dRatio;
            dPosition -= dRelativeWidth;
            xSeparators[i].Position = (short) Math.ceil(dPosition);

            // Do not forget to set TableColumnSeparators back! Otherwise, it
            // doesn't work.
            xPS.setPropertyValue("TableColumnSeparators", xSeparators);
        } catch (UnknownPropertyException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (WrappedTargetException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (PropertyVetoException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}