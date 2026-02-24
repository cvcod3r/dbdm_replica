package com.dbms.service.impl;

import com.alibaba.fastjson.JSON;
import com.dbms.constant.Constants;
import com.dbms.constant.UserConstants;
import com.dbms.entity.MenuEntity;
import com.dbms.dao.MenuDao;
import com.dbms.service.MenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dbms.utils.StringUtils;
import com.dbms.vo.RouterVo;
import com.dbms.vo.MetaVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author
 * @since 2022-04-11
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuDao, MenuEntity> implements MenuService {

//    @Override
//    public List<RouterVo> buildMenus(List<MenuEntity> menuEntities) {
//        List<RouterVo> routers = new LinkedList<RouterVo>();
//        for (MenuEntity menu : menuEntities)
//        {
//            RouterVo router = new RouterVo();
////            router.setHidden("1".equals(menu.getVisible()));
//            router.setName(getRouteName(menu));
//            router.setPath(getRouterPath(menu));
//            router.setComponent(getComponent(menu));
////            router.setQuery(menu.getQuery());
//            router.setMeta(new MetaVo(menu.getMenuName(), menu.getPath()));
//            List<MenuEntity> cMenus = menu.getChildren();
////            System.out.println("cMenus:" + JSON.toJSONString(cMenus));
//            if (!cMenus.isEmpty() && cMenus.size() > 0 && UserConstants.TYPE_DIR.equals(menu.getMenuType()))
//            {
////                System.out.println("cmenus");
////                router.setAlwaysShow(true);
////                router.setRedirect("noRedirect");
//                router.setChildren(buildMenus(cMenus));
//            }
//            else if (isMenuFrame(menu))
//            {
////                System.out.println("frame");
//                router.setMeta(null);
//                List<RouterVo> childrenList = new ArrayList<RouterVo>();
//                RouterVo children = new RouterVo();
//                children.setPath(menu.getPath());
//                children.setComponent(menu.getComponent());
//                children.setName(StringUtils.capitalize(menu.getPath()));
//                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache().toString()), menu.getPath()));
//                children.setQuery(menu.getQuery());
//                childrenList.add(children);
//                router.setChildren(childrenList);
//            }
//            else if (menu.getParentId().intValue() == 0 && isInnerLink(menu))
//            {
////                System.out.println("innerLink");
//                router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
//                router.setPath("/");
//                List<RouterVo> childrenList = new ArrayList<RouterVo>();
//                RouterVo children = new RouterVo();
//                String routerPath = innerLinkReplaceEach(menu.getPath());
//                children.setPath(routerPath);
//                children.setName(StringUtils.capitalize(routerPath));
//                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), menu.getPath()));
//                childrenList.add(children);
//                router.setChildren(childrenList);
//            }
//            routers.add(router);
//            System.out.println( "ROUTE" + JSON.toJSONString(router));
//        }
////        System.out.println("routers" + JSON.toJSONString(routers));
//        return routers;
//
//    }

    @Override
    public List<MenuEntity> getChildPerms(List<MenuEntity> menuEntities, int parentId) {
        List<MenuEntity> returnList = new ArrayList<>();
        for (Iterator<MenuEntity> iterator = menuEntities.iterator(); iterator.hasNext();)
        {
            MenuEntity t = iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId)
            {
                recursionFn(menuEntities, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     *
     * @param list
     * @param t
     */
    private void recursionFn(List<MenuEntity> list, MenuEntity t)
    {
        // 得到子节点列表
        List<MenuEntity> childList = getChildList(list, t);
        t.setChildren(childList);
        for (MenuEntity tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }
    /**
     * 获取路由名称
     *
     * @param menu 菜单信息
     * @return 路由名称
     */
//    public String getRouteName(MenuEntity menu)
//    {
//        String routerName = StringUtils.capitalize(menu.getPath());
//        // 非外链并且是一级目录（类型为目录）
//        if (isMenuFrame(menu))
//        {
//            routerName = StringUtils.EMPTY;
//        }
//        return routerName;
//    }

    /**
     * 是否为parent_view组件
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isParentView(MenuEntity menu)
    {
        return menu.getParentId().intValue() != 0 && UserConstants.TYPE_DIR.equals(menu.getMenuType());
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<MenuEntity> list, MenuEntity t)
    {
        return getChildList(list, t).size() > 0;
    }

    /**
     * 内链域名特殊字符替换
     *
     * @return
     */
    public String innerLinkReplaceEach(String path)
    {
        return StringUtils.replaceEach(path, new String[] { Constants.HTTP, Constants.HTTPS },
                new String[] { "", "" });
    }

    /**
     * 得到子节点列表
     */
    private List<MenuEntity> getChildList(List<MenuEntity> list, MenuEntity t)
    {
        List<MenuEntity> tlist = new ArrayList<MenuEntity>();
        Iterator<MenuEntity> it = list.iterator();
        while (it.hasNext())
        {
            MenuEntity n = (MenuEntity) it.next();
            if (n.getParentId().longValue() == t.getMenuId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

}
