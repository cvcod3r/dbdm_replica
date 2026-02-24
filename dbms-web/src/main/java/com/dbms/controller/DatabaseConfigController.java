package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.annotation.SysLogAnnotation;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.*;
import com.dbms.page.TableDataInfo;
import com.dbms.service.DatabaseConfigService;
import com.dbms.utils.CryptoUtil;
import com.dbms.utils.SecurityUtils;
import com.dbms.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.PUT;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author YSL
 * @since 2023-02-23
 */
@RestController
@RequestMapping("/database-config")
public class DatabaseConfigController extends BaseController {

    @Autowired
    private DatabaseConfigService databaseConfigService;


    @GetMapping("/listDbaseConfig")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "数据源配置", operation = "查询", procedureInfo = "查询数据源配置")
    public TableDataInfo listDbaseConfig(DatabaseConfigEntity databaseConfigEntity){
        startPage();
        QueryWrapper<DatabaseConfigEntity> queryWrapper=new QueryWrapper<>();
        if (StringUtils.isNotNull(databaseConfigEntity.getDatabaseName())) {
            queryWrapper.like(DatabaseConfigEntity.DATABASE_NAME,databaseConfigEntity.getDatabaseName());
        }
        if(StringUtils.isNotNull(databaseConfigEntity.getDsType())){
            queryWrapper.like(DatabaseConfigEntity.DS_TYPE,databaseConfigEntity.getDsType());
        }
        if(StringUtils.isNotNull(databaseConfigEntity.getDbType())){
            queryWrapper.like(DatabaseConfigEntity.DB_TYPE,databaseConfigEntity.getDbType());
        }
        if(StringUtils.isNotNull(databaseConfigEntity.getDbDriver())){
            queryWrapper.like(DatabaseConfigEntity.DB_DRIVER,databaseConfigEntity.getDbDriver());
        }
//        queryWrapper.eq(DatabaseConfigEntity.IS_DELETE,0);
        List<DatabaseConfigEntity> list = databaseConfigService.list(queryWrapper);
        return getDataTable(list);
    }

    @GetMapping("/getDbaseConfigById/{databaseId}")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "数据源配置", operation = "获取", procedureInfo = "根据Id获取数据源信息")
    public AjaxResult getDbaseConfigById(@PathVariable Integer databaseId){
        return AjaxResult.success(databaseConfigService.getById(databaseId));
    }


    @PutMapping(value ="/editDbaseConfig")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "数据源配置", operation = "修改", procedureInfo = "修改数据源类名")
    public AjaxResult editDbaseConfig(@Validated @RequestBody DatabaseConfigEntity databaseConfigEntity){
        {
            //存储更改的用户信息
            return toAjax(databaseConfigService.saveOrUpdate(databaseConfigEntity));
        }
    }

    @DeleteMapping("/delDbaseConfig/{databaseIds}")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "数据源配置", operation = "删除", procedureInfo = "删除数据源配置")
    public AjaxResult remove(@PathVariable Integer[] databaseIds)
    {
        System.out.println(JSON.toJSONString(databaseIds));
        return toAjax(databaseConfigService.removeBatchByIds(Arrays.asList(databaseIds)));
    }

    @PostMapping("/addConfig")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "数据源配置", operation = "新增", procedureInfo = "新增数据源配置")
    public AjaxResult addConfig(@Validated @RequestBody DatabaseConfigEntity databaseConfigEntity)
    {
        System.out.println("1234564---------------");
        return toAjax(databaseConfigService.saveOrUpdate(databaseConfigEntity));
    }

    @GetMapping("/allName")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "数据源配置", operation = "查询", procedureInfo = "查询数据源配置")
    public AjaxResult allName()
    {
        System.out.println("123");
        QueryWrapper<DatabaseConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT " + DatabaseConfigEntity.DATABASE_NAME);
        List<DatabaseConfigEntity> list = databaseConfigService.list(queryWrapper);
        for(DatabaseConfigEntity x : list){
            System.out.println(x.toString());
        }
        return AjaxResult.success(list);
    }
    /**
     * 根据数据类型获取数据源类型，关系型数据库或大数据
     * @param dsType
     * @return
     */
    @GetMapping("/getDbTypeByDsType/{dsType}")
    public AjaxResult getDbTypeByDsType(@PathVariable String dsType){
        QueryWrapper<DatabaseConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConfigEntity.DS_TYPE, dsType);
        queryWrapper.select("DISTINCT " + DatabaseConfigEntity.DB_TYPE);
        List<DatabaseConfigEntity> list = databaseConfigService.list(queryWrapper);
        return AjaxResult.success(list);
    }

    @GetMapping("/getDbType")
    public AjaxResult getDbType(){
        QueryWrapper<DatabaseConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT " + DatabaseConfigEntity.DB_TYPE);
        List<DatabaseConfigEntity> list = databaseConfigService.list(queryWrapper);
        return AjaxResult.success(list);
    }

    /**
     * 根据数据库类型获取驱动名称
     * @param dbType
     * @return
     */
    @GetMapping("/getDbDriverByDbType/{dbType}")
    public AjaxResult getDbDriverByDbType(@PathVariable String dbType){
        QueryWrapper<DatabaseConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConfigEntity.DB_TYPE, dbType);
        List<DatabaseConfigEntity> list = databaseConfigService.list(queryWrapper);
        return AjaxResult.success(list);
    }


}
