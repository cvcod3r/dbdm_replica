package com.dbms.service;

import com.dbms.entity.MenuEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dbms.vo.RouterVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author
 * @since 2022-04-11
 */
public interface MenuService extends IService<MenuEntity> {

//    public List<RouterVo> buildMenus(List<MenuEntity> menuEntities);

    List<MenuEntity> getChildPerms(List<MenuEntity> menuEntities, int i);
}
