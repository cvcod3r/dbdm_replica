package com.dbms.service;

import com.dbms.entity.DbaseAccessEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dbms.entity.DbaseEntity;
import com.dbms.vo.DbaseEntityVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author YSL
 * @since 2023-02-07
 */
public interface DbaseAccessService extends IService<DbaseAccessEntity> {

    List<DbaseEntityVo> getAccessibleDbaseVo(Integer userId, Integer groupId, String dsType);

    DbaseEntityVo getDbaseVo(Integer userId, Integer groupId, Integer dbId, String dsType);

    DbaseEntity getAccessDbaseEntity(Integer userId, Integer groupId, Integer dbId, String dsType);
}
