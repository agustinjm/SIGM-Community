package es.dipucr.contratacion.dao.procedure;



import ieci.tdw.ispac.api.errors.ISPACException;
import ieci.tdw.ispac.ispaclib.dao.ObjectDAO;
import ieci.tdw.ispac.ispaclib.dao.idsequences.IdSequenceMgr;
import ieci.tdw.ispac.ispaclib.db.DbCnt;


public class ContratacionDatosContratoSDAO extends ObjectDAO
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final String TABLENAME 	= "CONTRATACION_DATOS_CONTRATO_S";
	//Para calcular el id de la secuencia Math.abs("CONTRATACION_DATOS_LIC_S".hashCode())
	static final String IDSEQUENCE 	= "SPAC_SQ_520856832";
	static final String IDKEY = "ID";

	/**
	 * @throws ISPACException
	 */
	public ContratacionDatosContratoSDAO() throws ISPACException	{
		super(null);
	}
	
	/**
	 * @param cnt
	 * @throws ISPACException
	 */
	public ContratacionDatosContratoSDAO(DbCnt cnt) throws ISPACException {
		super(cnt, null);
	}
	
	/**
	 * @throws ISPACException
	 */
	public ContratacionDatosContratoSDAO(DbCnt cnt, int id) throws ISPACException {
		 super(cnt,id,null);
	}

	public String getTableName()
	{
		return TABLENAME;
	}

	protected String getDefaultSQL(int nActionDAO) throws ISPACException
    {
	    return " WHERE " + IDKEY + " = " + getInt(IDKEY);
    }

	public String getKeyName() throws ISPACException
	{
	    return IDKEY;
	}

	public void newObject(DbCnt cnt) throws ISPACException
	{
	    set(IDKEY,IdSequenceMgr.getIdSequence(cnt,IDSEQUENCE));
	}
}