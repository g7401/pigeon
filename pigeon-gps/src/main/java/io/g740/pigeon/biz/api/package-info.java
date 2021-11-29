package io.g740.pigeon.biz.api;

/**
 * 资源的常见操作：
 * create  -- 创建，POST /<resource name>，在request body中指定初始属性值
 * update  -- 更新，PATCH /<resource name>，在request body中指定id以及需要更新的属性
 * delete  -- 删除，DELETE /<resource name>，在request body中指定id
 * get     -- 获取，GET /<resource name>，在request parameter中指定id
 * list    -- 列出，GET /<resource name>/list，无request parameter
 * query   -- 查询，GET /<resource name>/query，在request parameter指定查询参数和分页参数
 */