package ${package}.action;

import java.util.Map;
import com.bocsoft.bfw.core.BfwException;
import com.bocsoft.bfw.core.Context;
import com.gcs.common.action.PagableAction;
import ${package}.service.${class}Service;

public class ${class}Action extends PagableAction{

	private ${class}Service ${class?lower_case}Service;
	
	protected Map initParameters(Context context) throws BfwException{
		Map dataMap = context.getDataMap();
		<#list columns as item>
		String ${item.column} = (String)context.getData("${item.column}");
  		</#list>  
		<#list columns as item>
		dataMap.put("${item.column}",${item.column}.trim());
  		</#list>  
		return dataMap;
	}

	protected Object queryForList(Map map, int start, int offset) {
		return ${class?lower_case}Service.queryForList(map, start, offset);
	}
	
	protected Object queryRecordNumber(Map map){
		return ${class?lower_case}Service.queryRecordNumber(map);
	}
}