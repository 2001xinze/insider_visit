package com.dianping.service;

import com.dianping.pojo.SeckillVoucher;
import com.dianping.pojo.Voucher;

public interface SckillVoucherService {
    public void addSeckillVoucher(Voucher voucher);

    public SeckillVoucher getByVoucherId(Long voucherId);

    boolean update(Long voucherId);
}
