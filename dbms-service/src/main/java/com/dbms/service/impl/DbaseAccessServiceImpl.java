package com.dbms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dbms.constant.CacheConstants;
import com.dbms.core.RedisCache;
import com.dbms.dao.DbaseAccountDao;
import com.dbms.dao.DbaseDao;
import com.dbms.entity.DbaseAccessEntity;
import com.dbms.dao.DbaseAccessDao;
import com.dbms.entity.DbaseAccountEntity;
import com.dbms.entity.DbaseEntity;
import com.dbms.vo.DbaseEntityVo;
import com.dbms.service.DbaseAccessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author YSL
 * @since 2023-02-07
 */
@Service
public class DbaseAccessServiceImpl extends ServiceImpl<DbaseAccessDao, DbaseAccessEntity> implements DbaseAccessService {


    @Autowired
    private DbaseDao dbaseDao;

    @Autowired
    private DbaseAccessDao dbaseAccessDao;

    @Autowired
    private DbaseAccountDao dbaseAccountDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisCache redisCache;

    @Override
    public List<DbaseEntityVo> getAccessibleDbaseVo(Integer userId, Integer groupId, String dsType) {
        String redisKey = CacheConstants.DBASE_ACCESSIBLE_LIST + "userId:" + userId + ":" + dsType;
//        System.out.println(redisKey);
//        ValueOperations valueOperations = redisTemplate.opsForValue();
        if (redisTemplate.hasKey(redisKey)){
            System.out.println("redisVo::");
            List<DbaseEntityVo> dbaseEntityVos = redisCache.getCacheObject(redisKey) ;
            System.out.println(JSON.toJSONString(dbaseEntityVos));
            return dbaseEntityVos;
        } else {
            QueryWrapper<DbaseAccessEntity> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT DB_ID").eq(DbaseAccessEntity.DS_TYPE, dsType)
                    .eq(DbaseAccessEntity.IS_DELETE, 0)
                    .eq(DbaseAccessEntity.STATUS, 0)
                    .and(wr -> wr.eq(DbaseAccessEntity.GROUP_ID, groupId)
                            .or().eq(DbaseAccessEntity.USER_ID, userId));
            List<DbaseAccessEntity> dbaseAccessEntities = dbaseAccessDao.selectList(wrapper);
            System.out.println("AccessList::" + JSON.toJSONString(dbaseAccessEntities));
            List<DbaseEntityVo> dbaseEntityVos = dbaseAccessEntities.stream().map(d -> new DbaseEntityVo(
                    d.getDbId(),d.getDbType(),d.getDsType(),d.getUsername(),d.getPassword(),
                    d.getGroupId(),d.getUserId(),d.getAccessId(), d.getAccountId(), d.getUrl())
            ).collect(Collectors.toList());
            addDbaseListInfo(dbaseEntityVos);
//            System.out.println(JSON.toJSONString(dbaseEntityVos));
            if (dbaseEntityVos != null && dbaseEntityVos.size()!=0){
                redisCache.setCacheObject(redisKey, dbaseEntityVos, 60*5, TimeUnit.SECONDS);
                //设置过期时间
//                redisTemplate.expire(redisKey,);
            }
            return dbaseEntityVos;
        }
    }

    private void addDbaseListInfo(List<DbaseEntityVo> dbaseEntityVos) {
        Set<Integer> dbIds = dbaseEntityVos.stream().map((DbaseEntityVo t) -> t.getDbId()).collect(Collectors.toSet());
//        System.out.println("set::" + JSON.toJSONString(dbIds));
        if (dbIds == null || dbIds.isEmpty()){
            return;
        }
        LambdaQueryWrapper<DbaseEntity> wrapper = Wrappers.lambdaQuery(DbaseEntity.class);
        wrapper.in(DbaseEntity::getDbId, dbIds)
                .eq(DbaseEntity::getIsDelete, 0);
        List<DbaseEntity> dbaseEntityList = dbaseDao.selectList(wrapper);
        Map<Integer, String> urlMap = dbaseEntityList.stream().collect(Collectors.toMap(DbaseEntity::getDbId, DbaseEntity::getUrl));
        Map<Integer, String> connNameMap = dbaseEntityList.stream().collect(Collectors.toMap(DbaseEntity::getDbId, DbaseEntity::getConnName));
        Map<Integer, String> hostMap = dbaseEntityList.stream().collect(Collectors.toMap(DbaseEntity::getDbId, DbaseEntity::getHost));
        Map<Integer, String> portMap = dbaseEntityList.stream().collect(Collectors.toMap(DbaseEntity::getDbId, DbaseEntity::getPort));
        Map<Integer, Integer> statusMap = dbaseEntityList.stream().collect(Collectors.toMap(DbaseEntity::getDbId, DbaseEntity::getStatus));
        Map<Integer, String> dbNameMap = dbaseEntityList.stream().collect(Collectors.toMap(DbaseEntity::getDbId, d -> Optional.ofNullable(d.getDbName()).orElse("null")));
        Map<Integer, String> dbTypeMap = dbaseEntityList.stream().collect(Collectors.toMap(DbaseEntity::getDbId, d -> Optional.ofNullable(d.getDbType()).orElse("null")));
        Map<Integer, String> iconOpenMap = dbaseEntityList.stream().collect(Collectors.toMap(DbaseEntity::getDbId,d -> Optional.ofNullable(d.getIconOpen()).orElse("null")));
        Map<Integer, String> iconCloseMap = dbaseEntityList.stream().collect(Collectors.toMap(DbaseEntity::getDbId, d -> Optional.ofNullable(d.getIconClose()).orElse("null")));
//        Map<Integer, String> versionMap = dbaseEntityList.stream().collect(Collectors.toMap(DbaseEntity::getDbId, DbaseEntity::getVersion));
        for(DbaseEntityVo dbaseEntityVo:dbaseEntityVos){
            Integer dbId = dbaseEntityVo.getDbId();
            dbaseEntityVo.setUrl(urlMap.getOrDefault(dbId, null));
            dbaseEntityVo.setConnName(connNameMap.getOrDefault(dbId, null));
            dbaseEntityVo.setHost(hostMap.getOrDefault(dbId, null));
            dbaseEntityVo.setPort(portMap.getOrDefault(dbId, null));
            dbaseEntityVo.setStatus(statusMap.getOrDefault(dbId, null));
            dbaseEntityVo.setDbName(dbNameMap.getOrDefault(dbId, null));
            dbaseEntityVo.setDbType(dbTypeMap.getOrDefault(dbId, null));
            dbaseEntityVo.setIconOpen(iconOpenMap.getOrDefault(dbId, null));
            dbaseEntityVo.setIconClose(iconCloseMap.getOrDefault(dbId, null));
//            dbaseEntityVo.setVersion(versionMap.getOrDefault(dbId, null));
        }
    }

    @Override
    public DbaseEntityVo getDbaseVo(Integer userId, Integer groupId, Integer dbId, String dsType) {
        String redisKey = "dbaseVo:" + userId + ":" + dbId + ":" + dsType;
        ValueOperations<String, DbaseEntityVo> valueOperations = redisTemplate.opsForValue();
        // 判断redis中是否有key，有则从redis中获取，没有则从数据库中获取
        if (redisTemplate.hasKey(redisKey)){
//            System.out.println("redis");
            Object object = valueOperations.get(redisKey);
            DbaseEntityVo dbaseEntityVoRedis = null;
            if (object!=null){
                dbaseEntityVoRedis = (DbaseEntityVo) object;
            }
            return dbaseEntityVoRedis;
        }else{
//            System.out.println("DB");
            LambdaQueryWrapper<DbaseAccessEntity> wrapper = Wrappers.lambdaQuery(DbaseAccessEntity.class);
            wrapper.eq(DbaseAccessEntity::getDbId, dbId)
                    .eq(DbaseAccessEntity::getDsType, dsType)
                    .and(wr -> wr.eq(DbaseAccessEntity::getGroupId, groupId)
                            .or().eq(DbaseAccessEntity::getUserId, userId));
            DbaseAccessEntity dbaseAccessEntity = dbaseAccessDao.selectOne(wrapper);
            DbaseEntityVo dbaseEntityVo = new DbaseEntityVo();
            BeanUtils.copyProperties(dbaseAccessEntity, dbaseEntityVo);
            //DbaseEntityVo dbaseEntityVo = Optional.ofNullable(dbaseAccessEntity).map(DbaseEntityVo::new).orElse(null);
            LambdaQueryWrapper<DbaseEntity> wrapper2 = Wrappers.lambdaQuery(DbaseEntity.class);
            wrapper2.eq(DbaseEntity::getDbId, dbaseEntityVo.getDbId());
            wrapper2.eq(DbaseEntity::getIsDelete, 0);
            DbaseEntity dbaseEntity = dbaseDao.selectOne(wrapper2);
            Optional.ofNullable(dbaseEntity).ifPresent(e -> dbaseEntityVo.setConnName(e.getConnName()));
            Optional.ofNullable(dbaseEntity).ifPresent(e -> dbaseEntityVo.setUrl(e.getUrl()));
            Optional.ofNullable(dbaseEntity).ifPresent(e -> dbaseEntityVo.setHost(e.getHost()));
            Optional.ofNullable(dbaseEntity).ifPresent(e -> dbaseEntityVo.setPort(e.getPort()));
            Optional.ofNullable(dbaseEntity).ifPresent(e -> dbaseEntityVo.setStatus(e.getStatus()));
            Optional.ofNullable(dbaseEntity).ifPresent(e -> dbaseEntityVo.setVersion(e.getVersion()));
            Optional.ofNullable(dbaseEntity).ifPresent(e -> dbaseEntityVo.setDbName(e.getDbName()));
//            System.out.println(JSON.toJSONString(dbaseEntityVo));
            valueOperations.set(redisKey, dbaseEntityVo);
            //设置过期时间
            redisTemplate.expire(redisKey,60*20, TimeUnit.SECONDS);
            return dbaseEntityVo;
        }
    }

    @Override
    public DbaseEntity getAccessDbaseEntity(Integer userId, Integer groupId, Integer dbId, String dsType) {
        String redisKey = CacheConstants.DBASE_ACCESS + userId + ":" + dbId + ":" + dsType;
        // 判断redis中是否有key，有则从redis中获取，没有则从数据库中获取
        if (redisTemplate.hasKey(redisKey)){
//            System.out.println("redis");
            DbaseEntity dbaseEntityRedis = redisCache.getCacheObject(redisKey);
            return dbaseEntityRedis;
        }else{
//            System.out.println("DB");
            // 获取授权的数据库
            QueryWrapper<DbaseAccessEntity> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT DB_ID, ACCOUNT_ID").eq(DbaseAccessEntity.DB_ID, dbId)
                    .eq(DbaseAccessEntity.DS_TYPE, dsType)
                    .eq(DbaseAccessEntity.IS_DELETE, 0)
                    .eq(DbaseAccessEntity.STATUS, 0)
                    .and(wr -> wr.eq(DbaseAccessEntity.GROUP_ID, groupId)
                            .or().eq(DbaseAccessEntity.USER_ID, userId));
            DbaseAccessEntity dbaseAccessEntity = dbaseAccessDao.selectOne(wrapper);
            System.out.println("Access::" + JSON.toJSONString(dbaseAccessEntity));
//            LambdaQueryWrapper<DbaseAccountEntity> wrapper1 = Wrappers.lambdaQuery(DbaseAccountEntity.class);
//            wrapper1.eq(DbaseAccountEntity::getAccountId, );
            QueryWrapper<DbaseAccountEntity> wrapper1 = new QueryWrapper<>();

            // 获取账号
            DbaseAccountEntity dbaseAccountEntity = dbaseAccountDao.selectById(dbaseAccessEntity.getAccountId());
//            System.out.println("Account::" + JSON.toJSONString(dbaseAccountEntity));
            // DbaseEntityVo dbaseEntityVo = Optional.ofNullable(dbaseAccessEntity).map(DbaseEntityVo::new).orElse(null);
            LambdaQueryWrapper<DbaseEntity> wrapper2 = Wrappers.lambdaQuery(DbaseEntity.class);
            wrapper2.eq(DbaseEntity::getDbId, dbaseAccessEntity.getDbId());
            wrapper2.eq(DbaseEntity::getIsDelete, 0);
            DbaseEntity dbaseEntity = dbaseDao.selectOne(wrapper2);
            dbaseEntity.setUsername(dbaseAccountEntity.getUsername());
            dbaseEntity.setPassword(dbaseAccountEntity.getPassword());
            redisCache.setCacheObject(redisKey, dbaseEntity, 60*20, TimeUnit.SECONDS);
            //设置过期时间
            return dbaseEntity;
        }
    }

}
