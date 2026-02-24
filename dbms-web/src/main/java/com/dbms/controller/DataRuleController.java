package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.annotation.SysLogAnnotation;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.DataRuleEntity;
import com.dbms.entity.DbaseEntity;
import com.dbms.entity.UserGroupEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.service.DataRuleService;
import com.dbms.service.DbaseAccessService;
import com.dbms.service.LogDbaseService;
import com.dbms.service.UserGroupService;
import com.dbms.utils.CryptoUtil;
import com.dbms.utils.GlobalMessageUtil;
import com.dbms.utils.StringUtils;
import com.dbms.utils.ip.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@RestController
@RequestMapping("/data-rule")
public class DataRuleController extends BaseController  {
    @Autowired
    private DataRuleService dataRuleService;

    @Autowired
    private DbaseAccessService dbaseAccessService;

    @Autowired
    HttpServletRequest request;

    @Autowired
    private LogDbaseService logDbaseService;

    /**
     * 查询数据识别规则
     * @param dataRuleEntity
     * @return
     */
    @GetMapping("/listDataRule")
    @SysLogAnnotation(moduleName = "脱敏模块", childModule = "数据识别规则", operation = "查询", procedureInfo = "查询数据识别规则")
    public TableDataInfo listDataRule(DataRuleEntity dataRuleEntity)
    {
        startPage();
        QueryWrapper<DataRuleEntity> queryWrapper=new QueryWrapper<>();
        if (StringUtils.isNotNull(dataRuleEntity.getRuleName())) {
            queryWrapper.like(DataRuleEntity.RULE_NAME,dataRuleEntity.getRuleName());
        }
        if(StringUtils.isNotNull(dataRuleEntity.getStatus())){
            queryWrapper.like(DataRuleEntity.STATUS,dataRuleEntity.getStatus());
        }
        queryWrapper.eq(DataRuleEntity.IS_DELETE,0);
        List<DataRuleEntity> list = dataRuleService.list(queryWrapper);
        return getDataTable(list);
    }

    /**
     * 查询所有数据识别规则
     * @return
     */
    @GetMapping("/allDataRule")
    @SysLogAnnotation(moduleName = "脱敏模块", childModule = "数据识别规则", operation = "获取", procedureInfo = "获取数据识别列表")
    public AjaxResult allDataRule()
    {
        QueryWrapper<DataRuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DataRuleEntity.IS_DELETE,0);
        List<DataRuleEntity> res = dataRuleService.list(queryWrapper);
        return AjaxResult.success(res);
    }

    /**
     * 校验用户组名是否重复
     */
    @GetMapping(value = "/checkGroupName/{ruleID}/{ruleName}")
    public AjaxResult checkRuleName(@PathVariable Integer ruleID, @PathVariable String ruleName)
    {
        QueryWrapper<DataRuleEntity> queryWrapper = new QueryWrapper<>();
        if (ruleID != -1){
            queryWrapper.ne(DataRuleEntity.RULE_ID,ruleID);
        }
        queryWrapper.eq(DataRuleEntity.RULE_NAME, ruleName);
        queryWrapper.ne(DataRuleEntity.IS_DELETE, 1);
        List<DataRuleEntity> DataRuleEntities = dataRuleService.list(queryWrapper);
        if (DataRuleEntities != null && DataRuleEntities.size() != 0){
            return AjaxResult.success(GlobalMessageUtil.statusSuccess);
        }
        return AjaxResult.success(GlobalMessageUtil.statusFailure);
    }

    /**
     * 新增数据识别规则
     */
    @PostMapping(value = "/addDataRule")
    @SysLogAnnotation(moduleName = "脱敏模块", childModule = "数据识别规则", operation = "新增", procedureInfo = "新增数据识别规则")
    public AjaxResult addDataRule(@Validated @RequestBody DataRuleEntity dataRuleEntity)
    {
        //存储创建的数据识别规则信息
        dataRuleEntity.setIsDelete(0);
        dataRuleEntity.setCreatetime(LocalDateTime.now());
        return toAjax(dataRuleService.save(dataRuleEntity));
    }

    @GetMapping(value = "/selectById/{ruleID}")
    @SysLogAnnotation(moduleName = "脱敏模块", childModule = "数据识别规则", operation = "查询", procedureInfo = "根据ID查询数据识别规则")
    public AjaxResult selectById(@PathVariable Integer ruleID)
    {
        return AjaxResult.success(dataRuleService.getById(ruleID));
    }
    /**
     * 修改数据识别规则
     */
    @PutMapping(value = "/updateDataRule")
    @SysLogAnnotation(moduleName = "脱敏模块", childModule = "数据识别规则", operation = "修改", procedureInfo = "修改数据识别规则")
    public AjaxResult updateDataRule(@Validated @RequestBody DataRuleEntity dataRuleEntity)
    {
        dataRuleEntity.setUpdatetime(LocalDateTime.now());
        return toAjax(dataRuleService.saveOrUpdate(dataRuleEntity));
    }

    /**
     * 删除数据识别规则
     */
    @DeleteMapping("/deleteDataRule/{ruleIds}")
    @SysLogAnnotation(moduleName = "脱敏模块", childModule = "数据识别规则", operation = "删除", procedureInfo = "删除数据识别规则")
    public AjaxResult deleteDataRule(@PathVariable Integer[] ruleIds)
    {
        return toAjax(dataRuleService.deleteByIds(ruleIds));
    }

    /**
     * 根据用户Id或用户组Id获取数据源实体,主要为了获取数据源的账号
     * @param dbId
     * @return
     */
    public DbaseEntity getAccessDbaseEntityByDbId(Integer dbId){
        Integer userId = getUserId();
        Integer groupId = getGroupId();
        String dsType = "SQL";
        DbaseEntity accessDbaseEntity = dbaseAccessService.getAccessDbaseEntity(userId, groupId, dbId, dsType);
        return accessDbaseEntity;
    }

    public void setDbaseEntityInfo(DbaseEntity dbaseEntity, String schemaName){
        dbaseEntity.setSchemaName(schemaName);
        String pw = CryptoUtil.decode(dbaseEntity.getPassword());
        dbaseEntity.setPassword(pw);
        if (StringUtils.isNotEmpty(dbaseEntity.getSchemaName())){
//            String dir = "/";
//            if (dbaseEntity.getDbType().equals("DM") || dbaseEntity.getDbType().equals("Oracle")){
//                dir = ":";
//            } else if (dbaseEntity.getDbType().equals("MSSQL")){
//                dir = ";database=";
//            }
            if (dbaseEntity.getDbType().equals("GBase")||dbaseEntity.getDbType().equals("Oracle")||dbaseEntity.getDbType().equals("ElasticSearch")){

            } else {
                dbaseEntity.setUrl(dbaseEntity.getUrl() + dbaseEntity.getUrlDir() + dbaseEntity.getSchemaName());
            }
        }
    }

    /**
     * 执行敏感数据扫描
     * @param dbId
     * @param schemaName
     * @param tableNames
     * @return
     */
    @PostMapping("/dataScan/{dbId}/{schemaName}/{tableNames}")
    public AjaxResult dataScan(@PathVariable Integer dbId,@PathVariable String schemaName,@PathVariable List<String> tableNames,@RequestBody List<List<Integer>> dataRuleList)
    {
        System.out.println("================收到敏感数据扫描指令===============");
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        Map<String,List<Map<String,List<Integer>>>> resultMap = null;
        String dbType = dbaseEntity.getDbType();
        Map<String, List<String>> ruleMap = new HashMap<>();
        System.out.println("dbId:" + dbId);
        System.out.println("schemaName:" + schemaName);
        System.out.println("tableNames:" + tableNames);
        System.out.println("dataRuleList:" + dataRuleList);
        if (dbType.equals("DM") || dbType.equals("KingBase") || dbType.equals("GBase") || dbType.equals("Hive") || dbType.equals("Oscar")) {
            System.out.println("dbType:" + dbType);
            try {
                resultMap = dataRuleService.sensitiveDataScan(dbaseEntity, schemaName, tableNames, dataRuleList);
                if (resultMap != null && resultMap.size() != 0) {
                    System.out.println("resultMap:" + JSON.toJSONString(resultMap));
                    return AjaxResult.success(GlobalMessageUtil.statusSuccess, resultMap);
                }
            } catch (Exception e) {
                System.out.println("error");
            }
        } else if (dbType.equals("ElasticSearch")
                || dbType.equals("MySql")
                || dbType.equals("MSSQL")
                || dbType.equals("Oracle")
                || dbType.equals("HBasePhoenix")) {
            System.out.println("敬请期待");
        }
        System.out.println("---------------敏感数据扫描失败----------------");
        return AjaxResult.error(GlobalMessageUtil.statusFailure);
//        // 正则表达式字符串匹配的测试
//        System.out.println("-------------------test-------------------------");
//        String regex1= "(.).+\\1";
//        // \\组号：表示把第X组的内容再拿出来用一次
//        System.out.println("a123a".matches(regex1));//T
//
//        String regex2 = "(.+).+\\1";
//        System.out.println("abcssabc".matches(regex2));//T
//
//        //(.) 把首字母看成一组，\\2：把首字母拿出来再次使用 *表示后面重复的内容出现0次或多次
//        String regex3 = "((.)\\2*).+\\1";
//        System.out.println("aaasdsaaa".matches(regex3));//T
//
//        String str = "我要学学编编编编编编程程程程程程";
//        //(.)取重复的字符作为第一组
//        //\\1 再次出现
//        //1+ 出现至少一次
//        //$1 复用第一组的内容
//        String result = str.replaceAll("(.)\\1+", "$1");
//        System.out.println(result);//我要学编程
//        System.out.println("------");
//
//        // 匹配特定字符
//        String regex4 = ".*nation.*";
//        System.out.println("The nation of you".matches(regex4));
//
//        // 匹配数字
//        regex4 = "^[1-9][0-9]*$";
//        System.out.println("124143212".matches(regex4));
//        System.out.println("024143212".matches(regex4));
//        // 匹配纯中文
//        regex4 = "^[\\u4e00-\\u9fa5]{0,}$";
//
//        // 匹配日期
//        regex4 = "^\\d{4}-\\d{1,2}-\\d{1,2}.*";
//        System.out.println("2023-02-07 08:16:50.484084".matches(regex4));
//
//
//
//        String s = "The nation of you.";
//        String pattern = "(?:.*)nation(?:.*)";
//
//        Pattern r = Pattern.compile(pattern);
//        Matcher m = r.matcher(s);
//        System.out.println(m.matches());
//        System.out.println("-------------------test_end-------------------------");

        // 测试脱敏
//        System.out.println("----------------------------");
//
//        // 身份证脱敏，前三后四
//        String str = "339005199906270056";
////        String regex = String.format("(\\d{3})\\d{4}(\\d{4})","$1****$2");
//        str = str.replaceAll("(\\w{3})\\w*(\\w{4})","$1*$2");
//        System.out.println("str: " + str);
//
//        //邮箱脱敏(对@前三位脱敏)
//        str = "15700066440@163.com";
//        str = str.replaceAll("(\\w*)\\w{3}@(\\w+)","$1*@$2");
//        System.out.println("str: " + str);
//
//        //银行账号脱敏
//        str = "603367571200917191";
//        str = str.replaceAll("(\\w{4})\\w*(\\w{4})","$1**********$2");
//        System.out.println("str: " + str);
//
//        //客户名脱敏（两字）
//        str = "张宇";
//        str = str.replaceAll("([a-zA-Z0-9_\\u4e00-\\u9fa5]{1})[a-zA-Z0-9_\\u4e00-\\u9fa5]*","$1*");
//        System.out.println("str: " + str);
//
//        //三字
//        str = "黄来浩";
//        str = str.replaceAll("([a-zA-Z0-9_\\u4e00-\\u9fa5]{1})[a-zA-Z0-9_\\u4e00-\\u9fa5]*([a-zA-Z0-9_\\u4e00-\\u9fa5]{1})","$1*$2");
//        System.out.println("str: " + str);
//
//
//        //多字
//        str = "艾弗森德国发啥实打实的嘎嘎大哥";
//        str = str.replaceAll("([a-zA-Z0-9_\\u4e00-\\u9fa5]{1})[a-zA-Z0-9_\\u4e00-\\u9fa5]*([a-zA-Z0-9_\\u4e00-\\u9fa5]{2})","$1**$2");
//        System.out.println("str: " + str);
//
//        //企业名称脱敏（要求长度超过5个字符），从第五个字开始脱敏，最多脱敏6位
//        str = "美国圣地亚";
//        str = str.replaceAll("([a-zA-Z0-9_\\u4e00-\\u9fa5]{4})[a-zA-Z0-9_\\u4e00-\\u9fa5]{1,6}([a-zA-Z0-9_\\u4e00-\\u9fa5]*)","$1***$2");
//        System.out.println("str: " + str);
//
//
//        //家庭地址脱敏（从第四位开始隐藏，隐藏八位）
//        str = "浙江省杭州";
//        str = str.replaceAll("([a-zA-Z0-9_\\u4e00-\\u9fa5]{4})[a-zA-Z0-9_\\u4e00-\\u9fa5]{1,8}([a-zA-Z0-9_\\u4e00-\\u9fa5]*)","$1***$2");
//        System.out.println("str: " + str);
//
//
//        return AjaxResult.success("123");
    }
}
