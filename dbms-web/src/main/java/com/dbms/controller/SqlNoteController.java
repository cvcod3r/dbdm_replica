package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.core.AjaxResult;
import com.dbms.entity.SqlNoteEntity;
import com.dbms.service.SqlNoteService;
import com.dbms.utils.GlobalMessageUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.dbms.utils.SecurityUtils.getUserId;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author
 * @since 2022-03-23
 */
@RestController
@RequestMapping("/note")
public class SqlNoteController {

    @Autowired
    SqlNoteService noteService;

    @PostMapping("/saveNote")
    @ResponseBody
    public AjaxResult saveNote(@RequestBody SqlNoteEntity noteEntity){
//        System.out.println(JSON.toJSONString(noteEntity));
        Integer userId = getUserId();
        noteEntity.setUserId(userId);
        noteEntity.setCreatetime(LocalDateTime.now());
        boolean flag = noteService.saveOrUpdate(noteEntity);
        if (flag){
            return AjaxResult.success(GlobalMessageUtil.saveSuccess);
        }
        return AjaxResult.error(GlobalMessageUtil.saveFailure);
    }

    @GetMapping("/getNoteById/{noteId}")
    @ResponseBody
    public AjaxResult getNoteById(@PathVariable Integer noteId){

        SqlNoteEntity noteEntity = noteService.getById(noteId);
        if (noteEntity!=null){
            return AjaxResult.success(noteEntity);
        }
        return AjaxResult.error(GlobalMessageUtil.opFailure);
    }

    @GetMapping("/getNoteList/{dbId}")
    @ResponseBody
    public AjaxResult getNoteList(@PathVariable Integer dbId){
        Integer userId = getUserId();
        QueryWrapper<SqlNoteEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SqlNoteEntity.USER_ID,userId);
        queryWrapper.eq(SqlNoteEntity.DB_ID, dbId);
        List<SqlNoteEntity> noteEntityList = noteService.list(queryWrapper);
        return AjaxResult.success(noteEntityList);
    }

    @GetMapping("/delNote/{noteId}")
    @ResponseBody
    public AjaxResult delNote(@PathVariable Integer noteId){
        boolean flag = noteService.removeById(noteId);
        if (flag){
            return AjaxResult.success(GlobalMessageUtil.dropSuccess);
        }
        return AjaxResult.error(GlobalMessageUtil.dropFailure);
    }
}
