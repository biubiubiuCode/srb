package org.example.srb.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.srb.core.pojo.entity.Borrower;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.srb.core.pojo.vo.BorrowerApprovalVO;
import org.example.srb.core.pojo.vo.BorrowerDetailVO;
import org.example.srb.core.pojo.vo.BorrowerVO;

/**
 * <p>
 * 借款人 服务类
 * </p>
 *
 * @author wendao
 * @since 2024-04-01
 */
public interface BorrowerService extends IService<Borrower> {

    void saveBorrowerVOByUserId(BorrowerVO borrowerVO, Long userId);

    Integer getStatusByUserId(Long userId);

    IPage<Borrower> listPage(Page<Borrower> pageParam, String keyWord);

    BorrowerDetailVO getBorrowerDetailVOById(Long id);

    void approval(BorrowerApprovalVO borrowerApprovalVO);
}
