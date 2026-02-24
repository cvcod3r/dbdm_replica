package com.dbms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.core.AjaxResult;
import com.dbms.entity.SqlGeneralEntity;
import com.dbms.service.SqlGeneralService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.List;



/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author
 * @since 2022-03-23
 */
@RestController
@RequestMapping("/general")
public class SqlGeneralController {
    @Autowired
    SqlGeneralService generalService;

    @GetMapping("/listAll")
    @ResponseBody
    public AjaxResult listAll(){

        List<SqlGeneralEntity> parentList = generalService.list(
                new QueryWrapper<SqlGeneralEntity>().eq(SqlGeneralEntity.PID,1)
        );

        List<SqlGeneralEntity> childList = generalService.list(
                new QueryWrapper<SqlGeneralEntity>().ne(SqlGeneralEntity.PID,1)
        );

        for (SqlGeneralEntity sql:parentList){
            List<SqlGeneralEntity> list = buildSQLTree(childList,sql.getId());
            sql.setChildren(list);
        }
//        List<GeneralEntity> sql = new ArrayList<>();
//        List<GeneralEntity> tableList = new ArrayList<>();
//        List<GeneralEntity> viewList = new ArrayList<>();
//        List<GeneralEntity> funcList = new ArrayList<>();
//        sql = generalService.sqlList();
//        tableList = generalService.tableList();
//        viewList = generalService.viewList();
//        funcList = generalService.funcList();
//        HashMap<String, List<GeneralEntity>> ans = new HashMap<>();
//
//        ans.put("sqlList", sql);
//        ans.put("tableList", tableList);
//        ans.put("viewList", viewList);
//        ans.put("funcList", funcList);
//        System.out.println(JSON.toJSONString(ans));
        return AjaxResult.success(parentList);
    }

    public List<SqlGeneralEntity> buildSQLTree(List<SqlGeneralEntity> sqls, int pid){
        List<SqlGeneralEntity> result = new ArrayList<>();
        for (SqlGeneralEntity sql:sqls){
            int id = sql.getId();
            if (sql.getPid() == pid){
                List<SqlGeneralEntity> child = buildSQLTree(sqls,id);
                sql.setChildren(child);
                result.add(sql);
            }
        }
        return result;
    }
    /**
     * 递归构建常用SQL树
     * @param menus
     * @param parentId
     * @return
     */
//    public List<MenuEntity> buildSQLTrees(List<MenuEntity> menus, int parentId){
//
//        for (MenuEntity menu : menus) {
//            //菜单id
//            int menuId = menu.getMenuId();
//            //判断父id是否匹配
//            if(menu.getParentId() == parentId){
//                List<MenuEntity> childMenu = buildSQLTrees(menus,menuId);
//                menu.setChildren(childMenu);
//                result.add(menu);
//            }
//        }
//        return result;
//    }
}
