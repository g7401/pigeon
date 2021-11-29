package io.g740.components.tag;

import io.g740.commons.exception.impl.ResourceDuplicateException;
import io.g740.commons.exception.impl.ResourceNotFoundException;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.Handler;
import io.g740.commons.types.UserInfo;

import io.g740.components.uid.tinyid.IdHelper;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import io.g740.pigeon.biz.share.constants.TagConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author bbottong
 */
@Handler
public class TagHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagHandler.class);

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private IdHelper idHelper;

    public List<String> listAllTags(UserInfo operationUserInfo) throws ServiceException {
        List<String> result = new ArrayList<>();
        this.tagRepository.findAll().forEach(tagDo -> {
            result.add(tagDo.getName());
        });

        if (CollectionUtils.isNotEmpty(result)) {
            if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
                result.remove(TagConstants.TAG_SYSTEM_NAME);
            }
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public String createTag(String tag, UserInfo operationUserInfo) throws ServiceException {
        boolean existsDuplicate = this.tagRepository.existsByName(tag);
        if (existsDuplicate) {
            throw new ResourceDuplicateException(tag);
        }

        TagDo tagDo = new TagDo();
        tagDo.setUid(this.idHelper.getNextId(TagDo.RESOURCE_NAME));
        tagDo.setName(tag);
        tagDo.setDescription(tag);
        BaseDo.create(tagDo, operationUserInfo.getUsername(), new Date());

        return tag;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(String tag, UserInfo operationUserInfo) throws ServiceException {
        TagDo tagDo = this.tagRepository.findByName(tag);
        if (tagDo == null) {
            throw new ResourceNotFoundException(tag);
        }

        tagDo.setDeleted(true);
        BaseDo.update(tagDo, operationUserInfo.getUsername(), new Date());
    }
}
