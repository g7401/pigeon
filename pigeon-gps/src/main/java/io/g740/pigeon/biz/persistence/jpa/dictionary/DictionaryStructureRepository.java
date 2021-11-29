package io.g740.pigeon.biz.persistence.jpa.dictionary;

import io.g740.pigeon.biz.object.entity.dictionary.DictionaryStructureDo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
public interface DictionaryStructureRepository extends PagingAndSortingRepository<DictionaryStructureDo, Long> {
    /**
     * 按dictionary category uid, parent uid, name检查，是否存在同一dictionary category的结构中同属于一个上一级节点的节点name重复
     *
     * @param dictionaryCategoryUid 字典类目UID
     * @param parentUid 结构中上一级节点的UID
     * @param name 节点名称
     * @return
     */
    boolean existsByDictionaryCategoryUidAndParentUidAndName(Long dictionaryCategoryUid, Long parentUid, String name);

    /**
     * 按dictionary category uid, name检查，是否存在同一dictionary category的结构中同一级节点（且没有上一级节点）的name重复
     *
     * @param dictionaryCategoryUid
     * @param name
     * @return
     */
    boolean existsByDictionaryCategoryUidAndName(Long dictionaryCategoryUid, String name);

    /**
     * 在指定parent uid is null层级内按dictionary category uid查找
     *
     * @param dictionaryCategoryUid
     * @return
     */
    @Query("SELECT u FROM DictionaryStructureDo u WHERE u.dictionaryCategoryUid = ?1 AND u.parentUid IS NULL")
    List<DictionaryStructureDo> findByDictionaryCategoryUidWithoutParentUid(Long dictionaryCategoryUid);

    /**
     * 在指定parent uid is not null层级内按dictionary category uid查找
     *
     * @param dictionaryCategoryUid
     * @param parentUid
     * @return
     */
    @Query("SELECT u FROM DictionaryStructureDo u WHERE u.dictionaryCategoryUid = ?1 AND u.parentUid = ?2")
    List<DictionaryStructureDo> findByDictionaryCategoryUidWithParentUid(Long dictionaryCategoryUid, Long parentUid);

    /**
     * 按dictionary category uid检查，是否存在dictionary category的结构
     *
     * @param dictionaryCategoryUid
     * @return
     */
    boolean existsByDictionaryCategoryUid(Long dictionaryCategoryUid);

    /**
     * 按parent uid检查，是否存在子节点
     *
     * @param parentUid
     * @return
     */
    boolean existsByParentUid(Long parentUid);

    /**
     * 按list of dictionary category uid查找dictionary structure
     *
     * @param listOfDictionaryCategoryUid
     * @return
     */
    List<DictionaryStructureDo> findByDictionaryCategoryUidIn(List<Long> listOfDictionaryCategoryUid);

    /**
     * 按dictionary category uid查找
     *
     * @param dictionaryCategoryUid
     * @return
     */
    List<DictionaryStructureDo> findByDictionaryCategoryUid(Long dictionaryCategoryUid);

    /**
     * 按uid查找
     *
     * @param uid
     * @return
     */
    DictionaryStructureDo findByUid(Long uid);

    /**
     * 按uid检查存在
     *
     * @param uid
     * @return
     */
    boolean existsByUid(Long uid);

    /**
     * 按uid删除
     *
     * @param uid
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DictionaryStructureDo u WHERE u.uid = ?1")
    void deleteByUid(Long uid);
}
