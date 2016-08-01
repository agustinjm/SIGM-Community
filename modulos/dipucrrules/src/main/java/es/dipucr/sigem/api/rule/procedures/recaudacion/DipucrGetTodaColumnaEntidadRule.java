package es.dipucr.sigem.api.rule.procedures.recaudacion;

import ieci.tdw.ispac.api.IEntitiesAPI;
import ieci.tdw.ispac.api.IInvesflowAPI;
import ieci.tdw.ispac.api.errors.ISPACRuleException;
import ieci.tdw.ispac.api.item.IItem;
import ieci.tdw.ispac.api.item.IItemCollection;
import ieci.tdw.ispac.api.rule.IRule;
import ieci.tdw.ispac.api.rule.IRuleContext;
import ieci.tdw.ispac.ispaclib.context.ClientContext;
import ieci.tdw.ispac.ispaclib.utils.StringUtils;

import java.util.Iterator;

import org.apache.log4j.Logger;

public class DipucrGetTodaColumnaEntidadRule implements IRule {
	private static final Logger logger = Logger
			.getLogger(DipucrGetTodaColumnaEntidadRule.class);

	public boolean init(IRuleContext rulectx) throws ISPACRuleException {
		return true;
	}

	public boolean validate(IRuleContext rulectx) throws ISPACRuleException {
		return true;
	}

	@SuppressWarnings("rawtypes")
	public Object execute(IRuleContext rulectx) throws ISPACRuleException {
		try {
			// ----------------------------------------------------------------------------------------------
			ClientContext cct = (ClientContext) rulectx.getClientContext();
			IInvesflowAPI invesFlowAPI = cct.getAPI();
			IEntitiesAPI entitiesAPI = invesFlowAPI.getEntitiesAPI();
			// ----------------------------------------------------------------------------------------------
			logger.info("INICIO - DipucrGetTodaColumnaEntidadRule");
			StringBuffer listado = new StringBuffer(); // Listado de
														// liquidaciones
			String entidad = rulectx.get("entity");
			String columna = rulectx.get("patronCol");
			if (!StringUtils.isEmpty(entidad)) {
				String strQuery = "WHERE NUMEXP = '" + rulectx.getNumExp()
						+ "'";
				IItemCollection collection = entitiesAPI.queryEntities(entidad,
						strQuery);
				Iterator it = collection.iterator();
				while (it.hasNext()) {
					IItem item = ((IItem) it.next());
					String nLiq = item.getString(columna);
					if (nLiq != null && nLiq.length() > 0) {						
						listado.append(nLiq);					
					}
					listado.append("\n");
				}
			}
			logger.info("FIN - DipucrGetTodaColumnaEntidadRule");
			return listado.toString();

		} catch (Exception e) {
			if (e instanceof ISPACRuleException)
				throw new ISPACRuleException(e);
			throw new ISPACRuleException(
					"No se ha podido obtener la lista de liquidaciones", e);
		}
	}

	public void cancel(IRuleContext rulectx) throws ISPACRuleException {
	}

}