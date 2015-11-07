<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE sqlMap PUBLIC "-//iBATIS.com//DTD SQL Map 2.0//EN" "http://iBATIS.com/dtd/sql-map-2.dtd" >
<sqlMap namespace="${class}">
	<resultMap id="${class?lower_case}RESULT" class="${package}.entity.${class}">
  		<#list columns as item>
			<result column="${item.column}" property="${item.property}" jdbcType="${item.jdbctype}" />  
  		</#list>  
	</resultMap>
	<select id="queryForList" parameterClass="java.util.HashMap" resultClass="java.util.HashMap">
		SELECT <#list columns as item>${class}.${item.column}<#if item_has_next>,</#if></#list>
		FROM ${class} WHERE 1=1
		<dynamic>
			<#list columns as item>
			<isNotEmpty prepend=" AND " property="${item.property}">
				${class}.${item.column} = #${item.property}#
			</isNotEmpty> 
  			</#list>  
		</dynamic>
	</select>
	<select id="queryRecordNumber" parameterClass="java.util.HashMap" resultClass="java.lang.Integer">
		SELECT count(*) FROM ${class} WHERE 1=1
		<dynamic>
			<#list columns as item>
			<isNotEmpty prepend=" AND " property="${item.property}">
				${class}.${item.column} = #${item.property}#
			</isNotEmpty> 
  			</#list>  
		</dynamic>
	</select>
</sqlMap>