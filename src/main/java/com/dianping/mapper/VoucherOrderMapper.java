package com.dianping.mapper;

import com.dianping.pojo.VoucherOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VoucherOrderMapper {
    @Insert("insert into tb_voucher_order (id, user_id, voucher_id) values (#{orderId}, #{userId}, #{voucherId})")
    void save(VoucherOrder voucherOrder);

    @Select("select COUNT(tb_voucher_order.id) from tb_voucher_order where user_id = #{userId} and voucher_id = #{voucherId}")
    int count(Long userId, Long voucherId);
}
