package com.dianping.mapper;

import com.dianping.pojo.Voucher;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VoucherMapper {
    void addVoucher(Long id);

    void addSeckillVoucher(Long id);

    Voucher queryVoucherOfShop(Long shopId);
}
