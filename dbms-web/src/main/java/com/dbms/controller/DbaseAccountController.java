package com.dbms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.AccessStrategyEntity;
import com.dbms.entity.DbaseAccountEntity;
import com.dbms.entity.DbaseEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.repository.BigDataOpService;
import com.dbms.repository.DbopService;
import com.dbms.service.DbaseAccountService;
import com.dbms.service.DbaseService;
import com.dbms.utils.CryptoUtil;
import com.dbms.utils.StringUtils;
import com.dbms.annotation.SysLogAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@RestController
@RequestMapping("/dbase-account")
public class DbaseAccountController extends BaseController {

    @Autowired
    private DbaseAccountService dbaseAccountService;

    @Autowired
    private DbaseService dbaseService;

    @Autowired
    private DbopService dbopService;

    @Autowired
    private BigDataOpService bigDataOpService;

    @GetMapping("/listDbaseAccount")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "数据源账号", operation = "查询", procedureInfo = "查询数据源账号")
    public TableDataInfo listGroup(DbaseAccountEntity dbaseAccountEntity)
    {
        startPage();
        QueryWrapper<DbaseAccountEntity> queryWrapper=new QueryWrapper<>();
        if (StringUtils.isNotNull(dbaseAccountEntity.getConnName())) {
            queryWrapper.like(DbaseAccountEntity.CONN_NAME, dbaseAccountEntity.getConnName());
        }
        if(StringUtils.isNotNull(dbaseAccountEntity.getUrl())){
            queryWrapper.like(DbaseAccountEntity.URL,dbaseAccountEntity.getUrl());
        }
        if(StringUtils.isNotNull(dbaseAccountEntity.getUsername())){
            queryWrapper.like(DbaseAccountEntity.USERNAME,dbaseAccountEntity.getUsername());
        }
        if(StringUtils.isNotNull(dbaseAccountEntity.getStatus())){
            queryWrapper.like(DbaseAccountEntity.STATUS,dbaseAccountEntity.getStatus());
        }
        queryWrapper.eq(DbaseAccountEntity.IS_DELETE,0);
        List<DbaseAccountEntity> list = dbaseAccountService.list(queryWrapper);
        return getDataTable(list);
    }

    @PostMapping("/saveAccount/{dbId}")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "数据源账号", operation = "新增", procedureInfo = "新增数据源账号")
    public AjaxResult saveAccount(@PathVariable Integer dbId, @RequestBody List<String> accountList){
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        try{
            for(String account:accountList){
                DbaseAccountEntity dbaseAccountEntity = new DbaseAccountEntity();
                dbaseAccountEntity.setUrl(dbaseEntity.getUrl());
                dbaseAccountEntity.setConnName(dbaseEntity.getConnName());
                dbaseAccountEntity.setUsername(account);
                dbaseAccountEntity.setDbId(dbId);
                QueryWrapper<DbaseAccountEntity> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq(DbaseAccountEntity.DB_ID, dbId);
                queryWrapper.eq(DbaseAccountEntity.USERNAME, account);
                queryWrapper.eq(DbaseAccountEntity.IS_DELETE, 0);
                List<DbaseAccountEntity> dbaseAccountEntityList = dbaseAccountService.list(queryWrapper);
                if (dbaseAccountEntityList != null && dbaseAccountEntityList.size() != 0){
                    continue;
                }
                dbaseAccountService.save(dbaseAccountEntity);
            }
        }catch (Exception e){
            return AjaxResult.error();
        }
        return AjaxResult.success();
    }

    @GetMapping("/getDbaseAccountById/{accountId}")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "数据源账号", operation = "获取", procedureInfo = "根据Id获取数据源账号")
    public AjaxResult getDbaseAccountById(@PathVariable Integer accountId){
        DbaseAccountEntity dbaseAccountEntity = dbaseAccountService.getById(accountId);
        String decodePassword = CryptoUtil.decode(dbaseAccountEntity.getPassword());
        dbaseAccountEntity.setPassword(decodePassword);
        return AjaxResult.success(dbaseAccountEntity);
    }

    @GetMapping("/getDbaseAccountByDbId/{dbId}")
    public AjaxResult getDbaseAccountByDbId(@PathVariable Integer dbId){
        QueryWrapper<DbaseAccountEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DbaseAccountEntity.DB_ID, dbId);
        queryWrapper.eq(DbaseAccountEntity.IS_DELETE, 0);
        queryWrapper.eq(DbaseAccountEntity.STATUS, 0);
        List<DbaseAccountEntity> list = dbaseAccountService.list(queryWrapper);
        return AjaxResult.success(list);
    }

    @PostMapping("/editDbaseAccount")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "数据源账号", operation = "修改", procedureInfo = "修改数据源账号")
    public AjaxResult editDbaseAccount(@RequestBody DbaseAccountEntity dbaseAccountEntity){
//        QueryWrapper<DbaseAccountEntity> queryWrapper = new QueryWrapper<>();
        Integer dbId = dbaseAccountEntity.getDbId();
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        dbaseEntity.setPassword(dbaseAccountEntity.getPassword());
        dbaseAccountEntity.setPassword(CryptoUtil.encode(dbaseAccountEntity.getPassword()));
        if (dbaseEntity.getDsType().equals("NoSQL")){
            if (bigDataOpService.testConn(dbaseEntity)) {
                dbaseAccountService.updateById(dbaseAccountEntity);
                return AjaxResult.success("修改成功");
            } else {
                return AjaxResult.error();
            }
        } else {
            if (dbopService.testConn(dbaseEntity)) {
                dbaseAccountService.updateById(dbaseAccountEntity);
                return AjaxResult.success("修改成功");
            } else {
                return AjaxResult.error();
            }
        }
    }

    @DeleteMapping("/delDbaseAccount/{accountIds}")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "数据源账号", operation = "删除", procedureInfo = "删除数据源账号")
    public AjaxResult delDbaseAccount(@PathVariable Integer[] accountIds){
        System.out.println("---------------------------------");
        for (Integer accountId : accountIds) {
            DbaseAccountEntity dbaseAccountEntity = dbaseAccountService.getById(accountId);
            dbaseAccountEntity.setIsDelete(1);

            dbaseAccountService.updateById(dbaseAccountEntity);
        }
        return AjaxResult.success();
    }

    @PostMapping("testAccount")
    public AjaxResult testAccount(@RequestBody DbaseAccountEntity dbaseAccountEntity){
        Integer dbId = dbaseAccountEntity.getDbId();
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        dbaseEntity.setUsername(dbaseAccountEntity.getUsername());
        dbaseEntity.setPassword(dbaseAccountEntity.getPassword());
        String mess;
        String status;
        if (dbaseEntity.getDsType().equals("NoSQL")){
            if (bigDataOpService.testConn(dbaseEntity)) {
                mess = "连接成功！";
                status = "success";
            } else {
                mess = "连接失败！";
                status = "fail";
            }
        } else {
            if (dbopService.testConn(dbaseEntity)) {
                mess = "连接成功！";
                status = "success";
            }
            else {
                mess = "连接失败！";
                status = "fail";
            }
        }
        AjaxResult ajaxResult = AjaxResult.success(mess);
        ajaxResult.put("status", status);
        return ajaxResult;
    }


    @PostMapping("addAccount")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "数据源账号", operation = "新增", procedureInfo = "新增数据源账号")
    public AjaxResult addAccount(@RequestBody DbaseAccountEntity dbaseAccountEntity){
        Integer dbId = dbaseAccountEntity.getDbId();
        DbaseEntity dbaseEntity = dbaseService.getById(dbId);
        dbaseEntity.setUsername(dbaseAccountEntity.getUsername());
        dbaseEntity.setPassword(dbaseAccountEntity.getPassword());
        QueryWrapper<DbaseAccountEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DbaseAccountEntity.DB_ID, dbId);
        queryWrapper.eq(DbaseAccountEntity.USERNAME, dbaseAccountEntity.getUsername());
        queryWrapper.eq(DbaseAccountEntity.IS_DELETE, 0);
        if (dbaseEntity.getDsType().equals("NoSQL")){
            if (bigDataOpService.testConn(dbaseEntity)) {
                dbaseAccountEntity.setUrl(dbaseEntity.getUrl());
                dbaseAccountEntity.setConnName(dbaseEntity.getConnName());
                dbaseAccountEntity.setPassword(CryptoUtil.encode(dbaseAccountEntity.getPassword()));

                dbaseAccountService.saveOrUpdate(dbaseAccountEntity, queryWrapper);
                return AjaxResult.success("保存成功");
            } else {
                return AjaxResult.error();
            }
        } else {
            if (dbopService.testConn(dbaseEntity)) {
                dbaseAccountEntity.setUrl(dbaseEntity.getUrl());
                dbaseAccountEntity.setConnName(dbaseEntity.getConnName());
                dbaseAccountEntity.setPassword(CryptoUtil.encode(dbaseAccountEntity.getPassword()));
                dbaseAccountService.saveOrUpdate(dbaseAccountEntity, queryWrapper);
                return AjaxResult.success("保存成功");
            } else {
                return AjaxResult.error();
            }
        }
    }

}
