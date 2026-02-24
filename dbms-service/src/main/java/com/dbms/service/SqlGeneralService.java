package com.dbms.service;

import com.dbms.entity.SqlGeneralEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author
 * @since 2022-03-23
 */
public interface SqlGeneralService extends IService<SqlGeneralEntity> {
    /**
     * 常用sql
     * @return
     */
    List<SqlGeneralEntity> sqlList();

    /**
     * 表或索引
     * @return
     */
    List<SqlGeneralEntity> tableList();

    /**
     * 视图
     * @return
     */
    List<SqlGeneralEntity> viewList();

    /**
     * 函数
     * @return
     */
    List<SqlGeneralEntity> funcList();
}
