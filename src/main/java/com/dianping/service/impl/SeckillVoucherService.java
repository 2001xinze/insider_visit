package com.dianping.service.impl;

import com.dianping.mapper.SeckillVoucherMapper;
import com.dianping.pojo.SeckillVoucher;
import com.dianping.pojo.Voucher;
import com.dianping.service.SckillVoucherService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.dianping.constant.Constant.SECKILL_STOCK_KEY;

@Slf4j
@Service
public class SeckillVoucherService implements SckillVoucherService {
    @Resource
    private SeckillVoucherMapper seckillVoucherMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        Long id = voucher.getId();
        seckillVoucherMapper.addSeckillVoucher(id);
        // save the sckill voucher to redis
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + id, voucher.getStock().toString());
    }

    @Override
    public SeckillVoucher getByVoucherId(Long voucherId) {
        return seckillVoucherMapper.getByVoucherId(voucherId);
    }

    @Override
    public boolean update(Long voucherId) {
        return seckillVoucherMapper.update(voucherId);
    }
}
