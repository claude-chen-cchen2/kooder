package com.gitee.kooder.code;

import com.gitee.kooder.models.CodeIndexDocument;

/**
 * 文件遍历回调接口
 * @author Winter Lau<javayou@gmail.com>
 */
public interface FileTraveler {

    /**
     * 更新源码文档（新文件、更改文件）
     * @param doc  文档信息
     * @return true: 继续下一个文档， false 不再处理下面文档
     */
    void updateDocument(CodeIndexDocument doc);

    /**
     * 删除文档
     * @param doc
     * @return
     */
    void deleteDocument(CodeIndexDocument doc);

    /**
     * 清空仓库所有文件，以待重建
     * @param repoId
     */
    void resetRepository(long repoId);

}