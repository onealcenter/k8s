package ${package}.service;

import java.util.Map;
import ${package}.dao.${class}Dao;

public class ${class}Service{

	private ${class}Dao ${class?lower_case}Dao;
	
	public Object queryForList(Map map, int start, int offset) {
		return ${class?lower_case}Dao.queryForList(map, start, offset);
	}
	
	public Object queryRecordNumber(Map map){
		return ${class?lower_case}Dao.queryRecordNumber(map);
	}
} 