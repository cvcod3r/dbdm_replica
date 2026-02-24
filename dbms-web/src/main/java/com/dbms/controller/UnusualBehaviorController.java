package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.annotation.CacheRemove;
import com.dbms.annotation.SysLogAnnotation;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.DbaseAccessEntity;
import com.dbms.entity.UnusualBehaviorEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.service.DbaseAccessService;
import com.dbms.service.UnusualBehaviorService;
import com.dbms.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.jdo.annotations.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author HLH
 * @since 2023-06-15
 */
@RestController
@RequestMapping("/unusual-behavior")
public class UnusualBehaviorController extends BaseController {

    @Autowired
    private UnusualBehaviorService unusualBehaviorService;
    /**
     * 查询列表
     * @param unusualBehaviorEntity
     * @return
     */
    @GetMapping("/listUnusualBehavior")
    public TableDataInfo listUnusualBehavior(UnusualBehaviorEntity unusualBehaviorEntity){
        startPage();
        QueryWrapper<UnusualBehaviorEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotNull(unusualBehaviorEntity.getUsername())){
            queryWrapper.eq(UnusualBehaviorEntity.USERNAME, unusualBehaviorEntity.getUsername());
        }
        List<UnusualBehaviorEntity> list = unusualBehaviorService.list(queryWrapper);
        System.out.println(JSON.toJSONString(list));
        return getDataTable(list);
    }

    /**
     * 添加异常行为配置
     * @param unusualBehaviorEntity
     * @return
     */
    @PostMapping("/saveUnusualBehavior")
    @SysLogAnnotation(moduleName = "异常行为模块", childModule = "异常行为配置", operation = "新增", procedureInfo = "新增异常行为配置")
    @Transactional
    public AjaxResult saveUnusualBehavior(@RequestBody UnusualBehaviorEntity unusualBehaviorEntity){
        unusualBehaviorEntity.setCreatetime(LocalDateTime.now());
        System.out.println(JSON.toJSONString(unusualBehaviorEntity));
        return toAjax(unusualBehaviorService.save(unusualBehaviorEntity));
    }

    /**
     * 删除异常行为配置
     * @param behaviorIds
     * @return
     */
    @DeleteMapping("/delUnusualBehavior/{behaviorIds}")
    @SysLogAnnotation(moduleName = "异常行为模块", childModule = "异常行为配置", operation = "删除", procedureInfo = "删除异常行为配置")
    public AjaxResult delUnusualBehavior(@PathVariable Integer[] behaviorIds){
        System.out.println(JSON.toJSONString(behaviorIds));
        return toAjax(unusualBehaviorService.removeBatchByIds(Arrays.asList(behaviorIds)));
    }

    /**
     * 修改异常行为配置
     * @param unusualBehaviorEntity
     * @return
     */
    @PostMapping("/updateUnusualBehavior")
    @SysLogAnnotation(moduleName = "异常行为模块", childModule = "异常行为配置", operation = "修改", procedureInfo = "修改异常行为配置")
    @Transactional
    public AjaxResult updateUnusualBehavior(@RequestBody UnusualBehaviorEntity unusualBehaviorEntity){
        System.out.println("更新异常行为配置");
        unusualBehaviorEntity.setUpdatetime(LocalDateTime.now());
        return toAjax(unusualBehaviorService.saveOrUpdate(unusualBehaviorEntity));
    }

    @GetMapping(value = "/getUnusualBehaviorById/{behaviorId}")
    public AjaxResult getUnusualBehaviorById(@PathVariable Integer behaviorId)
    {
        return AjaxResult.success(unusualBehaviorService.getById(behaviorId));
    }
}
