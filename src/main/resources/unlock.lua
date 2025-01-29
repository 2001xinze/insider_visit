-- check if they are the same
if (redis.call('get', KEYS[1]) == ARGV[1]) then
    -- release the key
    return redis.call('del', KEYS[1])
end
return 0