package com.dianping.mapper;

import com.dianping.pojo.SeckillVoucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillVoucherMapper {

    void addSeckillVoucher(Long id);

    SeckillVoucher getByVoucherId(Long voucherId);

    @Update("update tb_seckill_voucher set stock = stock - 1 where voucher_id = #{voucher_id} and stock > 0")
    boolean update(Long voucherId);
}
