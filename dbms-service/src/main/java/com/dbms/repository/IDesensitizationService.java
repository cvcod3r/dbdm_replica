package com.dbms.repository;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import com.dbms.bean.SensitiveBean;
import com.dbms.core.dbase.HBaseShellMeta;
import com.dbms.entity.RiskOperationEntity;
import net.sf.jsqlparser.JSQLParserException;

public interface IDesensitizationService {

    SensitiveBean getSensitiveOptions(Integer userId, Integer groupId, Integer dbId, String schemaName, String sql) throws JSQLParserException;

    SensitiveBean getSensitiveOptionsForHbase(HBaseShellMeta shellMeta,Integer userId, Integer groupId, Integer dbId) throws JSQLParserException;

    SensitiveBean getSensitiveOptionsForHbaseForStatic(HBaseShellMeta shellMeta,Map<String, Integer> tableInfoNow) throws JSQLParserException;

    SensitiveBean getSensitiveOptionsForStatic( String tableName, Map<String, Integer> tableInfoNow) throws JSQLParserException;

    String desensitizationCore(String str,String reg,String params);

    List<Map<String, Object>> dynamicDesensitization(List<Map<String, Object>> list , SensitiveBean sensitiveBean) throws Exception;

    List<Map<String, Object>> dynamicDesensitizationForHbase(List<Map<String, Object>> resultList ,SensitiveBean sensitiveBean) throws Exception;

    List<RiskOperationEntity> getDesensitizationOperations(Integer userId, Integer groupId, Integer dbId);

    String strtos(String str);
}
