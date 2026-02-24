package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.annotation.SysLogAnnotation;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.DatabaseConfigEntity;
import com.dbms.entity.DbaseAccountEntity;
import com.dbms.entity.DbaseEntity;
import com.dbms.entity.WorkOrderEntity;
import com.dbms.vo.DbaseEntityVo;
import com.dbms.page.TableDataInfo;
import com.dbms.repository.DbopService;
import com.dbms.service.*;
import com.dbms.utils.*;
import com.dbms.utils.ip.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dbms.domain.dbase.DataBaseMetric;
import com.dbms.domain.dbase.MetaInfo;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import static com.dbms.utils.SecurityUtils.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author
 * @since 2022-03-23
 */
@RestController
@RequestMapping("/dbms")
public class DbaseController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DbaseController.class);

    @Autowired
    private DbaseService dbaseService;

    @Autowired
    private DbopService dbopService;

    @Autowired
    private LogDbaseService logDbaseService;

    @Autowired
    private DbaseAccessService dbaseAccessService;

    @Autowired
    private DbaseAccountService dbaseAccountService;

    @Autowired
    private DatabaseConfigService databaseConfigService;

    @Autowired
    private LogUnusualBehaviorService logUnusualBehaviorService;

    @Autowired WorkOrderService workOrderService;

    @Autowired
    HttpServletRequest request;

    private final MessageInfo msg = new MessageInfo();

    /**
     * 检查连接名是否重复，dbId有效时可以跟自己相同（修改可以相同）
     *
     * @param dbId
     * @param connName
     * @return
     */
    @RequestMapping("/checkConnName/{dbId}/{connName}")
    public AjaxResult checkConnName(@PathVariable Integer dbId, @PathVariable String connName) {
        QueryWrapper<DbaseEntity> queryWrapper = new QueryWrapper<>();
        if (dbId != -1) {
            queryWrapper.ne(DbaseEntity.DB_ID, dbId);
        }
        queryWrapper.eq(DbaseEntity.CONN_NAME, connName);
        queryWrapper.eq(DbaseEntity.IS_DELETE, 0);
        List<DbaseEntity> dbaseEntityList = dbaseService.list(queryWrapper);
//        System.out.println(JSON.toJSONString(dbaseEntityList));
        if (dbaseEntityList != null && dbaseEntityList.size() != 0) {
            return AjaxResult.success(GlobalMessageUtil.statusSuccess);
        }
        return AjaxResult.success(GlobalMessageUtil.statusFailure);
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
            if (dbaseEntity.getDbType().equals("Oscar")||dbaseEntity.getDbType().equals("GBase")||dbaseEntity.getDbType().equals("KingBase")||dbaseEntity.getDbType().equals("Oracle")||dbaseEntity.getDbType().equals("ElasticSearch")){

            } else {
                dbaseEntity.setUrl(dbaseEntity.getUrl() + dbaseEntity.getUrlDir() + dbaseEntity.getSchemaName());
            }
        }
    }

    public void setDbaseEntityInfo(DbaseEntity dbaseEntity){
        setDbaseEntityInfo(dbaseEntity, null);
    }

    @RequestMapping("/testConn")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "国产数据源", operation = "测试", procedureInfo = "测试数据源")
    public AjaxResult testConn(@RequestBody DbaseEntity dbaseEntity){
//        System.out.println(JSON.toJSONString(dbaseEntity));
        String mess;
        String status;
        if (dbaseEntity.getUrl() == null || dbaseEntity.getUrl().equals("")){
            dbaseEntity.setUrl(DbInfoUtil.getURL(dbaseEntity));
        }
        boolean flag = dbopService.testConn(dbaseEntity);
        if (flag)
        {
            mess = "连接成功！";
            status = "success";
        }
        else
        {
            mess = "连接失败！";
            status = "fail";
        }
        AjaxResult ajaxResult = AjaxResult.success(mess);
        ajaxResult.put("mess", mess);
        ajaxResult.put("status", status);
        return ajaxResult;
    }

    /**
     * 保存连接，先测试是否连接成功
     *
     * @param dbaseEntity
     * @return
     */
    @RequestMapping("/saveConn")
    @ResponseBody
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "国产数据源", operation = "新增", procedureInfo = "新增数据源信息")
    public AjaxResult saveConn(@RequestBody DbaseEntity dbaseEntity) {
//        System.out.println(JSON.toJSONString(dbaseEntity));
        String mess;
        if (dbaseEntity.getUrl() == null || dbaseEntity.getUrl().equals("")){
            dbaseEntity.setUrl(DbInfoUtil.getURL(dbaseEntity));
        }
//        System.out.println(JSON.toJSONString(dbaseEntity));
        boolean connFlag = dbopService.testConn(dbaseEntity);
        if (connFlag) {
            // 数据库连接成功
            String encodePassword = CryptoUtil.encode(dbaseEntity.getPassword());
            dbaseEntity.setPassword(encodePassword);
            dbaseEntity.setUrl(DbInfoUtil.getURL(dbaseEntity));
            dbaseEntity.setCreatetime(LocalDateTime.now());
//            System.out.println("-----------");
//            System.out.println(JSON.toJSONString(dbaseEntity));
            QueryWrapper<DbaseEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(DbaseEntity.URL, dbaseEntity.getUrl());
            queryWrapper.eq(DbaseEntity.IS_DELETE, 0);
            if (dbaseService.count(queryWrapper) > 0){
                mess = GlobalMessageUtil.dbSaveFailure;
            } else {
                boolean flag = dbaseService.save(dbaseEntity);
                if (flag) {
                    DbaseAccountEntity dbaseAccountEntity = new DbaseAccountEntity();
                    dbaseAccountEntity.setUrl(dbaseEntity.getUrl());
                    dbaseAccountEntity.setConnName(dbaseEntity.getConnName());
                    dbaseAccountEntity.setDbId(dbaseEntity.getDbId());
                    dbaseAccountEntity.setUsername(dbaseEntity.getUsername());
                    dbaseAccountEntity.setPassword(dbaseEntity.getPassword());
                    dbaseAccountService.save(dbaseAccountEntity);
                    mess = GlobalMessageUtil.saveSuccess;
                } else {
                    mess = GlobalMessageUtil.saveFailure;
                }
            }
        } else {
            // 连接失败
            mess = GlobalMessageUtil.dbConnFailure;
        }
        AjaxResult ajaxResult = AjaxResult.success(mess);
        return ajaxResult;
    }

    /**
     * 获取环境连接列表，支持条件搜索
     *
     * @param dbaseEntity
     * @return
     */
    @GetMapping("/listDbase")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "国产数据源", operation = "查询", procedureInfo = "查询数据源列表")
    public TableDataInfo listDbase(DbaseEntity dbaseEntity) {
        startPage();
        List<DbaseEntity> list = dbaseService.selectDbaseList(dbaseEntity);
        return getDataTable(list);
    }

    @GetMapping("/getAccessibleDbase/{dsType}")
    public AjaxResult getAccessibleDbase(@PathVariable String dsType){
        Integer userId = getUserId();
        Integer groupId = getGroupId();
        List<DbaseEntityVo> dbaseEntityVoList = dbaseAccessService.getAccessibleDbaseVo(userId, groupId, dsType);
        return AjaxResult.success(dbaseEntityVoList);
    }
    /**
     * 获取所有连接名
     *
     * @return
     */
    @GetMapping("/SQLDbase")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "国产数据源", operation = "获取", procedureInfo = "获取国产数据源")
    public AjaxResult SQLDbase()
    {
        QueryWrapper<DbaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DbaseEntity.IS_DELETE,0);
        //queryWrapper.between(DbaseEntity.DB_TYPE,"DM","GBase");
        queryWrapper.in(DbaseEntity.DB_TYPE,("DM"),("GBase"),("KingBase"),("Oscar"));
        //queryWrapper.in(DbaseEntity.DB_TYPE,"KingBase");
        //queryWrapper.in(DbaseEntity.DB_TYPE,"GBase");
        return AjaxResult.success(dbaseService.list(queryWrapper));
    }
    /**
     * 获取国产连接名
     *
     * @return
     */
    @GetMapping("/allDbase")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "国产数据源", operation = "获取", procedureInfo = "获取数据源")
    public AjaxResult allDbase()
    {
        QueryWrapper<DbaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DbaseEntity.IS_DELETE,0);
        return AjaxResult.success(dbaseService.list(queryWrapper));
    }
    /**
     * 根据dbId获取连接信息
     *
     * @param dbId
     * @return
     */
    @GetMapping("/getById/{dbId}")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "国产数据源", operation = "查询", procedureInfo = "根据Id查询数据源信息")
    public AjaxResult getById(@PathVariable Integer dbId) {
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        String decodePassword = CryptoUtil.decode(dbaseEntity.getPassword());
        dbaseEntity.setPassword(decodePassword);
        return AjaxResult.success(dbaseEntity);
    }

    /**
     * 修改连接信息
     *
     * @param dbaseEntity
     * @return
     */
    @PostMapping("/editConn")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "国产数据源", operation = "修改", procedureInfo = "修改数据源信息")
    public AjaxResult editConn(@RequestBody DbaseEntity dbaseEntity) {
        String dbType = dbaseEntity.getDbType();
        String dbDriver = dbaseEntity.getDbDriver();
        QueryWrapper<DatabaseConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConfigEntity.DB_TYPE, dbType)
                .eq(DatabaseConfigEntity.DB_DRIVER, dbDriver);
        DatabaseConfigEntity databaseConfigEntity = databaseConfigService.getOne(queryWrapper);
        dbaseEntity.setUrlPrefix(databaseConfigEntity.getUrlPrefix());
        dbaseEntity.setUrlDir(databaseConfigEntity.getUrlDir());
        dbaseEntity.setUrl(DbInfoUtil.getURL(dbaseEntity));
        boolean connFlag = dbopService.testConn(dbaseEntity);
        if (connFlag) {
            // 数据库连接成功
            String encodePassword = CryptoUtil.encode(dbaseEntity.getPassword());
            dbaseEntity.setPassword(encodePassword);
            dbaseEntity.setUpdatetime(LocalDateTime.now());
            boolean flag = dbaseService.saveOrUpdate(dbaseEntity);
            if (flag) {
                return AjaxResult.success(GlobalMessageUtil.editSuccess);
            } else {
                return AjaxResult.error(GlobalMessageUtil.editFailure);
            }

        } else {
            // 连接失败
            return AjaxResult.error(GlobalMessageUtil.dbConnFailure);
        }
    }

    @GetMapping("/getConnectionSession/{dbId}/{schemaName}")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "国产数据源", operation = "获取", procedureInfo = "获取数据源连接会话")
    public AjaxResult getConnectionSession(@PathVariable Integer dbId, @PathVariable String schemaName){
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        dbopService.getConnectionSession(dbaseEntity);
        return AjaxResult.success();
    }

    /**
     * 关闭连接，释放map中的conn
     *
     * @param dbId
     * @return
     */
    @RequestMapping("/closeConn/{dbId}/{schemaName}")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "国产数据源", operation = "关闭", procedureInfo = "关闭数据源连接")
    public AjaxResult closeConn(@PathVariable Integer dbId, @PathVariable String schemaName) {
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        dbopService.closeConn(dbaseEntity);
        return AjaxResult.success();
    }

    /**
     * 获取连接列表
     *
     * @return
     */
    @RequestMapping("/refreshDbList")
    @ResponseBody
    public AjaxResult refreshDbList() {
        List<DbaseEntity> dbList = null;
        QueryWrapper<DbaseEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.in(DbaseEntity.PROJECT_ID, projectIds);
        queryWrapper.eq(DbaseEntity.IS_DELETE, 0);
        dbList = dbaseService.list(queryWrapper);
        return AjaxResult.success(dbList);
    }

    @RequestMapping("/getAccountById/{dbId}")
    public AjaxResult getAccountById(@PathVariable Integer dbId) {
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        setDbaseEntityInfo(dbaseEntity);
        List<Map<String, Object>> accountList = dbopService.getAccount(dbaseEntity);
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("accountList", accountList);
        return ajaxResult;
    }

    /**
     * 获取数据库模式,无需授权
     * @param dbId
     * @return
     */
    @RequestMapping("/getDbSchema/{dbId}")
    public AjaxResult getDbSchema(@PathVariable Integer dbId) {
//        System.out.println(dbId);
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        setDbaseEntityInfo(dbaseEntity);
//        DbaseEntityVo dbaseEntityVo = dbaseAccessService.getDbaseVo(getUserId(), getGroupId(), dbId,"SQL");

        List<String> schemaList = null;
        try {
            schemaList = dbopService.getSchemas(dbaseEntity);
        } catch (Exception e) {
            return AjaxResult.error(GlobalMessageUtil.dbSchemaFailure);
        }
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("schemaList", schemaList);
        return ajaxResult;
    }

    /**
     * 获取数据库模式，需授权
     * @param dbId
     * @return
     */
    @RequestMapping("/getDbAccessSchema/{dbId}")
    public AjaxResult getDbAccessSchema(@PathVariable Integer dbId) {
//        System.out.println(dbId);
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity);
        List<String> schemaList = null;
        try {
            schemaList = dbopService.getSchemas(dbaseEntity);
        } catch (Exception e) {
            return AjaxResult.error(GlobalMessageUtil.dbOpenConnFailure);
        }
        if(schemaList == null || schemaList.size() == 0){
            return AjaxResult.error(GlobalMessageUtil.dbOpenConnFailure);
        }
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("schemaList", schemaList);
        return ajaxResult;
    }

    /**
     * 根据dbId获取数据库元数据，表，视图，函数；表名，字段名等；
     *
     * @param dbId
     * @return
     */
    @RequestMapping("/getDbInfo/{dbId}/{schemaName}")
    public AjaxResult getDbInfo(@PathVariable Integer dbId, @PathVariable String schemaName) {
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        System.out.println(dbaseEntity.getUrl());
        dbopService.closeConn(dbaseEntity);
        dbopService.getConnectionSession(dbaseEntity);
        List<String> tableList = dbopService.getTables(dbaseEntity, schemaName);
        List<String> viewList =  dbopService.getViews(dbaseEntity, schemaName);
        List<Map<String, Object>> funcList = dbopService.getFuncs(dbaseEntity, schemaName);
        List<Map<String, Object>> tableMeta = new ArrayList<>();
        tableMeta = dbopService.getTableMetas(dbaseEntity, schemaName);
        // 获取一个连接，并在ConnMap中维持会话
//        tableMeta = dbopService.getTableMetas(dbaseEntity);
//        List<String> tableList = dbopService.getTablesByType(dbaseEntity);
//        List<String> viewList = dbopService.getViewsByType(dbaseEntity);
//        List<Map<String, Object>> tableMeta = dbopService.getTableMeta(dbaseEntity);
//        System.out.println("table_Meta:" + JSON.toJSONString(tableMeta));
//        List<Map<String, Object>> funcList = dbopService.getFuncsByType(dbaseEntity);
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("tableList", tableList);
        ajaxResult.put("viewList", viewList);
        ajaxResult.put("funcList", funcList);
        ajaxResult.put("tableMeta", tableMeta);
        return ajaxResult;
    }


    /**
     * 根据dbId获取数据库表的详细信息；
     *
     * @param dbId
     * @return
     */
    @RequestMapping("/getTableMeta/{dbId}/{schemaName}")
    public AjaxResult getTableMeta(@PathVariable Integer dbId, @PathVariable String schemaName) {
//        System.out.println(dbId);
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        List<Map<String, Object>> tableMeta = dbopService.getTableMetas(dbaseEntity, schemaName);
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("tableMeta", tableMeta);
        return ajaxResult;
    }

    /**
     * 获取数据库表
     * @param dbId
     * @return
     */
    @RequestMapping("/getDbTables/{dbId}/{schemaName}")
    public AjaxResult getDbTables(@PathVariable Integer dbId, @PathVariable String schemaName) {
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        List<String> tableList = dbopService.getTables(dbaseEntity, schemaName);
        return AjaxResult.success(tableList);
    }

    /**
     * 根据dbId获取数据库表的详细信息,需授权信息；
     *
     * @param dbId
     * @return
     */
    @RequestMapping("/getAccessTableMeta/{dbId}/{schemaName}")
    public AjaxResult getAccessTableMeta(@PathVariable Integer dbId, @PathVariable String schemaName) {
//        System.out.println(dbId);
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        List<Map<String, Object>> tableMeta = dbopService.getTableMetas(dbaseEntity, schemaName);
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("tableMeta", tableMeta);
        return ajaxResult;
    }

    /**
     * 获取数据库表，需授权信息
     * @param dbId
     * @return
     */
    @RequestMapping("/getAccessDbTables/{dbId}/{schemaName}")
    public AjaxResult getAccessDbTables(@PathVariable Integer dbId, @PathVariable String schemaName) {
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        List<String> tableList = dbopService.getTables(dbaseEntity, schemaName);
        return AjaxResult.success(tableList);
    }

    /**
     * 获取视图列表
     * @param dbId
     * @return
     */
    @RequestMapping("/getDbViews/{dbId}/{schemaName}")
    public AjaxResult getDbViews(@PathVariable Integer dbId, @PathVariable String schemaName) {
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        List<String> viewList = dbopService.getViews(dbaseEntity, schemaName);
        return AjaxResult.success(viewList);
    }

    /**
     * 获取函数列表
     * @param dbId
     * @return
     */
    @RequestMapping("/getDbFuncs/{dbId}/{schemaName}")
    public AjaxResult getDbFuncs(@PathVariable Integer dbId, @PathVariable String schemaName) {
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        List<Map<String, Object>> funcList = dbopService.getFuncs(dbaseEntity, schemaName);
        return AjaxResult.success(funcList);
    }

    /**
     * 删除连接，逻辑删
     *
     * @param dbIds
     * @return
     */
    @RequestMapping("/delDbConn/{dbIds}")
    @ResponseBody
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "国产数据源", operation = "删除", procedureInfo = "删除数据源")
    public AjaxResult delDbConn(@PathVariable Integer[] dbIds) {
//        System.out.println(userId + "--" + dbId);
        DbaseEntity dbaseEntity = new DbaseEntity();
        dbaseEntity.setIsDelete(1);
        QueryWrapper<DbaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(DbaseEntity.DB_ID, dbIds);
        boolean flag = dbaseService.update(dbaseEntity, queryWrapper);
        if (flag) {
            msg.setMess(GlobalMessageUtil.dropSuccess);
            msg.setStatus(GlobalMessageUtil.statusSuccess);
        } else {
            msg.setMess(GlobalMessageUtil.dropFailure);
            msg.setStatus(GlobalMessageUtil.statusFailure);
        }
        return AjaxResult.success(msg);
    }

    /**
     * 批量执行update
     *
     * @param dbId
     * @param sqlList
     * @return
     */
    @RequestMapping("/executeUpdateQueue/{dbId}/{schemaName}")
    @ResponseBody
    public AjaxResult executeUpdateQueue(@PathVariable Integer dbId, @PathVariable String schemaName, @RequestBody List<String> sqlList) {
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        List<Map<String, Object>> resultMapList = null;
        String ip = IpUtils.getIpAddress(request);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        resultMapList = dbopService.executeUpdate(dbaseEntity, schemaName, sqlList);
        if (resultMapList != null && resultMapList.size() != 0) {
            for (Map<String, Object> map : resultMapList) {
                logDbaseService.saveLog(map, dbaseEntity, ip);
            }
            return AjaxResult.success(GlobalMessageUtil.statusSuccess, resultMapList);
        }
        return AjaxResult.error(GlobalMessageUtil.statusFailure);
    }

    /**
     * 执行函数
     *
     * @param dbId
     * @param sql
     * @return
     */
    @PostMapping("/executeSQLFunction/{dbId}/{schemaName}")
    @ResponseBody
    public AjaxResult executeSQLFunction(@PathVariable Integer dbId, @PathVariable String schemaName, @RequestBody String sql) {
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        List<Map<String, Object>> resultMapList = null;
        String ip = IpUtils.getIpAddress(request);
        try {
            resultMapList = dbopService.executeSQLFunction(dbaseEntity, schemaName, sql);
            if (resultMapList != null && resultMapList.size() != 0) {
                for (Map<String, Object> map : resultMapList) {
                    logDbaseService.saveLog(map, dbaseEntity, ip);
                }
                return AjaxResult.success(GlobalMessageUtil.statusSuccess, resultMapList);
            }
        } catch (Exception e) {

        }
        return AjaxResult.error(GlobalMessageUtil.statusFailure);
    }

    /**
     * 执行select操作，无权限验证
     *
     * @param dbId
     * @param sqlList
     * @return
     */
    @PostMapping("/executeSelectSQL/{dbId}/{schemaName}")
    @ResponseBody
    public AjaxResult executeSelectSQL(@PathVariable Integer dbId, @PathVariable String schemaName, @RequestBody List<String> sqlList) {
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        List<Map<String, Object>> resultMapList = null;
        String ip = IpUtils.getIpAddress(request);
        try {
            resultMapList = dbopService.executeSelectSQL(dbaseEntity, schemaName, sqlList);
            if (resultMapList != null && resultMapList.size() != 0) {
                for (Map<String, Object> map : resultMapList) {
                    logDbaseService.saveLog(map, dbaseEntity, ip);
                }
                return AjaxResult.success(GlobalMessageUtil.statusSuccess, resultMapList);
            }

        } catch (Exception e) {

        }
        return AjaxResult.error(GlobalMessageUtil.statusFailure);
    }


    /**
     * 执行单条sql
     * @param dbId
     * @param schemaName
     * @param sql
     * @return
     */
    @PostMapping("/executeSQLStatement/{dbId}/{schemaName}")
    public AjaxResult executeSQLStatement(@PathVariable Integer dbId, @PathVariable String schemaName, @RequestBody String sql){
//        System.out.println(sql);
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        Map<String, Object> resultMap = null;
        String dbType = dbaseEntity.getDbType();
        String ip = IpUtils.getIpAddress(request);
//        if (dbType.equals("DM")
//                || dbType.equals("ElasticSearch")
//                || dbType.equals("Hive")
//                || dbType.equals("MySql")
//                || dbType.equals("KingBase")
//                || dbType.equals("GBase")
//                || dbType.equals("MSSQL")
//                || dbType.equals("Oracle")
//                || dbType.equals("HBasePhoenix")) {
        try {
            resultMap = dbopService.executeSQLStatement(dbaseEntity, schemaName, sql);
            if (resultMap != null && resultMap.size() != 0) {
                logDbaseService.saveLog(resultMap, dbaseEntity, ip);
//                    System.out.println(JSON.toJSONString(resultMap));
                logUnusualBehaviorService.timeLimitCheck(dbaseEntity);
                return AjaxResult.success(GlobalMessageUtil.execStatusSuccess, resultMap);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
//        }
        return AjaxResult.error(GlobalMessageUtil.execStatusFailure);
    }

    @PostMapping("/executeSQLStatementOrder/{dbId}/{schemaName}")
    public AjaxResult executeSQLStatementOrder(@PathVariable Integer dbId, @PathVariable String schemaName, @RequestBody WorkOrderEntity workOrderEntity){
//        System.out.println(sql);
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        Map<String, Object> resultMap = null;
        String dbType = dbaseEntity.getDbType();
        String ip = IpUtils.getIpAddress(request);
        String sql = workOrderEntity.getSqlStatement();
        if (workOrderEntity.getLimitCount()!=null && workOrderEntity.getLimitCount()>0){
//            if (dbType.equals("DM")
//                    || dbType.equals("ElasticSearch")
//                    || dbType.equals("Hive")
//                    || dbType.equals("MySql")
//                    || dbType.equals("KingBase")
//                    || dbType.equals("GBase")
//                    || dbType.equals("MSSQL")
//                    || dbType.equals("Oracle")
//                    || dbType.equals("HBasePhoenix")) {
                try {
                    resultMap = dbopService.executeSQLStatementOrder(dbaseEntity, schemaName, sql);

                    if (resultMap != null && resultMap.size() != 0) {
                        logDbaseService.saveLog(resultMap, dbaseEntity, ip);
                        logUnusualBehaviorService.timeLimitCheck(dbaseEntity);
                        // 工单限制次数减1
                        WorkOrderEntity temp = new WorkOrderEntity();
                        temp.setLimitCount(workOrderEntity.getLimitCount()-1);
                        QueryWrapper<WorkOrderEntity> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq(WorkOrderEntity.WORK_ORDER_ID, workOrderEntity.getWorkOrderId());
                        workOrderService.update(temp, queryWrapper);
                        return AjaxResult.success(GlobalMessageUtil.execStatusSuccess, resultMap);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
//            };
        }
        return AjaxResult.error(GlobalMessageUtil.execStatusFailure);
    }
    /**m
     * 执行SQL
     *
     * @param dbId
     * @param sqlList 语句队列
     * @return
     */
    @PostMapping("/executeSQLQueue/{dbId}/{schemaName}")
    @ResponseBody
    public AjaxResult executeSQLQueue(@PathVariable Integer dbId, @PathVariable String schemaName, @RequestBody List<String> sqlList) {
        System.out.println(dbId);
        System.out.println(sqlList);
        Integer limitSize = getLimitCount();
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        List<Map<String, Object>> resultMapList = null;
        String dbType = dbaseEntity.getDbType();
        String ip = IpUtils.getIpAddress(request);
        if (dbType.equals("DM")||dbType.equals("ElasticSearch")||dbType.equals("Hive")) {
            try {
                resultMapList = dbopService.executeSQLQueueForDM(dbaseEntity, schemaName, sqlList, limitSize);
                if (resultMapList != null && resultMapList.size() != 0) {
                    for (Map<String, Object> map : resultMapList) {
                        logDbaseService.saveLog(map, dbaseEntity, ip);
                    }
                    System.out.println(JSON.toJSONString(resultMapList));
                    return AjaxResult.success(GlobalMessageUtil.statusSuccess, resultMapList);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (dbType.equals("GBase")) {
            try {
                resultMapList = dbopService.executeSQLQueueForGBase(dbaseEntity, schemaName, sqlList, limitSize);
                if (resultMapList != null && resultMapList.size() != 0) {
                    for (Map<String, Object> map : resultMapList) {
                        logDbaseService.saveLog(map, dbaseEntity, ip);
                    }
                    return AjaxResult.success(GlobalMessageUtil.statusSuccess, resultMapList);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (dbType.equals("KingBase")) {
            try {

                resultMapList = dbopService.executeSQLQueueForKingBase(dbaseEntity, schemaName, sqlList, limitSize);
                if (resultMapList != null && resultMapList.size() != 0) {
                    for (Map<String, Object> map : resultMapList) {
                        logDbaseService.saveLog(map, dbaseEntity, ip);
                    }
                    return AjaxResult.success(GlobalMessageUtil.statusSuccess, resultMapList);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return AjaxResult.error(GlobalMessageUtil.statusFailure);
    }

    /**
     * 提交事务
     *
     * @param dbId
     * @return
     */
    @RequestMapping("/commit/{dbId}/{schemaName}")
    public AjaxResult commit(@PathVariable Integer dbId, @PathVariable String schemaName) {
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        Map<String, Object> map = dbopService.commit(dbaseEntity, schemaName);
        String ip = IpUtils.getIpAddress(request);
        logDbaseService.saveLog(map, dbaseEntity, ip);
        return AjaxResult.success(map);
    }

    /**
     * 回滚事务
     *
     * @param dbId
     * @return
     */
    @RequestMapping("/rollback/{dbId}/{schemaName}")
    public AjaxResult rollback(@PathVariable Integer dbId, @PathVariable String schemaName) {
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        Map<String, Object> map = dbopService.rollback(dbaseEntity, schemaName);
        String ip = IpUtils.getIpAddress(request);
        logDbaseService.saveLog(map, dbaseEntity, ip);
        return AjaxResult.success(map);
    }

    /**
     * 打开表
     *
     * @param dbId
     * @param tableName
     * @return
     */
    @GetMapping("/showTableData/{dbId}/{schemaName}/{tableName}")
    public AjaxResult showTableData(@PathVariable Integer dbId, @PathVariable String schemaName, @PathVariable String tableName) {
        List<String> sqlList = new ArrayList<>();
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        if (dbaseEntity.getDbType().equals("MySql")) {
            sqlList.add("select * from " + tableName);
            sqlList.add("select * from information_schema.columns "
                    + " where table_name=\"" + tableName + "\" and table_schema=\"" + schemaName + "\"");
        } else if (dbaseEntity.getDbType().equals("Oracle")) {
            sqlList.add("select * from \"" + tableName + "\"");
            sqlList.add("select cu.* from user_cons_columns cu, user_constraints au where cu.constraint_name = au.constraint_name and au.constraint_type = 'P' and au.table_name = '" + tableName + "'");
        } else if (dbaseEntity.getDbType().equals("MSSQL")) {
            sqlList.add("select * from \"" + tableName + "\"");
            sqlList.add("SELECT  TABLE_NAME,COLUMN_NAME  FROM  INFORMATION_SCHEMA.KEY_COLUMN_USAGE  WHERE  TABLE_NAME= '" + tableName + "'");
        } else if (dbaseEntity.getDbType().equals("DM")) {
            sqlList.add("select * from \"" + schemaName + "\"." + "\"" + tableName + "\"");
            sqlList.add("select * from all_tab_columns where owner='\"" + schemaName + "\"' and Table_Name='\"" + tableName + "\"'");
        } else if (dbaseEntity.getDbType().equals("GBase")) {
            sqlList.add("select * from \"" + schemaName + "\"." + "\"" + tableName + "\"");
            sqlList.add("select * from all_tab_columns where owner='\"" + schemaName + "\"' and Table_Name='\"" + tableName + "\"'");
        } else if (dbaseEntity.getDbType().equals("KingBase")) {
            sqlList.add("select * from " + "\"" + tableName + "\"");
            sqlList.add("select * from all_tab_columns where owner='\"" + schemaName + "\"' and Table_Name='\"" + tableName + "\"'");
        }
        return executeSelectSQL(dbId, schemaName, sqlList);
    }

    /**
     * 打开视图
     *
     * @param dbId
     * @param viewName
     * @return
     */
    @GetMapping("/showView/{dbId}/{schemaName}/{viewName}")
    public AjaxResult showView(@PathVariable Integer dbId, @PathVariable String schemaName, @PathVariable String viewName) {
        List<String> sqlList = new ArrayList<>();
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        if (dbaseEntity.getDbType().equals("MySql")) {
            sqlList.add("select * from `" + viewName + "`");
            sqlList.add("show create view `" + viewName + "`");
//            sqlList.add("select * from information_schema.columns "
//                    +" where table_name=\""+tableName+"\" and table_schema=\"" + dbaseEntity.getDbName() + "\"");
        } else if (dbaseEntity.getDbType().equals("Oracle")) {
            sqlList.add("select * from \"" + viewName + "\"");
            sqlList.add("select OWNER,VIEW_NAME,TEXT from all_views where view_name='" + viewName + "'");
//            sqlList.add("select cu.* from user_cons_columns cu, user_constraints au where cu.constraint_name = au.constraint_name and au.constraint_type = 'P' and au.table_name = '" + tableName+"'");
        } else if (dbaseEntity.getDbType().equals("MSSQL")) {
            sqlList.add("select * from \"" + viewName + "\"");
            sqlList.add("SELECT text from syscomments s1 join sysobjects s2 on s1.id=s2.id where name='" + viewName + "'");
//            sqlList.add("sp_helptext  '"+viewName+"'");
//            sqlList.add("SELECT  TABLE_NAME,COLUMN_NAME  FROM  INFORMATION_SCHEMA.KEY_COLUMN_USAGE  WHERE  TABLE_NAME= '"+tableName+"'");
        }
        return executeSelectSQL(dbId, schemaName, sqlList);
    }

    /**
     * 设计视图
     *
     * @param dbId
     * @param viewName
     * @return
     */
    @GetMapping("/designView/{dbId}/{schemaName}/{viewName}")
    public AjaxResult designView(@PathVariable Integer dbId, @PathVariable String schemaName, @PathVariable String viewName) {
        List<String> sqlList = new ArrayList<>();
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        if (dbaseEntity.getDbType().equals("MySql")) {
            sqlList.add("show create view `" + viewName + "`");
        } else if (dbaseEntity.getDbType().equals("Oracle")) {
            sqlList.add("select OWNER,VIEW_NAME,TEXT from all_views where view_name='" + viewName + "'");
        } else if (dbaseEntity.getDbType().equals("MSSQL")) {
            sqlList.add("SELECT text from syscomments s1 join sysobjects s2 on s1.id=s2.id where name='" + viewName + "'");
        }
        return executeSelectSQL(dbId, schemaName, sqlList);
    }

    /**
     * 执行函数
     *
     * @param dbId
     * @param funcName
     * @return
     */
    @GetMapping("/executeFunction/{dbId}/{schemaName}/{funcName}")
    public AjaxResult executeFunction(@PathVariable Integer dbId, @PathVariable String schemaName, @PathVariable String funcName) {
        List<String> sqlList = new ArrayList<>();
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        if (dbaseEntity.getDbType().equals("MySql")) {
            sqlList.add("SHOW  CREATE  FUNCTION `" + funcName + "`");
        } else if (dbaseEntity.getDbType().equals("Oracle")) {
            sqlList.add("SELECT NAME,TEXT FROM USER_SOURCE WHERE NAME = '" + funcName + "'");
        } else if (dbaseEntity.getDbType().equals("MSSQL")) {
            sqlList.add("SELECT text from syscomments s1 join sysobjects s2 on s1.id=s2.id where name='" + funcName + "'");
        }
        return executeSelectSQL(dbId, schemaName, sqlList);
    }

    /**
     * 设计表
     *
     * @param dbId
     * @param tableName
     * @return
     */
    @RequestMapping("/designTableData/{dbId}/{schemaName}/{tableName}")
    public AjaxResult designTableData(@PathVariable Integer dbId, @PathVariable String schemaName, @PathVariable String tableName) {
        List<String> sqlList = new ArrayList<>();
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
//        System.out.println(dbId + "-----" + tableName);
//        System.out.println(dbaseEntity.getDbName());
        if (dbaseEntity.getDbType().equals("MySql")) {
            sqlList.add("select * from information_schema.columns "
                    + " where table_name=\"" + tableName + "\" and table_schema=\"" + dbaseEntity.getDbName() + "\"");
        } else if (dbaseEntity.getDbType().equals("Oracle")) {
            tableName = "'" + tableName + "'";
//            sqlList.add("select * from user_tab_columns WHERE table_name=" + tableName);
            sqlList.add("SELECT utc.COLUMN_ID AS COLUMN_ID,utc.COLUMN_NAME AS COLUMN_NAME,ucc.COLUMN_NAME AS COLUMN_KEY," +
                    "ucct.COMMENTS AS COMMENTS,utc.data_default AS DATA_DEFAULT,utc.DATA_TYPE AS DATA_TYPE,utc.DATA_LENGTH " +
                    "AS DATA_LENGTH,utc.NULLABLE AS NULLABLE FROM user_tab_columns utc LEFT JOIN user_cons_columns ucc ON " +
                    "utc.TABLE_NAME = ucc.TABLE_NAME LEFT JOIN user_col_comments ucct ON utc.COLUMN_NAME = ucct.COLUMN_NAME " +
                    "AND utc.TABLE_NAME = ucct.TABLE_NAME WHERE POSITION = 1 AND ucc.TABLE_NAME = " + tableName + " ORDER BY utc.COLUMN_ID");
        } else if (dbaseEntity.getDbType().equals("MSSQL")) {
            tableName = "'" + tableName + "'";
            sqlList.add("SELECT  \n" +
                    "        col.colorder AS ORDINAL_POSITION ,\n" +
                    "        col.name AS COLUMN_NAME ,\n" +
                    "        ISNULL(ep.[value], '') AS COLUMN_COMMENT,\n" +
                    "        t.name AS DATA_TYPE ,\n" +
                    "        col.length AS COLUMN_TYPE ,\n" +
                    "        ISNULL(COLUMNPROPERTY(col.id, col.name, 'Scale'), 0) AS weishu ,\n" +
                    "        CASE WHEN EXISTS ( SELECT   1\n" +
                    "                           FROM     dbo.sysindexes si\n" +
                    "                                    INNER JOIN dbo.sysindexkeys sik ON si.id = sik.id\n" +
                    "                                                              AND si.indid = sik.indid\n" +
                    "                                    INNER JOIN dbo.syscolumns sc ON sc.id = sik.id\n" +
                    "                                                              AND sc.colid = sik.colid\n" +
                    "                                    INNER JOIN dbo.sysobjects so ON so.name = si.name\n" +
                    "                                                              AND so.xtype = 'PK'\n" +
                    "                           WHERE    sc.id = col.id\n" +
                    "                                    AND sc.colid = col.colid ) THEN '1'\n" +
                    "             ELSE ''\n" +
                    "        END AS COLUMN_KEY ,\n" +
                    "        CASE WHEN col.isnullable = 1 THEN '1'\n" +
                    "             ELSE ''\n" +
                    "        END AS IS_NULLABLE ,\n" +
                    "        ISNULL(comm.text, '') AS COLUMN_DEFAULT\n" +
                    "FROM    dbo.syscolumns col\n" +
                    "        LEFT  JOIN dbo.systypes t ON col.xtype = t.xusertype\n" +
                    "        inner JOIN dbo.sysobjects obj ON col.id = obj.id\n" +
                    "                                         AND obj.xtype = 'U'\n" +
                    "                                         AND obj.status >= 0\n" +
                    "        LEFT  JOIN dbo.syscomments comm ON col.cdefault = comm.id\n" +
                    "        LEFT  JOIN sys.extended_properties ep ON col.id = ep.major_id\n" +
                    "                                                      AND col.colid = ep.minor_id\n" +
                    "                                                      AND ep.name = 'MS_Description'\n" +
                    "        LEFT  JOIN sys.extended_properties epTwo ON obj.id = epTwo.major_id\n" +
                    "                                                         AND epTwo.minor_id = 0\n" +
                    "                                                         AND epTwo.name = 'MS_Description'\n" +
                    "WHERE   obj.name = " + tableName + "");
        } else if (dbaseEntity.getDbType().equals("DM")) {
            tableName = "'" + tableName + "'";
//            sqlList.add("select * from user_tab_columns WHERE table_name=" + tableName);
            sqlList.add("SELECT utc.COLUMN_ID AS COLUMN_ID,utc.COLUMN_NAME AS COLUMN_NAME,ucc.COLUMN_NAME AS COLUMN_KEY," +
                    "ucct.COMMENTS AS COMMENTS,utc.data_default AS DATA_DEFAULT,utc.DATA_TYPE AS DATA_TYPE,utc.DATA_LENGTH " +
                    "AS DATA_LENGTH,utc.NULLABLE AS NULLABLE FROM user_tab_columns utc LEFT JOIN user_cons_columns ucc ON " +
                    "utc.TABLE_NAME = ucc.TABLE_NAME LEFT JOIN user_col_comments ucct ON utc.COLUMN_NAME = ucct.COLUMN_NAME " +
                    "AND utc.TABLE_NAME = ucct.TABLE_NAME WHERE POSITION = 1 AND ucc.TABLE_NAME = " + tableName + " ORDER BY utc.COLUMN_ID");
        } else if (dbaseEntity.getDbType().equals("GBase")) {
            tableName = "'" + tableName + "'";
//            sqlList.add("select * from user_tab_columns WHERE table_name=" + tableName);
            sqlList.add("SELECT utc.COLUMN_ID AS COLUMN_ID,utc.COLUMN_NAME AS COLUMN_NAME,ucc.COLUMN_NAME AS COLUMN_KEY," +
                    "ucct.COMMENTS AS COMMENTS,utc.data_default AS DATA_DEFAULT,utc.DATA_TYPE AS DATA_TYPE,utc.DATA_LENGTH " +
                    "AS DATA_LENGTH,utc.NULLABLE AS NULLABLE FROM user_tab_columns utc LEFT JOIN user_cons_columns ucc ON " +
                    "utc.TABLE_NAME = ucc.TABLE_NAME LEFT JOIN user_col_comments ucct ON utc.COLUMN_NAME = ucct.COLUMN_NAME " +
                    "AND utc.TABLE_NAME = ucct.TABLE_NAME WHERE POSITION = 1 AND ucc.TABLE_NAME = " + tableName + " ORDER BY utc.COLUMN_ID");
        } else if (dbaseEntity.getDbType().equals("KingBase")) {
            tableName = "'" + tableName + "'";
//            sqlList.add("select * from user_tab_columns WHERE table_name=" + tableName);
            sqlList.add("SELECT utc.COLUMN_ID AS COLUMN_ID,utc.COLUMN_NAME AS COLUMN_NAME,ucc.COLUMN_NAME AS COLUMN_KEY," +
                    "ucct.COMMENTS AS COMMENTS,utc.data_default AS DATA_DEFAULT,utc.DATA_TYPE AS DATA_TYPE,utc.DATA_LENGTH " +
                    "AS DATA_LENGTH,utc.NULLABLE AS NULLABLE FROM user_tab_columns utc LEFT JOIN user_cons_columns ucc ON " +
                    "utc.TABLE_NAME = ucc.TABLE_NAME LEFT JOIN user_col_comments ucct ON utc.COLUMN_NAME = ucct.COLUMN_NAME " +
                    "AND utc.TABLE_NAME = ucct.TABLE_NAME WHERE POSITION = 1 AND ucc.TABLE_NAME = " + tableName + " ORDER BY utc.COLUMN_ID");
        }
        return executeSelectSQL(dbId, schemaName, sqlList);
    }

    /**
     * 设计无主键表
     * @param dbId
     * @param tableName
     * @return
     */
    @RequestMapping("/designUnTableData/{dbId}/{tableName}")
    public AjaxResult designUnTableData(@PathVariable Integer dbId, @PathVariable String schemaName, @PathVariable String tableName) {
        List<String> sqlList = new ArrayList<>();
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        if (dbaseEntity.getDbType().equals("Oracle")) {
            tableName = "'" + tableName + "'";
            sqlList.add("SELECT\n" +
                    "\tutc.COLUMN_ID AS COLUMN_ID,\n" +
                    "\tutc.COLUMN_NAME AS COLUMN_NAME,\n" +
                    "\t ucct.COMMENTS AS COMMENTS,\n" +
                    "\tutc.data_default AS DATA_DEFAULT,\n" +
                    "\tutc.DATA_TYPE AS DATA_TYPE,\n" +
                    "\tutc.DATA_LENGTH AS DATA_LENGTH,\n" +
                    "\tutc.NULLABLE AS NULLABLE \n" +
                    "FROM\n" +
                    "\tuser_tab_columns utc\n" +
                    "\tLEFT JOIN user_col_comments ucct ON utc.COLUMN_NAME = ucct.COLUMN_NAME \n" +
                    "\tAND utc.TABLE_NAME = ucct.TABLE_NAME \n" +
                    "WHERE\n" +
                    "  utc.TABLE_NAME = " + tableName + "\n" +
                    "\tORDER BY utc.COLUMN_ID");
        }
        return executeSelectSQL(dbId, schemaName, sqlList);
    }

    /**
     * 创建表
     *
     * @param dbId
     * @param sqlList
     * @return
     */
    @RequestMapping("/createTable/{dbId}/{schemaName}")
    @ResponseBody
    public AjaxResult createTable(@PathVariable Integer dbId, @PathVariable String schemaName, @RequestBody List<String> sqlList) {
        return executeUpdateQueue(dbId, schemaName, sqlList);
    }

    @RequestMapping("/exportTableStruct/{dbId}/{schemaName}/{tableName}")
    public AjaxResult exportTableStruct(@PathVariable Integer dbId, @PathVariable String schemaName, @PathVariable String tableName) throws SQLException {
        //List<String> sqlList = new ArrayList<>();
        String sql = null;
        List<String> sqlList = new ArrayList<>();
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        if (dbaseEntity.getDbType().equals("MySql")) {
            sql = "SHOW  CREATE  table `" + tableName + "`";
            //sqlList.add("SHOW  CREATE  table `" + tableName + "`");
        } else if (dbaseEntity.getDbType().equals("Oracle")) {
            sql = "select DBMS_METADATA.GET_DDL('TABLE','" + tableName + "') as \"Create Table\" from DUAL";
            //sqlList.add("select DBMS_METADATA.GET_DDL('TABLE','"+ tableName + "') as \"Create Table\" from DUAL");
        } else if (dbaseEntity.getDbType().equals("MSSQL")) {
            sql = "";
        }
        sqlList.add(sql);
        return executeSelectSQL(dbId, schemaName, sqlList);
    }

    @RequestMapping("/testSSH/{dbId}")
    public AjaxResult testSSH(@PathVariable Integer dbId){
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        String host = dbaseEntity.getHost();
        String sshUsername = dbaseEntity.getSshUsername();
        String sshPassword = dbaseEntity.getSshPassword();
        boolean flag = false;
        try {
            flag = DataBaseMetric.checkSSH(host, sshUsername, sshPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (flag) {
            return AjaxResult.success("连接成功");
        } else {
            return AjaxResult.error("ssh连接失败, 请检查数据源配置");
        }
    }

    @RequestMapping("/getMonitorInfo/{dbId}")
    public AjaxResult getMonitorInfo(@PathVariable Integer dbId){
        // 获取数据库对象dbaseEntity
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        String host = dbaseEntity.getHost();
        String sshUsername = dbaseEntity.getSshUsername();
        String sshPassword = dbaseEntity.getSshPassword();

        String cpuUsage = null;
        try {
            cpuUsage = DataBaseMetric.getCpuUsage(host, sshUsername, sshPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, String> memoryInfo = null;
        try {
            memoryInfo = DataBaseMetric.getMemUsage(host, sshUsername, sshPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, String> diskInfo = null;
        try {
            diskInfo = DataBaseMetric.getDiskUsage(host, sshUsername, sshPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.put("cpuUsage", cpuUsage);
        ajaxResult.put("memoryInfo", memoryInfo);
        ajaxResult.put("diskInfo", diskInfo);
        return ajaxResult;
    }

    @RequestMapping("/getNetworkSpeed/{dbId}")
    public AjaxResult getNetworkSpeed(@PathVariable Integer dbId){
        // 获取数据库对象dbaseEntity
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        String host = dbaseEntity.getHost();
        String sshUsername = dbaseEntity.getSshUsername();
        String sshPassword = dbaseEntity.getSshPassword();

        Map<String, String> networkSpeed = new HashMap<>();
        try {
            Map<String, String> networkBytes1 = DataBaseMetric.getNetworkBytes(host, sshUsername, sshPassword);
            Thread.sleep(2000);
            Map<String, String> networkBytes2 = DataBaseMetric.getNetworkBytes(host, sshUsername, sshPassword);
            String rb1 = networkBytes1.getOrDefault("receiveBytes", "0");
            String tb1 = networkBytes1.getOrDefault("transmitBytes", "0");
            String rb2 = networkBytes2.getOrDefault("receiveBytes", "0");
            String tb2 = networkBytes2.getOrDefault("transmitBytes", "0");
            String receiveSpeed = String.valueOf((Long.parseLong(rb2) - Long.parseLong(rb1)) / 2);
            String transmitSpeed = String.valueOf((Long.parseLong(tb2) - Long.parseLong(tb1)) / 2);
            networkSpeed.put("receivedSpeed", receiveSpeed);
            networkSpeed.put("transmitSpeed", transmitSpeed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.put("networkSpeed", networkSpeed);
        return ajaxResult;
    }
    @RequestMapping("/getDbaseInfo/{dbId}")
    public AjaxResult getDbaseInfo(@PathVariable Integer dbId){
        // 获取数据库对象dbaseEntity
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        setDbaseEntityInfo(dbaseEntity);
        MetaInfo metaInfo = null;
        // 获取元数据信息
        if (dbaseEntity.getDsType().equals("SQL") && !dbaseEntity.getDbType().equals("HBasePhoenix")){
            metaInfo = dbopService.getDataBaseMetaInfo(dbaseEntity);
        }
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.put("metaInfo", metaInfo);
        return ajaxResult;
    }


}
