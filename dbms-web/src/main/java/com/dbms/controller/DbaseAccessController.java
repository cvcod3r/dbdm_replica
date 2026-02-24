package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.annotation.CacheRemove;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.DbaseAccessEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.service.DbaseAccessService;
import com.dbms.utils.StringUtils;
import com.dbms.annotation.SysLogAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import javax.jdo.annotations.Transactional;
import java.util.List;

import static com.dbms.utils.PageUtils.startPage;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author YSL
 * @since 2023-02-07
 */
@RestController
@RequestMapping("/dbase-access")
public class DbaseAccessController extends BaseController {

    @Autowired
    private DbaseAccessService dbaseAccessService;

    /**
     * 查询列表
     * @param dbaseAccessEntity
     * @return
     */
    @GetMapping("/listDbaseAccess")
    public TableDataInfo listDbaseAccess(DbaseAccessEntity dbaseAccessEntity){
        startPage();
        QueryWrapper<DbaseAccessEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotNull(dbaseAccessEntity.getUrl())){
            queryWrapper.eq(DbaseAccessEntity.URL, dbaseAccessEntity.getUrl());
        }
        if (StringUtils.isNotNull(dbaseAccessEntity.getGroupName())){
            queryWrapper.eq(DbaseAccessEntity.GROUP_NAME, dbaseAccessEntity.getGroupName());
        }
        if (StringUtils.isNotNull(dbaseAccessEntity.getUname())){
            queryWrapper.eq(DbaseAccessEntity.USER_NAME, dbaseAccessEntity.getUname());
        }
        if (StringUtils.isNotNull(dbaseAccessEntity.getUsername())){
            queryWrapper.eq(DbaseAccessEntity.USERNAME, dbaseAccessEntity.getUsername());
        }
        queryWrapper.eq(DbaseAccessEntity.IS_DELETE,0);
        queryWrapper.eq(DbaseAccessEntity.STATUS, 0);
        List<DbaseAccessEntity> list = dbaseAccessService.list(queryWrapper);
        System.out.println(JSON.toJSONString(list));
        return getDataTable(list);
    }

    @PostMapping("/saveDbaseAccess")
    @SysLogAnnotation(moduleName = "访问控制模块", childModule = "数据源访问控制", operation = "新增", procedureInfo = "新增数据源访问控制")
    @CacheRemove(value = {"dbase_access_list:*"})
    @Transactional
    public AjaxResult saveDbaseAccess(@RequestBody DbaseAccessEntity dbaseAccessEntity){
        System.out.println(JSON.toJSONString(dbaseAccessEntity));
        return toAjax(dbaseAccessService.save(dbaseAccessEntity));
    }

    @GetMapping("/delDbaseAccess/{accessIds}")
    @SysLogAnnotation(moduleName = "访问控制模块", childModule = "数据源访问控制", operation = "删除", procedureInfo = "删除数据源访问控制")
    @CacheRemove(value = {"dbase_access_list:*"})
    @Transactional
    public AjaxResult delDbaseAccess(@PathVariable Integer[] accessIds){
        DbaseAccessEntity dbaseAccessEntity = new DbaseAccessEntity();
        dbaseAccessEntity.setIsDelete(1);
        QueryWrapper<DbaseAccessEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(DbaseAccessEntity.ACCESS_ID, accessIds);
        return toAjax(dbaseAccessService.update(dbaseAccessEntity, queryWrapper));
    }

    @PostMapping("/editDbaseAccess")
    @SysLogAnnotation(moduleName = "访问控制模块", childModule = "数据源访问控制", operation = "修改", procedureInfo = "删除数据源访问控制")
    @CacheRemove(value = {"dbase_access_list:*"})
    @Transactional
    public AjaxResult updateDbaseAccess(@RequestBody DbaseAccessEntity dbaseAccessEntity){
        System.out.println("更新数据源授权");
        return toAjax(dbaseAccessService.saveOrUpdate(dbaseAccessEntity));
    }

    @GetMapping("/getDbaseAccessById/{accessId}")
    @SysLogAnnotation(moduleName = "访问控制模块", childModule = "数据源访问控制", operation = "修改", procedureInfo = "删除数据源访问控制")
    public AjaxResult getDbaseAccessById(@PathVariable Integer accessId){
        return AjaxResult.success(dbaseAccessService.getById(accessId));
    }

}
