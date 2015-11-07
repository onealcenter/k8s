package ${package}.dao;

import java.util.Map;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

public class ${class}Dao extends SqlMapClientDaoSupport {

	private String queryForList = "${class}.queryForList";
	private String queryRecordNumber = "${class}.queryRecordNumber";
	
	public Object queryForList(Map map, int start, int offset) {
		return super.getSqlMapClientTemplate().queryForList(queryForList, map, start, offset);
	}
	
	public Object queryRecordNumber(Map map){
		return super.getSqlMapClientTemplate().queryRecordNumber(queryRecordNumber, map);
	}
} 