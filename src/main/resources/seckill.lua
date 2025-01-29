-- 1 parameters
local voucherId = ARGV[1]
local userId = ARGV[2]
local orderId = ARGV[3]

-- 2 data
local stockKey = 'scekill:stock:' .. voucherId
local orderKey = 'seckill:order:' .. voucherId

-- 3
-- 3.1 check the stock is enough
if (tonumber(redis.call('get', stockKey)) <= 0) then
    -- 3.2 not enough
    return 1
end
-- 3.2 check if the user has ordered
if (redis.call('sismember', orderKey, userId) == 1) then
    -- 3.3 has ordered
    return 2
end
-- 3.4 reduce the stock
redis.call('incrby', stockKey, -1)
-- 3.5 order(which means save the user info)
redis.call('sadd', orderKey, userId)
return 0