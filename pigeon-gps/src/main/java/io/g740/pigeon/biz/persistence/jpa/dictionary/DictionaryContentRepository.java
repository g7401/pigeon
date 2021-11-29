package io.g740.pigeon.biz.persistence.jpa.dictionary;

import io.g740.pigeon.biz.object.entity.dictionary.DictionaryContentDo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author bbottong
 */
public interface DictionaryContentRepository extends PagingAndSortingRepository<DictionaryContentDo, Long> {
    /**
     * 按dictionary category uid, parent uid, value检查，是否存在同一dictionary category的结构中同属于一个上一级节点的节点value重复
     *
     * @param dictionaryCategoryUid 字典类目UID
     * @param parentUid 结构中上一级节点的UID
     * @param value
     * @return
     */
    boolean existsByDictionaryCategoryUidAndParentUidAndValue(Long dictionaryCategoryUid, Long parentUid, String value);

    /**
     * 按dictionary category uid, parent uid, label检查，是否存在同一dictionary category的结构中同属于一个上一级节点的节点label重复
     *
     * @param dictionaryCategoryUid 字典类目UID
     * @param parentUid 结构中上一级节点的UID
     * @param label
     * @return
     */
    boolean existsByDictionaryCategoryUidAndParentUidAndLabel(Long dictionaryCategoryUid, Long parentUid, String label);


    /**
     * 按dictionary category uid, value检查，是否存在同一dictionary category的结构中同一级节点（且没有上一级节点）的value重复
     *
     * @param dictionaryCategoryUid
     * @param value
     * @return
     */
    boolean existsByDictionaryCategoryUidAndValue(Long dictionaryCategoryUid, String value);

    /**
     * 按dictionary category uid, label检查，是否存在同一dictionary category的结构中同一级节点（且没有上一级节点）的label重复
     *
     * @param dictionaryCategoryUid
     * @param label
     * @return
     */
    boolean existsByDictionaryCategoryUidAndLabel(Long dictionaryCategoryUid, String label);

    /**
     * 按dictionary category uid检查存在
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
     * 按dictionary category uid删除
     *
     * @param dictionaryCategoryUid
     * @param createBy
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DictionaryContentDo u WHERE u.dictionaryCategoryUid = ?1 AND u.createBy = ?2")
    void deleteByDictionaryCategoryUidAndCreateBy(Long dictionaryCategoryUid, String createBy);

    /**
     * 按list of dictionary category uid查找
     *
     * @param listOfDictionaryCategoryUid
     * @return
     */
    List<DictionaryContentDo> findByDictionaryCategoryUidIn(List<Long> listOfDictionaryCategoryUid);

    /**
     * 按dictionary category uid查找
     *
     * @param dictionaryCategoryUid
     * @return
     */
    List<DictionaryContentDo> findByDictionaryCategoryUid(Long dictionaryCategoryUid);

    /**
     * 按uid查找
     *
     * @param uid
     * @return
     */
    DictionaryContentDo findByUid(Long uid);

    /**
     * 按uid检查存在
     *
     * @param uid
     * @return
     */
    boolean existsByUid(Long uid);

    /**
     * 按uid和create by检查存在
     *
     * @param uid
     * @param createBy
     * @return
     */
    boolean existsByUidAndCreateBy(Long uid, String createBy);

    /**
     * 按uid, create by删除
     *
     * @param uid
     * @param createBy
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DictionaryContentDo u WHERE u.uid = ?1 AND u.createBy = ?2")
    void deleteByUidAndCreateBy(Long uid, String createBy);

    /**
     * 按uid删除
     *
     * @param uid
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("DELETE FROM DictionaryContentDo u WHERE u.uid = ?1")
    void deleteByUid(Long uid);

    /**
     * 按dictionary category uid查找第1级dictionary content
     * @param dictionaryCategoryUid
     * @return
     */
    @Query("SELECT u FROM DictionaryContentDo u WHERE u.dictionaryCategoryUid = ?1 AND u.parentUid IS NULL")
    List<DictionaryContentDo> findFirstLevelDictionaryContentByDictionaryCategoryUid(Long dictionaryCategoryUid);

    /**
     * 按dictionary category uid查找parent uid为指定dictionary content uid的内容
     *
     * @param dictionaryCategoryUid
     * @param dictionaryContentUid
     * @return
     */
    @Query("SELECT u FROM DictionaryContentDo u WHERE u.dictionaryCategoryUid = ?1 AND u.parentUid = ?2")
    List<DictionaryContentDo> findFirstLevelDictionaryContentByDictionaryCategoryUid(Long dictionaryCategoryUid, Long dictionaryContentUid);

    /**
     * 按list of dictionary category uid查找
     *
     * @param listOfDictionaryContentUid
     * @return
     */
    List<DictionaryContentDo> findByUidIn(List<Long> listOfDictionaryContentUid);
}
