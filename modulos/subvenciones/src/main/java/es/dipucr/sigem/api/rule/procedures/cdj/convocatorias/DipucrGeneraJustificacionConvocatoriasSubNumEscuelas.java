package es.dipucr.sigem.api.rule.procedures.cdj.convocatorias;


import ieci.tdw.ispac.api.IEntitiesAPI;
import ieci.tdw.ispac.api.errors.ISPACException;
import ieci.tdw.ispac.api.errors.ISPACRuleException;
import ieci.tdw.ispac.api.item.IItem;
import ieci.tdw.ispac.api.item.IItemCollection;
import ieci.tdw.ispac.api.rule.IRuleContext;
import ieci.tdw.ispac.ispaclib.context.IClientContext;
import ieci.tdw.ispac.ispaclib.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

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

import es.dipucr.sigem.api.rule.common.documento.DipucrAutoGeneraDocIniTramiteRule;
import es.dipucr.sigem.api.rule.common.utils.DocumentosUtil;
import es.dipucr.sigem.api.rule.common.utils.ExpedientesUtil;
import es.dipucr.sigem.api.rule.common.utils.QueryUtils;
import es.dipucr.sigem.api.rule.procedures.ConstantesString;
import es.dipucr.sigem.api.rule.procedures.Constants;

public class DipucrGeneraJustificacionConvocatoriasSubNumEscuelas extends DipucrAutoGeneraDocIniTramiteRule{

    public static final Logger LOGGER = Logger.getLogger(DipucrGeneraJustificacionConvocatoriasSubNumEscuelas.class);
    
    private IRuleContext ruleContext;
    
    public boolean init(IRuleContext rulectx) throws ISPACRuleException {
        LOGGER.info(ConstantesString.INICIO + this.getClass().getName());
        
        try{
            IClientContext cct = rulectx.getClientContext();
            
            setRuleContext(rulectx);
            
            plantilla = DocumentosUtil.getPlantillaDefecto(cct, rulectx.getTaskProcedureId());
            
            if(StringUtils.isNotEmpty(plantilla)){
                tipoDocumento = DocumentosUtil.getTipoDocumentoByPlantilla(cct, plantilla);
            }
            
            refTablas = "%TABLA1%";
        } catch(ISPACException e){
            LOGGER.error(ConstantesString.LOGGER_ERROR + " al recuperar la plantilla espec�fica del expediente: " + rulectx.getNumExp() + ". " + e.getMessage(), e);
            throw new ISPACRuleException(ConstantesString.LOGGER_ERROR + " al recuperar la plantilla espec�fica del expediente: " + rulectx.getNumExp() + ". " + e.getMessage(), e);
        }

        LOGGER.info(ConstantesString.FIN + this.getClass().getName());
        return true;
    }
    
    public void setSsVariables(IClientContext cct, IRuleContext rulectx) {
        String numexpConvocatoria = "";
        try {
            cct.setSsVariable("ANIO", "" + Calendar.getInstance().get(Calendar.YEAR));
            
            numexpConvocatoria = getRuleContext().getNumExp();
                         
            //Obtenemos el asunto de la convocatoria
            String convocatoria = "";
            IItem expConv = ExpedientesUtil.getExpediente(cct, numexpConvocatoria);
            if(expConv != null){
                convocatoria = expConv.getString("ASUNTO");
            }
            //Obtenemos el expediente de decreto
            IItemCollection expRelacionadosPadreCollection = cct.getAPI().getEntitiesAPI().queryEntities
                    (Constants.TABLASBBDD.SPAC_EXP_RELACIONADOS, "WHERE NUMEXP_PADRE='" + numexpConvocatoria + "' " + QueryUtils.EXPRELACIONADOS.ORDER_DESC);
            Iterator<?> expRelacionadosPadreIterator = expRelacionadosPadreCollection.iterator();
            String numexpDecreto = "";
            boolean encontrado = false;
            while (expRelacionadosPadreIterator.hasNext() && !encontrado){
                IItem expRel = (IItem)expRelacionadosPadreIterator.next();
                String numexpRel = expRel.getString("NUMEXP_HIJO");                
                String nombreProc = "";
                IItem exp = ExpedientesUtil.getExpediente(cct, numexpRel);
                if(exp != null ){
                    nombreProc = exp.getString("NOMBREPROCEDIMIENTO");
                    if(nombreProc.trim().toUpperCase().contains("DECRETO")){
                        numexpDecreto = numexpRel;
                        encontrado = true;
                    }
                }
            }
            
            IItemCollection decretoCollection = cct.getAPI().getEntitiesAPI().getEntities(Constants.TABLASBBDD.SGD_DECRETO, numexpDecreto);
            Iterator<?> decretoIterator = decretoCollection.iterator();
            String numDecreto = "";
            Date fechaDecreto = new Date();
            if(decretoIterator.hasNext()){
                IItem decreto = (IItem)decretoIterator.next();
                numDecreto = decreto.getInt("ANIO")+"/" +decreto.getInt("NUMERO_DECRETO");
                fechaDecreto = decreto.getDate("FECHA_DECRETO");
            }
            
            //Obtenemos el n�mero de bolet�n y la fecha
            IItemCollection expRelacionadosPadreCollectionBop = cct.getAPI().getEntitiesAPI().queryEntities
                    (Constants.TABLASBBDD.SPAC_EXP_RELACIONADOS, "WHERE NUMEXP_PADRE='" + numexpConvocatoria + 
                    "' " + QueryUtils.EXPRELACIONADOS.ORDER_ASC);
            Iterator<?> expRelacionadosPadreIteratorBop = expRelacionadosPadreCollectionBop.iterator();
            String numexpBoletin = "";
            encontrado = false;
            while (expRelacionadosPadreIteratorBop.hasNext() && !encontrado){
                IItem expRel = (IItem)expRelacionadosPadreIteratorBop.next();
                String numexpRel = expRel.getString("NUMEXP_HIJO");
                String nombreProc = "";
                IItem exp = ExpedientesUtil.getExpediente(cct, numexpRel);
                if(exp != null){
                    nombreProc = exp.getString("NOMBREPROCEDIMIENTO");                
                    if(nombreProc.trim().toUpperCase().contains("BOP")){
                        numexpBoletin = numexpRel;
                        encontrado = true;
                    }
                }
            }            
            IItemCollection boletinCollection = cct.getAPI().getEntitiesAPI().getEntities("BOP_SOLICITUD", numexpBoletin);
            Iterator<?> boletinIterator = boletinCollection.iterator();
            int numBoletin = 0;
            Date fechaBoletin = new Date();
            if(boletinIterator.hasNext()){
                IItem boletin = (IItem)boletinIterator.next();                
                fechaBoletin = boletin.getDate("FECHA_PUBLICACION");
                //Obtenemos el n�mero de bolet�n
                IItemCollection boletinesCollection = cct.getAPI().getEntitiesAPI().queryEntities("BOP_PUBLICACION", "WHERE FECHA='" + fechaBoletin + "'");
                Iterator<?> boletinesIterator = boletinesCollection.iterator();
                if(boletinesIterator.hasNext()){
                    numBoletin = ((IItem)boletinesIterator.next()).getInt("NUM_BOP");
                }
            }
            
            //Recuperamos el n�mero de informes generados ya
            String consulta = "WHERE NUMEXP IN (SELECT NUMEXP_HIJO FROM SPAC_EXP_RELACIONADOS WHERE NUMEXP_PADRE = '" +numexpConvocatoria+"') " +
                    "AND ID_TPDOC IN (SELECT ID FROM SPAC_CT_TPDOC WHERE NOMBRE='" +tipoDocumento+"')";
            IItemCollection justificacionesCollection = cct.getAPI().getEntitiesAPI().queryEntities(Constants.TABLASBBDD.SPAC_DT_DOCUMENTOS, consulta);

            cct.setSsVariable("NUM_DECRETO", numDecreto);
            cct.setSsVariable("FECHA_DECRETO", new SimpleDateFormat("dd/MM/yyyy").format(fechaDecreto));
            
            cct.setSsVariable("NUM_BOLETIN", "" + numBoletin);
            cct.setSsVariable("FECHA_BOLETIN", new SimpleDateFormat("dd/MM/yyyy").format(fechaBoletin));
            
            cct.setSsVariable("NUM_INFORME", "" +(justificacionesCollection.toList().size()+1));
            cct.setSsVariable("CONVOCATORIA", convocatoria);
        } catch (ISPACException e) {
            LOGGER.error(ConstantesString.LOGGER_ERROR + " en el expediente: " + numexpConvocatoria + ". " + e.getMessage(), e);
        }
    }

    public void deleteSsVariables(IClientContext cct) {
        try {
            cct.deleteSsVariable("ANIO");            
            cct.deleteSsVariable("NUM_DECRETO");
            cct.deleteSsVariable("FECHA_DECRETO");            
            cct.deleteSsVariable("NUM_BOLETIN");
            cct.deleteSsVariable("FECHA_BOLETIN");
            cct.deleteSsVariable("NUM_INFORME");
            cct.deleteSsVariable("CONVOCATORIA");
        } catch (ISPACException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    
    public void insertaTabla(IRuleContext rulectx, XComponent component, String refTabla, IEntitiesAPI entitiesAPI, String numexp) {
        String ayuntamiento = "";
        String cif = "";
        String motivoDenegacion = "";
        int nescuelas = 0;
        ArrayList<IItem> listaExp = new ArrayList<IItem>();
        
        try{
            //Recuperamos los expedientes a justificar
            IItemCollection expedientesJustificarCollection = entitiesAPI.queryEntities(Constants.TABLASBBDD.SPAC_EXP_RELACIONADOS, "WHERE NUMEXP_PADRE = '" + getRuleContext().getNumExp() + "'");
            Iterator<?> expedientesJustificarIterator = expedientesJustificarCollection.iterator();
            while (expedientesJustificarIterator.hasNext()){
                IItem expedienteJustificar = (IItem) expedientesJustificarIterator.next();
                numexp = expedienteJustificar.getString("NUMEXP_HIJO");

                   IItem expediente = ExpedientesUtil.getExpediente(rulectx.getClientContext(), numexp);
                   if(expediente != null){                                          
                       String estadoAdm = expediente.getString("ESTADOADM");
                       if(null != estadoAdm && "JF".equals(estadoAdm)){
                           listaExp.add(expediente);
                       }
                   }//fin if(expedientesIterator.hasNext())
            }//Fin del While expedientesJustificarIetrator.hasNext()
            if(!listaExp.isEmpty()){
                   int numFilas = listaExp.size();               
    
                //Busca la posici�n de la tabla y coloca el cursor ah�
                //Usaremos el localizador %TABLA1%
                XTextDocument xTextDocument = (XTextDocument)UnoRuntime.queryInterface(XTextDocument.class, component);
                XText xText = xTextDocument.getText();
                XSearchable xSearchable = (XSearchable) UnoRuntime.queryInterface( XSearchable.class, component);
                XSearchDescriptor xSearchDescriptor = xSearchable.createSearchDescriptor();
                xSearchDescriptor.setSearchString("%TABLA1%");
                XInterface xSearchInterface = null;
                XTextRange xSearchTextRange = null;
                xSearchInterface = (XInterface)xSearchable.findFirst(xSearchDescriptor);
                XTextTable xTable = null;
                
                if (xSearchInterface != null) {
                    //Cadena encontrada, la borro antes de insertar la tabla
                    xSearchTextRange = (XTextRange) UnoRuntime.queryInterface(XTextRange.class, xSearchInterface);
                    xSearchTextRange.setString("");
                    
                    //Inserta una tabla de 4 columnas y tantas filas
                    //como nuevas liquidaciones haya mas una de cabecera
                    XMultiServiceFactory xDocMSF = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, xTextDocument);
                    Object xObject = xDocMSF.createInstance("com.sun.star.text.TextTable");
                    xTable = (XTextTable) UnoRuntime.queryInterface(XTextTable.class, xObject);
                    
                    //A�adimos 3 filas m�s para las dos de la cabecera de la tabla y uno para la celda final
                    xTable.initialize(numFilas + 1, 4);
                    XTextContent xTextContent = (XTextContent) UnoRuntime.queryInterface(XTextContent.class, xTable);
                    xText.insertTextContent(xSearchTextRange, xTextContent, false);
    
                    colocaColumnas1(xTable);
    
                    //Rellena la cabecera de la tabla                
                    setHeaderCellText(xTable, "A1", "AYUNTAMIENTO");    
                    setHeaderCellText(xTable, "B1", "C.I.F");                
                    setHeaderCellText(xTable, "C1", "N� DE ESCUELAS");                
                    setHeaderCellText(xTable, "D1", "IMPORTE");    
                    
                    for(int i=0; i<listaExp.size(); i++){
                            
                        IItem expediente = listaExp.get(i);
                        ayuntamiento = expediente.getString("IDENTIDADTITULAR");
                        cif = expediente.getString("NIFCIFTITULAR");
                        
                        if(motivoDenegacion == null){
                            motivoDenegacion = "";
                        }
                        if(ayuntamiento == null){
                            ayuntamiento = "";
                        }
                        if(cif == null){
                            cif = "";
                        }
                        
                        double importe = 0;
                        
                        Iterator<?> expResolucion = entitiesAPI.getEntities("DPCR_RESOL_SOL_CONV_SUB", expediente.getString("NUMEXP")).iterator();
                        if(expResolucion.hasNext()){
                            IItem resolucion = (IItem) expResolucion.next();
                            try{
                                importe = Double.parseDouble(resolucion.getString("IMPORTE")==null?"0":resolucion.getString("IMPORTE"));
                            } catch(ISPACException e){ 
                                LOGGER.debug("El campo IMPORTE es nulo, vac�o o no num�rico. " + e.getMessage(), e);
                                importe = 0;
                            }
                            
                            nescuelas = resolucion.getInt("NUMESCUELAS");
                        }
                                            
                        setCellText(xTable, "A" + (i+2), ayuntamiento);
                        setCellText(xTable, "B" + (i+2), cif);
                        setCellText(xTable, "C" + (i+2), "" +nescuelas);
                        setCellText(xTable, "D" + (i+2), "" + new DecimalFormat("#,##0.00").format(importe));    
                    }//fin For listaExp
                }//fin if (xSearchInterface != null) 
            }//fin if(!listaAdm.isEmpty())
        } catch (ISPACException e) {
            LOGGER.error(ConstantesString.LOGGER_ERROR + " en el expediente: " + numexp + ". " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            LOGGER.error(ConstantesString.LOGGER_ERROR + " en el expediente: " + numexp + ". " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(ConstantesString.LOGGER_ERROR + " en el expediente: " + numexp + ". " + e.getMessage(), e);
        }
    }
    
    private void setHeaderCellText(XTextTable xTextTable, String cellName, String strText) throws UnknownPropertyException, PropertyVetoException, IllegalArgumentException, WrappedTargetException {
        XCell xCell = xTextTable.getCellByName(cellName);
        XText xCellText = (XText) UnoRuntime.queryInterface(XText.class, xTextTable.getCellByName(cellName));

        //Propiedades        
        XTextCursor xTC = xCellText.createTextCursor();
        XPropertySet xTPS = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xTC);
        xTPS.setPropertyValue("CharFontName", "Arial");
        xTPS.setPropertyValue("CharHeight", new Float(8.0));    
        xTPS.setPropertyValue("CharWeight", new Float(FontWeight.BOLD));
        xTPS.setPropertyValue("ParaAdjust", ParagraphAdjust.CENTER);
        xTPS.setPropertyValue("ParaVertAlignment", ParagraphVertAlign.BOTTOM);
        xTPS.setPropertyValue("ParaTopMargin", new Short((short)60));
        xTPS.setPropertyValue("ParaBottomMargin", new Short((short)60));
        XPropertySet xCPS = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xCell);
        xCPS.setPropertyValue("VertOrient", new Short(VertOrientation.CENTER));
        xCPS.setPropertyValue("BackColor", Integer.valueOf(0xC0C0C0));
        
        //Texto de la celda
        xCellText.setString(strText);
    }    

    private void setCellText(XTextTable xTextTable, String cellName, String strText) throws UnknownPropertyException, PropertyVetoException, IllegalArgumentException, WrappedTargetException {
        XCell xCell = xTextTable.getCellByName(cellName);        
        XText xCellText = (XText) UnoRuntime.queryInterface(XText.class, xCell);

        //Propiedades
        XTextCursor xTC = xCellText.createTextCursor();
        XPropertySet xTPS = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xTC);
        xTPS.setPropertyValue("CharFontName", "Arial");
        xTPS.setPropertyValue("CharHeight", new Float(8.0));    
        xTPS.setPropertyValue("ParaAdjust", ParagraphAdjust.CENTER);
        xTPS.setPropertyValue("ParaVertAlignment", ParagraphVertAlign.BOTTOM);
        xTPS.setPropertyValue("ParaTopMargin", new Short((short)0));
        xTPS.setPropertyValue("ParaBottomMargin", new Short((short)0));
        XPropertySet xCPS = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xCell);
        xCPS.setPropertyValue("VertOrient", new Short(VertOrientation.CENTER));

        //Texto de la celda
        xCellText.setString(strText);
    }
  
    private void colocaColumnas1(XTextTable xTextTable){
        
        XPropertySet xPS = ( XPropertySet ) UnoRuntime.queryInterface(XPropertySet.class, xTextTable);
         
        // Get table Width and TableColumnRelativeSum properties values
        int iWidth;
        try {
            iWidth = ( Integer ) xPS.getPropertyValue( "Width" );
            
            short sTableColumnRelativeSum = ( Short ) xPS.getPropertyValue( "TableColumnRelativeSum" );
             
            // Get table column separators
            Object xObj = xPS.getPropertyValue( "TableColumnSeparators" );
             
            TableColumnSeparator[] xSeparators = ( TableColumnSeparator[] )UnoRuntime.queryInterface(
                TableColumnSeparator[].class, xObj );

            
            //Calculamos el tama�o que le queremos dar a la celda
            //Se empieza colocando de la �ltima a la primera
            double dRatio = ( double ) sTableColumnRelativeSum / ( double ) iWidth;
            double dRelativeWidth = ( double ) 15000 * dRatio;
            
            // Last table column separator position
            double dPosition = sTableColumnRelativeSum - dRelativeWidth;
             
            // Set set new position for all column separators        
            //N�mero de separadores
            int i = xSeparators.length - 1;
            xSeparators[i].Position = (short) Math.ceil( dPosition );
            
            i--;            
            dRelativeWidth = ( double ) 15000 * dRatio;
            dPosition -= dRelativeWidth;                    
            xSeparators[i].Position = (short) Math.ceil( dPosition );

            i--;            
            dRelativeWidth = ( double ) 15000 * dRatio;
            dPosition -= dRelativeWidth;                    
            xSeparators[i].Position = (short) Math.ceil( dPosition );
                        
            
            // Do not forget to set TableColumnSeparators back! Otherwise, it doesn't work.
            xPS.setPropertyValue( "TableColumnSeparators", xSeparators );    
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

    private IRuleContext getRuleContext() {
        return ruleContext;
    }

    private void setRuleContext(IRuleContext ruleContext) {
        this.ruleContext = ruleContext;
    }
}