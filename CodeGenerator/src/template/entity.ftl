package ${package}.entity;  
import java.math.BigDecimal;
  
public class ${class} {  
  
	<#list columns as item>  
  	private ${item.javatype} ${item.property};  
  	</#list>  
   
} 